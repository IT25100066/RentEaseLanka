import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  HiOutlineUserAdd,
  HiOutlineUsers,
  HiOutlineTruck,
  HiOutlineClipboardList,
  HiOutlineCheckCircle,
  HiOutlineCollection,
  HiOutlineCog,
  HiOutlineExclamation,
  HiOutlineRefresh,
} from 'react-icons/hi';
import {
  getDashboardNotifications,
  getDashboardSummary,
  getVehiclePerformance,
} from '../../api/adminDashboard';
import '../PanelPages.css';
import './AdminDashboard.css';

const initialSummary = {
  totalVehicles: 0,
  availableVehicles: 0,
  rentedVehicles: 0,
  underMaintenanceVehicles: 0,
  fleetUtilizationRate: 0,
  totalMaintenanceRecords: 0,
  totalMaintenanceCost: 0,
};

export default function AdminDashboard() {
  const [summary, setSummary] = useState(initialSummary);
  const [vehicles, setVehicles] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError('');

    const results = await Promise.allSettled([
      getDashboardSummary(),
      getVehiclePerformance(),
      getDashboardNotifications(),
    ]);
    const [summaryResult, vehiclesResult, notificationsResult] = results;

    if (summaryResult.status === 'fulfilled') setSummary(summaryResult.value);
    if (vehiclesResult.status === 'fulfilled') setVehicles(vehiclesResult.value);
    if (notificationsResult.status === 'fulfilled') setNotifications(notificationsResult.value);

    if (results.every((result) => result.status === 'rejected')) {
      setError('Dashboard data could not be loaded.');
    } else if (results.some((result) => result.status === 'rejected')) {
      setError('Some dashboard data is temporarily unavailable.');
    }

    setLoading(false);
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  const formatCurrency = (value) => `LKR ${Number(value || 0).toLocaleString()}`;

  if (loading) return <div className="admin-dashboard-state">Loading dashboard data...</div>;

  return (
    <div className="panel-dashboard" id="admin-dashboard">
      <div className="panel-header">
        <h1>Admin Dashboard</h1>
        <p>Live fleet, maintenance, booking, and operations overview</p>
        <button className="admin-dashboard-refresh" type="button" onClick={loadDashboard}>
          <HiOutlineRefresh />
          Refresh data
        </button>
      </div>

      {error && (
        <div className="admin-dashboard-alert" role="status">
          <HiOutlineExclamation />
          <span>{error}</span>
        </div>
      )}

      <div className="stat-cards">
        <div className="stat-card">
          <div className="stat-icon green">
            <HiOutlineUsers />
          </div>
          <div className="stat-info">
            <h3>Total Vehicles</h3>
            <span className="stat-value">{summary.totalVehicles}</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon blue">
            <HiOutlineTruck />
          </div>
          <div className="stat-info">
            <h3>Available</h3>
            <span className="stat-value">{summary.availableVehicles}</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange">
            <HiOutlineClipboardList />
          </div>
          <div className="stat-info">
            <h3>Rented</h3>
            <span className="stat-value">{summary.rentedVehicles}</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon amber"><HiOutlineCog /></div>
          <div className="stat-info">
            <h3>Maintenance</h3>
            <span className="stat-value">{summary.underMaintenanceVehicles}</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon rose"><HiOutlineClipboardList /></div>
          <div className="stat-info">
            <h3>Utilization</h3>
            <span className="stat-value">{summary.fleetUtilizationRate}%</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><HiOutlineCheckCircle /></div>
          <div className="stat-info">
            <h3>Maintenance Cost</h3>
            <span className="stat-value stat-value-small">{formatCurrency(summary.totalMaintenanceCost)}</span>
          </div>
        </div>
      </div>

      <div className="admin-dashboard-grid">
        <section className="admin-dashboard-panel">
          <div className="admin-dashboard-panel-header">
            <h2>Operational alerts</h2>
            <span>{notifications.length} active</span>
          </div>
          {notifications.length === 0 ? <p className="admin-dashboard-empty">No active alerts.</p> : (
            <div className="admin-dashboard-notifications">
              {notifications.map((notification) => (
                <div className={`admin-dashboard-notification ${notification.severity?.toLowerCase()}`} key={notification.id}>
                  <HiOutlineExclamation />
                  <div><strong>{notification.title}</strong><p>{notification.message}</p></div>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="admin-dashboard-panel">
          <div className="admin-dashboard-panel-header">
            <h2>Maintenance overview</h2>
            <span>{summary.totalMaintenanceRecords} records</span>
          </div>
          <div className="admin-dashboard-maintenance-stats">
            <div><strong>{summary.scheduledMaintenance || 0}</strong><span>Scheduled</span></div>
            <div><strong>{summary.inProgressMaintenance || 0}</strong><span>In progress</span></div>
            <div><strong>{summary.completedMaintenance || 0}</strong><span>Completed</span></div>
            <div><strong>{summary.cancelledMaintenance || 0}</strong><span>Cancelled</span></div>
          </div>
        </section>
      </div>

      <section className="admin-dashboard-panel admin-dashboard-vehicles">
        <div className="admin-dashboard-panel-header">
          <h2>Vehicle performance</h2>
          <Link to="/admin/fleet">Manage fleet</Link>
        </div>
        {vehicles.length === 0 ? <p className="admin-dashboard-empty">No vehicle performance data is available.</p> : (
          <div className="admin-dashboard-table-wrapper">
            <table className="admin-dashboard-table">
              <thead><tr><th>Vehicle</th><th>Status</th><th>Odometer</th><th>Maintenance</th><th>Total cost</th></tr></thead>
              <tbody>
                {vehicles.slice(0, 8).map((vehicle) => (
                  <tr key={vehicle.vehicleId}>
                    <td><strong>{vehicle.model}</strong><span>{vehicle.licencePlate}</span></td>
                    <td><span className={`admin-dashboard-status ${vehicle.status?.toLowerCase()}`}>{vehicle.status}</span></td>
                    <td>{Number(vehicle.currentOdometer || 0).toLocaleString()} km</td>
                    <td>{vehicle.maintenanceCount}</td>
                    <td>{formatCurrency(vehicle.totalMaintenanceCost)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <Link to="/admin/fleet" className="action-card" id="admin-action-fleet">
            <HiOutlineTruck className="action-icon" />
            Manage Fleet
          </Link>
          <Link to="/admin/bookings" className="action-card" id="admin-action-bookings">
            <HiOutlineClipboardList className="action-icon" />
            Manage Bookings
          </Link>
          <Link to="/admin/pending-vehicles" className="action-card" id="admin-action-pending-vehicles">
            <HiOutlineCheckCircle className="action-icon" />
            Pending Approvals
          </Link>
          <Link to="/admin/categories" className="action-card" id="admin-action-categories">
            <HiOutlineCollection className="action-icon" />
            Categories
          </Link>
          <Link to="/admin/maintenance" className="action-card" id="admin-action-maintenance">
            <HiOutlineCog className="action-icon" />
            Maintenance
          </Link>
          <Link to="/admin/managers" className="action-card" id="admin-action-create-manager">
            <HiOutlineUserAdd className="action-icon" />
            Create Manager
          </Link>
        </div>
      </div>
    </div>
  );
}
