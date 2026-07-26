import AppSidebar from "./AppSidebar";

import AppNotificationBell from "../notifications/AppNotificationBell";

import "../../styles/appLayout.css";

function AppPageLayout({ children }) {
  return (
    <div className="trustnet-page-layout">
      <AppSidebar />

      <div className="trustnet-page-content">{children}</div>
      <AppNotificationBell />
    </div>
  );
}

export default AppPageLayout;
