import { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import ManagerSidebar from './ManagerSidebar';
import Topbar from './Topbar';
import './Layout.css';

export default function ManagerLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  useEffect(() => { setSidebarOpen(false); }, [location.pathname]);
  useEffect(() => {
    document.body.style.overflow = sidebarOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [sidebarOpen]);

  return (
    <div className="app-layout" id="manager-layout">
      <div className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`} onClick={() => setSidebarOpen(false)} aria-hidden="true" />
      <ManagerSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="main-area">
        <Topbar onMenuToggle={() => setSidebarOpen(p => !p)} />
        <main className="main-content" id="manager-main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
