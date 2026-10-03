import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import {
  HiOutlineTruck,
  HiOutlineCheckCircle,
  HiOutlineCog,
  HiOutlineClock,
  HiOutlineExclamation,
  HiOutlineCollection,
  HiOutlineCalendar,
} from 'react-icons/hi';
import StatusBadge from '../components/common/StatusBadge';
import LoadingSpinner from '../components/common/LoadingSpinner';
import './Dashboard.css';


export default function Dashboard() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchVehicles();
  }, []);

  const fetchVehicles = async () => {
    try {
      const res = await API.get('/vehicles');
      setVehicles(res.data);
    } catch (err) {
      console.error('Failed to load vehicles', err);
    } finally {
      setLoading(false);
    }
  };

  const stats = {
    total: vehicles.length,
    available: vehicles.filter((v) => v.status === 'AVAILABLE').length,
    underMaintenance: vehicles.filter((v) => v.status === 'UNDER_MAINTENANCE').length,
    pending: vehicles.filter((v) => v.status === 'PENDING').length,
    rented: vehicles.filter((v) => v.status === 'RENTED').length,
    reserved: vehicles.filter((v) => v.status === 'RESERVED').length,
  };

  if (loading) return <LoadingSpinner message="Loading dashboard..." />;

  return (
    <div className="page-container" id="dashboard-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Welcome back! Here's your fleet overview.</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid-stats">
        <div className="stat-card" id="stat-total">
          <div className="stat-icon primary">
            <HiOutlineTruck />
          </div>
          <div className="stat-info">
            <h3>{stats.total}</h3>
            <p>Total Vehicles</p>
          </div>
        </div>

        <div className="stat-card" id="stat-available">
          <div className="stat-icon emerald">
            <HiOutlineCheckCircle />
          </div>
          <div className="stat-info">
            <h3>{stats.available}</h3>
            <p>Available</p>
          </div>
        </div>

        <div className="stat-card" id="stat-maintenance">
          <div className="stat-icon violet">
            <HiOutlineCog />
          </div>
          <div className="stat-info">
            <h3>{stats.underMaintenance}</h3>
            <p>Under Maintenance</p>
          </div>
        </div>

        <div className="stat-card" id="stat-pending">
          <div className="stat-icon amber">
            <HiOutlineClock />
          </div>
          <div className="stat-info">
            <h3>{stats.pending}</h3>
            <p>Pending Approval</p>
          </div>
        </div>

        <div className="stat-card" id="stat-rented">
          <div className="stat-icon sky">
            <HiOutlineCollection />
          </div>
          <div className="stat-info">
            <h3>{stats.rented}</h3>
            <p>Rented</p>
          </div>
        </div>

        <div className="stat-card" id="stat-reserved">
          <div className="stat-icon rose">
            <HiOutlineExclamation />
          </div>
          <div className="stat-info">
            <h3>{stats.reserved}</h3>
            <p>Reserved</p>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="dashboard-section">
        <h2 className="section-title">Quick Actions</h2>
        <div className="quick-actions">
          <button className="quick-action-card" onClick={() => navigate('/admin/fleet')} id="qa-fleet">
            <div className="qa-icon primary">
              <HiOutlineTruck />
            </div>
            <span>Manage Fleet</span>
          </button>
          <button className="quick-action-card" onClick={() => navigate('/admin/pending')} id="qa-pending">
            <div className="qa-icon amber">
              <HiOutlineClock />
            </div>
            <span>Approve Vehicles</span>
          </button>
          <button className="quick-action-card" onClick={() => navigate('/admin/categories')} id="qa-categories">
            <div className="qa-icon emerald">
              <HiOutlineCollection />
            </div>
            <span>Categories</span>
          </button>
          <button className="quick-action-card" onClick={() => navigate('/admin/maintenance')} id="qa-maintenance">
            <div className="qa-icon violet">
              <HiOutlineCog />
            </div>
            <span>Maintenance</span>
          </button>
          <button className="quick-action-card" onClick={() => navigate('/admin/bookings')} id="qa-bookings">
            <div className="qa-icon sky">
              <HiOutlineCalendar />
            </div>
            <span>Bookings</span>
          </button>
        </div>
      </div>

      {/* Recent Vehicles Table */}
      <div className="dashboard-section">
        <h2 className="section-title">Recent Vehicles</h2>
        <div className="table-container">
          <table className="data-table" id="recent-vehicles-table">
            <thead>
              <tr>
                <th>Vehicle</th>
                <th>License Plate</th>
                <th>Category</th>
                <th>Status</th>
                <th>Daily Rate</th>
                <th>Added</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.slice(0, 8).map((v) => (
                <tr
                  key={v.id}
                  onClick={() => navigate(`/admin/vehicle/${v.id}`)}
                  style={{ cursor: 'pointer' }}
                >
                  <td>
                    <div className="table-vehicle-cell">
                      <div className="table-vehicle-thumb">
                        {v.imageUrls && v.imageUrls.length > 0 ? (
                          <img src={`${BASE_URL}${v.imageUrls[0]}`} alt={v.model} />
                        ) : (
                          <span>🚗</span>
                        )}
                      </div>
                      <div>
                        <span className="table-vehicle-model">{v.model}</span>
                        <span className="table-vehicle-year">{v.year}</span>
                      </div>
                    </div>
                  </td>
                  <td><code className="plate-code">{v.licensePlate}</code></td>
                  <td>{v.categoryName || '—'}</td>
                  <td><StatusBadge status={v.status} /></td>
                  <td className="rate-cell">LKR {v.dailyRentalRate?.toLocaleString()}</td>
                  <td className="date-cell">
                    {v.createdAt ? new Date(v.createdAt).toLocaleDateString() : '—'}
                  </td>
                </tr>
              ))}
              {vehicles.length === 0 && (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                    No vehicles found. Add your first vehicle!
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
