import apiClient from "./axiosInstance";

/**
 * Create a pending follow request.
 */
export const sendFollowRequest = async (receiverId) => {
  if (!receiverId) {
    throw new Error("Receiver user ID is required");
  }

  const response = await apiClient.post("/api/social/follow-requests", {
    receiverId,
  });

  return response.data;
};

/**
 * Check the complete relationship state between
 * the logged-in user and another user.
 *
 * Expected response:
 * {
 *   following: false,
 *   requestPending: true,
 *   requestStatus: "PENDING"
 * }
 */
export const getRelationshipStatusRequest = async (receiverId) => {
  if (!receiverId) {
    throw new Error("Receiver ID is required");
  }

  const response = await apiClient.get(
    `/api/social/follow-requests/${receiverId}/status`,
  );

  return response.data;
};

/**
 * Return follow requests received by the
 * logged-in user.
 */
export const getIncomingFollowRequestsRequest = async () => {
  const response = await apiClient.get("/api/social/follow-requests/incoming");

  return response.data;
};

/**
 * Accept an incoming follow request.
 */
export const acceptFollowRequestRequest = async (requestId) => {
  if (!requestId) {
    throw new Error("Follow request ID is required");
  }

  const response = await apiClient.post(
    `/api/social/follow-requests/${requestId}/accept`,
  );

  return response.data;
};

/**
 * Reject an incoming follow request.
 */
export const rejectFollowRequestRequest = async (requestId) => {
  if (!requestId) {
    throw new Error("Follow request ID is required");
  }

  const response = await apiClient.post(
    `/api/social/follow-requests/${requestId}/reject`,
  );

  return response.data;
};

/**
 * Remove an accepted follow relationship.
 */
export const unfollowUserRequest = async (followedUserId) => {
  if (!followedUserId) {
    throw new Error("Followed user ID is required");
  }

  const response = await apiClient.post("/api/social/unfollow", {
    followedUserId,
  });

  return response.data;
};

/**
 * Return the logged-in user's social totals.
 *
 * Expected response:
 * {
 *   followingCount: 1,
 *   followerCount: 2,
 *   pendingIncomingRequestCount: 1
 * }
 */
export const getSocialSummaryRequest = async () => {
  const response = await apiClient.get("/api/social/summary");

  return response.data;
};

/**
 * Return profiles followed by the logged-in user.
 */
export const getFollowingUsersRequest = async () => {
  const response = await apiClient.get("/api/social/following");

  return response.data;
};

/**
 * Return suggested profiles.
 */
export const getSuggestedUsersRequest = async (limit = 6) => {
  const safeLimit = Number.isInteger(limit) && limit > 0 ? limit : 6;

  const response = await apiClient.get("/api/social/suggestions", {
    params: {
      limit: safeLimit,
    },
  });

  return response.data;
};
