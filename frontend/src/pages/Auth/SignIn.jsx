import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FiMail, FiLock } from 'react-icons/fi';
import { login } from '../../api/auth';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

const SignIn = () => {
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
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
    setLoading(true);

    try {
      const data = await login(formData);
      if (data.token) {
        authLogin(data);
        toast.success('Welcome back! Signed in successfully.');
        const role = data.user?.role;
        switch (role) {
          case 'ADMIN': navigate('/admin'); break;
          case 'MANAGER': navigate('/manager'); break;
          case 'STAFF': navigate('/staff'); break;
          case 'DRIVER': navigate('/driver'); break;
          default: navigate('/'); break;
        }
      } else {
        toast.error('Authentication failed: No token received.');
        triggerShake();
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Failed to sign in. Please check your credentials.';
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
            Your Journey<br />
            <span>Starts Here</span>
          </h2>
          <p className="auth-brand-sub">
            Explore Sri Lanka in style with our premium fleet. Seamless booking, transparent pricing, 24/7 support.
          </p>

          <div className="auth-features">
            <div className="auth-feature-item">
              <div className="auth-feature-icon">🛡️</div>
              <span className="auth-feature-text">Fully Insured Vehicles</span>
            </div>
            <div className="auth-feature-item">
              <div className="auth-feature-icon">💰</div>
              <span className="auth-feature-text">Best Price Guarantee</span>
            </div>
            <div className="auth-feature-item">
              <div className="auth-feature-icon">🕐</div>
              <span className="auth-feature-text">24/7 Customer Support</span>
            </div>
          </div>
        </div>
      </div>

      {/* ===== RIGHT FORM PANEL ===== */}
      <div className="auth-right">
        <div className="auth-form-wrapper">
          <div className="auth-form-header">
            <h2>Welcome Back</h2>
            <p>Sign in to your account to continue your journey.</p>
          </div>

          <form className={`auth-form${shake ? ' shake' : ''}`} onSubmit={handleSubmit} id="signin-form">
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
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  autoComplete="current-password"
                />
              </div>
            </div>

            <button type="submit" className="auth-btn" disabled={loading} id="signin-submit-btn">
              {loading ? <div className="spinner" /> : 'Sign In'}
            </button>
          </form>

          <div className="auth-form-footer">
            <p>Don't have an account? <Link to="/signup">Create one free</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
