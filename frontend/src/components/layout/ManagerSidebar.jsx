import { NavLink } from 'react-router-dom';
import {
  HiOutlineViewGrid,
  HiOutlineUserAdd,
  HiOutlineTruck,
  HiOutlineCollection,
  HiOutlineCheckCircle,
  HiOutlineClipboardList,
  HiOutlineClipboardCheck,
  HiOutlineCurrencyDollar,
  HiOutlineArrowLeft,
} from 'react-icons/hi';
import { HiX } from 'react-icons/hi';
import './Sidebar.css';

const navItems = [
  { path: '/manager', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/manager/fleet', label: 'Fleet', icon: HiOutlineTruck },
  { path: '/manager/pending-vehicles', label: 'Pending Approvals', icon: HiOutlineCheckCircle },
  { path: '/manager/bookings', label: 'Bookings', icon: HiOutlineClipboardList },
  { path: '/manager/handovers', label: 'Handovers', icon: HiOutlineClipboardCheck },
  { path: '/manager/returns', label: 'Return Inspections', icon: HiOutlineClipboardList },
  { path: '/manager/categories', label: 'Categories', icon: HiOutlineCollection },
  { path: '/manager/financial', label: 'Financial Reports', icon: HiOutlineCurrencyDollar },
  { path: '/manager/staff', label: 'Create Staff', icon: HiOutlineUserAdd },
  { path: '/manager/drivers', label: 'Create Driver', icon: HiOutlineUserAdd },
];

export default function ManagerSidebar({ open, onClose }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar--open' : ''}`} id="manager-sidebar-nav">
      <button className="sidebar-close-btn" onClick={onClose} aria-label="Close sidebar">
        <HiX />
      </button>
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span className="logo-icon">🚗</span>
          <div>
            <h1 className="logo-text">RentEase</h1>
            <span className="logo-subtext">Manager Panel</span>
          </div>
        </div>
      </div>

      <div className="sidebar-back">
        <NavLink to="/" className="back-to-site-link" id="manager-back-to-site">
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
                end={item.path === '/manager'}
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : ''}`
                }
                id={`manager-nav-${item.label.toLowerCase().replace(/\s/g, '-')}`}
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
