import { useState, useEffect } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  HiOutlineHome,
  HiOutlineTruck,
  HiOutlineMenu,
  HiOutlineX,
  HiOutlineUser,
  HiOutlineLogout,
  HiOutlineClipboardList,
  HiOutlineIdentification,
} from 'react-icons/hi';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, role, logout } = useAuth();
  const isHome = location.pathname === '/';

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 60);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    setMobileOpen(false);
  }, [location]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Get the panel link for the current user's role (only their own)
  const getPanelLink = () => {
    switch (role) {
      case 'ADMIN': return { path: '/admin', label: 'Admin Panel' };
      case 'MANAGER': return { path: '/manager', label: 'Manager Panel' };
      case 'STAFF': return { path: '/staff', label: 'Staff Panel' };
      default: return null;
    }
  };

  const panelLink = getPanelLink();
  const navClass = `navbar ${scrolled || !isHome ? 'navbar-solid' : 'navbar-transparent'}`;

  return (
    <header className={navClass} id="main-navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="navbar-brand" id="navbar-logo">
          <span className="navbar-logo-icon">🚗</span>
          <div className="navbar-logo-text">
            <span className="navbar-logo-main">RentEase</span>
            <span className="navbar-logo-sub">Lanka</span>
          </div>
        </NavLink>

        <nav className={`navbar-links ${mobileOpen ? 'navbar-links-open' : ''}`} id="navbar-nav">
          <NavLink
            to="/"
            className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
            id="nav-link-home"
            end
          >
            <HiOutlineHome className="navbar-link-icon" />
            Home
          </NavLink>

          <NavLink
            to="/vehicles"
            className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
            id="nav-link-vehicles"
          >
            <HiOutlineTruck className="navbar-link-icon" />
            Vehicles
          </NavLink>

          {/* Customer-specific links */}
          {isAuthenticated && role === 'CUSTOMER' && (
            <>
              <NavLink
                to="/my/bookings"
                className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
                id="nav-link-my-bookings"
              >
                <HiOutlineClipboardList className="navbar-link-icon" />
                My Bookings
              </NavLink>
              <NavLink
                to="/my/nic-upload"
                className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
                id="nav-link-nic-upload"
              >
                <HiOutlineIdentification className="navbar-link-icon" />
                NIC Upload
              </NavLink>
            </>
          )}

          {/* Auth section */}
          {!isAuthenticated ? (
            <NavLink
              to="/signin"
              className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
              id="nav-link-signin"
            >
              <HiOutlineUser className="navbar-link-icon" />
              Sign In
            </NavLink>
          ) : (
            <>
              {/* Panel link — only for the user's own role */}
              {panelLink && (
                <NavLink
                  to={panelLink.path}
                  className="navbar-admin-btn"
                  id="nav-panel-btn"
                >
                  {panelLink.label}
                </NavLink>
              )}

              <div className="navbar-user-section">
                <span className="navbar-user-name" id="navbar-user-name">
                  {user?.firstName || 'User'}
                </span>
                <button
                  className="navbar-logout-btn"
                  onClick={handleLogout}
                  id="nav-logout-btn"
                >
                  <HiOutlineLogout />
                  Logout
                </button>
              </div>
            </>
          )}
        </nav>

        <button
          className="navbar-mobile-toggle"
          onClick={() => setMobileOpen(!mobileOpen)}
          id="mobile-menu-toggle"
          aria-label="Toggle navigation"
        >
          {mobileOpen ? <HiOutlineX /> : <HiOutlineMenu />}
        </button>
      </div>
    </header>
  );
}
