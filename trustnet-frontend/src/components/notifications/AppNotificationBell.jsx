import { useCallback, useState } from "react";
import { Bell } from "lucide-react";

import NotificationCenter from "./NotificationCenter";

function AppNotificationBell() {
  const [isNotificationCenterOpen, setIsNotificationCenterOpen] =
    useState(false);

  const [notificationCount, setNotificationCount] = useState(0);

  const closeNotificationCenter = useCallback(() => {
    setIsNotificationCenterOpen(false);
  }, []);

  const toggleNotificationCenter = () => {
    setIsNotificationCenterOpen((currentValue) => !currentValue);
  };

  return (
    <div className="app-notification-shell">
      <button
        type="button"
        className={`app-notification-bell ${
          isNotificationCenterOpen ? "is-active" : ""
        }`}
        data-notification-trigger="true"
        aria-label="Open notification centre"
        aria-expanded={isNotificationCenterOpen}
        onClick={toggleNotificationCenter}
      >
        <Bell size={22} />

        {notificationCount > 0 && (
          <span className="app-notification-badge">
            {notificationCount > 99 ? "99+" : notificationCount}
          </span>
        )}
      </button>

      <NotificationCenter
        isOpen={isNotificationCenterOpen}
        onClose={closeNotificationCenter}
        onCountChange={setNotificationCount}
      />
    </div>
  );
}

export default AppNotificationBell;
