import AppSidebar from "./AppSidebar";

import "../../styles/appLayout.css";

function AppPageLayout({ children }) {
  return (
    <div className="trustnet-page-layout">
      <AppSidebar />

      <div className="trustnet-page-content">{children}</div>
    </div>
  );
}

export default AppPageLayout;
