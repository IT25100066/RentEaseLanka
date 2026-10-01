import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
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
} from 'react-icons/hi';
import './CustomerVehicleDetail.css';


export default function CustomerVehicleDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [vehicle, setVehicle] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeImage, setActiveImage] = useState(0);

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
                  src={`${BASE_URL}${images[activeImage]}`}
                  alt={vehicle.model}
                  id="cdetail-main-img"
                />
              ) : (
                <div className="cdetail-no-image">
                  <span>🚗</span>
                  <p>No images available</p>
                </div>
              )}
            </div>
            {images.length > 1 && (
              <div className="cdetail-thumbs">
                {images.map((url, idx) => (
                  <button
                    key={idx}
                    className={`cdetail-thumb ${idx === activeImage ? 'cdetail-thumb-active' : ''}`}
                    onClick={() => setActiveImage(idx)}
                  >
                    <img src={`${BASE_URL}${url}`} alt={`View ${idx + 1}`} />
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

            {/* CTA */}
            <div className="cdetail-cta">
              <button className="cdetail-book-btn" id="book-vehicle-btn">
                <HiOutlineCalendar />
                Book This Vehicle
              </button>
              <button
                className="cdetail-contact-btn"
                id="contact-us-btn"
              >
                <HiOutlineSupport />
                Contact Us
              </button>
            </div>

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
