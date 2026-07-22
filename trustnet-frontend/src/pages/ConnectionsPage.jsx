import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import {
  ArrowLeft,
  Check,
  Search,
  ShieldCheck,
  UserMinus,
  UserPlus,
  Users,
} from "lucide-react";
import { toast } from "react-toastify";

import {
  getProfileByUserIdRequest,
  getProfileByUsernameRequest,
} from "../api/profileApi";

import {
  acceptFollowRequestRequest,
  getFollowingUsersRequest,
  getIncomingFollowRequestsRequest,
  getRelationshipStatusRequest,
  getSuggestedUsersRequest,
  rejectFollowRequestRequest,
  sendFollowRequest,
  unfollowUserRequest,
} from "../api/socialApi";

import tokenService from "../auth/tokenService";
import AppPageLayout from "../components/layout/AppPageLayout";

import "../styles/connections.css";

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

const formatTrustLevel = (trustLevel) => {
  if (!trustLevel) {
    return "New User";
  }

  return trustLevel
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
};

const normalizeProfile = (profile) => {
  if (!profile) {
    return null;
  }

  return {
    userId: profile.userId || profile.id || profile.profileId || null,

    username: profile.username || profile.displayName || "TrustNet User",

    email: profile.email || "",
    bio: profile.bio || "",
    website: profile.website || "",

    trustLevel: profile.trustLevel || profile.trust_level || "NEW_USER",

    profilePictureUrl:
      profile.profilePictureUrl ||
      profile.profileImageUrl ||
      profile.avatarUrl ||
      null,
  };
};

const extractList = (response) => {
  if (Array.isArray(response)) {
    return response;
  }

  if (Array.isArray(response?.content)) {
    return response.content;
  }

  if (Array.isArray(response?.users)) {
    return response.users;
  }

  if (Array.isArray(response?.requests)) {
    return response.requests;
  }

  return [];
};

const getErrorMessage = (error, fallbackMessage) => {
  const responseData = error?.response?.data;

  if (typeof responseData === "string") {
    return responseData;
  }

  return (
    responseData?.message ||
    responseData?.error ||
    error?.message ||
    fallbackMessage
  );
};

const isPendingRelationship = (response) =>
  response?.requestPending === true || response?.requestStatus === "PENDING";

function ConnectionsPage() {
  const currentUserId = tokenService.getCurrentUserId();

  const [username, setUsername] = useState("");

  const [searchedProfile, setSearchedProfile] = useState(null);

  const [isSearching, setIsSearching] = useState(false);

  const [searchError, setSearchError] = useState("");

  const [isFollowing, setIsFollowing] = useState(false);

  const [isRequestPending, setIsRequestPending] = useState(false);

  const [isRelationshipLoading, setIsRelationshipLoading] = useState(false);

  const [followingUsers, setFollowingUsers] = useState([]);

  const [suggestedUsers, setSuggestedUsers] = useState([]);

  const [incomingRequests, setIncomingRequests] = useState([]);

  const [isConnectionsLoading, setIsConnectionsLoading] = useState(true);

  const [connectionsError, setConnectionsError] = useState("");

  const [updatingUserId, setUpdatingUserId] = useState(null);

  const [processingRequestId, setProcessingRequestId] = useState(null);

  const isOwnProfile =
    Boolean(searchedProfile?.userId) &&
    searchedProfile.userId === currentUserId;

  const initials = useMemo(
    () => getInitials(searchedProfile?.username),
    [searchedProfile?.username],
  );

  const markSuggestedRequestPending = (userId, requestPending) => {
    setSuggestedUsers((currentUsers) =>
      currentUsers.map((user) =>
        user.userId === userId
          ? {
              ...user,
              requestPending,
            }
          : user,
      ),
    );
  };

  const removeFromFollowingList = (profile) => {
    if (!profile?.userId) {
      return;
    }

    setFollowingUsers((currentUsers) =>
      currentUsers.filter((user) => user.userId !== profile.userId),
    );

    setSuggestedUsers((currentUsers) => {
      const alreadyPresent = currentUsers.some(
        (user) => user.userId === profile.userId,
      );

      if (alreadyPresent) {
        return currentUsers.map((user) =>
          user.userId === profile.userId
            ? {
                ...user,
                following: false,
                requestPending: false,
              }
            : user,
        );
      }

      return [
        {
          ...profile,
          following: false,
          requestPending: false,
        },
        ...currentUsers,
      ];
    });
  };

  const loadConnections = useCallback(async () => {
    setIsConnectionsLoading(true);
    setConnectionsError("");

    try {
      const [followingResult, incomingResult, suggestedResult] =
        await Promise.allSettled([
          getFollowingUsersRequest(),
          getIncomingFollowRequestsRequest(),
          getSuggestedUsersRequest(6),
        ]);

      const followingResponse =
        followingResult.status === "fulfilled" ? followingResult.value : [];

      const incomingResponse =
        incomingResult.status === "fulfilled" ? incomingResult.value : [];

      const suggestedResponse =
        suggestedResult.status === "fulfilled" ? suggestedResult.value : [];

      if (followingResult.status === "rejected") {
        console.error(
          "Unable to load following users:",
          followingResult.reason,
        );
      }

      if (incomingResult.status === "rejected") {
        console.error(
          "Unable to load incoming requests:",
          incomingResult.reason,
        );
      }

      if (suggestedResult.status === "rejected") {
        console.warn(
          "Suggestions endpoint is currently unavailable:",
          suggestedResult.reason,
        );
      }

      const followingUserIds = extractList(followingResponse).filter(
        (userId) => typeof userId === "string" && userId !== currentUserId,
      );

      const loadedFollowing = (
        await Promise.all(
          followingUserIds.map(async (followedUserId) => {
            try {
              const profileResponse =
                await getProfileByUserIdRequest(followedUserId);

              const profile = normalizeProfile(profileResponse);

              if (!profile?.userId) {
                return null;
              }

              return {
                ...profile,
                following: true,
                requestPending: false,
              };
            } catch (profileError) {
              console.error(
                "Unable to load followed user profile:",
                followedUserId,
                profileError.response?.status,
                profileError.response?.data || profileError.message,
              );

              return null;
            }
          }),
        )
      ).filter(Boolean);

      const followedUserIds = new Set(
        loadedFollowing.map((profile) => profile.userId),
      );

      const suggestedProfiles = extractList(suggestedResponse)
        .map(normalizeProfile)
        .filter(
          (profile) =>
            profile?.userId &&
            profile.userId !== currentUserId &&
            !followedUserIds.has(profile.userId),
        );

      /*
       * Load each suggested user's current
       * relationship status so an existing
       * pending request shows Requested.
       */
      const enrichedSuggestedUsers = await Promise.all(
        suggestedProfiles.map(async (profile) => {
          try {
            const statusResponse = await getRelationshipStatusRequest(
              profile.userId,
            );

            if (statusResponse?.following === true) {
              return null;
            }

            return {
              ...profile,
              following: false,
              requestPending: isPendingRelationship(statusResponse),
            };
          } catch (statusError) {
            console.error(
              "Unable to load suggestion relationship status:",
              profile.userId,
              statusError.response?.status,
            );

            return {
              ...profile,
              following: false,
              requestPending: false,
            };
          }
        }),
      );

      const rawIncomingRequests = extractList(incomingResponse);

      /*
       * SocialGraph stores UUIDs. ProfileService
       * provides username, bio and avatar.
       */
      const enrichedIncomingRequests = await Promise.all(
        rawIncomingRequests.map(async (request) => {
          try {
            const profileResponse = await getProfileByUserIdRequest(
              request.requesterId,
            );

            return {
              ...request,
              requesterProfile: normalizeProfile(profileResponse),
            };
          } catch (profileError) {
            console.error(
              "Unable to load follow requester profile:",
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

      setFollowingUsers(loadedFollowing);

      setSuggestedUsers(enrichedSuggestedUsers.filter(Boolean));

      setIncomingRequests(enrichedIncomingRequests);
    } catch (error) {
      console.error(
        "Unable to load connections:",
        error.response?.status,
        error.response?.data || error.message,
      );

      setConnectionsError(
        getErrorMessage(error, "Unable to load your connections."),
      );
    } finally {
      setIsConnectionsLoading(false);
    }
  }, [currentUserId]);

  useEffect(() => {
    loadConnections();
  }, [loadConnections]);

  const handleSearch = async (event) => {
    event.preventDefault();

    const cleanUsername = username.trim();

    if (!cleanUsername) {
      setSearchError("Enter a username to search.");

      return;
    }

    setIsSearching(true);
    setSearchError("");
    setSearchedProfile(null);
    setIsFollowing(false);
    setIsRequestPending(false);

    try {
      const profileResponse = await getProfileByUsernameRequest(cleanUsername);

      const profile = normalizeProfile(profileResponse);

      if (!profile?.userId) {
        throw new Error("The profile response does not contain a user ID.");
      }

      let followingStatus = false;
      let pendingStatus = false;

      if (profile.userId !== currentUserId) {
        const relationshipResponse = await getRelationshipStatusRequest(
          profile.userId,
        );

        followingStatus = relationshipResponse?.following === true;

        pendingStatus = isPendingRelationship(relationshipResponse);
      }

      setSearchedProfile(profile);
      setIsFollowing(followingStatus);
      setIsRequestPending(pendingStatus);
    } catch (error) {
      console.error(
        "Profile search failed:",
        error.response?.status,
        error.response?.data || error.message,
      );

      if (error.response?.status === 404) {
        setSearchError(
          `No TrustNet user was found with the username "${cleanUsername}".`,
        );
      } else {
        setSearchError(
          getErrorMessage(error, "Unable to search for this user."),
        );
      }
    } finally {
      setIsSearching(false);
    }
  };

  const handleFollow = async () => {
    if (
      !searchedProfile?.userId ||
      isRelationshipLoading ||
      isOwnProfile ||
      isRequestPending
    ) {
      return;
    }

    setIsRelationshipLoading(true);

    try {
      await sendFollowRequest(searchedProfile.userId);

      setIsRequestPending(true);

      markSuggestedRequestPending(searchedProfile.userId, true);

      toast.success(`Follow request sent to ${searchedProfile.username}.`);
    } catch (error) {
      const errorMessage = getErrorMessage(
        error,
        "Unable to send follow request.",
      );

      const isExistingRequest =
        error.response?.status === 409 ||
        errorMessage.toLowerCase().includes("pending") ||
        errorMessage.toLowerCase().includes("already");

      if (isExistingRequest) {
        setIsRequestPending(true);

        markSuggestedRequestPending(searchedProfile.userId, true);

        toast.info(
          `Your request to ${searchedProfile.username} is already pending.`,
        );
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setIsRelationshipLoading(false);
    }
  };

  const handleUnfollow = async () => {
    if (!searchedProfile?.userId || isRelationshipLoading) {
      return;
    }

    setIsRelationshipLoading(true);

    try {
      await unfollowUserRequest(searchedProfile.userId);

      setIsFollowing(false);
      setIsRequestPending(false);

      removeFromFollowingList(searchedProfile);

      toast.success(`You unfollowed ${searchedProfile.username}.`);
    } catch (error) {
      const errorMessage = getErrorMessage(
        error,
        "Unable to unfollow this user.",
      );

      if (error.response?.status === 404) {
        setIsFollowing(false);

        removeFromFollowingList(searchedProfile);
      }

      toast.error(errorMessage);
    } finally {
      setIsRelationshipLoading(false);
    }
  };

  const handleCompactRelationship = async (user, currentlyFollowing) => {
    if (!user?.userId || updatingUserId) {
      return;
    }

    if (!currentlyFollowing && user.requestPending) {
      return;
    }

    setUpdatingUserId(user.userId);

    try {
      if (currentlyFollowing) {
        await unfollowUserRequest(user.userId);

        removeFromFollowingList(user);

        if (searchedProfile?.userId === user.userId) {
          setIsFollowing(false);
          setIsRequestPending(false);
        }

        toast.success(`You unfollowed ${user.username}.`);
      } else {
        await sendFollowRequest(user.userId);

        markSuggestedRequestPending(user.userId, true);

        if (searchedProfile?.userId === user.userId) {
          setIsRequestPending(true);
        }

        toast.success(`Follow request sent to ${user.username}.`);
      }
    } catch (error) {
      const errorMessage = getErrorMessage(
        error,
        "Unable to update this relationship.",
      );

      const isExistingRequest =
        error.response?.status === 409 ||
        errorMessage.toLowerCase().includes("pending") ||
        errorMessage.toLowerCase().includes("already");

      if (!currentlyFollowing && isExistingRequest) {
        markSuggestedRequestPending(user.userId, true);

        if (searchedProfile?.userId === user.userId) {
          setIsRequestPending(true);
        }

        toast.info(`Your request to ${user.username} is already pending.`);
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setUpdatingUserId(null);
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
          statusError.response?.status,
          statusError.response?.data || statusError.message,
        );
      }

      const alreadyFollowingRequester = relationshipStatus?.following === true;

      const reverseRequestPending = isPendingRelationship(relationshipStatus);

      /*
       * Example:
       * Aman already follows Suvam.
       * Aman accepts Suvam's reverse request.
       * Aman must not see Follow Back again.
       */
      if (alreadyFollowingRequester) {
        setIncomingRequests((currentRequests) =>
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

      /*
       * Keep the same card in Incoming requests,
       * but change its buttons to Follow Back.
       */
      setIncomingRequests((currentRequests) =>
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
      toast.error(getErrorMessage(error, "Unable to accept this request."));
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

      setIncomingRequests((currentRequests) =>
        currentRequests.filter((item) => item.requestId !== request.requestId),
      );

      toast.info("Follow request declined.");
    } catch (error) {
      toast.error(getErrorMessage(error, "Unable to decline this request."));
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

      /*
       * Prevent a duplicate follow relationship.
       */
      if (relationshipStatus?.following === true) {
        setIncomingRequests((currentRequests) =>
          currentRequests.filter(
            (item) => item.requestId !== request.requestId,
          ),
        );

        toast.info(
          `You already follow ${
            request.requesterProfile?.username || "this user"
          }.`,
        );

        return;
      }

      /*
       * Prevent a duplicate reverse request.
       */
      if (isPendingRelationship(relationshipStatus)) {
        setIncomingRequests((currentRequests) =>
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

      setIncomingRequests((currentRequests) =>
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
      const errorMessage = getErrorMessage(
        error,
        "Unable to send the follow-back request.",
      );

      const requestAlreadyExists =
        error.response?.status === 409 ||
        errorMessage.toLowerCase().includes("pending") ||
        errorMessage.toLowerCase().includes("already");

      if (requestAlreadyExists) {
        setIncomingRequests((currentRequests) =>
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

  const renderCompactUserCard = (user, relationshipType) => {
    const currentlyFollowing = relationshipType === "FOLLOWING";

    const requestPending = user.requestPending === true;

    const isUpdating = updatingUserId === user.userId;

    return (
      <article className="connection-list-user-card" key={user.userId}>
        <div className="connection-list-avatar">
          {user.profilePictureUrl ? (
            <img src={user.profilePictureUrl} alt={user.username} />
          ) : (
            getInitials(user.username)
          )}
        </div>

        <div className="connection-list-user-info">
          <strong>{user.username}</strong>

          <p>{user.bio || "This user has not added an introduction yet."}</p>

          <span>{formatTrustLevel(user.trustLevel)}</span>
        </div>

        {currentlyFollowing ? (
          <button
            type="button"
            className="connection-list-unfollow"
            disabled={isUpdating}
            onClick={() => handleCompactRelationship(user, true)}
          >
            <UserMinus size={17} />

            {isUpdating ? "Updating..." : "Unfollow"}
          </button>
        ) : (
          <button
            type="button"
            className={`connection-list-follow ${
              requestPending ? "requested-button" : ""
            }`}
            disabled={isUpdating || requestPending}
            onClick={() => handleCompactRelationship(user, false)}
          >
            {requestPending ? <Check size={17} /> : <UserPlus size={17} />}

            {isUpdating
              ? "Sending..."
              : requestPending
                ? "Requested"
                : "Follow"}
          </button>
        )}
      </article>
    );
  };

  return (
    <AppPageLayout>
      <main className="connections-page">
        <header className="connections-header">
          <div className="connections-header-content">
            <Link className="connections-back-link" to="/home">
              <ArrowLeft size={18} />
              <span>Back to Home</span>
            </Link>

            <div className="connections-heading">
              <div className="connections-heading-icon">
                <Users size={24} />
              </div>

              <div>
                <p>Build genuine connections</p>

                <h1>Find people</h1>
              </div>
            </div>

            <p className="connections-description">
              Search for people you know and choose meaningful accounts to
              follow.
            </p>
          </div>
        </header>

        <section className="connections-content">
          <motion.div
            className="connection-search-card"
            initial={{
              opacity: 0,
              y: 14,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
          >
            <div className="connection-search-heading">
              <h2>Search TrustNet</h2>

              <p>Enter an exact username to find a profile.</p>
            </div>

            <form className="connection-search-form" onSubmit={handleSearch}>
              <div className="connection-search-input">
                <Search size={19} />

                <input
                  type="text"
                  value={username}
                  placeholder="Search by username"
                  autoComplete="off"
                  onChange={(event) => {
                    setUsername(event.target.value);

                    setSearchError("");
                  }}
                />
              </div>

              <button type="submit" disabled={isSearching}>
                {isSearching ? "Searching..." : "Search"}
              </button>
            </form>

            {searchError && (
              <div className="connection-search-error">{searchError}</div>
            )}
          </motion.div>

          {searchedProfile && (
            <motion.article
              className="connection-profile-card"
              initial={{
                opacity: 0,
                scale: 0.98,
              }}
              animate={{
                opacity: 1,
                scale: 1,
              }}
            >
              <div className="connection-cover" />

              <div className="connection-profile-body">
                <div className="connection-profile-top">
                  <div className="connection-avatar">
                    {searchedProfile.profilePictureUrl ? (
                      <img
                        src={searchedProfile.profilePictureUrl}
                        alt={searchedProfile.username}
                      />
                    ) : (
                      <span>{initials}</span>
                    )}
                  </div>

                  {!isOwnProfile && (
                    <div className="connection-action-wrapper">
                      {isFollowing ? (
                        <button
                          className="connection-button unfollow-button"
                          type="button"
                          disabled={isRelationshipLoading}
                          onClick={handleUnfollow}
                        >
                          <UserMinus size={18} />

                          <span>
                            {isRelationshipLoading ? "Updating..." : "Unfollow"}
                          </span>
                        </button>
                      ) : (
                        <button
                          className={`connection-button follow-button ${
                            isRequestPending ? "requested-button" : ""
                          }`}
                          type="button"
                          disabled={isRelationshipLoading || isRequestPending}
                          onClick={handleFollow}
                        >
                          {isRequestPending ? (
                            <Check size={18} />
                          ) : (
                            <UserPlus size={18} />
                          )}

                          <span>
                            {isRelationshipLoading
                              ? "Sending..."
                              : isRequestPending
                                ? "Requested"
                                : "Follow"}
                          </span>
                        </button>
                      )}
                    </div>
                  )}
                </div>

                <div className="connection-profile-details">
                  <div className="connection-name-row">
                    <h2>{searchedProfile.username}</h2>

                    {isFollowing && (
                      <span className="following-status">
                        <Check size={13} />
                        Following
                      </span>
                    )}

                    {!isFollowing && isRequestPending && (
                      <span className="following-status">
                        <Check size={13} />
                        Requested
                      </span>
                    )}
                  </div>

                  {searchedProfile.email && (
                    <p className="connection-email">{searchedProfile.email}</p>
                  )}

                  <div className="connection-profile-bio">
                    <span>About</span>

                    <p>
                      {searchedProfile.bio ||
                        "This user has not added an introduction yet."}
                    </p>
                  </div>

                  <div className="connection-trust-level">
                    <ShieldCheck size={15} />

                    <span>{formatTrustLevel(searchedProfile.trustLevel)}</span>
                  </div>

                  {isOwnProfile && (
                    <div className="own-profile-message">
                      This is your profile. You cannot follow yourself.
                    </div>
                  )}
                </div>
              </div>
            </motion.article>
          )}

          {isConnectionsLoading && (
            <div className="connections-list-status">
              Loading your connections...
            </div>
          )}

          {!isConnectionsLoading && connectionsError && (
            <div className="connections-list-status connections-list-error">
              <span>{connectionsError}</span>

              <button type="button" onClick={loadConnections}>
                Try again
              </button>
            </div>
          )}

          {!isConnectionsLoading && !connectionsError && (
            <>
              <section className="connections-list-section">
                <div className="connections-list-heading">
                  <div>
                    <p>Waiting for you</p>
                    <h2>Incoming requests</h2>
                  </div>

                  <span>{incomingRequests.length}</span>
                </div>

                {incomingRequests.length === 0 ? (
                  <div className="connections-list-empty">
                    You have no pending follow requests.
                  </div>
                ) : (
                  <div className="connections-list-grid">
                    {incomingRequests.map((request) => {
                      const requester = request.requesterProfile;

                      const isProcessing =
                        processingRequestId === request.requestId;

                      const isAccepted = request.uiStatus === "ACCEPTED";

                      const followBackPending =
                        request.followBackPending === true;

                      return (
                        <article
                          className={`connection-list-user-card ${
                            isAccepted ? "accepted-request-card" : ""
                          }`}
                          key={request.requestId}
                        >
                          <div className="connection-list-avatar">
                            {requester?.profilePictureUrl ? (
                              <img
                                src={requester.profilePictureUrl}
                                alt={requester.username || "TrustNet user"}
                              />
                            ) : (
                              getInitials(requester?.username)
                            )}
                          </div>

                          <div className="connection-list-user-info">
                            <strong>
                              {requester?.username || "TrustNet User"}
                            </strong>

                            <p>
                              {isAccepted
                                ? "You accepted this follow request."
                                : requester?.bio ||
                                  "Sent you a follow request."}
                            </p>

                            <span>
                              {isAccepted
                                ? followBackPending
                                  ? "Follow-back request pending"
                                  : "Follow back to connect both ways"
                                : "Wants to follow you"}
                            </span>
                          </div>

                          {isAccepted ? (
                            <button
                              type="button"
                              className={`follow-request-follow-back ${
                                followBackPending ? "requested-button" : ""
                              }`}
                              disabled={isProcessing || followBackPending}
                              onClick={() => handleFollowBack(request)}
                            >
                              {followBackPending ? (
                                <>
                                  <Check size={17} />
                                  Requested
                                </>
                              ) : (
                                <>
                                  <UserPlus size={17} />

                                  {isProcessing ? "Sending..." : "Follow Back"}
                                </>
                              )}
                            </button>
                          ) : (
                            <div className="follow-request-actions">
                              <button
                                type="button"
                                className="follow-request-reject"
                                disabled={isProcessing}
                                onClick={() => handleRejectRequest(request)}
                              >
                                {isProcessing ? "Updating..." : "Decline"}
                              </button>

                              <button
                                type="button"
                                className="follow-request-accept"
                                disabled={isProcessing}
                                onClick={() => handleAcceptRequest(request)}
                              >
                                {isProcessing ? "Updating..." : "Accept"}
                              </button>
                            </div>
                          )}
                        </article>
                      );
                    })}
                  </div>
                )}
              </section>

              <section className="connections-list-section">
                <div className="connections-list-heading">
                  <div>
                    <p>Your network</p>
                    <h2>Following</h2>
                  </div>

                  <span>{followingUsers.length}</span>
                </div>

                {followingUsers.length === 0 ? (
                  <div className="connections-list-empty">
                    You are not following anyone yet.
                  </div>
                ) : (
                  <div className="connections-list-grid">
                    {followingUsers.map((user) =>
                      renderCompactUserCard(user, "FOLLOWING"),
                    )}
                  </div>
                )}
              </section>

              <section className="connections-list-section">
                <div className="connections-list-heading">
                  <div>
                    <p>Discover thoughtfully</p>

                    <h2>Suggested for you</h2>
                  </div>
                </div>

                {suggestedUsers.length === 0 ? (
                  <div className="connections-list-empty">
                    No new suggestions are available.
                  </div>
                ) : (
                  <div className="connections-list-grid">
                    {suggestedUsers.map((user) =>
                      renderCompactUserCard(user, "SUGGESTED"),
                    )}
                  </div>
                )}
              </section>
            </>
          )}

          <aside className="connections-privacy-note">
            <ShieldCheck size={22} />

            <div>
              <strong>Connection quality over quantity</strong>

              <p>
                TrustNet does not promote follower counts or public popularity
                rankings.
              </p>
            </div>
          </aside>
        </section>
      </main>
    </AppPageLayout>
  );
}

export default ConnectionsPage;
