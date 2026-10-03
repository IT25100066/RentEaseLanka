import { useLocation, useNavigate } from 'react-router-dom';
import { HiOutlineSearch, HiOutlineLogout, HiOutlineMenu } from 'react-icons/hi';
import { useAuth } from '../../context/AuthContext';
import './Topbar.css';

const pageTitles = {
  '/admin': 'Admin Dashboard',
  '/admin/fleet': 'Fleet Management',
  '/admin/bookings': 'Bookings Management',
  '/admin/pending-vehicles': 'Pending Vehicle Approvals',
  '/admin/categories': 'Category Management',
  '/admin/maintenance': 'Maintenance Management',
  '/admin/financial': 'Financial Reports',
  '/admin/managers': 'Create Manager',
  '/manager': 'Manager Dashboard',
  '/manager/fleet': 'Fleet Management',
  '/manager/pending-vehicles': 'Pending Vehicle Approvals',
  '/manager/bookings': 'Bookings Overview',
  '/manager/categories': 'Category Management',
  '/manager/staff': 'Create Staff',
  '/manager/financial': 'Financial Reports',
  '/staff': 'Staff Dashboard',
  '/staff/fleet': 'Fleet Management',
  '/staff/bookings': 'Bookings',
  '/staff/pending-customers': 'Pending Customer Verifications',
  '/staff/maintenance': 'Maintenance Management',
  '/driver': 'Driver Dashboard',
  '/driver/trips': 'My Assigned Trips',
  '/driver/profile': 'Driver Profile & Schedule',
};

export default function Topbar({ onMenuToggle }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const getTitle = () => {
    if (location.pathname.includes('/vehicle/')) return 'Vehicle Details';
    return pageTitles[location.pathname] || 'RentEaseLanka';
  };

  const handleLogout = () => {
    logout();
    navigate('/signin');
  };

  const initials = user
    ? `${(user.firstName || '')[0] || ''}${(user.lastName || '')[0] || ''}`.toUpperCase()
    : '?';

  return (
    <header className="topbar" id="topbar">
      <div className="topbar-left">
        {/* Hamburger — visible on mobile only */}
        <button
          className="topbar-menu-btn"
          onClick={onMenuToggle}
          aria-label="Open navigation menu"
          id="topbar-menu-toggle"
        >
          <HiOutlineMenu />
        </button>
        <h2 className="topbar-title">{getTitle()}</h2>
      </div>

      <div className="topbar-right">
        <div className="topbar-search">
          <HiOutlineSearch className="search-icon" />
          <input
            type="text"
            placeholder="Search..."
            className="search-input"
            id="global-search"
          />
        </div>

        <div className="topbar-user-info" id="topbar-user-info">
          <div className="topbar-avatar" id="user-avatar">
            <span>{initials}</span>
          </div>
          <span className="topbar-user-name">{user?.firstName || 'User'}</span>
        </div>

        <button className="topbar-logout" onClick={handleLogout} title="Logout" id="topbar-logout">
          <HiOutlineLogout />
        </button>
      </div>
    </header>
  );
}
