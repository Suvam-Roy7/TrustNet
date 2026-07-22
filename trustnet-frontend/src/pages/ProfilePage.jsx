import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import {
  AlignLeft,
  ArrowLeft,
  AtSign,
  CalendarDays,
  Heart,
  MessageCircle,
  MoreHorizontal,
  Save,
  ShieldCheck,
  UserRound,
  X,
} from "lucide-react";
import { toast } from "react-toastify";

import {
  getProfileByUserIdRequest,
  updateProfileRequest,
} from "../api/profileApi";

import { getUserPostsRequest } from "../api/postApi";

import { getSocialSummaryRequest } from "../api/socialApi";

import tokenService from "../auth/tokenService";
import AppPageLayout from "../components/layout/AppPageLayout";

import "../styles/profile.css";

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

const formatPostDate = (createdAt) => {
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
  }).format(date);
};

function ProfilePage() {
  const userId = tokenService.getCurrentUserId();

  const [profile, setProfile] = useState(null);

  const [posts, setPosts] = useState([]);

  const [isLoading, setIsLoading] = useState(true);

  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const [errorMessage, setErrorMessage] = useState("");

  const [postMeta, setPostMeta] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    hasMore: false,
  });

  const [socialSummary, setSocialSummary] = useState({
    followingCount: 0,
    followerCount: 0,
  });

  const [isEditProfileOpen, setIsEditProfileOpen] = useState(false);

  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  const [editProfileError, setEditProfileError] = useState("");

  const [editProfileForm, setEditProfileForm] = useState({
    username: "",
    bio: "",
  });

  const displayName =
    profile?.username || profile?.displayName || "TrustNet User";

  const initials = useMemo(() => getInitials(displayName), [displayName]);

  const loadProfilePage = useCallback(async () => {
    if (!userId) {
      setErrorMessage("Unable to identify the logged-in user.");

      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage("");

    try {
      const [profileResponse, postsResponse] = await Promise.all([
        getProfileByUserIdRequest(userId),

        getUserPostsRequest(userId, 0, 10),
      ]);

      setProfile(profileResponse);

      const loadedPosts = Array.isArray(postsResponse)
        ? postsResponse
        : postsResponse?.content || [];

      setPosts(loadedPosts);

      setPostMeta({
        page: postsResponse?.number ?? 0,

        size: postsResponse?.size ?? 10,

        totalElements: postsResponse?.totalElements ?? loadedPosts.length,

        hasMore:
          postsResponse?.last === false || postsResponse?.hasMore === true,
      });

      /*
       * Social summary is optional.
       * Failure must not break profile/posts.
       */
      try {
        const summaryResponse = await getSocialSummaryRequest();

        setSocialSummary({
          followingCount: summaryResponse?.followingCount ?? 0,

          followerCount: summaryResponse?.followerCount ?? 0,
        });
      } catch (summaryError) {
        console.error(
          "Unable to load social summary:",
          summaryError.response?.status,
          summaryError.response?.data || summaryError.message,
        );

        setSocialSummary({
          followingCount: 0,
          followerCount: 0,
        });
      }
    } catch (error) {
      console.error(
        "Unable to load profile page:",
        error.response?.status,
        error.response?.data || error.message,
      );

      setErrorMessage(
        error.response?.data?.message || "Unable to load your profile.",
      );
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    loadProfilePage();
  }, [loadProfilePage]);

  useEffect(() => {
    if (!isEditProfileOpen) {
      return undefined;
    }

    const previousOverflow = document.body.style.overflow;

    document.body.style.overflow = "hidden";

    const handleEscape = (event) => {
      if (event.key === "Escape" && !isUpdatingProfile) {
        setIsEditProfileOpen(false);
        setEditProfileError("");
      }
    };

    window.addEventListener("keydown", handleEscape);

    return () => {
      document.body.style.overflow = previousOverflow;

      window.removeEventListener("keydown", handleEscape);
    };
  }, [isEditProfileOpen, isUpdatingProfile]);

  const openEditProfileModal = () => {
    setEditProfileForm({
      username: profile?.username || profile?.displayName || "",

      bio: profile?.bio || "",
    });

    setEditProfileError("");
    setIsEditProfileOpen(true);
  };

  const closeEditProfileModal = () => {
    if (isUpdatingProfile) {
      return;
    }

    setIsEditProfileOpen(false);
    setEditProfileError("");
  };

  const handleEditProfileChange = (event) => {
    const { name, value } = event.target;

    setEditProfileForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }));

    setEditProfileError("");
  };

  const handleUpdateProfile = async (event) => {
    event.preventDefault();

    const cleanUsername = editProfileForm.username.trim();

    const cleanBio = editProfileForm.bio.trim();

    if (!cleanUsername) {
      setEditProfileError("Username cannot be empty.");

      return;
    }

    if (cleanUsername.length < 3) {
      setEditProfileError("Username must contain at least 3 characters.");

      return;
    }

    if (cleanUsername.length > 30) {
      setEditProfileError("Username cannot exceed 30 characters.");

      return;
    }

    if (!/^[a-zA-Z0-9._]+$/.test(cleanUsername)) {
      setEditProfileError(
        "Username may contain letters, numbers, dots and underscores only.",
      );

      return;
    }

    if (cleanBio.length > 250) {
      setEditProfileError("Bio cannot exceed 250 characters.");

      return;
    }

    if (!userId) {
      setEditProfileError("Unable to identify the logged-in user.");

      return;
    }

    setIsUpdatingProfile(true);
    setEditProfileError("");

    try {
      const updatedProfile = await updateProfileRequest(userId, {
        username: cleanUsername,
        bio: cleanBio,
      });

      setProfile((currentProfile) => ({
        ...currentProfile,
        ...updatedProfile,

        username: updatedProfile?.username ?? cleanUsername,

        bio: updatedProfile?.bio ?? cleanBio,
      }));

      setIsEditProfileOpen(false);

      toast.success("Your profile has been updated.");
    } catch (error) {
      console.error(
        "Unable to update profile:",
        error.response?.status,
        error.response?.data || error.message,
      );

      setEditProfileError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          "Unable to update your profile.",
      );
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handleLoadMore = async () => {
    if (!postMeta.hasMore || !userId || isLoadingMore) {
      return;
    }

    setIsLoadingMore(true);

    try {
      const nextPage = postMeta.page + 1;

      const response = await getUserPostsRequest(
        userId,
        nextPage,
        postMeta.size,
      );

      const nextPosts = Array.isArray(response)
        ? response
        : response?.content || [];

      setPosts((currentPosts) => {
        const uniquePosts = new Map();

        [...currentPosts, ...nextPosts].forEach((post, index) => {
          const key = post.postId || post.id || `${post.createdAt}-${index}`;

          uniquePosts.set(key, post);
        });

        return Array.from(uniquePosts.values());
      });

      setPostMeta((currentMeta) => ({
        page: response?.number ?? nextPage,

        size: response?.size ?? currentMeta.size,

        totalElements: response?.totalElements ?? currentMeta.totalElements,

        hasMore: response?.last === false || response?.hasMore === true,
      }));
    } catch (error) {
      console.error(
        "Unable to load more posts:",
        error.response?.data || error.message,
      );

      toast.error("Unable to load more posts.");
    } finally {
      setIsLoadingMore(false);
    }
  };

  if (isLoading) {
    return (
      <AppPageLayout>
        <main className="profile-page">
          <div className="profile-page-status">
            Loading your private space...
          </div>
        </main>
      </AppPageLayout>
    );
  }

  if (errorMessage) {
    return (
      <AppPageLayout>
        <main className="profile-page">
          <div className="profile-page-status profile-page-error">
            <span>{errorMessage}</span>

            <button type="button" onClick={loadProfilePage}>
              Try again
            </button>
          </div>
        </main>
      </AppPageLayout>
    );
  }

  return (
    <AppPageLayout>
      <main className="profile-page">
        <header className="profile-page-header">
          <div className="profile-header-content">
            <Link className="profile-back-link" to="/">
              <ArrowLeft size={18} />
              <span>Back to Home</span>
            </Link>

            <div className="profile-title-row">
              <div className="profile-title-icon">
                <UserRound size={24} />
              </div>

              <div>
                <p>Your private identity</p>
                <h1>Profile</h1>
              </div>
            </div>
          </div>
        </header>

        <section className="profile-page-content">
          <motion.article
            className="profile-main-card"
            initial={{
              opacity: 0,
              y: 14,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
          >
            <div className="profile-cover" />

            <div className="profile-card-body">
              <div className="profile-identity-row">
                <div className="profile-identity-left">
                  <div className="profile-large-avatar">
                    {profile?.profilePictureUrl ? (
                      <img src={profile.profilePictureUrl} alt={displayName} />
                    ) : (
                      <span>{initials}</span>
                    )}
                  </div>

                  <div className="profile-information">
                    <h2>{displayName}</h2>

                    {profile?.email && (
                      <p className="profile-email">{profile.email}</p>
                    )}

                    <div className="profile-badges">
                      <span className="profile-trust-badge">
                        <ShieldCheck size={15} />

                        {formatTrustLevel(profile?.trustLevel)}
                      </span>

                      {profile?.createdAt && (
                        <span className="profile-joined-badge">
                          <CalendarDays size={15} />
                          Joined {formatPostDate(profile.createdAt)}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <button
                  className="profile-edit-button"
                  type="button"
                  onClick={openEditProfileModal}
                >
                  Edit profile
                </button>
              </div>

              <div className="profile-about-section">
                <span className="profile-about-label">About</span>

                {profile?.bio ? (
                  <p className="profile-bio">{profile.bio}</p>
                ) : (
                  <p className="profile-bio profile-empty-bio">
                    Add a short introduction so your connections know more about
                    you.
                  </p>
                )}
              </div>

              <div className="profile-stats">
                <div className="profile-stat-item">
                  <strong>{postMeta.totalElements}</strong>

                  <span>Posts</span>
                </div>

                <div className="profile-stat-divider" />

                <div className="profile-stat-item">
                  <strong>{socialSummary.followingCount}</strong>

                  <span>Following</span>
                </div>

                <div className="profile-stat-divider" />

                <div className="profile-stat-item">
                  <strong>{socialSummary.followerCount}</strong>

                  <span>Followers</span>
                </div>
              </div>
            </div>
          </motion.article>

          <section className="profile-posts-section">
            <div className="profile-posts-heading">
              <div>
                <p>Your activity</p>
                <h2>Posts</h2>
              </div>

              <span>{postMeta.totalElements} total</span>
            </div>

            {posts.length === 0 && (
              <div className="profile-empty-posts">
                <UserRound size={28} />

                <h3>No posts yet</h3>

                <p>Posts you create will appear here.</p>

                <Link to="/">Create your first post</Link>
              </div>
            )}

            {posts.map((post, index) => (
              <motion.article
                className="profile-post-card"
                key={post.postId || post.id || `${post.createdAt}-${index}`}
                initial={{
                  opacity: 0,
                  y: 12,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
              >
                <header className="profile-post-header">
                  <div className="profile-post-author">
                    <div className="profile-post-avatar">{initials}</div>

                    <div>
                      <strong>{displayName}</strong>

                      <span>{formatPostDate(post.createdAt)}</span>
                    </div>
                  </div>

                  <button type="button" aria-label="Post options">
                    <MoreHorizontal size={21} />
                  </button>
                </header>

                <div className="profile-post-content">
                  <p>{post.content}</p>
                </div>

                <footer className="profile-post-actions">
                  <button type="button">
                    <Heart size={19} />

                    <span>
                      Appreciate
                      {(post.likeCount ?? 0) > 0 ? ` (${post.likeCount})` : ""}
                    </span>
                  </button>

                  <button type="button">
                    <MessageCircle size={19} />

                    <span>
                      Comment
                      {(post.commentCount ?? 0) > 0
                        ? ` (${post.commentCount})`
                        : ""}
                    </span>
                  </button>
                </footer>
              </motion.article>
            ))}

            {postMeta.hasMore && (
              <div className="profile-load-more">
                <button
                  type="button"
                  disabled={isLoadingMore}
                  onClick={handleLoadMore}
                >
                  {isLoadingMore ? "Loading..." : "Load more posts"}
                </button>
              </div>
            )}
          </section>
        </section>

        {isEditProfileOpen && (
          <div
            className="profile-modal-overlay"
            role="presentation"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) {
                closeEditProfileModal();
              }
            }}
          >
            <motion.section
              className="profile-edit-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="edit-profile-title"
              initial={{
                opacity: 0,
                scale: 0.97,
                y: 15,
              }}
              animate={{
                opacity: 1,
                scale: 1,
                y: 0,
              }}
            >
              <header className="profile-modal-header">
                <div>
                  <p>Your identity</p>

                  <h2 id="edit-profile-title">Edit profile</h2>
                </div>

                <button
                  type="button"
                  aria-label="Close edit profile"
                  disabled={isUpdatingProfile}
                  onClick={closeEditProfileModal}
                >
                  <X size={21} />
                </button>
              </header>

              <form
                className="profile-edit-form"
                onSubmit={handleUpdateProfile}
              >
                <div className="profile-edit-field">
                  <label htmlFor="profile-username">Username</label>

                  <div className="profile-edit-input">
                    <AtSign size={18} />

                    <input
                      id="profile-username"
                      name="username"
                      type="text"
                      value={editProfileForm.username}
                      placeholder="Enter your username"
                      maxLength={30}
                      autoComplete="username"
                      disabled={isUpdatingProfile}
                      onChange={handleEditProfileChange}
                    />
                  </div>

                  <span className="profile-field-hint">
                    Letters, numbers, dots and underscores only.
                  </span>
                </div>

                <div className="profile-edit-field">
                  <div className="profile-edit-label-row">
                    <label htmlFor="profile-bio">Bio</label>

                    <span>
                      {editProfileForm.bio.length}
                      /250
                    </span>
                  </div>

                  <div className="profile-edit-textarea">
                    <AlignLeft size={18} />

                    <textarea
                      id="profile-bio"
                      name="bio"
                      value={editProfileForm.bio}
                      placeholder="Share a short introduction..."
                      maxLength={250}
                      rows={5}
                      disabled={isUpdatingProfile}
                      onChange={handleEditProfileChange}
                    />
                  </div>
                </div>

                {editProfileError && (
                  <div className="profile-edit-error">{editProfileError}</div>
                )}

                <footer className="profile-modal-actions">
                  <button
                    className="profile-modal-cancel"
                    type="button"
                    disabled={isUpdatingProfile}
                    onClick={closeEditProfileModal}
                  >
                    Cancel
                  </button>

                  <button
                    className="profile-modal-save"
                    type="submit"
                    disabled={isUpdatingProfile}
                  >
                    <Save size={17} />

                    <span>
                      {isUpdatingProfile ? "Saving..." : "Save changes"}
                    </span>
                  </button>
                </footer>
              </form>
            </motion.section>
          </div>
        )}
      </main>
    </AppPageLayout>
  );
}

export default ProfilePage;
