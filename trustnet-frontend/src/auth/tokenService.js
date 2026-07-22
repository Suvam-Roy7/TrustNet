const ACCESS_TOKEN_KEY = "trustnet_access_token";
const REFRESH_TOKEN_KEY = "trustnet_refresh_token";

const getAccessToken = () => {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
};

const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

const setTokens = ({ accessToken, refreshToken }) => {
  if (!accessToken) {
    throw new Error("Authentication response does not contain an access token");
  }

  const normalizedAccessToken = accessToken.replace(/^Bearer\s+/i, "").trim();

  localStorage.setItem(ACCESS_TOKEN_KEY, normalizedAccessToken);

  /*
   * Keep the existing refresh token when the backend
   * does not rotate it.
   */
  if (refreshToken) {
    const normalizedRefreshToken = refreshToken
      .replace(/^Bearer\s+/i, "")
      .trim();

    localStorage.setItem(REFRESH_TOKEN_KEY, normalizedRefreshToken);
  }
};

const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

const hasAccessToken = () => {
  return Boolean(getAccessToken());
};

const decodeTokenPayload = () => {
  const accessToken = getAccessToken();

  if (!accessToken) {
    return null;
  }

  try {
    const tokenParts = accessToken.split(".");

    if (tokenParts.length !== 3) {
      return null;
    }

    const base64Payload = tokenParts[1].replace(/-/g, "+").replace(/_/g, "/");

    const paddedPayload = base64Payload.padEnd(
      Math.ceil(base64Payload.length / 4) * 4,
      "=",
    );

    const payload = atob(paddedPayload);

    return JSON.parse(payload);
  } catch (error) {
    console.error("Unable to decode access token:", error);
    return null;
  }
};

const isUuid = (value) => {
  if (typeof value !== "string") {
    return false;
  }

  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value,
  );
};

const getCurrentUserId = () => {
  const payload = decodeTokenPayload();

  if (!payload) {
    return null;
  }

  const possibleUserIds = [
    payload.userId,
    payload.user_id,
    payload.id,
    payload.sub,
  ];

  return possibleUserIds.find((value) => isUuid(value)) || null;
};

const getCurrentEmail = () => {
  const payload = decodeTokenPayload();

  if (!payload) {
    return null;
  }

  if (payload.email) {
    return payload.email;
  }

  if (typeof payload.sub === "string" && payload.sub.includes("@")) {
    return payload.sub;
  }

  return null;
};

const getCurrentRole = () => {
  const payload = decodeTokenPayload();

  return (
    payload?.role || payload?.authority || payload?.authorities?.[0] || null
  );
};

const tokenService = {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
  hasAccessToken,
  decodeTokenPayload,
  getCurrentUserId,
  getCurrentEmail,
  getCurrentRole,
};

export default tokenService;
