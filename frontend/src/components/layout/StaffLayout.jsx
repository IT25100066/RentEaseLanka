import { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import StaffSidebar from './StaffSidebar';
import Topbar from './Topbar';
import './Layout.css';

export default function StaffLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  useEffect(() => { setSidebarOpen(false); }, [location.pathname]);
  useEffect(() => {
    document.body.style.overflow = sidebarOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [sidebarOpen]);

  return (
    <div className="app-layout" id="staff-layout">
      <div className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`} onClick={() => setSidebarOpen(false)} aria-hidden="true" />
      <StaffSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="main-area">
        <Topbar onMenuToggle={() => setSidebarOpen(p => !p)} />
        <main className="main-content" id="staff-main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
