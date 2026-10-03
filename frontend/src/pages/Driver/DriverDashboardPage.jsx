import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  HiOutlineCalendar,
  HiOutlineCheckCircle,
  HiOutlineTruck,
  HiOutlineShieldCheck,
  HiOutlineLocationMarker,
  HiOutlineClock,
  HiOutlineUser,
  HiOutlineArrowRight,
  HiOutlineRefresh,
} from 'react-icons/hi';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { getDriverProfile, getDriverMyBookings } from '../../api/drivers';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import './DriverDashboard.css';

export default function DriverDashboardPage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    try {
      const [profileData, tripsData] = await Promise.all([
        getDriverProfile(),
        getDriverMyBookings(),
      ]);
      setProfile(profileData);
      setTrips(tripsData);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load driver dashboard data');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    fetchData();
  };

  if (loading) {
    return <LoadingSpinner message="Loading driver dashboard..." />;
  }

  // Active trip (Status = ACTIVE)
  const activeTrip = trips.find((t) => t.status === 'ACTIVE');

  // Upcoming trips (Status = CONFIRMED or PENDING, pickup in future)
  const upcomingTrips = trips.filter(
    (t) => t.status === 'CONFIRMED' || t.status === 'PENDING'
  );

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="driver-dashboard" id="driver-dashboard-page">
      {/* ===== HERO WELCOME BANNER ===== */}
      <div className="driver-hero-banner">
        <div className="hero-text">
          <div className="hero-badge">
            <span className="live-dot" /> Driver Duty Hub
          </div>
          <h1 className="hero-title">
            Welcome back, {user?.firstName || 'Driver'}!
          </h1>
          <p className="hero-sub">
            Here is your live assignment status and scheduled trip dispatch overview.
          </p>
        </div>
        <div className="hero-actions">
          <button
            className="driver-btn driver-btn-refresh"
            onClick={handleRefresh}
            disabled={refreshing}
            id="driver-refresh-btn"
          >
            <HiOutlineRefresh className={refreshing ? 'spinning' : ''} />
            <span>{refreshing ? 'Updating...' : 'Refresh Schedule'}</span>
          </button>
          <Link to="/driver/trips" className="driver-btn driver-btn-primary" id="driver-view-all-trips-btn">
            <span>View All Trips</span>
            <HiOutlineArrowRight />
          </Link>
        </div>
      </div>

      {/* ===== KPI METRICS GRID ===== */}
      <div className="driver-kpi-grid">
        <div className="kpi-card kpi-card-active">
          <div className="kpi-icon-wrap active-icon">
            <HiOutlineTruck />
          </div>
          <div className="kpi-info">
            <span className="kpi-label">Current Trip Status</span>
            <h3 className="kpi-val">{activeTrip ? 'On Duty' : 'Available'}</h3>
            <span className="kpi-hint">
              {activeTrip ? `Ride #${activeTrip.bookingId} in progress` : 'Ready for upcoming dispatch'}
            </span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon-wrap upcoming-icon">
            <HiOutlineCalendar />
          </div>
          <div className="kpi-info">
            <span className="kpi-label">Upcoming Trips</span>
            <h3 className="kpi-val">{upcomingTrips.length}</h3>
            <span className="kpi-hint">Scheduled assignments</span>
          </div>
        </div>

        <div className="kpi-card">
          <div className="kpi-icon-wrap completed-icon">
            <HiOutlineCheckCircle />
          </div>
          <div className="kpi-info">
            <span className="kpi-label">Completed Rides</span>
            <h3 className="kpi-val">{profile?.totalCompletedTrips ?? 0}</h3>
            <span className="kpi-hint">Lifetime successful trips</span>
          </div>
        </div>

        <div className="kpi-card kpi-card-guard">
          <div className="kpi-icon-wrap guard-icon">
            <HiOutlineShieldCheck />
          </div>
          <div className="kpi-info">
            <span className="kpi-label">Double-Booking Guard</span>
            <h3 className="kpi-val text-emerald">Active</h3>
            <span className="kpi-hint">Interval overlap protection on</span>
          </div>
        </div>
      </div>

      {/* ===== ACTIVE TRIP SPOTLIGHT ===== */}
      <div className="dashboard-section">
        <div className="section-header">
          <div>
            <h2 className="section-title">Active Ride Spotlight</h2>
            <p className="section-subtitle">Real-time status of your in-progress assignment</p>
          </div>
        </div>

        {activeTrip ? (
          <div className="active-trip-spotlight-card" id="active-trip-spotlight">
            <div className="spotlight-top">
              <div className="spotlight-id">
                <span className="tag-booking">Booking #{activeTrip.bookingId}</span>
                <StatusBadge status={activeTrip.status} />
              </div>
              <div className="spotlight-vehicle">
                <HiOutlineTruck className="spotlight-veh-icon" />
                <div>
                  <h4>{activeTrip.vehicleModel || 'Vehicle'}</h4>
                  <span>Plate: {activeTrip.vehiclePlate || 'N/A'}</span>
                </div>
              </div>
            </div>

            <div className="spotlight-route-grid">
              <div className="route-point">
                <div className="point-icon pickup-dot"><HiOutlineLocationMarker /></div>
                <div>
                  <span className="point-label">Pickup Location</span>
                  <p className="point-val">{activeTrip.pickupLocation}</p>
                  <span className="point-time"><HiOutlineClock /> {formatDate(activeTrip.pickupDateTime)}</span>
                </div>
              </div>

              <div className="route-divider">
                <div className="route-line" />
                <span className="route-arrow">➔</span>
              </div>

              <div className="route-point">
                <div className="point-icon return-dot"><HiOutlineLocationMarker /></div>
                <div>
                  <span className="point-label">Return Destination</span>
                  <p className="point-val">{activeTrip.returnLocation}</p>
                  <span className="point-time"><HiOutlineClock /> {formatDate(activeTrip.returnDateTime)}</span>
                </div>
              </div>
            </div>

            <div className="spotlight-footer">
              <div className="customer-chip">
                <HiOutlineUser />
                <div>
                  <span className="cust-title">Customer</span>
                  <strong>{activeTrip.customerName}</strong>
                </div>
              </div>
              {activeTrip.customerEmail && (
                <a
                  href={`mailto:${activeTrip.customerEmail}`}
                  className="driver-btn driver-btn-outline"
                >
                  Contact Customer
                </a>
              )}
            </div>
          </div>
        ) : (
          <div className="no-active-trip-card">
            <div className="no-active-icon">🟢</div>
            <div>
              <h3>No Active Trip Currently In Progress</h3>
              <p>
                You are currently free and available. When a scheduled booking starts or is assigned by dispatch, details will appear here.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* ===== UPCOMING ASSIGNMENTS SECTION ===== */}
      <div className="dashboard-section">
        <div className="section-header">
          <div>
            <h2 className="section-title">Next Scheduled Assignments</h2>
            <p className="section-subtitle">Your upcoming booked trips verified conflict-free</p>
          </div>
          <Link to="/driver/trips" className="section-link">View all ({trips.length})</Link>
        </div>

        {upcomingTrips.length > 0 ? (
          <div className="upcoming-cards-grid">
            {upcomingTrips.slice(0, 3).map((trip) => (
              <div key={trip.bookingId} className="upcoming-trip-card">
                <div className="upcoming-card-header">
                  <span className="trip-badge">Trip #{trip.bookingId}</span>
                  <StatusBadge status={trip.status} />
                </div>

                <div className="upcoming-vehicle-info">
                  <HiOutlineTruck />
                  <span><strong>{trip.vehicleModel || 'Vehicle'}</strong> ({trip.vehiclePlate || 'N/A'})</span>
                </div>

                <div className="upcoming-route">
                  <div className="route-row">
                    <span className="route-pin from">From:</span>
                    <span className="route-text">{trip.pickupLocation}</span>
                  </div>
                  <div className="route-row">
                    <span className="route-pin to">To:</span>
                    <span className="route-text">{trip.returnLocation}</span>
                  </div>
                </div>

                <div className="upcoming-date-footer">
                  <HiOutlineClock />
                  <span>Pickup: {formatDate(trip.pickupDateTime)}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-trips-box">
            <p>No upcoming trips scheduled at the moment.</p>
          </div>
        )}
      </div>
    </div>
  );
}
