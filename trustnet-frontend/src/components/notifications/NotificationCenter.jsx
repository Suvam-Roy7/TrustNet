import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "motion/react";
import {
  Bell,
  Check,
  ChevronRight,
  Clock3,
  LoaderCircle,
  UserPlus,
  Users,
  X,
} from "lucide-react";
import { toast } from "react-toastify";

import axiosInstance from "../../api/axiosInstance";
import { getProfileByUserIdRequest } from "../../api/profileApi";
import {
  acceptFollowRequestRequest,
  getIncomingFollowRequestsRequest,
  getRelationshipStatusRequest,
  rejectFollowRequestRequest,
  sendFollowRequest,
} from "../../api/socialApi";

import "../../styles/notificationCenter.css";

const getInitials = (name) => {
  if (!name?.trim()) {
    return "TN";
  }

  return name
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .split(/[\s._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word.charAt(0).toUpperCase())
    .join("");
};

const extractList = (response) => {
  const payload = response?.data ?? response;

  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  if (Array.isArray(payload?.notifications)) {
    return payload.notifications;
  }

  if (Array.isArray(payload?.requests)) {
    return payload.requests;
  }

  return [];
};

const normalizeProfile = (profile) => {
  const profileData = profile?.data ?? profile;

  if (!profileData) {
    return null;
  }

  return {
    userId:
      profileData.userId || profileData.id || profileData.profileId || null,
    username:
      profileData.username ||
      profileData.userName ||
      profileData.displayName ||
      "TrustNet User",
    bio: profileData.bio || "",
    trustLevel: profileData.trustLevel || profileData.trust_level || "NEW_USER",
    profilePictureUrl:
      profileData.profilePictureUrl ||
      profileData.profileImageUrl ||
      profileData.avatarUrl ||
      null,
  };
};

const formatNotificationTime = (value) => {
  if (!value) {
    return "Recently";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Recently";
  }

  const difference = Date.now() - date.getTime();
  const minutes = Math.max(0, Math.floor(difference / 60000));

  if (minutes < 1) {
    return "Just now";
  }

  if (minutes < 60) {
    return `${minutes}m ago`;
  }

  const hours = Math.floor(minutes / 60);

  if (hours < 24) {
    return `${hours}h ago`;
  }

  const days = Math.floor(hours / 24);

  if (days < 7) {
    return `${days}d ago`;
  }

  return date.toLocaleDateString(undefined, {
    day: "numeric",
    month: "short",
  });
};

const isPendingRelationship = (response) => {
  const payload = response?.data ?? response;

  return (
    payload?.requestPending === true || payload?.requestStatus === "PENDING"
  );
};

function NotificationCenter({ isOpen, onClose, onCountChange }) {
  const navigate = useNavigate();
  const panelRef = useRef(null);

  const [activeTab, setActiveTab] = useState("NOTIFICATIONS");
  const [activityNotifications, setActivityNotifications] = useState([]);
  const [connectionRequests, setConnectionRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [processingRequestId, setProcessingRequestId] = useState(null);
  const [markingNotificationId, setMarkingNotificationId] = useState(null);

  const loadNotificationCenter = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const [notificationsResult, requestsResult] = await Promise.allSettled([
        axiosInstance.get("/api/notifications", {
          params: {
            page: 0,
            size: 8,
          },
        }),
        getIncomingFollowRequestsRequest(),
      ]);

      if (notificationsResult.status === "fulfilled") {
        const notificationList = extractList(notificationsResult.value).filter(
          (notification) => notification?.type !== "FOLLOW_REQUEST",
        );

        const enrichedNotifications = await Promise.all(
          notificationList.map(async (notification) => {
            const actorUserId =
              notification.actorUserId ||
              notification.actorId ||
              notification.senderUserId ||
              null;

            if (!actorUserId) {
              return notification;
            }

            try {
              const actorProfileResponse =
                await getProfileByUserIdRequest(actorUserId);

              return {
                ...notification,
                actorProfile: normalizeProfile(actorProfileResponse),
              };
            } catch (profileError) {
              console.error(
                "Unable to load notification actor profile:",
                actorUserId,
                profileError.response?.status,
              );

              return notification;
            }
          }),
        );

        setActivityNotifications(enrichedNotifications);
      } else {
        console.error(
          "Unable to load notifications:",
          notificationsResult.reason,
        );
        setActivityNotifications([]);
      }

      if (requestsResult.status === "fulfilled") {
        const rawRequests = extractList(requestsResult.value);

        const enrichedRequests = await Promise.all(
          rawRequests.map(async (request) => {
            try {
              const requesterProfileResponse = await getProfileByUserIdRequest(
                request.requesterId,
              );

              return {
                ...request,
                requesterProfile: normalizeProfile(requesterProfileResponse),
              };
            } catch (profileError) {
              console.error(
                "Unable to load request profile:",
                request.requesterId,
                profileError.response?.status,
              );

              return {
                ...request,
                requesterProfile: {
                  userId: request.requesterId,
                  username: "TrustNet User",
                  bio: "",
                  trustLevel: "NEW_USER",
                  profilePictureUrl: null,
                },
              };
            }
          }),
        );

        setConnectionRequests(enrichedRequests);
      } else {
        console.error(
          "Unable to load connection requests:",
          requestsResult.reason,
        );
        setConnectionRequests([]);
      }

      if (
        notificationsResult.status === "rejected" &&
        requestsResult.status === "rejected"
      ) {
        setErrorMessage("Unable to load your notifications right now.");
      }
    } catch (error) {
      console.error(
        "Unable to load notification centre:",
        error.response?.data || error.message,
      );
      setErrorMessage("Unable to load your notifications right now.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadNotificationCenter();
  }, [loadNotificationCenter]);

  useEffect(() => {
    if (isOpen) {
      loadNotificationCenter();
    }
  }, [isOpen, loadNotificationCenter]);

  const unreadActivityCount = useMemo(
    () =>
      activityNotifications.filter(
        (notification) =>
          notification?.isRead !== true && notification?.read !== true,
      ).length,
    [activityNotifications],
  );

  const requestAttentionCount = useMemo(
    () =>
      connectionRequests.filter(
        (request) =>
          request.uiStatus !== "ACCEPTED" || request.followBackPending !== true,
      ).length,
    [connectionRequests],
  );

  const totalCount = unreadActivityCount + requestAttentionCount;

  useEffect(() => {
    onCountChange?.(totalCount);
  }, [onCountChange, totalCount]);

  useEffect(() => {
    const handleOutsideClick = (event) => {
      const clickedNotificationTrigger = event.target.closest(
        '[data-notification-trigger="true"]',
      );

      if (
        isOpen &&
        panelRef.current &&
        !panelRef.current.contains(event.target) &&
        !clickedNotificationTrigger
      ) {
        onClose();
      }
    };

    const handleEscape = (event) => {
      if (isOpen && event.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [isOpen, onClose]);

  const handleNotificationRead = async (notification) => {
    const notificationId = notification.notificationId || notification.id;
    const alreadyRead =
      notification.isRead === true || notification.read === true;

    if (!notificationId || alreadyRead || markingNotificationId) {
      return;
    }

    setMarkingNotificationId(notificationId);

    try {
      await axiosInstance.patch(`/api/notifications/${notificationId}/read`);

      setActivityNotifications((currentNotifications) =>
        currentNotifications.map((item) => {
          const itemId = item.notificationId || item.id;

          return itemId === notificationId
            ? {
                ...item,
                isRead: true,
                read: true,
              }
            : item;
        }),
      );
    } catch (error) {
      console.error(
        "Unable to mark notification as read:",
        error.response?.data || error.message,
      );
      toast.error("Unable to update this notification.");
    } finally {
      setMarkingNotificationId(null);
    }
  };

  const handleAcceptRequest = async (request) => {
    if (!request?.requestId || !request?.requesterId || processingRequestId) {
      return;
    }

    setProcessingRequestId(request.requestId);

    try {
      await acceptFollowRequestRequest(request.requestId);

      let relationshipStatus = null;

      try {
        relationshipStatus = await getRelationshipStatusRequest(
          request.requesterId,
        );
      } catch (statusError) {
        console.error(
          "Unable to check follow-back status:",
          statusError.response?.data || statusError.message,
        );
      }

      const relationshipPayload =
        relationshipStatus?.data ?? relationshipStatus;
      const alreadyFollowingRequester = relationshipPayload?.following === true;
      const reverseRequestPending = isPendingRelationship(relationshipPayload);

      if (alreadyFollowingRequester) {
        setConnectionRequests((currentRequests) =>
          currentRequests.filter(
            (item) => item.requestId !== request.requestId,
          ),
        );

        toast.success(
          `You accepted ${
            request.requesterProfile?.username || "the user"
          }. You now follow each other.`,
        );

        return;
      }

      setConnectionRequests((currentRequests) =>
        currentRequests.map((item) =>
          item.requestId === request.requestId
            ? {
                ...item,
                uiStatus: "ACCEPTED",
                followBackPending: reverseRequestPending,
              }
            : item,
        ),
      );

      toast.success(
        `You accepted ${
          request.requesterProfile?.username || "the user"
        }'s follow request.`,
      );
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          error.response?.data?.error ||
          "Unable to accept this request.",
      );
    } finally {
      setProcessingRequestId(null);
    }
  };

  const handleRejectRequest = async (request) => {
    if (!request?.requestId || processingRequestId) {
      return;
    }

    setProcessingRequestId(request.requestId);

    try {
      await rejectFollowRequestRequest(request.requestId);

      setConnectionRequests((currentRequests) =>
        currentRequests.filter((item) => item.requestId !== request.requestId),
      );

      toast.info("Follow request declined.");
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          error.response?.data?.error ||
          "Unable to decline this request.",
      );
    } finally {
      setProcessingRequestId(null);
    }
  };

  const handleFollowBack = async (request) => {
    if (
      !request?.requesterId ||
      processingRequestId ||
      request.followBackPending
    ) {
      return;
    }

    setProcessingRequestId(request.requestId);

    try {
      const relationshipStatus = await getRelationshipStatusRequest(
        request.requesterId,
      );
      const relationshipPayload =
        relationshipStatus?.data ?? relationshipStatus;

      if (relationshipPayload?.following === true) {
        setConnectionRequests((currentRequests) =>
          currentRequests.filter(
            (item) => item.requestId !== request.requestId,
          ),
        );

        toast.info("You already follow this user.");
        return;
      }

      if (isPendingRelationship(relationshipPayload)) {
        setConnectionRequests((currentRequests) =>
          currentRequests.map((item) =>
            item.requestId === request.requestId
              ? {
                  ...item,
                  followBackPending: true,
                }
              : item,
          ),
        );

        toast.info("Your follow-back request is already pending.");
        return;
      }

      await sendFollowRequest(request.requesterId);

      setConnectionRequests((currentRequests) =>
        currentRequests.map((item) =>
          item.requestId === request.requestId
            ? {
                ...item,
                followBackPending: true,
              }
            : item,
        ),
      );

      toast.success(
        `Follow-back request sent to ${
          request.requesterProfile?.username || "the user"
        }.`,
      );
    } catch (error) {
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Unable to send the follow-back request.";
      const alreadyExists =
        error.response?.status === 409 ||
        errorMessage.toLowerCase().includes("already") ||
        errorMessage.toLowerCase().includes("pending");

      if (alreadyExists) {
        setConnectionRequests((currentRequests) =>
          currentRequests.map((item) =>
            item.requestId === request.requestId
              ? {
                  ...item,
                  followBackPending: true,
                }
              : item,
          ),
        );

        toast.info("Your follow-back request is already pending.");
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setProcessingRequestId(null);
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <motion.section
      ref={panelRef}
      className="shared-notification-center"
      initial={{ opacity: 0, y: -8, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      aria-label="Notification centre"
    >
      <header className="shared-notification-header">
        <div>
          <span>Your updates</span>
          <h2>Notification centre</h2>
        </div>

        <button
          type="button"
          onClick={onClose}
          aria-label="Close notification centre"
        >
          <X size={19} />
        </button>
      </header>

      <div className="shared-notification-tabs" role="tablist">
        <button
          className={activeTab === "NOTIFICATIONS" ? "active" : ""}
          type="button"
          role="tab"
          onClick={() => setActiveTab("NOTIFICATIONS")}
        >
          <Bell size={16} />
          Notifications
          {unreadActivityCount > 0 && <span>{unreadActivityCount}</span>}
        </button>

        <button
          className={activeTab === "REQUESTS" ? "active" : ""}
          type="button"
          role="tab"
          onClick={() => setActiveTab("REQUESTS")}
        >
          <Users size={16} />
          Requests
          {connectionRequests.length > 0 && (
            <span>{connectionRequests.length}</span>
          )}
        </button>
      </div>

      <div className="shared-notification-body">
        {isLoading && (
          <div className="shared-notification-status">
            <LoaderCircle className="shared-notification-spinner" size={22} />
            <span>Loading your updates...</span>
          </div>
        )}

        {!isLoading && errorMessage && (
          <div className="shared-notification-status is-error">
            <span>{errorMessage}</span>
            <button type="button" onClick={loadNotificationCenter}>
              Try again
            </button>
          </div>
        )}

        {!isLoading && !errorMessage && activeTab === "NOTIFICATIONS" && (
          <div className="shared-notification-list">
            {activityNotifications.length === 0 ? (
              <div className="shared-notification-empty">
                <Bell size={23} />
                <strong>No new notifications</strong>
                <p>Your meaningful activity will appear here.</p>
              </div>
            ) : (
              activityNotifications.map((notification) => {
                const notificationId =
                  notification.notificationId || notification.id;
                const actorName =
                  notification.actorProfile?.username || "TrustNet user";
                const isRead =
                  notification.isRead === true || notification.read === true;

                return (
                  <button
                    className={`shared-notification-item ${
                      isRead ? "is-read" : "is-unread"
                    }`}
                    type="button"
                    key={notificationId}
                    disabled={markingNotificationId === notificationId}
                    onClick={() => handleNotificationRead(notification)}
                  >
                    <div className="shared-notification-avatar">
                      {notification.actorProfile?.profilePictureUrl ? (
                        <img
                          src={notification.actorProfile.profilePictureUrl}
                          alt={actorName}
                        />
                      ) : (
                        getInitials(actorName)
                      )}
                    </div>

                    <div className="shared-notification-copy">
                      <p>
                        <strong>{actorName}</strong>{" "}
                        {notification.message || "shared an update with you."}
                      </p>
                      <span>
                        <Clock3 size={12} />
                        {formatNotificationTime(notification.createdAt)}
                      </span>
                    </div>

                    {!isRead && <span className="shared-unread-dot" />}
                  </button>
                );
              })
            )}
          </div>
        )}

        {!isLoading && !errorMessage && activeTab === "REQUESTS" && (
          <div className="shared-request-list">
            {connectionRequests.length === 0 ? (
              <div className="shared-notification-empty">
                <Users size={24} />
                <strong>No pending requests</strong>
                <p>New connection requests will appear here.</p>
              </div>
            ) : (
              connectionRequests.map((request) => {
                const requester = request.requesterProfile;
                const isProcessing = processingRequestId === request.requestId;
                const isAccepted = request.uiStatus === "ACCEPTED";
                const followBackPending = request.followBackPending === true;

                return (
                  <article
                    className={`shared-request-item ${
                      isAccepted ? "is-accepted" : ""
                    }`}
                    key={request.requestId}
                  >
                    <div className="shared-notification-avatar">
                      {requester?.profilePictureUrl ? (
                        <img
                          src={requester.profilePictureUrl}
                          alt={requester.username}
                        />
                      ) : (
                        getInitials(requester?.username)
                      )}
                    </div>

                    <div className="shared-request-copy">
                      <strong>{requester?.username || "TrustNet User"}</strong>
                      <p>
                        {isAccepted
                          ? followBackPending
                            ? "Follow-back request sent."
                            : "You accepted this request."
                          : "Wants to follow you."}
                      </p>

                      {isAccepted ? (
                        <button
                          className={`shared-follow-back ${
                            followBackPending ? "is-requested" : ""
                          }`}
                          type="button"
                          disabled={isProcessing || followBackPending}
                          onClick={() => handleFollowBack(request)}
                        >
                          {followBackPending ? (
                            <Check size={15} />
                          ) : (
                            <UserPlus size={15} />
                          )}
                          {isProcessing
                            ? "Sending..."
                            : followBackPending
                              ? "Requested"
                              : "Follow Back"}
                        </button>
                      ) : (
                        <div className="shared-request-actions">
                          <button
                            className="shared-request-reject"
                            type="button"
                            disabled={isProcessing}
                            onClick={() => handleRejectRequest(request)}
                          >
                            Decline
                          </button>

                          <button
                            className="shared-request-accept"
                            type="button"
                            disabled={isProcessing}
                            onClick={() => handleAcceptRequest(request)}
                          >
                            {isProcessing ? "Updating..." : "Accept"}
                          </button>
                        </div>
                      )}
                    </div>
                  </article>
                );
              })
            )}
          </div>
        )}
      </div>

      <footer className="shared-notification-footer">
        <button
          type="button"
          onClick={() => {
            onClose();
            navigate("/notifications");
          }}
        >
          View all notifications
          <ChevronRight size={16} />
        </button>
      </footer>
    </motion.section>
  );
}

export default NotificationCenter;
