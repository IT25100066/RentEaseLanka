import { Link } from 'react-router-dom';
import {
  HiOutlineUserAdd,
  HiOutlineTruck,
  HiOutlineCollection,
  HiOutlineCheckCircle,
  HiOutlineClipboardList,
} from 'react-icons/hi';
import '../PanelPages.css';

export default function ManagerDashboard() {
  return (
    <div className="panel-dashboard" id="manager-dashboard">
      <div className="panel-header">
        <h1>Manager Dashboard</h1>
        <p>Manage staff, fleet, pending vehicle approvals, and vehicle categories</p>
      </div>

      <div className="stat-cards">
        <div className="stat-card">
          <div className="stat-icon blue">
            <HiOutlineTruck />
          </div>
          <div className="stat-info">
            <h3>Fleet</h3>
            <span className="stat-value">Active</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange">
            <HiOutlineCheckCircle />
          </div>
          <div className="stat-info">
            <h3>Approvals</h3>
            <span className="stat-value">Review Pending</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green">
            <HiOutlineCollection />
          </div>
          <div className="stat-info">
            <h3>Categories</h3>
            <span className="stat-value">Active</span>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <Link to="/manager/pending-vehicles" className="action-card" id="action-pending-vehicles">
            <HiOutlineCheckCircle className="action-icon" />
            Pending Approvals
          </Link>
          <Link to="/manager/fleet" className="action-card" id="action-manage-fleet">
            <HiOutlineTruck className="action-icon" />
            Manage Fleet
          </Link>
          <Link to="/manager/bookings" className="action-card" id="action-manage-bookings">
            <HiOutlineClipboardList className="action-icon" />
            Bookings
          </Link>
          <Link to="/manager/categories" className="action-card" id="action-manage-categories">
            <HiOutlineCollection className="action-icon" />
            Manage Categories
          </Link>
          <Link to="/manager/staff" className="action-card" id="action-create-staff">
            <HiOutlineUserAdd className="action-icon" />
            Create New Staff
          </Link>
          <Link to="/manager/drivers" className="action-card" id="action-create-driver">
            <HiOutlineUserAdd className="action-icon" />
            Create New Driver
          </Link>
        </div>
      </div>
    </div>
  );
}
