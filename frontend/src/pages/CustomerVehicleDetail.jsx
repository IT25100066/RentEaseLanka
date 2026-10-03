import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import API, { BASE_URL } from '../api/axiosInstance';
import {
  HiOutlineArrowLeft,
  HiOutlineCalendar,
  HiOutlineLocationMarker,
  HiOutlineTruck,
  HiOutlineShieldCheck,
  HiOutlineSupport,
  HiOutlineStar,
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
} from 'react-icons/hi';
import { toast } from 'react-toastify';
import './CustomerVehicleDetail.css';

export default function CustomerVehicleDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, user, role } = useAuth();
  const [vehicle, setVehicle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeImage, setActiveImage] = useState(0);

  // Booking Form State
  const [showBookingForm, setShowBookingForm] = useState(false);
  const [pickupDate, setPickupDate] = useState('');
  const [returnDate, setReturnDate] = useState('');
  const [pickupLocation, setPickupLocation] = useState('');
  const [returnLocation, setReturnLocation] = useState('');
  const [withDriver, setWithDriver] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [totalAmount, setTotalAmount] = useState(0);
  const [calculatingPrice, setCalculatingPrice] = useState(false);

  const handleCalculatePrice = async () => {
    if (!pickupDate || !returnDate || !vehicle?.id) {
      toast.error('Please select pickup and return dates');
      setTotalAmount(0);
      return;
    }
    
    const start = new Date(pickupDate);
    const end = new Date(returnDate);
    if (end <= start) {
      toast.error('Return date must be after pickup date');
      setTotalAmount(0);
      return;
    }
    
    setCalculatingPrice(true);
    try {
      const formattedPickup = pickupDate.length === 16 ? `${pickupDate}:00` : pickupDate;
      const formattedReturn = returnDate.length === 16 ? `${returnDate}:00` : returnDate;
      
      const payload = {
        vehicleId: vehicle.id,
        pickupDateTime: formattedPickup,
        returnDateTime: formattedReturn,
        withDriver: withDriver
      };
      
      const res = await API.post('/booking/calculate-price', payload);
      setTotalAmount(res.data);
    } catch (err) {
      console.error("Error calculating price", err);
      toast.error(err.response?.data || 'Failed to calculate price');
      setTotalAmount(0);
    } finally {
      setCalculatingPrice(false);
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      toast.info('Please sign in to complete your booking.');
      navigate('/signin');
      return;
    }

    if (!pickupDate || !returnDate || !pickupLocation || !returnLocation) {
      toast.error('Please fill all required fields');
      return;
    }

    if (totalAmount <= 0) {
      toast.error('Invalid dates selected or price could not be calculated');
      return;
    }

    setIsSubmitting(true);
    try {
      const formattedPickup = pickupDate.length === 16 ? `${pickupDate}:00` : pickupDate;
      const formattedReturn = returnDate.length === 16 ? `${returnDate}:00` : returnDate;

      const payload = {
        vehicleId: vehicle.id,
        pickupDateTime: formattedPickup,
        returnDateTime: formattedReturn,
        pickupLocation,
        returnLocation,
        totalAmount,
        withDriver
      };

      await API.post('/booking/create', payload);
      toast.success('Booking requested successfully!');
      setShowBookingForm(false);
      // Reset form
      setPickupDate('');
      setReturnDate('');
      setPickupLocation('');
      setReturnLocation('');
      setWithDriver(false);
      navigate('/my/bookings');
    } catch (err) {
      toast.error(err.response?.data || 'Failed to submit booking');
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    const fetchVehicle = async () => {
      setLoading(true);
      try {
        const res = await API.get(`/vehicle/${id}`);
        setVehicle(res.data);
      } catch (err) {
        navigate('/vehicles');
      } finally {
        setLoading(false);
      }
    };
    fetchVehicle();
  }, [id, navigate]);

  if (loading) {
    return (
      <div className="customer-detail-loading">
        <div className="loading-pulse"></div>
        <p>Loading vehicle details...</p>
      </div>
    );
  }

  if (!vehicle) return null;

  const images = vehicle.imageUrls || [];

  return (
    <div className="customer-detail" id="customer-vehicle-detail">
      {/* Header */}
      <div className="cdetail-header">
        <div className="cdetail-header-inner">
          <button
            className="cdetail-back-btn"
            onClick={() => navigate('/vehicles')}
            id="back-to-vehicles"
          >
            <HiOutlineArrowLeft />
            Back to Vehicles
          </button>
        </div>
      </div>

      <div className="cdetail-main">
        <div className="cdetail-layout">
          {/* Left - Image Gallery */}
          <div className="cdetail-gallery">
            <div className="cdetail-main-image">
              {images.length > 0 ? (
                <img
                  src={`${BASE_URL}${images[activeImage].startsWith('/') ? '' : '/'}${images[activeImage]}`}
                  alt={vehicle.model}
                  id="cdetail-main-img"
                  onError={(e) => {
                    e.target.style.display = 'none';
                    if (e.target.nextSibling) e.target.nextSibling.style.display = 'flex';
                  }}
                />
              ) : null}
              <div className="cdetail-no-image" style={{ display: images.length > 0 ? 'none' : 'flex' }}>
                <span>🚗</span>
                <p>No images available</p>
              </div>
            </div>
            {images.length > 1 && (
              <div className="cdetail-thumbs">
                {images.map((url, idx) => (
                  <button
                    key={idx}
                    className={`cdetail-thumb ${idx === activeImage ? 'cdetail-thumb-active' : ''}`}
                    onClick={() => setActiveImage(idx)}
                  >
                    <img src={`${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`} alt={`View ${idx + 1}`} />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Right - Info & Booking */}
          <div className="cdetail-info">
            <div className="cdetail-category-tag">
              {vehicle.categoryName || 'Vehicle'}
            </div>

            <h1 className="cdetail-model">{vehicle.model}</h1>
            <p className="cdetails-categoryDescription">{vehicle.categoryDescription}</p>
            <p className="cdetail-plate">
              <HiOutlineLocationMarker />
              {vehicle.licensePlate}
            </p>

            {/* Price Box */}
            <div className="cdetail-price-box">
              <div className="cdetail-price-main">
                <span className="cdetail-price-amount">
                  LKR {vehicle.dailyRentalRate?.toLocaleString()}
                </span>
                <span className="cdetail-price-per">/ day</span>
              </div>
              <span className="cdetail-price-note">All taxes included</span>
            </div>

            {/* Specs */}
            <div className="cdetail-specs">
              <div className="cdetail-spec">
                <HiOutlineCalendar className="cdetail-spec-icon" />
                <div>
                  <span className="cdetail-spec-label">Year</span>
                  <span className="cdetail-spec-value">{vehicle.year}</span>
                </div>
              </div>
              <div className="cdetail-spec">
                <HiOutlineTruck className="cdetail-spec-icon" />
                <div>
                  <span className="cdetail-spec-label">Odometer</span>
                  <span className="cdetail-spec-value">{vehicle.currentOdometer?.toLocaleString()} km</span>
                </div>
              </div>
              <div className="cdetail-spec">
                <HiOutlineStar className="cdetail-spec-icon" />
                <div>
                  <span className="cdetail-spec-label">Category</span>
                  <span className="cdetail-spec-value">{vehicle.categoryName || '—'}</span>
                </div>
              </div>
              <div className="cdetail-spec">
                <HiOutlineCheckCircle className="cdetail-spec-icon" />
                <div>
                  <span className="cdetail-spec-label">Status</span>
                  <span className="cdetail-spec-value cdetail-status-badge">
                    {vehicle.status?.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>
            </div>

            {/* CTA / Booking Form */}
            {!showBookingForm ? (
              <div className="cdetail-cta">
                <button 
                  className="cdetail-book-btn" 
                  id="book-vehicle-btn"
                  onClick={() => setShowBookingForm(true)}
                  disabled={vehicle.status == 'OUT_OF_SERVICE'}
                >
                  <HiOutlineCalendar />
                  {vehicle.status != 'OUT_OF_SERVICE' ? 'Book This Vehicle' : 'Currently Unavailable'}
                </button>
                <button
                  className="cdetail-contact-btn"
                  id="contact-us-btn"
                >
                  <HiOutlineSupport />
                  Contact Us
                </button>
              </div>
            ) : (
              <div className="cdetail-booking-form">
                <h3>Request Booking</h3>
                {isAuthenticated && role === 'CUSTOMER' && user?.verificationStatus !== 'VERIFIED' && (
                  <div style={{
                    padding: '10px 14px',
                    marginBottom: '14px',
                    borderRadius: '8px',
                    background: '#fffbeb',
                    border: '1px solid #fef3c7',
                    color: '#92400e',
                    fontSize: '0.875rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px'
                  }}>
                    <HiOutlineExclamationCircle style={{ fontSize: '1.2rem', flexShrink: 0 }} />
                    <span>
                      Your account is pending verification. Please <Link to="/my/nic-upload" style={{ fontWeight: 'bold', textDecoration: 'underline' }}>Upload NIC</Link> to ensure booking confirmation.
                    </span>
                  </div>
                )}
                <form onSubmit={handleBookingSubmit}>
                  <div className="form-group">
                    <label>Pickup Date & Time</label>
                    <input 
                      type="datetime-local" 
                      value={pickupDate}
                      onChange={(e) => { setPickupDate(e.target.value); setTotalAmount(0); }}
                      required 
                    />
                  </div>
                  <div className="form-group">
                    <label>Return Date & Time</label>
                    <input 
                      type="datetime-local" 
                      value={returnDate}
                      onChange={(e) => { setReturnDate(e.target.value); setTotalAmount(0); }}
                      required 
                    />
                  </div>
                  <div className="form-group">
                    <label>Pickup Location</label>
                    <input 
                      type="text" 
                      placeholder="Enter pickup location"
                      value={pickupLocation}
                      onChange={(e) => setPickupLocation(e.target.value)}
                      required 
                    />
                  </div>
                  <div className="form-group">
                    <label>Return Location</label>
                    <input 
                      type="text" 
                      placeholder="Enter return location"
                      value={returnLocation}
                      onChange={(e) => setReturnLocation(e.target.value)}
                      required 
                    />
                  </div>
                  <div className="form-group checkbox-group">
                    <label>
                      <input 
                        type="checkbox" 
                        checked={withDriver}
                        onChange={(e) => { setWithDriver(e.target.checked); setTotalAmount(0); }}
                      />
                      Include Driver (+2,500 LKR/day)
                    </label>
                  </div>
                  
                  <button 
                    type="button" 
                    onClick={handleCalculatePrice}
                    disabled={calculatingPrice}
                    style={{
                      width: '100%', 
                      padding: '12px', 
                      backgroundColor: '#3b82f6', 
                      color: 'white', 
                      border: 'none', 
                      borderRadius: '8px', 
                      fontWeight: '600',
                      cursor: 'pointer',
                      marginBottom: '15px'
                    }}
                  >
                    {calculatingPrice ? 'Calculating...' : 'Calculate Total Price'}
                  </button>

                  <div className="booking-summary">
                    <span>Total Amount:</span>
                    <strong>
                      {calculatingPrice ? 'Calculating...' : `LKR ${totalAmount.toLocaleString()}`}
                    </strong>
                  </div>

                  <div className="booking-actions">
                    <button type="submit" className="btn-submit" disabled={isSubmitting}>
                      {isSubmitting ? 'Submitting...' : 'Confirm Request'}
                    </button>
                    <button type="button" className="btn-cancel" onClick={() => setShowBookingForm(false)}>
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Trust Badges */}
            <div className="cdetail-trust">
              <div className="trust-item">
                <HiOutlineShieldCheck />
                <span>Fully Insured</span>
              </div>
              <div className="trust-item">
                <HiOutlineSupport />
                <span>24/7 Support</span>
              </div>
              <div className="trust-item">
                <HiOutlineCheckCircle />
                <span>Free Cancellation</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
