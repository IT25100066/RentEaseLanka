import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import './Layout.css';

export default function AdminLayout() {
  return (
    <div className="app-layout" id="admin-layout">
      <Sidebar />
      <div className="main-area">
        <Topbar />
        <main className="main-content" id="admin-main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
