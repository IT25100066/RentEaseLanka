import { Link } from 'react-router-dom';
import { HiOutlineCalendar, HiOutlineClock, HiOutlineCog, HiOutlineClipboardCheck } from 'react-icons/hi';
import '../PanelPages.css';

export default function StaffDashboard() {
  return (
    <div className="panel-dashboard" id="staff-dashboard">
      <div className="panel-header">
        <h1>Staff Dashboard</h1>
        <p>Manage bookings, customer verifications, vehicle handovers, and maintenance</p>
      </div>

      <div className="stat-cards">
        <div className="stat-card">
          <div className="stat-icon amber">
            <HiOutlineClock />
          </div>
          <div className="stat-info">
            <h3>Pending</h3>
            <span className="stat-value">—</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue">
            <HiOutlineCalendar />
          </div>
          <div className="stat-info">
            <h3>Bookings</h3>
            <span className="stat-value">—</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green">
            <HiOutlineClipboardCheck />
          </div>
          <div className="stat-info">
            <h3>Handovers</h3>
            <span className="stat-value">—</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green">
            <HiOutlineCog />
          </div>
          <div className="stat-info">
            <h3>Maintenance</h3>
            <span className="stat-value">Active</span>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <Link to="/staff/bookings" className="action-card" id="action-view-bookings">
            <HiOutlineCalendar className="action-icon" />
            View All Bookings
          </Link>
          <Link to="/staff/handovers" className="action-card" id="action-handovers">
            <HiOutlineClipboardCheck className="action-icon" />
            Vehicle Handovers
          </Link>
          <Link to="/staff/pending-customers" className="action-card" id="action-pending-customers">
            <HiOutlineClock className="action-icon" />
            Pending Customer Verifications
          </Link>
          <Link to="/staff/maintenance" className="action-card" id="action-maintenance">
            <HiOutlineCog className="action-icon" />
            Vehicle Maintenance
          </Link>
        </div>
      </div>
    </div>
  );
}

