import { useEffect, useMemo, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";

import {
  Bookmark,
  Home,
  LogOut,
  Settings,
  ShieldCheck,
  UserRound,
  Users,
} from "lucide-react";

import { getProfileByUserIdRequest } from "../../api/profileApi";
import tokenService from "../../auth/tokenService";

import "../../styles/appLayout.css";

const navigationItems = [
  {
    label: "Home",
    path: "/home",
    icon: Home,
  },
  {
    label: "Profile",
    path: "/profile",
    icon: UserRound,
  },
  {
    label: "Connections",
    path: "/connections",
    icon: Users,
  },
  {
    label: "Saved",
    path: "/saved",
    icon: Bookmark,
    disabled: true,
  },
  {
    label: "Settings",
    path: "/settings",
    icon: Settings,
    disabled: true,
  },
];

const getInitials = (value) => {
  if (typeof value !== "string" || !value.trim()) {
    return "TN";
  }

  return value
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .split(/[\s@._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
};

const formatTrustLevel = (trustLevel) => {
  if (!trustLevel) {
    return "New member";
  }

  return trustLevel
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
};

function AppSidebar() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [isProfileLoading, setIsProfileLoading] = useState(true);

  const currentUserId = tokenService.getCurrentUserId();
  const currentEmail = tokenService.getCurrentEmail() || "TrustNet User";

  useEffect(() => {
    const loadProfile = async () => {
      if (!currentUserId) {
        setIsProfileLoading(false);
        return;
      }

      try {
        const response = await getProfileByUserIdRequest(currentUserId);
        setProfile(response?.data ?? response);
      } catch (error) {
        console.error(
          "Unable to load sidebar profile:",
          error.response?.data || error.message,
        );
        setProfile(null);
      } finally {
        setIsProfileLoading(false);
      }
    };

    loadProfile();
  }, [currentUserId]);

  const displayName = useMemo(() => {
    const profileUsername = profile?.username || profile?.userName;

    if (typeof profileUsername === "string" && profileUsername.trim()) {
      return profileUsername.trim();
    }

    return currentEmail.includes("@")
      ? currentEmail.split("@")[0]
      : currentEmail;
  }, [currentEmail, profile]);

  const initials = useMemo(() => getInitials(displayName), [displayName]);
  const trustLevel = useMemo(
    () => formatTrustLevel(profile?.trustLevel),
    [profile?.trustLevel],
  );

  const handleLogout = () => {
    tokenService.clearTokens();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <aside className="trustnet-sidebar">
      <div className="trustnet-sidebar-brand">
        <div className="trustnet-brand-icon">
          <ShieldCheck size={25} />
        </div>

        <div>
          <strong>TrustNet</strong>
          <span>Social, without the noise</span>
        </div>
      </div>

      <nav className="trustnet-sidebar-navigation" aria-label="Main navigation">
        {navigationItems.map((item) => {
          const Icon = item.icon;

          if (item.disabled) {
            return (
              <button
                key={item.label}
                className="trustnet-sidebar-item trustnet-sidebar-disabled"
                type="button"
                disabled
                aria-disabled="true"
              >
                <Icon size={21} />
                <span>{item.label}</span>
              </button>
            );
          }

          return (
            <NavLink
              key={item.label}
              to={item.path}
              end={item.path === "/home"}
              className={({ isActive }) =>
                [
                  "trustnet-sidebar-item",
                  isActive ? "trustnet-sidebar-active" : "",
                ]
                  .filter(Boolean)
                  .join(" ")
              }
            >
              <Icon size={21} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="trustnet-sidebar-user">
        <div className="trustnet-sidebar-avatar" aria-hidden="true">
          {initials}
        </div>

        <div className="trustnet-sidebar-user-details">
          <strong>{isProfileLoading ? "Loading..." : displayName}</strong>
          <span>{isProfileLoading ? "Loading profile" : trustLevel}</span>
        </div>

        <button
          type="button"
          aria-label="Log out"
          title="Log out"
          onClick={handleLogout}
        >
          <LogOut size={20} />
        </button>
      </div>
    </aside>
  );
}

export default AppSidebar;
