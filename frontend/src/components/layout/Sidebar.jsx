import { NavLink, useLocation } from 'react-router-dom';
import {
  HiOutlineViewGrid,
  HiOutlineTruck,
  HiOutlineClipboardCheck,
  HiOutlineCollection,
  HiOutlineCog,
  HiOutlineClock,
  HiOutlineArrowLeft,
} from 'react-icons/hi';
import './Sidebar.css';

const navItems = [
  { path: '/admin', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/admin/fleet', label: 'Fleet', icon: HiOutlineTruck },
  { path: '/admin/pending', label: 'Pending Approvals', icon: HiOutlineClock },
  { path: '/admin/categories', label: 'Categories', icon: HiOutlineCollection },
  { path: '/admin/maintenance', label: 'Maintenance', icon: HiOutlineCog },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="sidebar" id="sidebar-nav">
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span className="logo-icon">🚗</span>
          <div>
            <h1 className="logo-text">RentEase</h1>
            <span className="logo-subtext">Admin Panel</span>
          </div>
        </div>
      </div>

      {/* Back to Site Link */}
      <div className="sidebar-back">
        <NavLink to="/" className="back-to-site-link" id="back-to-site">
          <HiOutlineArrowLeft />
          <span>Back to Site</span>
        </NavLink>
      </div>

      <nav className="sidebar-nav">
        <ul className="nav-list">
          {navItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                end={item.path === '/admin'}
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : ''}`
                }
                id={`nav-${item.label.toLowerCase().replace(/\s/g, '-')}`}
              >
                <item.icon className="nav-icon" />
                <span className="nav-label">{item.label}</span>
                {item.path === '/admin/pending' && (
                  <span className="nav-badge" id="pending-badge"></span>
                )}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-footer-content">
          <p className="footer-version">v1.0.0</p>
          <p className="footer-copyright">© 2026 RentEaseLanka</p>
        </div>
      </div>
    </aside>
  );
}
