import { useState, useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  HiOutlineHome,
  HiOutlineTruck,
  HiOutlineInformationCircle,
  HiOutlinePhone,
  HiOutlineCog,
  HiOutlineMenu,
  HiOutlineX,
} from 'react-icons/hi';
import './Navbar.css';

const navLinks = [
  { path: '/', label: 'Home', icon: HiOutlineHome },
  { path: '/vehicles', label: 'Vehicles', icon: HiOutlineTruck },
];

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();
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
          {navLinks.map((link) => (
            <NavLink
              key={link.path}
              to={link.path}
              className={({ isActive }) => `navbar-link ${isActive ? 'navbar-link-active' : ''}`}
              id={`nav-link-${link.label.toLowerCase()}`}
              end={link.path === '/'}
            >
              <link.icon className="navbar-link-icon" />
              {link.label}
            </NavLink>
          ))}
          <NavLink
            to="/admin"
            className="navbar-admin-btn"
            id="nav-admin-btn"
          >
            <HiOutlineCog />
            Admin Panel
          </NavLink>
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
