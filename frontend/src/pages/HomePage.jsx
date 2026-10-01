import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import {
  HiOutlineShieldCheck,
  HiOutlineCurrencyDollar,
  HiOutlineSupport,
  HiOutlineTruck,
  HiOutlineSearch,
  HiOutlineArrowRight,
  HiOutlineStar,
  HiOutlineLocationMarker,
} from 'react-icons/hi';
import heroBg from '../assets/hero-bg.png';
import './HomePage.css';


export default function HomePage() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    API.get('/vehicles')
      .then((res) => {
        const available = res.data.filter((v) => v.status === 'AVAILABLE');
        setVehicles(available.slice(0, 6));
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="home-page" id="home-page">
      {/* ===== HERO SECTION ===== */}
      <section className="hero-section" id="hero-section">
        <div className="hero-bg">
          <img src={heroBg} alt="Scenic Sri Lankan road" className="hero-bg-image" />
          <div className="hero-overlay"></div>
        </div>

        <div className="hero-content">
          <div className="hero-badge">
            <HiOutlineStar />
            <span>#1 Vehicle Rental in Sri Lanka</span>
          </div>
          <h1 className="hero-title">
            Explore Sri Lanka
            <span className="hero-title-accent"> Your Way</span>
          </h1>
          <p className="hero-subtitle">
            Discover the island's beauty with our premium fleet. From bustling Colombo streets
            to scenic hill country roads — we've got you covered.
          </p>
          <div className="hero-actions">
            <button
              className="hero-btn hero-btn-primary"
              onClick={() => navigate('/vehicles')}
              id="hero-browse-btn"
            >
              Browse Vehicles
              <HiOutlineArrowRight />
            </button>
            <button
              className="hero-btn hero-btn-outline"
              onClick={() => {
                document.getElementById('featured-section')?.scrollIntoView({ behavior: 'smooth' });
              }}
              id="hero-explore-btn"
            >
              <HiOutlineSearch />
              Explore Fleet
            </button>
          </div>

          <div className="hero-stats">
            <div className="hero-stat">
              <span className="hero-stat-number">500+</span>
              <span className="hero-stat-label">Vehicles</span>
            </div>
            <div className="hero-stat-divider"></div>
            <div className="hero-stat">
              <span className="hero-stat-number">10K+</span>
              <span className="hero-stat-label">Happy Customers</span>
            </div>
            <div className="hero-stat-divider"></div>
            <div className="hero-stat">
              <span className="hero-stat-number">24/7</span>
              <span className="hero-stat-label">Support</span>
            </div>
          </div>
        </div>
      </section>

      {/* ===== HOW IT WORKS ===== */}
      <section className="section how-it-works" id="how-it-works">
        <div className="section-inner">
          <div className="section-header">
            <span className="section-tag">Simple Process</span>
            <h2 className="section-title">How It Works</h2>
            <p className="section-desc">Renting a vehicle has never been easier. Three simple steps to get you on the road.</p>
          </div>

          <div className="steps-grid">
            <div className="step-card">
              <div className="step-number">01</div>
              <div className="step-icon-wrap">
                <HiOutlineSearch className="step-icon" />
              </div>
              <h3 className="step-title">Choose Your Vehicle</h3>
              <p className="step-desc">Browse our extensive fleet and find the perfect vehicle for your journey.</p>
            </div>

            <div className="step-connector">
              <svg width="60" height="2" viewBox="0 0 60 2">
                <line x1="0" y1="1" x2="60" y2="1" stroke="var(--primary-200)" strokeWidth="2" strokeDasharray="6 4"/>
              </svg>
            </div>

            <div className="step-card">
              <div className="step-number">02</div>
              <div className="step-icon-wrap">
                <HiOutlineShieldCheck className="step-icon" />
              </div>
              <h3 className="step-title">Book & Confirm</h3>
              <p className="step-desc">Select your dates, complete the booking, and receive instant confirmation.</p>
            </div>

            <div className="step-connector">
              <svg width="60" height="2" viewBox="0 0 60 2">
                <line x1="0" y1="1" x2="60" y2="1" stroke="var(--primary-200)" strokeWidth="2" strokeDasharray="6 4"/>
              </svg>
            </div>

            <div className="step-card">
              <div className="step-number">03</div>
              <div className="step-icon-wrap">
                <HiOutlineTruck className="step-icon" />
              </div>
              <h3 className="step-title">Drive & Explore</h3>
              <p className="step-desc">Pick up your vehicle and start your adventure across beautiful Sri Lanka.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ===== FEATURED VEHICLES ===== */}
      <section className="section featured-section" id="featured-section">
        <div className="section-inner">
          <div className="section-header">
            <span className="section-tag">Our Fleet</span>
            <h2 className="section-title">Featured Vehicles</h2>
            <p className="section-desc">Handpicked vehicles ready for your next adventure. All well-maintained and fully insured.</p>
          </div>

          {loading ? (
            <div className="featured-loading">
              <div className="loading-pulse"></div>
              <p>Loading vehicles...</p>
            </div>
          ) : vehicles.length === 0 ? (
            <div className="featured-empty">
              <HiOutlineTruck className="featured-empty-icon" />
              <p>No vehicles available at the moment. Check back soon!</p>
            </div>
          ) : (
            <div className="featured-grid">
              {vehicles.map((vehicle, index) => (
                <div
                  key={vehicle.id}
                  className="featured-card"
                  style={{ animationDelay: `${index * 0.1}s` }}
                  onClick={() => navigate(`/vehicles/${vehicle.id}`)}
                  id={`featured-vehicle-${vehicle.id}`}
                >
                  <div className="featured-card-image">
                    {vehicle.imageUrls && vehicle.imageUrls.length > 0 ? (
                      <img
                        src={`${BASE_URL}${vehicle.imageUrls[0]}`}
                        alt={vehicle.model}
                        loading="lazy"
                      />
                    ) : (
                      <div className="featured-no-image">
                        <span>🚗</span>
                      </div>
                    )}
                    <div className="featured-card-category">
                      {vehicle.categoryName || 'Vehicle'}
                    </div>
                  </div>
                  <div className="featured-card-body">
                    <div className="featured-card-top">
                      <h3 className="featured-card-model">{vehicle.model}</h3>
                      <span className="featured-card-year">{vehicle.year}</span>
                    </div>
                    <div className="featured-card-details">
                      <span className="featured-detail">
                        <HiOutlineLocationMarker />
                        {vehicle.licensePlate}
                      </span>
                    </div>
                    <div className="featured-card-footer">
                      <div className="featured-price">
                        <span className="featured-price-amount">
                          LKR {vehicle.dailyRentalRate?.toLocaleString()}
                        </span>
                        <span className="featured-price-per">/day</span>
                      </div>
                      <button className="featured-rent-btn" id={`rent-btn-${vehicle.id}`}>
                        Rent Now
                        <HiOutlineArrowRight />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="featured-cta">
            <button
              className="hero-btn hero-btn-primary"
              onClick={() => navigate('/vehicles')}
              id="view-all-vehicles-btn"
            >
              View All Vehicles
              <HiOutlineArrowRight />
            </button>
          </div>
        </div>
      </section>

      {/* ===== WHY CHOOSE US ===== */}
      <section className="section why-us-section" id="why-us-section">
        <div className="section-inner">
          <div className="section-header">
            <span className="section-tag">Why RentEaseLanka</span>
            <h2 className="section-title">Why Choose Us</h2>
            <p className="section-desc">We're committed to providing the best vehicle rental experience in Sri Lanka.</p>
          </div>

          <div className="why-grid">
            <div className="why-card">
              <div className="why-icon-wrap">
                <HiOutlineCurrencyDollar className="why-icon" />
              </div>
              <h3 className="why-title">Best Prices</h3>
              <p className="why-desc">
                Competitive daily rates with no hidden fees. Get the best value for your money.
              </p>
            </div>

            <div className="why-card">
              <div className="why-icon-wrap">
                <HiOutlineShieldCheck className="why-icon" />
              </div>
              <h3 className="why-title">Fully Insured</h3>
              <p className="why-desc">
                All our vehicles come with comprehensive insurance for a worry-free journey.
              </p>
            </div>

            <div className="why-card">
              <div className="why-icon-wrap">
                <HiOutlineSupport className="why-icon" />
              </div>
              <h3 className="why-title">24/7 Support</h3>
              <p className="why-desc">
                Round-the-clock customer support to help you anytime, anywhere on the island.
              </p>
            </div>

            <div className="why-card">
              <div className="why-icon-wrap">
                <HiOutlineTruck className="why-icon" />
              </div>
              <h3 className="why-title">Wide Selection</h3>
              <p className="why-desc">
                From economy cars to luxury SUVs, find the perfect ride for every occasion.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ===== CTA BANNER ===== */}
      <section className="cta-banner" id="cta-banner">
        <div className="cta-inner">
          <h2 className="cta-title">Ready to Hit the Road?</h2>
          <p className="cta-desc">Book your dream vehicle today and explore the beauty of Sri Lanka.</p>
          <button
            className="hero-btn hero-btn-white"
            onClick={() => navigate('/vehicles')}
            id="cta-browse-btn"
          >
            Browse Vehicles Now
            <HiOutlineArrowRight />
          </button>
        </div>
      </section>
    </div>
  );
}
