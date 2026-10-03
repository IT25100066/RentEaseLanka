import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import { getPublicReviews } from '../api/reviews';
import { useAuth } from '../context/AuthContext';
import AddReviewModal from '../components/review/AddReviewModal';
import {
  HiOutlineShieldCheck,
  HiOutlineCurrencyDollar,
  HiOutlineSupport,
  HiOutlineTruck,
  HiOutlineSearch,
  HiOutlineArrowRight,
  HiOutlineStar,
  HiOutlineLocationMarker,
  HiOutlineLightningBolt,
  HiOutlineBadgeCheck,
  HiOutlineClock,
  HiOutlinePencilAlt,
} from 'react-icons/hi';
import heroBg from '../assets/hero-bg.png';
import './HomePage.css';

const STATIC_TESTIMONIALS = [
  {
    id: 'static-1',
    customerName: 'Kavindra Perera',
    location: 'Colombo',
    initials: 'KP',
    comment: 'Absolutely seamless experience from booking to drop-off. The vehicle was spotless and the staff were incredibly helpful. Will definitely use RentEaseLanka again!',
    rating: 5,
  },
  {
    id: 'static-2',
    customerName: 'Nalini Fernando',
    location: 'Kandy',
    initials: 'NF',
    comment: 'Best prices I found for an SUV in all of Sri Lanka. The 24/7 support gave me real peace of mind during our hill-country road trip. Highly recommended!',
    rating: 5,
  },
  {
    id: 'static-3',
    customerName: 'Sahan Wickramasinghe',
    location: 'Galle',
    initials: 'SW',
    comment: 'From the online booking to the handover process, everything was transparent and fast. The vehicle was in perfect condition. A truly professional service.',
    rating: 5,
  },
];

export default function HomePage() {
  const { user } = useAuth();
  const [vehicles, setVehicles] = useState([]);
  const [reviews, setReviews] = useState(STATIC_TESTIMONIALS);
  const [loading, setLoading] = useState(true);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const navigate = useNavigate();

  const fetchReviews = () => {
    getPublicReviews()
      .then((data) => {
        if (data && data.length > 0) {
          setReviews(data);
        } else {
          setReviews(STATIC_TESTIMONIALS);
        }
      })
      .catch(() => {
        setReviews(STATIC_TESTIMONIALS);
      });
  };

  useEffect(() => {
    API.get('/vehicles')
      .then((res) => {
        const available = res.data.filter((v) => v.status !== 'OUT_OF_SERVICE');
        setVehicles(available.slice(0, 6));
      })
      .catch(() => {})
      .finally(() => setLoading(false));

    fetchReviews();
  }, []);

  // Global Scroll Reveal Observer (React-compliant, zero hook violations inside loops)
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.12 }
    );

    const elements = document.querySelectorAll('.reveal');
    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, [loading, reviews, vehicles]);

  const handleOpenReviewModal = () => {
    if (!user) {
      navigate('/signin');
      return;
    }
    setIsReviewModalOpen(true);
  };

  return (
    <div className="home-page" id="home-page">

      {/* ========== HERO ========== */}
      <section className="hero-section" id="hero-section">
        <div className="hero-bg">
          <img src={heroBg} alt="Scenic Sri Lankan road" className="hero-bg-image" />
          <div className="hero-overlay" />
        </div>

        <div className="hero-orb hero-orb-1" />
        <div className="hero-orb hero-orb-2" />
        <div className="hero-orb hero-orb-3" />

        <div className="hero-content">
          <div className="hero-badge">
            <HiOutlineStar />
            <span>#1 Vehicle Rental in Sri Lanka</span>
          </div>

          <h1 className="hero-title">
            Explore Sri Lanka
            <span className="hero-title-accent">Your Way</span>
          </h1>

          <p className="hero-subtitle">
            Discover the island's beauty with our premium fleet. From bustling Colombo streets
            to scenic hill-country roads — we've got you covered.
          </p>

          <div className="hero-actions">
            <button
              className="hero-btn hero-btn-primary"
              onClick={() => navigate('/vehicles')}
              id="hero-find-vehicle-btn"
            >
              <HiOutlineSearch />
              Find Your Vehicle
            </button>
            <button
              className="hero-btn hero-btn-outline"
              onClick={() => {
                const el = document.getElementById('how-it-works');
                el?.scrollIntoView({ behavior: 'smooth' });
              }}
              id="hero-how-it-works-btn"
            >
              How It Works
              <HiOutlineArrowRight />
            </button>
          </div>

          <div className="hero-stats">
            <div className="hero-stat">
              <span className="hero-stat-number">50+</span>
              <span className="hero-stat-label">Premium Vehicles</span>
            </div>
            <div className="hero-stat-divider" />
            <div className="hero-stat">
              <span className="hero-stat-number">2,500+</span>
              <span className="hero-stat-label">Happy Drivers</span>
            </div>
            <div className="hero-stat-divider" />
            <div className="hero-stat">
              <span className="hero-stat-number">4.9 ★</span>
              <span className="hero-stat-label">Customer Rating</span>
            </div>
          </div>
        </div>

        <div className="hero-scroll-indicator">
          <div className="hero-scroll-dot">
            <div className="hero-scroll-wheel" />
          </div>
        </div>
      </section>

      {/* ========== TRUST BAR ========== */}
      <section className="trust-bar" id="trust-bar">
        <div className="trust-bar-inner">
          <div className="trust-item">
            <HiOutlineBadgeCheck className="trust-icon" />
            <span>Verified Fleet Condition</span>
          </div>
          <div className="trust-divider" />
          <div className="trust-item">
            <HiOutlineShieldCheck className="trust-icon" />
            <span>Comprehensive Insurance</span>
          </div>
          <div className="trust-divider" />
          <div className="trust-item">
            <HiOutlineClock className="trust-icon" />
            <span>24/7 Breakdown Assistance</span>
          </div>
          <div className="trust-divider" />
          <div className="trust-item">
            <HiOutlineCurrencyDollar className="trust-icon" />
            <span>No Hidden Costs</span>
          </div>
        </div>
      </section>

      {/* ========== HOW IT WORKS ========== */}
      <section className="section how-it-works" id="how-it-works">
        <div className="section-inner">
          <div className="section-header reveal">
            <span className="section-tag">Simple Process</span>
            <h2 className="section-title">How It Works</h2>
            <p className="section-desc">
              Renting a vehicle has never been easier. Three simple steps to get you on the road.
            </p>
          </div>

          <div className="steps-grid">
            <div className="step-card reveal reveal-delay-1">
              <div className="step-number">01</div>
              <div className="step-icon-wrap">
                <HiOutlineSearch className="step-icon" />
              </div>
              <h3 className="step-title">Choose Your Vehicle</h3>
              <p className="step-desc">Browse our extensive fleet and find the perfect vehicle for your journey across Sri Lanka.</p>
            </div>

            <div className="step-connector">
              <svg width="60" height="2" viewBox="0 0 60 2">
                <line x1="0" y1="1" x2="60" y2="1" stroke="var(--primary-200)" strokeWidth="2" strokeDasharray="6 4"/>
              </svg>
            </div>

            <div className="step-card reveal reveal-delay-2">
              <div className="step-number">02</div>
              <div className="step-icon-wrap">
                <HiOutlineShieldCheck className="step-icon" />
              </div>
              <h3 className="step-title">Book & Confirm</h3>
              <p className="step-desc">Select your dates, complete the booking in minutes, and receive instant confirmation.</p>
            </div>

            <div className="step-connector">
              <svg width="60" height="2" viewBox="0 0 60 2">
                <line x1="0" y1="1" x2="60" y2="1" stroke="var(--primary-200)" strokeWidth="2" strokeDasharray="6 4"/>
              </svg>
            </div>

            <div className="step-card reveal reveal-delay-3">
              <div className="step-number">03</div>
              <div className="step-icon-wrap">
                <HiOutlineTruck className="step-icon" />
              </div>
              <h3 className="step-title">Drive & Explore</h3>
              <p className="step-desc">Pick up your vehicle and start your adventure across the beautiful island of Sri Lanka.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ========== FEATURED VEHICLES ========== */}
      <section className="section featured-section" id="featured-section">
        <div className="section-inner">
          <div className="section-header reveal">
            <span className="section-tag">Our Fleet</span>
            <h2 className="section-title">Featured Vehicles</h2>
            <p className="section-desc">
              Handpicked vehicles ready for your next adventure. All well-maintained and fully insured.
            </p>
          </div>

          {loading ? (
            <div className="featured-skeleton-grid">
              {[1, 2, 3].map(i => (
                <div key={i} className="featured-skeleton-card">
                  <div className="skeleton skeleton-image" />
                  <div className="featured-skeleton-body">
                    <div className="skeleton skeleton-text medium" />
                    <div className="skeleton skeleton-text short" />
                  </div>
                </div>
              ))}
            </div>
          ) : vehicles.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🚗</div>
              <p>No vehicles available right now. Check back soon!</p>
            </div>
          ) : (
            <div className="featured-grid">
              {vehicles.map((v, i) => {
                const img = v.images?.[0]
                  ? (v.images[0].startsWith('http') ? v.images[0] : `${BASE_URL}${v.images[0]}`)
                  : null;
                const price = v.dailyRate
                  ? `LKR ${Number(v.dailyRate).toLocaleString()}`
                  : v.pricePerDay
                    ? `LKR ${Number(v.pricePerDay).toLocaleString()}`
                    : null;

                return (
                  <div
                    key={v.id}
                    className="featured-card reveal"
                    style={{ transitionDelay: `${i * 0.08}s` }}
                    onClick={() => navigate(`/vehicles/${v.id}`)}
                  >
                    <div className="featured-card-image">
                      {img ? (
                        <img src={img} alt={`${v.make} ${v.model}`} />
                      ) : (
                        <div className="featured-card-placeholder">
                          <span>🚗</span>
                        </div>
                      )}
                      <div className="featured-card-overlay" />
                      {v.categoryName && (
                        <span className="featured-card-badge">{v.categoryName}</span>
                      )}

                      {/* Dynamic Vehicle Status Dot */}
                      <span className={`featured-card-status-dot ${v.status === 'AVAILABLE' ? 'available' : 'rented'}`}>
                        {v.status === 'AVAILABLE' ? 'Available' : 'Booked'}
                      </span>
                    </div>

                    <div className="featured-card-body">
                      <div className="featured-card-header">
                        <h3 className="featured-card-title">
                          {v.make} {v.model}
                        </h3>
                        {v.year && <span className="featured-card-year">{v.year}</span>}
                      </div>

                      <div className="featured-card-specs">
                        {v.transmission && (
                          <span className="featured-spec-chip">
                            <HiOutlineLightningBolt /> {v.transmission}
                          </span>
                        )}
                        {v.fuelType && (
                          <span className="featured-spec-chip">
                            <HiOutlineLocationMarker /> {v.fuelType}
                          </span>
                        )}
                        {v.seatingCapacity && (
                          <span className="featured-spec-chip">
                            👤 {v.seatingCapacity} Seats
                          </span>
                        )}
                      </div>

                      <div className="featured-card-footer">
                        <div className="featured-card-price">
                          {price ? (
                            <>
                              <span className="price-amount">{price}</span>
                              <span className="price-unit">/ day</span>
                            </>
                          ) : (
                            <span className="price-contact">Contact for price</span>
                          )}
                        </div>
                        <button
                          className="featured-card-btn"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/vehicles/${v.id}`);
                          }}
                        >
                          Book Now
                          <HiOutlineArrowRight />
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          <div className="featured-cta reveal">
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

      {/* ========== WHY CHOOSE US ========== */}
      <section className="section why-us-section" id="why-us-section">
        <div className="section-inner">
          <div className="section-header reveal">
            <span className="section-tag">Why RentEaseLanka</span>
            <h2 className="section-title">Why Choose Us</h2>
            <p className="section-desc">
              We're committed to providing the best vehicle rental experience in Sri Lanka.
            </p>
          </div>

          <div className="why-grid">
            {[
              {
                icon: HiOutlineCurrencyDollar,
                title: 'Best Prices',
                desc: 'Competitive daily rates with no hidden fees. Get the best value for your money guaranteed.',
              },
              {
                icon: HiOutlineShieldCheck,
                title: 'Fully Insured',
                desc: 'All our vehicles come with comprehensive insurance for a worry-free journey on any road.',
              },
              {
                icon: HiOutlineSupport,
                title: '24/7 Support',
                desc: 'Round-the-clock customer support to help you anytime, anywhere across the island.',
              },
              {
                icon: HiOutlineTruck,
                title: 'Wide Selection',
                desc: 'From economy cars to luxury SUVs, find the perfect ride for every occasion and budget.',
              },
            ].map(({ icon: Icon, title, desc }, i) => (
              <div
                key={title}
                className="why-card reveal"
                style={{ transitionDelay: `${i * 0.1}s` }}
              >
                <div className="why-icon-wrap">
                  <Icon className="why-icon" />
                </div>
                <h3 className="why-title">{title}</h3>
                <p className="why-desc">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ========== TESTIMONIALS ========== */}
      <section className="section testimonials-section" id="testimonials-section">
        <div className="section-inner">
          <div className="section-header reveal">
            <span className="section-tag">Customer Stories</span>
            <h2 className="section-title">What Our Customers Say</h2>
            <p className="section-desc">
              Thousands of satisfied travellers have explored Sri Lanka with us. Here's what they think.
            </p>
            <button 
              className="write-review-btn"
              onClick={handleOpenReviewModal}
            >
              <HiOutlinePencilAlt />
              Write a Review
            </button>
          </div>

          <div className="testimonials-grid">
            {reviews.map((t, i) => (
              <div
                key={t.id || t.customerName + i}
                className="testimonial-card reveal"
                style={{ transitionDelay: `${(i % 3) * 0.1}s` }}
              >
                <div className="testimonial-stars">
                  {Array.from({ length: t.rating || 5 }).map((_, si) => (
                    <HiOutlineStar key={si} style={{ fill: '#f59e0b', color: '#f59e0b' }} />
                  ))}
                </div>
                <p className="testimonial-text">"{t.comment || t.text}"</p>
                <div className="testimonial-author">
                  <div className="testimonial-avatar">{t.initials || 'C'}</div>
                  <div>
                    <div className="testimonial-name">{t.customerName || t.name}</div>
                    <div className="testimonial-location">{t.location || 'Sri Lanka'}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Add Review Modal */}
      <AddReviewModal
        isOpen={isReviewModalOpen}
        onClose={() => setIsReviewModalOpen(false)}
        onSuccess={fetchReviews}
      />

      {/* ========== CTA BANNER ========== */}
      <section className="cta-banner" id="cta-banner">
        <div className="cta-inner reveal">
          <h2 className="cta-title">Ready to Hit the Road?</h2>
          <p className="cta-desc">
            Book your dream vehicle today and explore the breathtaking beauty of Sri Lanka at your own pace.
          </p>
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
