import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FiMail, FiLock, FiUser } from 'react-icons/fi';
import { register } from '../../api/auth';
import './Auth.css';

const SignUp = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const triggerShake = () => {
    setShake(true);
    setTimeout(() => setShake(false), 600);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      triggerShake();
      return;
    }

    setLoading(true);

    try {
      const payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
      };

      await register(payload);
      toast.success('Account created! Please sign in.');
      navigate('/signin');
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Failed to register. Please try again.';
      toast.error(errorMsg);
      triggerShake();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* ===== LEFT BRAND PANEL ===== */}
      <div className="auth-left">
        <div className="auth-orb auth-orb-1" />
        <div className="auth-orb auth-orb-2" />
        <div className="auth-orb auth-orb-3" />

        <div className="auth-brand">
          <div className="auth-brand-logo">
            <div className="auth-brand-icon">🚗</div>
            <div>
              <div className="auth-brand-name">
                RentEaseLanka
                <span>Premium Vehicle Rental</span>
              </div>
            </div>
          </div>

          <h2 className="auth-brand-headline">
            Join Sri Lanka's<br />
            <span>Premier Fleet</span>
          </h2>
          <p className="auth-brand-sub">
            Create your account in seconds and gain instant access to 500+ premium vehicles across the island.
          </p>

          <div className="auth-features">
            <div className="auth-feature-item">
              <div className="auth-feature-icon">⚡</div>
              <span className="auth-feature-text">Instant Booking Confirmation</span>
            </div>
            <div className="auth-feature-item">
              <div className="auth-feature-icon">🌍</div>
              <span className="auth-feature-text">Island-wide Coverage</span>
            </div>
            <div className="auth-feature-item">
              <div className="auth-feature-icon">🔒</div>
              <span className="auth-feature-text">Secure & Private</span>
            </div>
          </div>
        </div>
      </div>

      {/* ===== RIGHT FORM PANEL ===== */}
      <div className="auth-right">
        <div className="auth-form-wrapper">
          <div className="auth-form-header">
            <h2>Create Account</h2>
            <p>Join thousands of travellers exploring Sri Lanka.</p>
          </div>

          <form className={`auth-form${shake ? ' shake' : ''}`} onSubmit={handleSubmit} id="signup-form">
            <div className="auth-name-row">
              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <div className="form-input-wrapper">

                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    placeholder="John"
                    value={formData.firstName}
                    onChange={handleChange}
                    required
                    autoComplete="given-name"
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <div className="form-input-wrapper">

                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    placeholder="Doe"
                    value={formData.lastName}
                    onChange={handleChange}
                    required
                    autoComplete="family-name"
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <div className="form-input-wrapper">
                <input
                  type="email"
                  id="email"
                  name="email"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  autoComplete="email"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <div className="form-input-wrapper">
                <input
                  type="password"
                  id="password"
                  name="password"
                  placeholder="Create a strong password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  autoComplete="new-password"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="form-input-wrapper">
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  placeholder="Repeat your password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  autoComplete="new-password"
                />
              </div>
            </div>

            <button type="submit" className="auth-btn" disabled={loading} id="signup-submit-btn">
              {loading ? <div className="spinner" /> : 'Create Account'}
            </button>
          </form>

          <div className="auth-form-footer">
            <p>Already have an account? <Link to="/signin">Sign In</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignUp;
