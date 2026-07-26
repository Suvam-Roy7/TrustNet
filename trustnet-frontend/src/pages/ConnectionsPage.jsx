import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import {
  ArrowLeft,
  ArrowRight,
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
  getFollowingUsersRequest,
  getRelationshipStatusRequest,
  getSuggestedUsersRequest,
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

  return name
    .replace(/([a-z])([A-Z])/g, "$1 $2")
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
    email: profileData.email || "",
    bio: profileData.bio || "",
    website: profileData.website || "",
    trustLevel: profileData.trustLevel || profileData.trust_level || "NEW_USER",
    profilePictureUrl:
      profileData.profilePictureUrl ||
      profileData.profileImageUrl ||
      profileData.avatarUrl ||
      null,
  };
};

const extractList = (response) => {
  const payload = response?.data ?? response;

  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  if (Array.isArray(payload?.users)) {
    return payload.users;
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

const FOLLOWING_PREVIEW_LIMIT = 15;

const isPendingRelationship = (response) => {
  const payload = response?.data ?? response;

  return (
    payload?.requestPending === true || payload?.requestStatus === "PENDING"
  );
};

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
  const [isConnectionsLoading, setIsConnectionsLoading] = useState(true);
  const [connectionsError, setConnectionsError] = useState("");
  const [updatingUserId, setUpdatingUserId] = useState(null);

  const isOwnProfile =
    Boolean(searchedProfile?.userId) &&
    searchedProfile.userId === currentUserId;

  const searchedInitials = useMemo(
    () => getInitials(searchedProfile?.username),
    [searchedProfile?.username],
  );

  const followingPreview = useMemo(
    () => followingUsers.slice(0, FOLLOWING_PREVIEW_LIMIT),
    [followingUsers],
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
      const [followingResult, suggestedResult] = await Promise.allSettled([
        getFollowingUsersRequest(),
        getSuggestedUsersRequest(8),
      ]);

      if (followingResult.status === "rejected") {
        console.error(
          "Unable to load following users:",
          followingResult.reason,
        );
      }

      if (suggestedResult.status === "rejected") {
        console.warn(
          "Suggestions endpoint is currently unavailable:",
          suggestedResult.reason,
        );
      }

      const followingResponse =
        followingResult.status === "fulfilled" ? followingResult.value : [];
      const suggestedResponse =
        suggestedResult.status === "fulfilled" ? suggestedResult.value : [];

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

      const enrichedSuggestedUsers = await Promise.all(
        suggestedProfiles.map(async (profile) => {
          try {
            const statusResponse = await getRelationshipStatusRequest(
              profile.userId,
            );
            const payload = statusResponse?.data ?? statusResponse;

            if (payload?.following === true) {
              return null;
            }

            return {
              ...profile,
              following: false,
              requestPending: isPendingRelationship(payload),
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

      setFollowingUsers(loadedFollowing);
      setSuggestedUsers(enrichedSuggestedUsers.filter(Boolean));

      if (
        followingResult.status === "rejected" &&
        suggestedResult.status === "rejected"
      ) {
        setConnectionsError("Unable to load your connections right now.");
      }
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
        const payload = relationshipResponse?.data ?? relationshipResponse;

        followingStatus = payload?.following === true;
        pendingStatus = isPendingRelationship(payload);
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

  const handleCardRelationship = async (user, currentlyFollowing) => {
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

  const renderPersonCard = (user, relationshipType) => {
    const currentlyFollowing = relationshipType === "FOLLOWING";
    const requestPending = user.requestPending === true;
    const isUpdating = updatingUserId === user.userId;

    return (
      <motion.article
        className="social-person-card"
        key={user.userId}
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="social-person-avatar">
          {user.profilePictureUrl ? (
            <img src={user.profilePictureUrl} alt={user.username} />
          ) : (
            getInitials(user.username)
          )}
        </div>

        <div className="social-person-copy">
          <strong>{user.username}</strong>
          <p>{user.bio || "This user has not added an introduction yet."}</p>
          <span>{formatTrustLevel(user.trustLevel)}</span>
        </div>

        <button
          type="button"
          className={`social-person-action ${
            currentlyFollowing ? "is-unfollow" : "is-follow"
          } ${requestPending ? "is-requested" : ""}`}
          disabled={isUpdating || requestPending}
          onClick={() => handleCardRelationship(user, currentlyFollowing)}
        >
          {currentlyFollowing ? (
            <UserMinus size={16} />
          ) : requestPending ? (
            <Check size={16} />
          ) : (
            <UserPlus size={16} />
          )}

          {isUpdating
            ? "Updating..."
            : currentlyFollowing
              ? "Unfollow"
              : requestPending
                ? "Requested"
                : "Follow"}
        </button>
      </motion.article>
    );
  };

  return (
    <AppPageLayout>
      <main className="connections-page connections-page-redesign">
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
              Search people you know, manage the accounts you follow, and
              discover thoughtful new connections.
            </p>
          </div>
        </header>

        <section className="connections-content connections-social-content">
          <motion.section
            className="connections-search-panel"
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <div className="connections-search-copy">
              <span>People search</span>
              <h2>Search TrustNet</h2>
              <p>Enter an exact username to find someone.</p>
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

            {searchedProfile && (
              <motion.article
                className="connection-search-result"
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
              >
                <div className="connection-search-result-avatar">
                  {searchedProfile.profilePictureUrl ? (
                    <img
                      src={searchedProfile.profilePictureUrl}
                      alt={searchedProfile.username}
                    />
                  ) : (
                    searchedInitials
                  )}
                </div>

                <div className="connection-search-result-copy">
                  <div className="connection-search-result-name">
                    <strong>{searchedProfile.username}</strong>

                    {isFollowing && (
                      <span className="following-status">
                        <Check size={12} /> Following
                      </span>
                    )}
                  </div>

                  <p>
                    {searchedProfile.bio ||
                      "This user has not added an introduction yet."}
                  </p>

                  <span className="connection-result-trust">
                    <ShieldCheck size={14} />
                    {formatTrustLevel(searchedProfile.trustLevel)}
                  </span>
                </div>

                {!isOwnProfile && (
                  <button
                    type="button"
                    className={`connection-result-action ${
                      isFollowing ? "is-unfollow" : "is-follow"
                    } ${isRequestPending ? "is-requested" : ""}`}
                    disabled={isRelationshipLoading || isRequestPending}
                    onClick={isFollowing ? handleUnfollow : handleFollow}
                  >
                    {isFollowing ? (
                      <UserMinus size={17} />
                    ) : isRequestPending ? (
                      <Check size={17} />
                    ) : (
                      <UserPlus size={17} />
                    )}

                    {isRelationshipLoading
                      ? "Updating..."
                      : isFollowing
                        ? "Unfollow"
                        : isRequestPending
                          ? "Requested"
                          : "Follow"}
                  </button>
                )}

                {isOwnProfile && (
                  <span className="connection-own-profile-label">
                    This is you
                  </span>
                )}
              </motion.article>
            )}
          </motion.section>

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
            <div className="connections-social-sections">
              <section className="connections-social-section">
                <div className="connections-social-heading">
                  <div>
                    <p>Your network</p>
                    <h2>Following</h2>
                    {followingUsers.length > FOLLOWING_PREVIEW_LIMIT && (
                      <small>
                        Showing {FOLLOWING_PREVIEW_LIMIT} of{" "}
                        {followingUsers.length}
                      </small>
                    )}
                  </div>

                  <div className="connections-heading-actions">
                    <span className="connections-count-badge">
                      {followingUsers.length}
                    </span>

                    {followingUsers.length > 0 && (
                      <Link className="connections-see-all" to="/following">
                        <span>See all</span>
                        <ArrowRight size={16} />
                      </Link>
                    )}
                  </div>
                </div>

                {followingUsers.length === 0 ? (
                  <div className="connections-social-empty">
                    <Users size={23} />
                    <div>
                      <strong>Your following list is empty</strong>
                      <p>Search for someone you know to start your network.</p>
                    </div>
                  </div>
                ) : (
                  <div className="connections-people-grid">
                    {followingPreview.map((user) =>
                      renderPersonCard(user, "FOLLOWING"),
                    )}
                  </div>
                )}
              </section>

              <section className="connections-social-section">
                <div className="connections-social-heading">
                  <div>
                    <p>Discover thoughtfully</p>
                    <h2>Suggested for you</h2>
                  </div>
                  <span>{suggestedUsers.length}</span>
                </div>

                {suggestedUsers.length === 0 ? (
                  <div className="connections-social-empty">
                    <UserPlus size={23} />
                    <div>
                      <strong>No new suggestions right now</strong>
                      <p>Try searching for someone by their exact username.</p>
                    </div>
                  </div>
                ) : (
                  <div className="connections-people-grid">
                    {suggestedUsers.map((user) =>
                      renderPersonCard(user, "SUGGESTED"),
                    )}
                  </div>
                )}
              </section>
            </div>
          )}

          <aside className="connections-privacy-note">
            <ShieldCheck size={22} />
            <div>
              <strong>Connection quality over quantity</strong>
              <p>
                TrustNet keeps discovery intentional and avoids public
                popularity rankings.
              </p>
            </div>
          </aside>
        </section>
      </main>
    </AppPageLayout>
  );
}

export default ConnectionsPage;
