import apiClient from "./axiosInstance";

export const createPostRequest = async ({ userId, content }) => {
  if (!userId) {
    throw new Error("User ID is required");
  }

  if (!content?.trim()) {
    throw new Error("Post content is required");
  }

  const response = await apiClient.post("/api/posts", {
    userId,
    content: content.trim(),
  });

  return response.data;
};

export const getUserPostsRequest = async (userId, page = 0, size = 10) => {
  if (!userId) {
    throw new Error("User ID is required");
  }

  const response = await apiClient.get(`/api/posts/user/${userId}`, {
    params: {
      page,
      size,
    },
  });

  return response.data;
};
