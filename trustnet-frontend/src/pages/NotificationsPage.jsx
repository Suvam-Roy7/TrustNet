import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import {
  ArrowLeft,
  Bell,
  Check,
  Heart,
  MessageCircle,
  UserCheck,
  UserPlus,
} from "lucide-react";
import { toast } from "react-toastify";

import {
  getNotificationsRequest,
  markNotificationAsReadRequest,
} from "../api/notificationApi";

import { getProfileByUserIdRequest } from "../api/profileApi";

import AppPageLayout from "../components/layout/AppPageLayout";

import "../styles/notifications.css";

const extractNotifications = (response) => {
  if (Array.isArray(response)) {
    return response;
  }

  if (Array.isArray(response?.content)) {
    return response.content;
  }

  if (Array.isArray(response?.notifications)) {
    return response.notifications;
  }

  return [];
};

const getNotificationId = (notification) =>
  notification?.id || notification?.notificationId || null;

const isNotificationRead = (notification) =>
  notification?.isRead === true || notification?.read === true;

const getInitials = (name) => {
  if (!name?.trim()) {
    return "TN";
  }

  const readableName = name.replace(/([a-z])([A-Z])/g, "$1 $2");

  return readableName
    .split(/[\s._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word.charAt(0).toUpperCase())
    .join("");
};

const formatNotificationDate = (createdAt) => {
  if (!createdAt) {
    return "Recently";
  }

  const date = new Date(createdAt);

  if (Number.isNaN(date.getTime())) {
    return "Recently";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(date);
};

const getNotificationIcon = (type) => {
  switch (type) {
    case "FOLLOW_REQUEST":
      return <UserPlus size={19} />;

    case "FOLLOW_ACCEPTED":
      return <UserCheck size={19} />;

    case "LIKE":
      return <Heart size={19} />;

    case "COMMENT":
    case "MENTION":
      return <MessageCircle size={19} />;

    default:
      return <Bell size={19} />;
  }
};

const getNotificationMessage = (notification) => {
  if (notification?.message) {
    return notification.message;
  }

  switch (notification?.type) {
    case "FOLLOW_REQUEST":
      return "sent you a follow request.";

    case "FOLLOW_ACCEPTED":
      return "accepted your follow request.";

    case "LIKE":
      return "appreciated your post.";

    case "COMMENT":
      return "commented on your post.";

    case "MENTION":
      return "mentioned you in a post.";

    default:
      return "sent you an update.";
  }
};

function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);

  const [isLoading, setIsLoading] = useState(true);

  const [errorMessage, setErrorMessage] = useState("");

  const [markingNotificationId, setMarkingNotificationId] = useState(null);

  const loadNotifications = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await getNotificationsRequest(0, 20);

      const rawNotifications = extractNotifications(response);

      const profileRequestCache = new Map();

      const enrichedNotifications = await Promise.all(
        rawNotifications.map(async (notification) => {
          const actorUserId = notification?.actorUserId;

          if (!actorUserId) {
            return {
              ...notification,
              actorProfile: {
                username: "TrustNet User",
                profilePictureUrl: null,
              },
            };
          }

          try {
            if (!profileRequestCache.has(actorUserId)) {
              profileRequestCache.set(
                actorUserId,
                getProfileByUserIdRequest(actorUserId),
              );
            }

            const actorProfile = await profileRequestCache.get(actorUserId);

            return {
              ...notification,
              actorProfile,
            };
          } catch (profileError) {
            console.error(
              "Unable to load notification actor profile:",
              actorUserId,
              profileError.response?.status,
              profileError.response?.data || profileError.message,
            );

            return {
              ...notification,
              actorProfile: {
                username: "TrustNet User",
                profilePictureUrl: null,
              },
            };
          }
        }),
      );

      setNotifications(enrichedNotifications);
    } catch (error) {
      console.error(
        "Unable to load notifications:",
        error.response?.status,
        error.response?.data || error.message,
      );

      const responseData = error.response?.data;

      const message =
        typeof responseData === "string"
          ? responseData
          : responseData?.message ||
            responseData?.error ||
            "Unable to load notifications.";

      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadNotifications();
  }, [loadNotifications]);

  const handleMarkAsRead = async (notification) => {
    const notificationId = getNotificationId(notification);

    if (
      !notificationId ||
      isNotificationRead(notification) ||
      markingNotificationId
    ) {
      return;
    }

    setMarkingNotificationId(notificationId);

    try {
      await markNotificationAsReadRequest(notificationId);

      setNotifications((currentNotifications) =>
        currentNotifications.map((item) => {
          const currentId = getNotificationId(item);

          if (currentId !== notificationId) {
            return item;
          }

          return {
            ...item,
            isRead: true,
            read: true,
          };
        }),
      );
    } catch (error) {
      const responseData = error.response?.data;

      const message =
        typeof responseData === "string"
          ? responseData
          : responseData?.message || "Unable to mark notification as read.";

      toast.error(message);
    } finally {
      setMarkingNotificationId(null);
    }
  };

  const handleNotificationAction = (notification) => {
    handleMarkAsRead(notification);
  };

  return (
    <AppPageLayout>
      <main className="notifications-page">
        <header className="notifications-header">
          <div className="notifications-header-content">
            <Link className="notifications-back-link" to="/">
              <ArrowLeft size={18} />
              <span>Back to Home</span>
            </Link>

            <div className="notifications-heading">
              <div className="notifications-heading-icon">
                <Bell size={24} />
              </div>

              <div>
                <p>Your important updates</p>
                <h1>Notifications</h1>
              </div>
            </div>

            <p className="notifications-description">
              Follow requests and meaningful activity from your TrustNet
              network.
            </p>
          </div>
        </header>

        <section className="notifications-content">
          {isLoading && (
            <div className="notifications-status">Loading notifications...</div>
          )}

          {!isLoading && errorMessage && (
            <div className="notifications-status notifications-error">
              <span>{errorMessage}</span>

              <button type="button" onClick={loadNotifications}>
                Try again
              </button>
            </div>
          )}

          {!isLoading && !errorMessage && notifications.length === 0 && (
            <div className="notifications-empty">
              <div className="notifications-empty-icon">
                <Bell size={30} />
              </div>

              <h2>No notifications yet</h2>

              <p>
                Follow requests and other important updates will appear here.
              </p>
            </div>
          )}

          {!isLoading && !errorMessage && notifications.length > 0 && (
            <div className="notifications-list">
              {notifications.map((notification, index) => {
                const notificationId = getNotificationId(notification);

                const notificationKey =
                  notificationId ||
                  `${notification.type}-${notification.createdAt}-${index}`;

                const read = isNotificationRead(notification);

                const isMarking = markingNotificationId === notificationId;

                const actorName =
                  notification?.actorProfile?.username || "TrustNet User";

                const actorImage =
                  notification?.actorProfile?.profilePictureUrl ||
                  notification?.actorProfile?.profileImageUrl ||
                  null;

                const isFollowRequest = notification.type === "FOLLOW_REQUEST";

                return (
                  <motion.article
                    key={notificationKey}
                    className={`notification-card ${
                      read ? "notification-read" : "notification-unread"
                    }`}
                    initial={{
                      opacity: 0,
                      y: 10,
                    }}
                    animate={{
                      opacity: 1,
                      y: 0,
                    }}
                  >
                    <button
                      type="button"
                      className="notification-main-button"
                      disabled={isMarking}
                      onClick={() => handleMarkAsRead(notification)}
                    >
                      <div className="notification-avatar">
                        {actorImage ? (
                          <img src={actorImage} alt={actorName} />
                        ) : (
                          <span>{getInitials(actorName)}</span>
                        )}
                      </div>

                      <div className="notification-information">
                        <p className="notification-message">
                          <strong>{actorName}</strong>{" "}
                          <span>{getNotificationMessage(notification)}</span>
                        </p>

                        <span className="notification-time">
                          {formatNotificationDate(notification.createdAt)}
                        </span>
                      </div>

                      <div className="notification-type-icon">
                        {read ? (
                          <Check size={18} />
                        ) : (
                          getNotificationIcon(notification.type)
                        )}
                      </div>
                    </button>

                    {isFollowRequest && (
                      <div className="notification-actions">
                        <Link
                          className="notification-review-link"
                          to="/connections"
                          onClick={() => handleNotificationAction(notification)}
                        >
                          Review request
                        </Link>
                      </div>
                    )}
                  </motion.article>
                );
              })}
            </div>
          )}
        </section>
      </main>
    </AppPageLayout>
  );
}

export default NotificationsPage;
