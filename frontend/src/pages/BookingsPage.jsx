import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import { toast } from 'react-toastify';
import {
  HiOutlineCheckCircle,
  HiOutlinePlay,
  HiOutlineSearch,
  HiOutlineStop,
  HiOutlineUserAdd,
  HiOutlineXCircle,
  HiOutlineClipboardCheck,
} from 'react-icons/hi';
import StatusBadge from '../components/common/StatusBadge';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Modal from '../components/common/Modal';
import { getBookingById, getBookingsByCustomerId } from '../api/bookings';
import { assignDriver, getAvailableDrivers } from '../api/drivers';
import './BookingsPage.css';

const formatBookingDate = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

export const getBookingId = (b) => b?.bookingId || b?.BookingId || b?.id;

export default function BookingsPage() {
  const { role } = useAuth();
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState(null);
  const [bookingSearch, setBookingSearch] = useState('');
  const [searchedBooking, setSearchedBooking] = useState(null);
  const [searchingBooking, setSearchingBooking] = useState(false);
  const [customerSearch, setCustomerSearch] = useState('');
  const [searchedCustomerBookings, setSearchedCustomerBookings] = useState(null);
  const [searchedCustomerId, setSearchedCustomerId] = useState(null);
  const [searchingCustomer, setSearchingCustomer] = useState(false);
  const [assignmentBooking, setAssignmentBooking] = useState(null);
  const [availableDrivers, setAvailableDrivers] = useState([]);
  const [selectedDriverId, setSelectedDriverId] = useState('');
  const [loadingDrivers, setLoadingDrivers] = useState(false);
  const [assigningDriver, setAssigningDriver] = useState(false);

  const canSearchBookings = role === 'ADMIN' || role === 'MANAGER';
  const canSearchCustomerBookings = role === 'STAFF';
  const canAssignDrivers = role === 'STAFF';

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const res = await API.get('/booking/all');
      // Sort bookings by ID descending
      const sortedBookings = (res.data || []).sort((a, b) => (getBookingId(b) || 0) - (getBookingId(a) || 0));
      setBookings(sortedBookings);
    } catch (err) {
      console.error('Failed to load bookings', err);
      toast.error('Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleBookingSearch = async (event) => {
    event.preventDefault();
    const bookingId = bookingSearch.trim();

    if (!bookingId || !/^\d+$/.test(bookingId)) {
      toast.error('Enter a valid booking ID');
      return;
    }

    setSearchingBooking(true);
    try {
      const booking = await getBookingById(bookingId);
      setSearchedBooking(booking);
    } catch (err) {
      setSearchedBooking(null);
      toast.error(err.response?.data || 'Booking not found');
    } finally {
      setSearchingBooking(false);
    }
  };

  const handleCustomerSearch = async (event) => {
    event.preventDefault();
    const customerId = customerSearch.trim();

    if (!customerId || !/^\d+$/.test(customerId)) {
      toast.error('Enter a valid customer ID');
      return;
    }

    setSearchingCustomer(true);
    try {
      const customerBookings = await getBookingsByCustomerId(customerId);
      setSearchedCustomerId(customerId);
      setSearchedCustomerBookings(customerBookings);
    } catch (err) {
      setSearchedCustomerId(null);
      setSearchedCustomerBookings(null);
      toast.error(err.response?.data || 'Customer not found');
    } finally {
      setSearchingCustomer(false);
    }
  };

  const handleStatusChange = async (id, action, booking = null) => {
    setProcessingId(id);
    try {
      await API.post(`/booking/${id}/${action}`);
      toast.success(`Booking ${action}ed successfully`);
      if (action === 'confirm' && booking) {
        const handoverPath = role === 'ADMIN' ? '/admin/handovers'
          : role === 'MANAGER' ? '/manager/handovers' : '/staff/handovers';
        const params = new URLSearchParams({ bookingId: String(id) });
        if (booking.vehicleId != null) params.set('vehicleId', String(booking.vehicleId));
        if (booking.customerId != null) params.set('customerId', String(booking.customerId));
        if (booking.customerName) params.set('customerName', booking.customerName);
        navigate(`${handoverPath}?${params.toString()}`);
        return;
      }
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data || `Failed to ${action} booking`);
    } finally {
      setProcessingId(null);
    }
  };

  const openAssignment = async (booking) => {
    setAssignmentBooking(booking);
    setSelectedDriverId(booking.driverId ? String(booking.driverId) : '');
    setLoadingDrivers(true);
    try {
      const drivers = await getAvailableDrivers(booking.pickupDateTime, booking.returnDateTime, getBookingId(booking));
      setAvailableDrivers(drivers);
    } catch (err) {
      setAvailableDrivers([]);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to load available drivers');
    } finally {
      setLoadingDrivers(false);
    }
  };

  const submitAssignment = async (event) => {
    event.preventDefault();
    if (!assignmentBooking || !selectedDriverId) {
      toast.error('Select a driver first');
      return;
    }
    setAssigningDriver(true);
    try {
      await assignDriver(getBookingId(assignmentBooking), Number(selectedDriverId));
      toast.success('Driver assigned successfully');
      setAssignmentBooking(null);
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to assign driver');
    } finally {
      setAssigningDriver(false);
    }
  };

  if (loading && bookings.length === 0) return <LoadingSpinner message="Loading bookings..." />;

  return (
    <div className="page-container" id="bookings-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Manage Bookings</h1>
          <p className="page-subtitle">View and update customer reservations and rentals.</p>
        </div>
      </div>

      {canSearchBookings && (
        <form className="booking-search" onSubmit={handleBookingSearch}>
          <HiOutlineSearch className="booking-search-icon" aria-hidden="true" />
          <input
            type="search"
            value={bookingSearch}
            onChange={(event) => setBookingSearch(event.target.value)}
            placeholder="Search by booking ID..."
            aria-label="Search by booking ID"
            id="booking-details-search"
          />
          <button type="submit" disabled={searchingBooking}>
            {searchingBooking ? 'Searching...' : 'View Details'}
          </button>
        </form>
      )}

      {canSearchCustomerBookings && (
        <form className="booking-search staff-customer-search" onSubmit={handleCustomerSearch}>
          <HiOutlineSearch className="booking-search-icon" aria-hidden="true" />
          <input
            type="search"
            value={customerSearch}
            onChange={(event) => setCustomerSearch(event.target.value)}
            placeholder="Search bookings by customer ID..."
            aria-label="Search bookings by customer ID"
            id="customer-bookings-search"
          />
          <button type="submit" disabled={searchingCustomer}>
            {searchingCustomer ? 'Searching...' : 'Find Bookings'}
          </button>
        </form>
      )}

      <div className="table-container">
        <table className="data-table" id="bookings-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Vehicle Model</th>
              <th>Dates</th>
              <th>Locations</th>
              <th>Total (LKR)</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {bookings.filter(booking => booking.status === 'PENDING' || booking.status === 'CONFIRMED' || booking.status === 'ACTIVE').map((booking) => {
              const bId = getBookingId(booking);
              return (
              <tr key={bId}>
                <td><strong>#{bId}</strong></td>
                <td>
                  <div className="table-vehicle-cell">
                    <span className="table-vehicle-model">{booking.model || `Vehicle ID: ${booking.vehicleId}`}</span>
                  </div>
                </td>
                <td className="date-cell" style={{ fontSize: '0.85rem' }}>
                  <div><strong>Pick:</strong> {new Date(booking.pickupDateTime).toLocaleString()}</div>
                  <div><strong>Ret:</strong> {new Date(booking.returnDateTime).toLocaleString()}</div>
                </td>
                <td className="location-cell" style={{ fontSize: '0.85rem' }}>
                  <div><strong>From:</strong> {booking.pickupLocation}</div>
                  <div><strong>To:</strong> {booking.returnLocation}</div>
                  {booking.withDriver && <span className="driver-badge" style={{ display: 'inline-block', marginTop: '4px', padding: '2px 6px', background: '#eef6f1', color: '#1D4533', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>With Driver</span>}
                  {booking.withDriver && <div style={{ marginTop: '4px', fontSize: '0.75rem' }}>Driver: {booking.driverName || 'Unassigned'}</div>}
                </td>
                <td className="rate-cell">{booking.totalAmount?.toLocaleString()}</td>
                <td><StatusBadge status={booking.status} /></td>
                <td>
                  <div className="action-buttons">
                    {canAssignDrivers && booking.withDriver && (booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                      <button
                        className="btn-icon btn-approve"
                        title={booking.driverId ? 'Reassign Driver' : 'Assign Driver'}
                        onClick={() => openAssignment(booking)}
                        disabled={processingId === bId}
                      >
                        <HiOutlineUserAdd />
                      </button>
                    )}
                    {booking.status === 'PENDING' && (
                      <>
                        <button
                          className="btn-icon btn-approve"
                          title="Confirm Booking (Customer must place deposit hold)"
                          onClick={() => handleStatusChange(bId, 'confirm', booking)}
                          disabled={processingId === bId}
                        >
                          <HiOutlineCheckCircle />
                        </button>
                        <button
                          className="btn-icon btn-reject"
                          title="Cancel Booking"
                          onClick={() => handleStatusChange(bId, 'cancel')}
                          disabled={processingId === bId}
                        >
                          <HiOutlineXCircle />
                        </button>
                      </>
                    )}
                    {booking.status === 'CONFIRMED' && (
                      <>
                        <button
                          className="btn-icon btn-activate"
                          title="Activate Booking (Start Rental / Complete Handover)"
                          onClick={() => handleStatusChange(bId, 'activate')}
                          disabled={processingId === bId}
                        >
                          <HiOutlinePlay />
                        </button>
                        <button
                          className="btn-icon btn-reject"
                          title="Cancel Booking"
                          onClick={() => handleStatusChange(bId, 'cancel')}
                          disabled={processingId === bId}
                        >
                          <HiOutlineXCircle />
                        </button>
                      </>
                    )}
                    {booking.status === 'ACTIVE' && (
                      <>
                        <button
                          className="btn-icon btn-complete"
                          title="Complete Booking (Requires completed return inspection & fully paid invoice)"
                          onClick={() => handleStatusChange(bId, 'complete')}
                          disabled={processingId === bId}
                        >
                          <HiOutlineStop />
                        </button>
                        <button
                          className="btn-icon btn-approve"
                          title="Open Return Inspection"
                          onClick={() => {
                            const returnPath = role === 'ADMIN' ? '/admin/returns'
                              : role === 'MANAGER' ? '/manager/returns' : '/staff/returns';
                            navigate(returnPath);
                          }}
                        >
                          <HiOutlineClipboardCheck />
                        </button>
                      </>
                    )}
                    {(booking.status === 'COMPLETED' || booking.status === 'CANCELLED') && (
                      <span className="no-actions">—</span>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
            {bookings.length === 0 && (
              <tr>
                <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                  No bookings found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={Boolean(searchedBooking)}
        onClose={() => setSearchedBooking(null)}
        title={`Booking #${searchedBooking?.BookingId}`}
        size="md"
      >
        {searchedBooking && (
          <div className="booking-details-grid">
            <div><span>Customer</span><strong>{searchedBooking.customerName || '—'}</strong></div>
            <div><span>Vehicle</span><strong>{searchedBooking.model || `Vehicle ID: ${searchedBooking.vehicleId}`}</strong></div>
            <div><span>Pickup</span><strong>{new Date(searchedBooking.pickupDateTime).toLocaleString()}</strong></div>
            <div><span>Return</span><strong>{new Date(searchedBooking.returnDateTime).toLocaleString()}</strong></div>
            <div><span>Pickup location</span><strong>{searchedBooking.pickupLocation || '—'}</strong></div>
            <div><span>Return location</span><strong>{searchedBooking.returnLocation || '—'}</strong></div>
            <div><span>Total</span><strong>Rs. {searchedBooking.totalAmount?.toLocaleString() || '—'}</strong></div>
            <div><span>Status</span><StatusBadge status={searchedBooking.status} /></div>
          </div>
        )}
      </Modal>

      <Modal
        isOpen={Boolean(assignmentBooking)}
        onClose={() => setAssignmentBooking(null)}
        title={assignmentBooking?.driverId ? `Reassign driver for booking #${getBookingId(assignmentBooking)}` : `Assign driver to booking #${getBookingId(assignmentBooking)}`}
        size="sm"
      >
        <form onSubmit={submitAssignment} className="create-form">
          <p>{assignmentBooking && `${formatBookingDate(assignmentBooking.pickupDateTime)} to ${formatBookingDate(assignmentBooking.returnDateTime)}`}</p>
          {loadingDrivers ? <LoadingSpinner message="Checking driver availability..." /> : (
            <div className="form-group">
              <label htmlFor="booking-driver-select">Available driver</label>
              <select id="booking-driver-select" value={selectedDriverId} onChange={(event) => setSelectedDriverId(event.target.value)} required>
                <option value="">Select a driver</option>
                {availableDrivers.map((driver) => (
                  <option key={driver.id} value={driver.id}>{driver.firstName} {driver.lastName} ({driver.email})</option>
                ))}
              </select>
              {!availableDrivers.length && <p>No drivers are available for this booking interval.</p>}
            </div>
          )}
          <button type="submit" className="create-form-btn" disabled={loadingDrivers || assigningDriver || !availableDrivers.length}>
            {assigningDriver ? 'Assigning...' : 'Assign Driver'}
          </button>
        </form>
      </Modal>

      <Modal
        isOpen={searchedCustomerBookings !== null}
        onClose={() => {
          setSearchedCustomerBookings(null);
          setSearchedCustomerId(null);
        }}
        title={`Customer #${searchedCustomerId} bookings`}
        size="lg"
      >
        {searchedCustomerBookings?.length ? (
          <div className="customer-booking-results-table-container">
            <table className="data-table customer-booking-results-table">
              <thead>
                <tr>
                  <th>Booking</th>
                  <th>Vehicle</th>
                  <th>Pickup</th>
                  <th>Return</th>
                  <th>Total</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {searchedCustomerBookings.map((booking) => {
                  const bId = getBookingId(booking);
                  return (
                  <tr key={bId}>
                    <td><strong>#{bId}</strong></td>
                    <td>{booking.model || `Vehicle ID: ${booking.vehicleId}`}</td>
                    <td>{formatBookingDate(booking.pickupDateTime)}</td>
                    <td>{formatBookingDate(booking.returnDateTime)}</td>
                    <td>Rs. {booking.totalAmount?.toLocaleString() || '—'}</td>
                    <td><StatusBadge status={booking.status} /></td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="customer-booking-results-empty">
            <p>No bookings found for this customer.</p>
          </div>
        )}
      </Modal>
    </div>
  );
}
