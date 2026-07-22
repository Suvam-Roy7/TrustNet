import { NavLink, useNavigate } from "react-router-dom";

import {
  Bell,
  Bookmark,
  Home,
  LogOut,
  Settings,
  ShieldCheck,
  UserRound,
  Users,
} from "lucide-react";

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
    label: "Notifications",
    path: "/notifications",
    icon: Bell,
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

function AppSidebar() {
  const navigate = useNavigate();

  const currentEmail = tokenService.getCurrentEmail() || "TrustNet User";

  const displayName = currentEmail.includes("@")
    ? currentEmail.split("@")[0]
    : currentEmail;

  const initials = getInitials(displayName);

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
          <strong>{displayName}</strong>
          <span>Private member</span>
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
