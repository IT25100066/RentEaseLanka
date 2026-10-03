import { NavLink } from 'react-router-dom';
import {
  HiOutlineViewGrid,
  HiOutlineCalendar,
  HiOutlineUser,
  HiOutlineArrowLeft,
  HiOutlineShieldCheck,
  HiX,
} from 'react-icons/hi';
import './Sidebar.css';

const navItems = [
  { path: '/driver', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/driver/trips', label: 'My Assigned Trips', icon: HiOutlineCalendar },
  { path: '/driver/profile', label: 'Profile & Schedule', icon: HiOutlineUser },
];

export default function DriverSidebar({ open, onClose }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar--open' : ''}`} id="driver-sidebar-nav">
      <button className="sidebar-close-btn" onClick={onClose} aria-label="Close sidebar">
        <HiX />
      </button>
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span className="logo-icon">🚗</span>
          <div>
            <h1 className="logo-text">RentEase</h1>
            <span className="logo-subtext">Driver Portal</span>
          </div>
        </div>
      </div>

      <div className="sidebar-back">
        <NavLink to="/" className="back-to-site-link" id="driver-back-to-site">
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
                end={item.path === '/driver'}
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : ''}`
                }
                id={`driver-nav-${item.label.toLowerCase().replace(/[\s&]/g, '-')}`}
              >
                <item.icon className="nav-icon" />
                <span className="nav-label">{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-footer-content">
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#10b981', fontSize: '0.8rem', marginBottom: '8px' }}>
            <HiOutlineShieldCheck />
            <span>Conflict Guard Active</span>
          </div>
          <p className="footer-version">v1.0.0</p>
          <p className="footer-copyright">© 2026 RentEaseLanka</p>
        </div>
      </div>
    </aside>
  );
}
