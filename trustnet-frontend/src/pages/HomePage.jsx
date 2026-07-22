import { useNavigate } from "react-router-dom";
import { motion } from "motion/react";

import {
  Bell,
  Bookmark,
  Heart,
  Home,
  Image,
  LogOut,
  MessageCircle,
  MoreHorizontal,
  PenLine,
  Search,
  Settings,
  ShieldCheck,
  Sparkles,
  UserRound,
  Users,
  X,
  Send,
} from "lucide-react";

import { useAuth } from "../auth/AuthContext";
import "../styles/home.css";

import { useCallback, useEffect, useMemo, useState } from "react";
import { getProfileByUserIdRequest } from "../api/profileApi";
import tokenService from "../auth/tokenService";

import { toast } from "react-toastify";
import { createPostRequest } from "../api/postApi";
import { getHomeFeedRequest } from "../api/feedApi";

import { NavLink } from "react-router-dom";

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
              <Bell size={21} />
              <span>Notifications</span>

              <span className="notification-count">3</span>
            </button>

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

            <button
              className="topbar-icon-button"
              type="button"
              aria-label="Notifications"
            >
              <Bell size={21} />
              <span className="topbar-notification-dot" />
            </button>
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

                <button className="view-profile-button" type="button">
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

              <button type="button">Explore people</button>
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
      <div className="trustnet-app">
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
    </div>
  );
}

export default HomePage;
