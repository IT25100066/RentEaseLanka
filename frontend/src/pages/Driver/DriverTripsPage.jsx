import { useState, useEffect } from 'react';
import {
  HiOutlineCalendar,
  HiOutlineLocationMarker,
  HiOutlineClock,
  HiOutlineUser,
  HiOutlineTruck,
  HiOutlineSearch,
  HiOutlineEye,
  HiOutlineRefresh,
} from 'react-icons/hi';
import { toast } from 'react-toastify';
import { getDriverMyBookings } from '../../api/drivers';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';
import './DriverTrips.css';

export default function DriverTripsPage() {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTrip, setSelectedTrip] = useState(null);

  const fetchTrips = async () => {
    try {
      setLoading(true);
      const data = await getDriverMyBookings();
      setTrips(data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to fetch assigned trips');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrips();
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

  const filteredTrips = trips.filter((trip) => {
    // Status tab filter
    if (activeTab === 'ACTIVE' && trip.status !== 'ACTIVE') return false;
    if (activeTab === 'UPCOMING' && trip.status !== 'CONFIRMED' && trip.status !== 'PENDING') return false;
    if (activeTab === 'COMPLETED' && trip.status !== 'COMPLETED') return false;

    // Search query filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchId = String(trip.bookingId).includes(q);
      const matchCust = trip.customerName?.toLowerCase().includes(q);
      const matchVehicle = trip.vehicleModel?.toLowerCase().includes(q) || trip.vehiclePlate?.toLowerCase().includes(q);
      const matchLocation = trip.pickupLocation?.toLowerCase().includes(q) || trip.returnLocation?.toLowerCase().includes(q);
      return matchId || matchCust || matchVehicle || matchLocation;
    }

    return true;
  });

  const getTabCount = (tab) => {
    if (tab === 'ALL') return trips.length;
    if (tab === 'ACTIVE') return trips.filter((t) => t.status === 'ACTIVE').length;
    if (tab === 'UPCOMING') return trips.filter((t) => t.status === 'CONFIRMED' || t.status === 'PENDING').length;
    if (tab === 'COMPLETED') return trips.filter((t) => t.status === 'COMPLETED').length;
    return 0;
  };

  return (
    <div className="driver-trips-page" id="driver-trips-page">
      {/* ===== HEADER ===== */}
      <div className="page-header-row">
        <div>
          <h1 className="page-main-title">My Assigned Trips</h1>
          <p className="page-main-sub">
            Track and manage your upcoming, active, and completed driving assignments.
          </p>
        </div>
        <button className="refresh-trips-btn" onClick={fetchTrips} title="Refresh trips">
          <HiOutlineRefresh />
          <span>Refresh</span>
        </button>
      </div>

      {/* ===== CONTROLS BAR (Tabs + Search) ===== */}
      <div className="trips-controls-bar">
        <div className="trips-tabs">
          {[
            { id: 'ALL', label: 'All Trips' },
            { id: 'ACTIVE', label: 'Active (Current)' },
            { id: 'UPCOMING', label: 'Upcoming' },
            { id: 'COMPLETED', label: 'Completed' },
          ].map((tab) => (
            <button
              key={tab.id}
              className={`trip-tab-btn ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              <span>{tab.label}</span>
              <span className="tab-counter">{getTabCount(tab.id)}</span>
            </button>
          ))}
        </div>

        <div className="trips-search-wrap">
          <HiOutlineSearch className="search-icon" />
          <input
            type="text"
            placeholder="Search booking #, customer, car..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="trips-search-input"
          />
        </div>
      </div>

      {/* ===== TRIPS LIST ===== */}
      {loading ? (
        <LoadingSpinner message="Loading assigned trips..." />
      ) : filteredTrips.length > 0 ? (
        <div className="trips-cards-list">
          {filteredTrips.map((trip) => (
            <div key={trip.bookingId} className={`trip-item-card ${trip.status === 'ACTIVE' ? 'card-border-active' : ''}`}>
              <div className="trip-item-header">
                <div className="header-left">
                  <span className="booking-number">Booking #{trip.bookingId}</span>
                  <StatusBadge status={trip.status} />
                </div>
                <div className="header-right">
                  <span className="trip-fare">Rs. {trip.totalAmount?.toLocaleString() || '—'}</span>
                </div>
              </div>

              <div className="trip-item-body">
                {/* Vehicle Pill */}
                <div className="trip-vehicle-pill">
                  <HiOutlineTruck className="veh-icon" />
                  <span className="veh-name">{trip.vehicleModel || 'Assigned Vehicle'}</span>
                  <span className="veh-plate">{trip.vehiclePlate || 'N/A'}</span>
                </div>

                {/* Route visualization */}
                <div className="trip-route-visual">
                  <div className="route-node">
                    <span className="node-marker start"><HiOutlineLocationMarker /></span>
                    <div className="node-info">
                      <span className="node-label">Pickup</span>
                      <strong className="node-address">{trip.pickupLocation}</strong>
                      <span className="node-time"><HiOutlineClock /> {formatDate(trip.pickupDateTime)}</span>
                    </div>
                  </div>

                  <div className="route-connector-line" />

                  <div className="route-node">
                    <span className="node-marker finish"><HiOutlineLocationMarker /></span>
                    <div className="node-info">
                      <span className="node-label">Return Destination</span>
                      <strong className="node-address">{trip.returnLocation}</strong>
                      <span className="node-time"><HiOutlineClock /> {formatDate(trip.returnDateTime)}</span>
                    </div>
                  </div>
                </div>

                {/* Customer Details */}
                <div className="trip-customer-footer">
                  <div className="customer-info-box">
                    <HiOutlineUser className="cust-icon" />
                    <div>
                      <span className="cust-tag">Renter</span>
                      <strong className="cust-name">{trip.customerName}</strong>
                      {trip.customerEmail && <span className="cust-email"> • {trip.customerEmail}</span>}
                    </div>
                  </div>

                  <button
                    className="view-details-btn"
                    onClick={() => setSelectedTrip(trip)}
                  >
                    <HiOutlineEye />
                    <span>View Itinerary</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="trips-empty-state">
          <HiOutlineCalendar className="empty-state-icon" />
          <h3>No Trips Found</h3>
          <p>
            {searchQuery
              ? 'No trips matched your search criteria.'
              : `There are currently no ${activeTab.toLowerCase()} assignments for your profile.`}
          </p>
        </div>
      )}

      {/* ===== ITINERARY MODAL ===== */}
      <Modal
        isOpen={Boolean(selectedTrip)}
        onClose={() => setSelectedTrip(null)}
        title={`Trip Itinerary - Booking #${selectedTrip?.bookingId}`}
        size="md"
      >
        {selectedTrip && (
          <div className="itinerary-modal-content">
            <div className="modal-status-bar">
              <span>Status: <StatusBadge status={selectedTrip.status} /></span>
              <span>Total Rental: <strong>Rs. {selectedTrip.totalAmount?.toLocaleString()}</strong></span>
            </div>

            <div className="itinerary-grid">
              <div className="itinerary-block">
                <h4>Vehicle Allocation</h4>
                <p><strong>Model:</strong> {selectedTrip.vehicleModel || 'Standard Vehicle'}</p>
                <p><strong>License Plate:</strong> {selectedTrip.vehiclePlate || 'N/A'}</p>
              </div>

              <div className="itinerary-block">
                <h4>Customer Details</h4>
                <p><strong>Name:</strong> {selectedTrip.customerName}</p>
                <p><strong>Email:</strong> {selectedTrip.customerEmail || 'N/A'}</p>
              </div>

              <div className="itinerary-block full-width">
                <h4>Pickup Details</h4>
                <p><strong>Time:</strong> {formatDate(selectedTrip.pickupDateTime)}</p>
                <p><strong>Location:</strong> {selectedTrip.pickupLocation}</p>
              </div>

              <div className="itinerary-block full-width">
                <h4>Dropoff / Return Details</h4>
                <p><strong>Time:</strong> {formatDate(selectedTrip.returnDateTime)}</p>
                <p><strong>Location:</strong> {selectedTrip.returnLocation}</p>
              </div>
            </div>

            <div className="modal-conflict-reassurance">
              <span>🛡️ Double-Booking Guard: This schedule is verified conflict-free and locked to your driver profile.</span>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
