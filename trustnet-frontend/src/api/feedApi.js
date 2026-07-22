import apiClient from "./axiosInstance";

export const getHomeFeedRequest = async (page = 0, size = 10) => {
  const response = await apiClient.get("/api/feed", {
    params: {
      page,
      size,
    },
  });

  return response.data;
};
