import apiClient, { publicApiClient } from "./axiosInstance";

export const loginRequest = async (credentials) => {
  const response = await publicApiClient.post("/api/auth/login", credentials);

  return response.data;
};

export const registerRequest = async (registrationData) => {
  const response = await publicApiClient.post(
    "/api/auth/register",
    registrationData,
  );

  return response.data;
};

export const logoutRequest = async (refreshToken) => {
  const response = await apiClient.post("/api/auth/logout", {
    refreshToken,
  });

  return response.data;
};
