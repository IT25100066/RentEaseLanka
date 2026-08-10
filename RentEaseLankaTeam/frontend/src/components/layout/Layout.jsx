import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import './Layout.css';

export default function Layout() {
  return (
    <div className="app-layout" id="app-layout">
      <Sidebar />
      <div className="main-area">
        <Topbar />
        <main className="main-content" id="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
