import { NavLink, useNavigate } from "react-router-dom";
import { motion } from "motion/react";

import {
  Bell,
  Bookmark,
  Check,
  ChevronRight,
  Clock3,
  Heart,
  Home,
  Image,
  LoaderCircle,
  LogOut,
  MessageCircle,
  MoreHorizontal,
  PenLine,
  Search,
  Settings,
  ShieldCheck,
  Sparkles,
  UserPlus,
  UserRound,
  Users,
  X,
  Send,
} from "lucide-react";

import { useAuth } from "../auth/AuthContext";
import "../styles/home.css";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { getProfileByUserIdRequest } from "../api/profileApi";
import axiosInstance from "../api/axiosInstance";
import tokenService from "../auth/tokenService";

import { toast } from "react-toastify";
import { createPostRequest } from "../api/postApi";
import { getHomeFeedRequest } from "../api/feedApi";
import {
  acceptFollowRequestRequest,
  getIncomingFollowRequestsRequest,
  getRelationshipStatusRequest,
  rejectFollowRequestRequest,
  sendFollowRequest,
} from "../api/socialApi";

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

function HomePage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [isComposerOpen, setIsComposerOpen] = useState(false);

  const [postContent, setPostContent] = useState("");

  const [isPublishing, setIsPublishing] = useState(false);

  const [feedPosts, setFeedPosts] = useState([]);

  const [isFeedLoading, setIsFeedLoading] = useState(true);

  const [feedError, setFeedError] = useState("");

  const [feedMeta, setFeedMeta] = useState({
    page: 0,
    size: 10,
    hasMore: false,
    followingPostCount: 0,
    ownPostCount: 0,
    suggestedPostCount: 0,
  });
  const [isProfileLoading, setIsProfileLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const [isNotificationCenterOpen, setIsNotificationCenterOpen] =
    useState(false);
  const [notificationTab, setNotificationTab] = useState("NOTIFICATIONS");
  const [activityNotifications, setActivityNotifications] = useState([]);
  const [connectionRequests, setConnectionRequests] = useState([]);
  const [isNotificationCenterLoading, setIsNotificationCenterLoading] =
    useState(true);
  const [notificationCenterError, setNotificationCenterError] = useState("");
  const [processingRequestId, setProcessingRequestId] = useState(null);
  const [markingNotificationId, setMarkingNotificationId] = useState(null);
  const notificationCenterRef = useRef(null);

  const userId = tokenService.getCurrentUserId();
  const email = tokenService.getCurrentEmail();

  useEffect(() => {
    const loadCurrentProfile = async () => {
      if (!userId) {
        console.error("User ID is unavailable");
        setIsProfileLoading(false);
        return;
      }

      try {
        const response = await getProfileByUserIdRequest(userId);

        console.log("Profile API response:", response);

        // Supports both:
        // { username: "SuvamRoy" }
        // { data: { username: "SuvamRoy" } }
        const profileData = response?.data ?? response;

        console.log("Normalized profile:", profileData);

        setProfile(profileData);
      } catch (error) {
        console.error(
          "Unable to load profile:",
          error.response?.status,
          error.response?.data || error.message,
        );

        setProfile(null);
      } finally {
        setIsProfileLoading(false);
      }
    };

    loadCurrentProfile();
  }, [userId]);

  const loadNotificationCenter = useCallback(async () => {
    setIsNotificationCenterLoading(true);
    setNotificationCenterError("");

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
        setNotificationCenterError(
          "Unable to load your notifications right now.",
        );
      }
    } catch (error) {
      console.error(
        "Unable to load notification centre:",
        error.response?.data || error.message,
      );
      setNotificationCenterError(
        "Unable to load your notifications right now.",
      );
    } finally {
      setIsNotificationCenterLoading(false);
    }
  }, []);

  useEffect(() => {
    loadNotificationCenter();
  }, [loadNotificationCenter]);

  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (
        notificationCenterRef.current &&
        !notificationCenterRef.current.contains(event.target)
      ) {
        setIsNotificationCenterOpen(false);
      }
    };

    const handleEscape = (event) => {
      if (event.key === "Escape") {
        setIsNotificationCenterOpen(false);
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
      document.removeEventListener("keydown", handleEscape);
    };
  }, []);

  const loadHomeFeed = useCallback(
    async (pageNumber = 0, replacePosts = true) => {
      if (replacePosts) {
        setIsFeedLoading(true);
      }

      setFeedError("");

      try {
        const response = await getHomeFeedRequest(pageNumber, 10);

        const loadedPosts = Array.isArray(response)
          ? response
          : response?.content || [];

        setFeedPosts((currentPosts) => {
          if (replacePosts) {
            return loadedPosts;
          }

          const combinedPosts = [...currentPosts, ...loadedPosts];

          /*
           * Remove duplicated posts using postId.
           */
          const uniquePosts = new Map();

          combinedPosts.forEach((post, index) => {
            const key = post.postId || post.id || `${post.createdAt}-${index}`;

            uniquePosts.set(key, post);
          });

          return Array.from(uniquePosts.values());
        });

        setFeedMeta({
          page: response?.page ?? pageNumber,

          size: response?.size ?? 10,

          hasMore: response?.hasMore ?? false,
        });
      } catch (error) {
        console.error(
          "Unable to load home feed:",
          error.response?.status,
          error.response?.data || error.message,
        );

        setFeedError(
          error.response?.data?.message || "Unable to load your feed.",
        );
      } finally {
        if (replacePosts) {
          setIsFeedLoading(false);
        }
      }
    },
    [],
  );

  useEffect(() => {
    loadHomeFeed(0, true);
  }, [loadHomeFeed]);

  const displayName = useMemo(() => {
    const username = profile?.username ?? profile?.userName;

    if (typeof username === "string" && username.trim()) {
      return username.trim();
    }

    return isProfileLoading ? "Loading..." : "TrustNet User";
  }, [profile, isProfileLoading]);

  const initials = useMemo(() => {
    return displayName
      .split(/[\s._-]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((word) => word.charAt(0).toUpperCase())
      .join("");
  }, [displayName]);

  const trustLevel = useMemo(() => {
    if (!profile?.trustLevel) {
      return "New member";
    }

    return profile.trustLevel
      .replaceAll("_", " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) => letter.toUpperCase());
  }, [profile]);

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

  const bellCount = unreadActivityCount + requestAttentionCount;

  const handleCreatePost = async (event) => {
    event.preventDefault();

    const cleanContent = postContent.trim();

    if (!cleanContent) {
      toast.error("Write something before publishing.");
      return;
    }

    if (!userId) {
      toast.error("Unable to identify the logged-in user.");
      return;
    }

    try {
      setIsPublishing(true);

      const createdPost = await createPostRequest({
        userId,
        content: cleanContent,
      });

      const optimisticPost = {
        postId:
          createdPost?.postId || createdPost?.id || `temporary-${Date.now()}`,

        authorUserId:
          createdPost?.userId || createdPost?.authorUserId || userId,

        authorUsername:
          createdPost?.username || createdPost?.authorUsername || displayName,

        authorProfilePictureUrl: null,

        content: createdPost?.content || cleanContent,

        likeCount: createdPost?.likeCount ?? 0,

        commentCount: createdPost?.commentCount ?? 0,

        likedByCurrentUser: false,

        createdAt: createdPost?.createdAt || new Date().toISOString(),

        sourceType: "OWN",

        suggestionReason: null,

        suggestionReasonText: null,
      };

      setFeedPosts((currentPosts) => [optimisticPost, ...currentPosts]);

      setPostContent("");
      setIsComposerOpen(false);

      toast.success("Your post has been published.");
    } catch (error) {
      console.error("Post creation failed:", error.response?.data || error);

      const message =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Unable to publish your post.";

      toast.error(message);
    } finally {
      setIsPublishing(false);
    }
  };

  const handleLoadMore = async () => {
    if (isLoadingMore || !feedMeta.hasMore) {
      return;
    }

    try {
      setIsLoadingMore(true);

      await loadHomeFeed(feedMeta.page + 1, false);
    } finally {
      setIsLoadingMore(false);
    }
  };

  const toggleNotificationCenter = () => {
    setIsNotificationCenterOpen((currentValue) => !currentValue);
  };

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

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      navigate("/login", {
        replace: true,
      });
    }
  };

  return (
    <div className="trustnet-app">
      {/* SIDEBAR */}

      <aside className="home-sidebar">
        <div>
          <div className="home-logo">
            <div className="home-logo-mark">
              <ShieldCheck size={22} />
            </div>

            <div>
              <h2>TrustNet</h2>
              <span>Social, without the noise</span>
            </div>
          </div>

          <nav className="home-navigation">
            <button className="navigation-item active" type="button">
              <Home size={21} />
              <span>Home</span>
            </button>

            <NavLink className="navigation-item" to="/profile">
              <UserRound size={22} />
              <span>Profile</span>
            </NavLink>

            <NavLink className="navigation-item" to="/connections">
              <Users size={22} />
              <span>Connections</span>
            </NavLink>

            <button className="navigation-item" type="button">
              <Bookmark size={21} />
              <span>Saved</span>
            </button>

            <button className="navigation-item" type="button">
              <Settings size={21} />
              <span>Settings</span>
            </button>
          </nav>
        </div>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-avatar">{initials}</div>

            <div className="sidebar-user-info">
              <strong>{isProfileLoading ? "Loading..." : displayName}</strong>

              <span>{trustLevel}</span>
            </div>
          </div>

          <button
            className="sidebar-logout"
            type="button"
            onClick={handleLogout}
            aria-label="Logout"
          >
            <LogOut size={20} />
          </button>
        </div>
      </aside>

      {/* MAIN AREA */}

      <main className="home-main">
        <header className="home-topbar">
          <div>
            <span className="home-eyebrow">Your private social space</span>
            <h1>Home</h1>
          </div>

          <div className="topbar-actions">
            <div className="home-search">
              <Search size={19} />

              <input
                type="search"
                placeholder="Search people or posts"
                aria-label="Search TrustNet"
              />
            </div>

            <div
              className="notification-center-shell"
              ref={notificationCenterRef}
            >
              <button
                className={`topbar-icon-button ${
                  isNotificationCenterOpen ? "is-active" : ""
                }`}
                type="button"
                aria-label="Open notification centre"
                aria-expanded={isNotificationCenterOpen}
                onClick={toggleNotificationCenter}
              >
                <Bell size={21} />

                {bellCount > 0 && (
                  <span className="topbar-notification-count">
                    {bellCount > 99 ? "99+" : bellCount}
                  </span>
                )}
              </button>

              {isNotificationCenterOpen && (
                <motion.section
                  className="notification-center-popover"
                  initial={{ opacity: 0, y: -8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                >
                  <header className="notification-center-header">
                    <div>
                      <span>Your updates</span>
                      <h2>Notification centre</h2>
                    </div>

                    <button
                      type="button"
                      onClick={() => setIsNotificationCenterOpen(false)}
                      aria-label="Close notification centre"
                    >
                      <X size={19} />
                    </button>
                  </header>

                  <div className="notification-center-tabs" role="tablist">
                    <button
                      className={
                        notificationTab === "NOTIFICATIONS" ? "active" : ""
                      }
                      type="button"
                      role="tab"
                      onClick={() => setNotificationTab("NOTIFICATIONS")}
                    >
                      <Bell size={16} />
                      Notifications
                      {unreadActivityCount > 0 && (
                        <span>{unreadActivityCount}</span>
                      )}
                    </button>

                    <button
                      className={notificationTab === "REQUESTS" ? "active" : ""}
                      type="button"
                      role="tab"
                      onClick={() => setNotificationTab("REQUESTS")}
                    >
                      <Users size={16} />
                      Requests
                      {connectionRequests.length > 0 && (
                        <span>{connectionRequests.length}</span>
                      )}
                    </button>
                  </div>

                  <div className="notification-center-body">
                    {isNotificationCenterLoading && (
                      <div className="notification-center-status">
                        <LoaderCircle
                          className="notification-spinner"
                          size={22}
                        />
                        <span>Loading your updates...</span>
                      </div>
                    )}

                    {!isNotificationCenterLoading &&
                      notificationCenterError && (
                        <div className="notification-center-status is-error">
                          <span>{notificationCenterError}</span>
                          <button
                            type="button"
                            onClick={loadNotificationCenter}
                          >
                            Try again
                          </button>
                        </div>
                      )}

                    {!isNotificationCenterLoading &&
                      !notificationCenterError &&
                      notificationTab === "NOTIFICATIONS" && (
                        <div className="notification-activity-list">
                          {activityNotifications.length === 0 ? (
                            <div className="notification-center-empty">
                              <Bell size={23} />
                              <strong>No new notifications</strong>
                              <p>Your meaningful activity will appear here.</p>
                            </div>
                          ) : (
                            activityNotifications.map((notification) => {
                              const notificationId =
                                notification.notificationId || notification.id;
                              const actorName =
                                notification.actorProfile?.username ||
                                "TrustNet user";
                              const isRead =
                                notification.isRead === true ||
                                notification.read === true;

                              return (
                                <button
                                  className={`notification-activity-item ${
                                    isRead ? "is-read" : "is-unread"
                                  }`}
                                  type="button"
                                  key={notificationId}
                                  disabled={
                                    markingNotificationId === notificationId
                                  }
                                  onClick={() =>
                                    handleNotificationRead(notification)
                                  }
                                >
                                  <div className="notification-item-avatar">
                                    {notification.actorProfile
                                      ?.profilePictureUrl ? (
                                      <img
                                        src={
                                          notification.actorProfile
                                            .profilePictureUrl
                                        }
                                        alt={actorName}
                                      />
                                    ) : (
                                      getInitials(actorName)
                                    )}
                                  </div>

                                  <div className="notification-item-copy">
                                    <p>
                                      <strong>{actorName}</strong>{" "}
                                      {notification.message ||
                                        "shared an update with you."}
                                    </p>
                                    <span>
                                      <Clock3 size={12} />
                                      {formatNotificationTime(
                                        notification.createdAt,
                                      )}
                                    </span>
                                  </div>

                                  {!isRead && (
                                    <span className="notification-unread-dot" />
                                  )}
                                </button>
                              );
                            })
                          )}
                        </div>
                      )}

                    {!isNotificationCenterLoading &&
                      !notificationCenterError &&
                      notificationTab === "REQUESTS" && (
                        <div className="connection-request-list">
                          {connectionRequests.length === 0 ? (
                            <div className="notification-center-empty">
                              <Users size={24} />
                              <strong>No pending requests</strong>
                              <p>New connection requests will appear here.</p>
                            </div>
                          ) : (
                            connectionRequests.map((request) => {
                              const requester = request.requesterProfile;
                              const isProcessing =
                                processingRequestId === request.requestId;
                              const isAccepted =
                                request.uiStatus === "ACCEPTED";
                              const followBackPending =
                                request.followBackPending === true;

                              return (
                                <article
                                  className={`connection-request-item ${
                                    isAccepted ? "is-accepted" : ""
                                  }`}
                                  key={request.requestId}
                                >
                                  <div className="notification-item-avatar request-avatar">
                                    {requester?.profilePictureUrl ? (
                                      <img
                                        src={requester.profilePictureUrl}
                                        alt={requester.username}
                                      />
                                    ) : (
                                      getInitials(requester?.username)
                                    )}
                                  </div>

                                  <div className="connection-request-copy">
                                    <strong>
                                      {requester?.username || "TrustNet User"}
                                    </strong>
                                    <p>
                                      {isAccepted
                                        ? followBackPending
                                          ? "Follow-back request sent."
                                          : "You accepted this request."
                                        : "Wants to follow you."}
                                    </p>

                                    {isAccepted ? (
                                      <button
                                        className={`request-follow-back ${
                                          followBackPending
                                            ? "is-requested"
                                            : ""
                                        }`}
                                        type="button"
                                        disabled={
                                          isProcessing || followBackPending
                                        }
                                        onClick={() =>
                                          handleFollowBack(request)
                                        }
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
                                      <div className="connection-request-actions">
                                        <button
                                          className="request-reject"
                                          type="button"
                                          disabled={isProcessing}
                                          onClick={() =>
                                            handleRejectRequest(request)
                                          }
                                        >
                                          Decline
                                        </button>
                                        <button
                                          className="request-accept"
                                          type="button"
                                          disabled={isProcessing}
                                          onClick={() =>
                                            handleAcceptRequest(request)
                                          }
                                        >
                                          {isProcessing
                                            ? "Updating..."
                                            : "Accept"}
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

                  <footer className="notification-center-footer">
                    <button
                      type="button"
                      onClick={() => {
                        setIsNotificationCenterOpen(false);
                        navigate("/notifications");
                      }}
                    >
                      View all notifications
                      <ChevronRight size={16} />
                    </button>
                  </footer>
                </motion.section>
              )}
            </div>
          </div>
        </header>

        <div className="home-content">
          {/* FEED */}

          <section className="feed-column">
            <motion.article
              className="create-post-card"
              initial={{
                opacity: 0,
                y: 12,
              }}
              animate={{
                opacity: 1,
                y: 0,
              }}
              transition={{
                duration: 0.35,
              }}
            >
              <div className="create-post-top">
                <div className="feed-avatar">{initials}</div>

                <button
                  className="create-post-input"
                  type="button"
                  onClick={() => setIsComposerOpen(true)}
                >
                  Share something meaningful...
                </button>
              </div>

              <div className="create-post-footer">
                <button type="button" onClick={() => setIsComposerOpen(true)}>
                  <PenLine size={18} />
                  Write a post
                </button>

                <button type="button">
                  <Image size={18} />
                  Add media
                </button>

                <button
                  className="publish-button"
                  type="button"
                  onClick={() => setIsComposerOpen(true)}
                >
                  Create
                </button>
              </div>
            </motion.article>

            <div className="feed-heading">
              <div>
                <span className="feed-heading-icon">
                  <Sparkles size={17} />
                </span>

                <h2>Your feed</h2>
              </div>

              <span>Thoughtful updates from your connections</span>
            </div>

            {isFeedLoading && (
              <div className="feed-status">Loading meaningful updates...</div>
            )}

            {!isFeedLoading && feedError && (
              <div className="feed-status feed-error">
                <span>{feedError}</span>

                <button type="button" onClick={() => loadHomeFeed(0, true)}>
                  Try again
                </button>
              </div>
            )}

            {!isFeedLoading && !feedError && feedPosts.length === 0 && (
              <div className="feed-status">
                Your feed is quiet right now. Create a post or connect with
                people.
              </div>
            )}

            {feedPosts.map((post, index) => {
              const isOwnPost =
                post.sourceType === "OWN" || post.authorUserId === userId;

              const authorName =
                post.authorUsername ||
                (isOwnPost ? displayName : "TrustNet User");

              const authorInitials = getInitials(authorName);

              return (
                <motion.article
                  className="post-card created-post-card"
                  key={post.postId || post.id || `${post.createdAt}-${index}`}
                  initial={{
                    opacity: 0,
                    y: 14,
                  }}
                  animate={{
                    opacity: 1,
                    y: 0,
                  }}
                >
                  {post.sourceType === "SUGGESTED" && (
                    <div className="suggestion-label">
                      <Sparkles size={15} />

                      <span>
                        {post.suggestionReasonText || "Suggested for you"}
                      </span>
                    </div>
                  )}

                  <header className="post-header">
                    <div className="post-author">
                      <div className="feed-avatar">{authorInitials}</div>

                      <div>
                        <div className="post-author-name">
                          <strong>{authorName}</strong>
                        </div>

                        <span>
                          {post.sourceType === "OWN" && "Your post"}

                          {post.sourceType === "FOLLOWING" &&
                            "From someone you follow"}

                          {post.sourceType === "SUGGESTED" &&
                            "Suggested update"}
                        </span>
                      </div>
                    </div>

                    <button
                      className="post-options"
                      type="button"
                      aria-label="Post options"
                    >
                      <MoreHorizontal size={22} />
                    </button>
                  </header>

                  <div className="post-content user-post-content">
                    <p>{post.content}</p>
                  </div>

                  <footer className="post-actions">
                    <button type="button">
                      <Heart size={20} />

                      <span>
                        Appreciate
                        {post.likeCount > 0 ? ` (${post.likeCount})` : ""}
                      </span>
                    </button>

                    <button type="button">
                      <MessageCircle size={20} />

                      <span>
                        Comment
                        {post.commentCount > 0 ? ` (${post.commentCount})` : ""}
                      </span>
                    </button>

                    <button type="button">
                      <Bookmark size={20} />
                      <span>Save</span>
                    </button>
                  </footer>
                </motion.article>
              );
            })}

            {!isFeedLoading &&
              !feedError &&
              feedPosts.length > 0 &&
              feedMeta.hasMore && (
                <div className="feed-load-more-wrapper">
                  <button
                    className="feed-load-more-button"
                    type="button"
                    onClick={handleLoadMore}
                    disabled={isLoadingMore}
                  >
                    {isLoadingMore ? "Loading more..." : "Load more updates"}
                  </button>
                </div>
              )}
            {!isFeedLoading &&
              !feedError &&
              feedPosts.length > 0 &&
              !feedMeta.hasMore && (
                <div className="feed-end-message">You’re all caught up.</div>
              )}

            <motion.article
              className="post-card"
              initial={{
                opacity: 0,
                y: 14,
              }}
              animate={{
                opacity: 1,
                y: 0,
              }}
              transition={{
                duration: 0.4,
                delay: 0.1,
              }}
            >
              <header className="post-header">
                <div className="post-author">
                  <div className="post-avatar trustnet-avatar">
                    <ShieldCheck size={22} />
                  </div>

                  <div>
                    <div className="post-author-name">
                      <strong>TrustNet</strong>
                      <span className="verified-badge">
                        <ShieldCheck size={13} />
                      </span>
                    </div>

                    <span>Welcome message · Just now</span>
                  </div>
                </div>

                <button
                  className="post-options"
                  type="button"
                  aria-label="Post options"
                >
                  <MoreHorizontal size={22} />
                </button>
              </header>

              <div className="post-content">
                <p>Welcome to your quieter corner of the internet.</p>

                <p>
                  TrustNet is designed for genuine connections, purposeful
                  conversations and privacy-first sharing. Your feed will begin
                  filling as you connect with people who matter to you.
                </p>
              </div>

              <div className="post-purpose-note">
                <Sparkles size={17} />

                <span>No endless scrolling. Only meaningful updates.</span>
              </div>

              <footer className="post-actions">
                <button type="button">
                  <Heart size={20} />
                  <span>Appreciate</span>
                </button>

                <button type="button">
                  <MessageCircle size={20} />
                  <span>Comment</span>
                </button>

                <button type="button">
                  <Bookmark size={20} />
                  <span>Save</span>
                </button>
              </footer>
            </motion.article>
          </section>

          {/* RIGHT PANEL */}

          <aside className="home-right-panel">
            <motion.section
              className="profile-summary-card"
              initial={{
                opacity: 0,
                x: 14,
              }}
              animate={{
                opacity: 1,
                x: 0,
              }}
              transition={{
                duration: 0.4,
                delay: 0.15,
              }}
            >
              <div className="profile-card-cover" />

              <div className="profile-summary-content">
                <div className="profile-large-avatar">{initials}</div>

                <h3>{isProfileLoading ? "Loading profile..." : displayName}</h3>

                <span className="profile-username">{email || trustLevel}</span>
                <span className="profile-trust-level">{trustLevel}</span>

                <div className="profile-stats">
                  <div>
                    <strong>0</strong>
                    <span>Connections</span>
                  </div>

                  <div>
                    <strong>0</strong>
                    <span>Posts</span>
                  </div>
                </div>

                <button
                  className="view-profile-button"
                  type="button"
                  onClick={() => navigate("/profile")}
                >
                  View profile
                </button>
              </div>
            </motion.section>

            <section className="calm-space-card">
              <div className="calm-card-icon">
                <ShieldCheck size={21} />
              </div>

              <div>
                <h3>Your space stays yours</h3>

                <p>
                  TrustNet does not use public popularity scores or
                  attention-driven recommendations.
                </p>
              </div>
            </section>

            <section className="connection-card">
              <div className="connection-card-header">
                <div>
                  <span>Grow thoughtfully</span>
                  <h3>Find connections</h3>
                </div>

                <Users size={21} />
              </div>

              <p>
                Connect with people you know and build a feed that feels
                relevant to you.
              </p>

              <button type="button" onClick={() => navigate("/connections")}>
                Explore people
              </button>
            </section>

            <footer className="home-footer-links">
              <span>Privacy</span>
              <span>Community</span>
              <span>About</span>
              <span>© 2026 TrustNet</span>
            </footer>
          </aside>
        </div>
      </main>

      {isComposerOpen && (
        <div
          className="composer-overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              setIsComposerOpen(false);
            }
          }}
        >
          <motion.section
            className="composer-modal"
            initial={{
              opacity: 0,
              scale: 0.96,
              y: 15,
            }}
            animate={{
              opacity: 1,
              scale: 1,
              y: 0,
            }}
            transition={{
              duration: 0.2,
            }}
          >
            <header className="composer-header">
              <div>
                <span>Create a meaningful update</span>
                <h2>Create post</h2>
              </div>

              <button
                type="button"
                className="composer-close-button"
                onClick={() => setIsComposerOpen(false)}
                disabled={isPublishing}
                aria-label="Close post composer"
              >
                <X size={21} />
              </button>
            </header>

            <form onSubmit={handleCreatePost}>
              <div className="composer-author">
                <div className="feed-avatar">{initials}</div>

                <div>
                  <strong>{displayName}</strong>
                  <span>Sharing with your connections</span>
                </div>
              </div>

              <textarea
                value={postContent}
                onChange={(event) => setPostContent(event.target.value)}
                placeholder="What would you like to share?"
                maxLength={2000}
                autoFocus
                disabled={isPublishing}
              />

              <div className="composer-character-count">
                {postContent.length}/2000
              </div>

              <footer className="composer-footer">
                <button
                  className="composer-media-button"
                  type="button"
                  disabled={isPublishing}
                >
                  <Image size={18} />
                  Add media
                </button>

                <button
                  className="composer-publish-button"
                  type="submit"
                  disabled={isPublishing || !postContent.trim()}
                >
                  <Send size={17} />

                  {isPublishing ? "Publishing..." : "Publish"}
                </button>
              </footer>
            </form>
          </motion.section>
        </div>
      )}
    </div>
  );
}

export default HomePage;
