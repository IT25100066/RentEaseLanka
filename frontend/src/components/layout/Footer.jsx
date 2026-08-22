import { NavLink } from 'react-router-dom';
import {
  HiOutlinePhone,
  HiOutlineMail,
  HiOutlineLocationMarker,
} from 'react-icons/hi';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="site-footer" id="site-footer">
      <div className="footer-wave">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
          <path
            d="M0,60 C360,120 720,0 1080,60 C1260,90 1380,70 1440,80 L1440,120 L0,120 Z"
            fill="currentColor"
          />
        </svg>
      </div>

      <div className="footer-content">
        <div className="footer-inner">
          {/* Brand Column */}
          <div className="footer-col footer-brand-col">
            <div className="footer-brand">
              <span className="footer-logo-icon">🚗</span>
              <div>
                <span className="footer-logo-main">RentEase</span>
                <span className="footer-logo-sub">Lanka</span>
              </div>
            </div>
            <p className="footer-desc">
              Sri Lanka's premier vehicle rental service. Explore the island with comfort,
              safety, and unbeatable prices.
            </p>
            <div className="footer-social">
              <a href="#" className="social-link" aria-label="Facebook">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>
              </a>
              <a href="#" className="social-link" aria-label="Instagram">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="2" width="20" height="20" rx="5"/><circle cx="12" cy="12" r="5"/><circle cx="17.5" cy="6.5" r="1.5"/></svg>
              </a>
              <a href="#" className="social-link" aria-label="Twitter">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"/></svg>
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div className="footer-col">
            <h4 className="footer-col-title">Quick Links</h4>
            <ul className="footer-links">
              <li><NavLink to="/">Home</NavLink></li>
              <li><NavLink to="/vehicles">Browse Vehicles</NavLink></li>
              <li><NavLink to="/admin">Admin Panel</NavLink></li>
            </ul>
          </div>

          {/* Vehicle Types */}
          <div className="footer-col">
            <h4 className="footer-col-title">Vehicle Types</h4>
            <ul className="footer-links">
              <li><a href="#">SUVs</a></li>
              <li><a href="#">Sedans</a></li>
              <li><a href="#">Vans</a></li>
              <li><a href="#">Luxury Cars</a></li>
            </ul>
          </div>

          {/* Contact */}
          <div className="footer-col">
            <h4 className="footer-col-title">Contact Us</h4>
            <ul className="footer-contact">
              <li>
                <HiOutlinePhone />
                <span>+94 11 234 5678</span>
              </li>
              <li>
                <HiOutlineMail />
                <span>info@renteaselanka.lk</span>
              </li>
              <li>
                <HiOutlineLocationMarker />
                <span>42 Galle Road, Colombo 03</span>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <p>© 2026 RentEaseLanka. All rights reserved.</p>
          <div className="footer-bottom-links">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
