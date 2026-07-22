import apiClient from "./axiosInstance";

/**
 * Return a profile using its user ID.
 */
export const getProfileByUserIdRequest = async (userId) => {
  if (!userId) {
    throw new Error("User ID is required");
  }

  const response = await apiClient.get(`/api/profiles/${userId}`);

  return response.data;
};

/**
 * Return a profile using an exact username.
 */
export const getProfileByUsernameRequest = async (username) => {
  const normalizedUsername = username?.trim();

  if (!normalizedUsername) {
    throw new Error("Username is required");
  }

  const encodedUsername = encodeURIComponent(normalizedUsername);

  const response = await apiClient.get(
    `/api/profiles/username/${encodedUsername}`,
  );

  return response.data;
};

/**
 * Update the logged-in user's profile.
 */
export const updateProfileRequest = async (userId, profileData) => {
  if (!userId) {
    throw new Error("User ID is required");
  }

  if (!profileData || typeof profileData !== "object") {
    throw new Error("Profile information is required");
  }

  const response = await apiClient.put(`/api/profiles/${userId}`, profileData);

  return response.data;
};
