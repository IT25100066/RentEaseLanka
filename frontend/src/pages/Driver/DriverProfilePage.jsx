import { useState, useEffect } from 'react';
import {
  HiOutlineUser,
  HiOutlineMail,
  HiOutlineShieldCheck,
  HiOutlineCheckCircle,
  HiOutlineCalendar,
  HiOutlineTruck,
  HiOutlineLockClosed,
  HiOutlineClock,
  HiOutlineLocationMarker,
} from 'react-icons/hi';
import { toast } from 'react-toastify';
import { getDriverProfile, getDriverMyBookings } from '../../api/drivers';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import './DriverProfile.css';

export default function DriverProfilePage() {
  const [profile, setProfile] = useState(null);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadProfileData = async () => {
      try {
        const [profData, tripsData] = await Promise.all([
          getDriverProfile(),
          getDriverMyBookings(),
        ]);
        setProfile(profData);
        setTrips(tripsData);
      } catch (err) {
        toast.error(err.response?.data?.message || 'Failed to load profile details');
      } finally {
        setLoading(false);
      }
    };
    loadProfileData();
  }, []);

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

  if (loading) {
    return <LoadingSpinner message="Loading driver profile & schedule..." />;
  }

  // Active or upcoming trips
  const scheduledTrips = trips.filter(
    (t) => t.status === 'ACTIVE' || t.status === 'CONFIRMED' || t.status === 'PENDING'
  );

  return (
    <div className="driver-profile-page" id="driver-profile-page">
      {/* ===== HEADER ===== */}
      <div className="profile-header">
        <div>
          <h1 className="profile-title">Driver Profile & Schedule</h1>
          <p className="profile-sub">
            Review your driver credentials, account status, and duty timeline.
          </p>
        </div>
      </div>

      <div className="profile-grid">
        {/* ===== LEFT: PROFILE CARD ===== */}
        <div className="profile-card">
          <div className="profile-avatar-wrap">
            <div className="profile-avatar">
              <HiOutlineUser />
            </div>
            <div className="profile-name-info">
              <h2>{profile?.firstName} {profile?.lastName}</h2>
              <span className="profile-role-tag">Official RentEase Driver</span>
            </div>
          </div>

          <div className="profile-details-list">
            <div className="profile-detail-row">
              <HiOutlineMail className="detail-icon" />
              <div>
                <span className="detail-label">Email Address</span>
                <span className="detail-val">{profile?.email}</span>
              </div>
            </div>

            <div className="profile-detail-row">
              <HiOutlineShieldCheck className="detail-icon verified-icon" />
              <div>
                <span className="detail-label">Verification Status</span>
                <span className="detail-val badge-verified">
                  {profile?.verificationStatus || 'VERIFIED'}
                </span>
              </div>
            </div>

            <div className="profile-detail-row">
              <HiOutlineLockClosed className="detail-icon" />
              <div>
                <span className="detail-label">Account Role</span>
                <span className="detail-val">{profile?.role || 'DRIVER'}</span>
              </div>
            </div>
          </div>

          <div className="profile-stats-row">
            <div className="stat-box">
              <span className="stat-number">{profile?.totalCompletedTrips ?? 0}</span>
              <span className="stat-name">Completed</span>
            </div>
            <div className="stat-box">
              <span className="stat-number">{profile?.upcomingTripsCount ?? 0}</span>
              <span className="stat-name">Upcoming</span>
            </div>
            <div className="stat-box">
              <span className="stat-number">{trips.length}</span>
              <span className="stat-name">Total Assigned</span>
            </div>
          </div>
        </div>

        {/* ===== RIGHT: DOUBLE BOOKING PROTECTION CARD ===== */}
        <div className="guard-info-card">
          <div className="guard-header">
            <div className="guard-badge">
              <HiOutlineShieldCheck />
              <span>Real-Time Double Booking Prevention</span>
            </div>
            <span className="guard-status-chip">Engine Active</span>
          </div>

          <p className="guard-desc">
            The RentEase Lanka backend enforces strict interval validation to ensure you are never double-booked.
          </p>

          <div className="guard-features-list">
            <div className="guard-feature-item">
              <HiOutlineCheckCircle className="check-icon" />
              <div>
                <strong>Overlapping Hours Blocked</strong>
                <p>Dispatchers cannot assign you to trips that conflict with your booked intervals.</p>
              </div>
            </div>

            <div className="guard-feature-item">
              <HiOutlineCheckCircle className="check-icon" />
              <div>
                <strong>Instant Release on Cancellation</strong>
                <p>If a customer cancels or completes a ride, your schedule is immediately freed for new bookings.</p>
              </div>
            </div>

            <div className="guard-feature-item">
              <HiOutlineCheckCircle className="check-icon" />
              <div>
                <strong>Direct Customer Transparency</strong>
                <p>Your vehicle model and assigned trips are linked directly to your authenticated profile.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ===== DUTY SCHEDULE TIMELINE ===== */}
      <div className="schedule-timeline-section">
        <div className="section-title-wrap">
          <HiOutlineCalendar className="section-icon" />
          <div>
            <h3>Your Duty Schedule Timeline</h3>
            <p>Chronological display of your scheduled bookings and available intervals</p>
          </div>
        </div>

        {scheduledTrips.length > 0 ? (
          <div className="timeline-container">
            {scheduledTrips.map((trip, idx) => (
              <div key={trip.bookingId} className="timeline-event">
                <div className="timeline-marker">
                  <div className={`timeline-dot ${trip.status === 'ACTIVE' ? 'active-dot' : ''}`} />
                  {idx < scheduledTrips.length - 1 && <div className="timeline-connector" />}
                </div>

                <div className="timeline-card">
                  <div className="timeline-card-head">
                    <span className="timeline-trip-id">Trip #{trip.bookingId}</span>
                    <StatusBadge status={trip.status} />
                  </div>

                  <div className="timeline-time-span">
                    <div className="time-block">
                      <span className="time-label">Pickup</span>
                      <strong>{formatDate(trip.pickupDateTime)}</strong>
                      <span className="time-loc"><HiOutlineLocationMarker /> {trip.pickupLocation}</span>
                    </div>

                    <div className="timeline-arrow-sep">➔</div>

                    <div className="time-block">
                      <span className="time-label">Return</span>
                      <strong>{formatDate(trip.returnDateTime)}</strong>
                      <span className="time-loc"><HiOutlineLocationMarker /> {trip.returnLocation}</span>
                    </div>
                  </div>

                  <div className="timeline-veh-pill">
                    <HiOutlineTruck />
                    <span>{trip.vehicleModel || 'Vehicle'} ({trip.vehiclePlate || 'N/A'})</span>
                    <span className="customer-tag">Renter: {trip.customerName}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="timeline-empty-box">
            <HiOutlineCalendar className="empty-icon" />
            <h4>No Active or Upcoming Duty Bookings</h4>
            <p>You have no current overlapping commitments. You are fully available for assignments.</p>
          </div>
        )}
      </div>
    </div>
  );
}
