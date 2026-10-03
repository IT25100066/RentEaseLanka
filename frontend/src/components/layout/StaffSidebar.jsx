import { NavLink } from 'react-router-dom';
import {
  HiOutlineViewGrid,
  HiOutlineCalendar,
  HiOutlineClock,
  HiOutlineCog,
  HiOutlineTruck,
  HiOutlineClipboardCheck,
  HiOutlineClipboardList,
  HiOutlineArrowLeft,
} from 'react-icons/hi';
import { HiX } from 'react-icons/hi';
import './Sidebar.css';

const navItems = [
  { path: '/staff', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/staff/fleet', label: 'Fleet', icon: HiOutlineTruck },
  { path: '/staff/bookings', label: 'Bookings', icon: HiOutlineCalendar },
  { path: '/staff/handovers', label: 'Handovers', icon: HiOutlineClipboardCheck },
  { path: '/staff/returns', label: 'Return Inspections', icon: HiOutlineClipboardList },
  { path: '/staff/pending-customers', label: 'Pending Customers', icon: HiOutlineClock },
  { path: '/staff/maintenance', label: 'Maintenance', icon: HiOutlineCog },
];

export default function StaffSidebar({ open, onClose }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar--open' : ''}`} id="staff-sidebar-nav">
      <button className="sidebar-close-btn" onClick={onClose} aria-label="Close sidebar">
        <HiX />
      </button>
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span className="logo-icon">🚗</span>
          <div>
            <h1 className="logo-text">RentEase</h1>
            <span className="logo-subtext">Staff Panel</span>
          </div>
        </div>
      </div>

      <div className="sidebar-back">
        <NavLink to="/" className="back-to-site-link" id="staff-back-to-site">
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
                end={item.path === '/staff'}
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : ''}`
                }
                id={`staff-nav-${item.label.toLowerCase().replace(/\s/g, '-')}`}
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
          <p className="footer-version">v1.0.0</p>
          <p className="footer-copyright">© 2026 RentEaseLanka</p>
        </div>
      </div>
    </aside>
  );
}
