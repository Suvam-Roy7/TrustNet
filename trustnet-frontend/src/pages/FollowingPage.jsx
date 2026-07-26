import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import {
  ArrowLeft,
  Search,
  ShieldCheck,
  UserMinus,
  Users,
  X,
} from "lucide-react";
import { toast } from "react-toastify";

import { getProfileByUserIdRequest } from "../api/profileApi";
import {
  getFollowingUsersRequest,
  unfollowUserRequest,
} from "../api/socialApi";
import tokenService from "../auth/tokenService";
import AppPageLayout from "../components/layout/AppPageLayout";

import "../styles/following.css";

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

function FollowingPage() {
  const currentUserId = tokenService.getCurrentUserId();

  const [followingUsers, setFollowingUsers] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [updatingUserId, setUpdatingUserId] = useState(null);

  const loadFollowingUsers = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await getFollowingUsersRequest();
      const rawFollowing = extractList(response);

      const profileResults = await Promise.allSettled(
        rawFollowing.map(async (item) => {
          if (typeof item === "string") {
            if (item === currentUserId) {
              return null;
            }

            const profileResponse = await getProfileByUserIdRequest(item);
            return normalizeProfile(profileResponse);
          }

          const profile = normalizeProfile(item);

          if (!profile?.userId || profile.userId === currentUserId) {
            return null;
          }

          return profile;
        }),
      );

      const loadedProfiles = profileResults
        .filter((result) => result.status === "fulfilled")
        .map((result) => result.value)
        .filter(Boolean)
        .sort((first, second) =>
          first.username.localeCompare(second.username, undefined, {
            sensitivity: "base",
          }),
        );

      setFollowingUsers(loadedProfiles);
    } catch (error) {
      console.error(
        "Unable to load following users:",
        error.response?.status,
        error.response?.data || error.message,
      );

      setErrorMessage(
        getErrorMessage(error, "Unable to load your following list."),
      );
    } finally {
      setIsLoading(false);
    }
  }, [currentUserId]);

  useEffect(() => {
    loadFollowingUsers();
  }, [loadFollowingUsers]);

  const filteredUsers = useMemo(() => {
    const cleanQuery = searchQuery.trim().toLowerCase();

    if (!cleanQuery) {
      return followingUsers;
    }

    return followingUsers.filter((user) => {
      const searchableText = [
        user.username,
        user.bio,
        user.email,
        formatTrustLevel(user.trustLevel),
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchableText.includes(cleanQuery);
    });
  }, [followingUsers, searchQuery]);

  const handleUnfollow = async (user) => {
    if (!user?.userId || updatingUserId) {
      return;
    }

    setUpdatingUserId(user.userId);

    try {
      await unfollowUserRequest(user.userId);

      setFollowingUsers((currentUsers) =>
        currentUsers.filter((item) => item.userId !== user.userId),
      );

      toast.success(`You unfollowed ${user.username}.`);
    } catch (error) {
      const message = getErrorMessage(error, "Unable to unfollow this user.");

      if (error.response?.status === 404) {
        setFollowingUsers((currentUsers) =>
          currentUsers.filter((item) => item.userId !== user.userId),
        );
      }

      toast.error(message);
    } finally {
      setUpdatingUserId(null);
    }
  };

  return (
    <AppPageLayout>
      <main className="following-page">
        <header className="following-header">
          <div className="following-header-content">
            <Link className="following-back-link" to="/connections">
              <ArrowLeft size={18} />
              <span>Back to Connections</span>
            </Link>

            <div className="following-title-row">
              <div className="following-title-icon">
                <Users size={25} />
              </div>

              <div>
                <p>Your trusted network</p>
                <h1>Following</h1>
              </div>
            </div>

            <p className="following-description">
              Browse and manage everyone whose updates you have chosen to see.
            </p>
          </div>
        </header>

        <section className="following-content">
          <motion.section
            className="following-toolbar"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <div className="following-toolbar-copy">
              <span>Manage network</span>
              <h2>People you follow</h2>
              <p>
                {followingUsers.length}{" "}
                {followingUsers.length === 1 ? "account" : "accounts"}
              </p>
            </div>

            <div className="following-search-box">
              <Search size={19} />

              <input
                type="text"
                value={searchQuery}
                placeholder="Search your following"
                aria-label="Search your following"
                onChange={(event) => setSearchQuery(event.target.value)}
              />

              {searchQuery && (
                <button
                  type="button"
                  aria-label="Clear following search"
                  onClick={() => setSearchQuery("")}
                >
                  <X size={17} />
                </button>
              )}
            </div>
          </motion.section>

          {isLoading && (
            <div className="following-status">Loading your network...</div>
          )}

          {!isLoading && errorMessage && (
            <div className="following-status following-error">
              <span>{errorMessage}</span>
              <button type="button" onClick={loadFollowingUsers}>
                Try again
              </button>
            </div>
          )}

          {!isLoading && !errorMessage && followingUsers.length === 0 && (
            <div className="following-empty-state">
              <Users size={28} />
              <div>
                <strong>You are not following anyone yet</strong>
                <p>Visit Find people to start building your network.</p>
              </div>
              <Link to="/connections">Find people</Link>
            </div>
          )}

          {!isLoading &&
            !errorMessage &&
            followingUsers.length > 0 &&
            filteredUsers.length === 0 && (
              <div className="following-empty-state following-search-empty">
                <Search size={27} />
                <div>
                  <strong>No matching accounts</strong>
                  <p>Try another username, bio, or trust level.</p>
                </div>
                <button type="button" onClick={() => setSearchQuery("")}>
                  Clear search
                </button>
              </div>
            )}

          {!isLoading && !errorMessage && filteredUsers.length > 0 && (
            <section className="following-results">
              <div className="following-results-heading">
                <span>
                  Showing {filteredUsers.length} of {followingUsers.length}
                </span>
              </div>

              <div className="following-list">
                {filteredUsers.map((user, index) => {
                  const isUpdating = updatingUserId === user.userId;

                  return (
                    <motion.article
                      className="following-user-card"
                      key={user.userId}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: Math.min(index * 0.025, 0.25) }}
                    >
                      <div className="following-user-avatar">
                        {user.profilePictureUrl ? (
                          <img
                            src={user.profilePictureUrl}
                            alt={user.username}
                          />
                        ) : (
                          getInitials(user.username)
                        )}
                      </div>

                      <div className="following-user-copy">
                        <strong>{user.username}</strong>
                        <p>
                          {user.bio ||
                            "This user has not added an introduction yet."}
                        </p>
                        <span>
                          <ShieldCheck size={14} />
                          {formatTrustLevel(user.trustLevel)}
                        </span>
                      </div>

                      <button
                        type="button"
                        className="following-unfollow-button"
                        disabled={isUpdating}
                        onClick={() => handleUnfollow(user)}
                      >
                        <UserMinus size={17} />
                        {isUpdating ? "Updating..." : "Unfollow"}
                      </button>
                    </motion.article>
                  );
                })}
              </div>
            </section>
          )}

          <aside className="following-privacy-note">
            <ShieldCheck size={22} />
            <div>
              <strong>Your network remains private</strong>
              <p>
                TrustNet helps you manage meaningful connections without public
                popularity rankings.
              </p>
            </div>
          </aside>
        </section>
      </main>
    </AppPageLayout>
  );
}

export default FollowingPage;
