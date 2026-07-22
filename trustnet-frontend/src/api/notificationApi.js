import apiClient from "./axiosInstance";

/**
 * Load notifications for the logged-in user.
 *
 * Backend:
 * GET /api/notifications?page=0&size=20
 */
export const getNotificationsRequest = async (page = 0, size = 20) => {
  const safePage = Number.isInteger(page) && page >= 0 ? page : 0;

  const safeSize = Number.isInteger(size) && size > 0 ? Math.min(size, 50) : 20;

  const response = await apiClient.get("/api/notifications", {
    params: {
      page: safePage,
      size: safeSize,
    },
  });

  return response.data;
};

/**
 * Mark one notification as read.
 *
 * Backend:
 * PATCH /api/notifications/{notificationId}/read
 */
export const markNotificationAsReadRequest = async (notificationId) => {
  if (!notificationId) {
    throw new Error("Notification ID is required");
  }

  const response = await apiClient.patch(
    `/api/notifications/${notificationId}/read`,
  );

  return response.data;
};

/**
 * Return the unread notification total.
 *
 * Backend:
 * GET /api/notifications/unread-count
 */
export const getUnreadNotificationCountRequest = async () => {
  const response = await apiClient.get("/api/notifications/unread-count");

  return response.data;
};
