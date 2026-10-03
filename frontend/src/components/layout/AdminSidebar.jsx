import { NavLink } from 'react-router-dom';
import {
  HiOutlineViewGrid,
  HiOutlineUserAdd,
  HiOutlineTruck,
  HiOutlineClipboardList,
  HiOutlineClipboardCheck,
  HiOutlineCheckCircle,
  HiOutlineCollection,
  HiOutlineCog,
  HiOutlineCurrencyDollar,
  HiOutlineArrowLeft,
} from 'react-icons/hi';
import { HiX } from 'react-icons/hi';
import './Sidebar.css';

const navItems = [
  { path: '/admin', label: 'Dashboard', icon: HiOutlineViewGrid },
  { path: '/admin/fleet', label: 'Fleet Management', icon: HiOutlineTruck },
  { path: '/admin/bookings', label: 'Bookings', icon: HiOutlineClipboardList },
  { path: '/admin/handovers', label: 'Handovers', icon: HiOutlineClipboardCheck },
  { path: '/admin/returns', label: 'Return Inspections', icon: HiOutlineClipboardList },
  { path: '/admin/pending-vehicles', label: 'Pending Approvals', icon: HiOutlineCheckCircle },
  { path: '/admin/categories', label: 'Categories', icon: HiOutlineCollection },
  { path: '/admin/maintenance', label: 'Maintenance', icon: HiOutlineCog },
  { path: '/admin/financial', label: 'Financial Reports', icon: HiOutlineCurrencyDollar },
  { path: '/admin/managers', label: 'Create Manager', icon: HiOutlineUserAdd },
];

export default function AdminSidebar({ open, onClose }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar--open' : ''}`} id="admin-sidebar-nav">
      <button className="sidebar-close-btn" onClick={onClose} aria-label="Close sidebar">
        <HiX />
      </button>
      <div className="sidebar-brand">
        <div className="sidebar-logo">
          <span className="logo-icon">🔐</span>
          <div>
            <h1 className="logo-text">RentEase</h1>
            <span className="logo-subtext">Admin Panel</span>
          </div>
        </div>
      </div>

      <div className="sidebar-back">
        <NavLink to="/" className="back-to-site-link" id="admin-back-to-site">
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
                id={`admin-nav-${item.label.toLowerCase().replace(/\s/g, '-')}`}
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
