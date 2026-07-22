import axios from "axios";

import tokenService from "../auth/tokenService";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:7000";

/*
 * Public requests:
 * register, login and token refresh.
 */
export const publicApiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

/*
 * Protected requests:
 * profile, posts, feed, notifications, etc.
 */
const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

/*
 * Automatically add the access token.
 */
apiClient.interceptors.request.use(
  (config) => {
    const storedAccessToken = tokenService.getAccessToken();

    if (storedAccessToken) {
      /*
       * Prevent:
       * Authorization: Bearer Bearer eyJ...
       */
      const normalizedAccessToken = storedAccessToken
        .replace(/^Bearer\s+/i, "")
        .trim();

      config.headers = config.headers || {};

      config.headers.Authorization = `Bearer ${normalizedAccessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

/*
 * Prevent multiple simultaneous refresh requests.
 */
let refreshPromise = null;

apiClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    const isUnauthorized = error.response?.status === 401;

    const refreshToken = tokenService.getRefreshToken();

    /*
     * Do not retry endlessly.
     */
    if (!isUnauthorized || !refreshToken || originalRequest?._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = publicApiClient
          .post("/api/auth/refresh", {
            refreshToken,
          })
          .then((response) => {
            const tokens = response.data;

            tokenService.setTokens(tokens);

            return tokens.accessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newAccessToken = await refreshPromise;

      originalRequest.headers = originalRequest.headers || {};

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      tokenService.clearTokens();

      if (window.location.pathname !== "/login") {
        window.location.replace("/login");
      }

      return Promise.reject(refreshError);
    }
  },
);

export default apiClient;
