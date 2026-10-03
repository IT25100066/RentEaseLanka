import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './UnauthorizedPage.css';

export default function UnauthorizedPage() {
  const { isAuthenticated, role } = useAuth();

  const dashboardLink = (() => {
    if (!isAuthenticated) return '/';
    switch (role) {
      case 'ADMIN': return '/admin';
      case 'MANAGER': return '/manager';
      case 'STAFF': return '/staff';
      default: return '/';
    }
  })();

  return (
    <div className="unauthorized-container" id="unauthorized-page">
      <div className="unauthorized-card">
        <div className="unauthorized-icon">🔒</div>
        <h1 className="unauthorized-code">403</h1>
        <h2 className="unauthorized-title">Access Denied</h2>
        <p className="unauthorized-message">
          You don't have permission to access this page.
        </p>
        <div className="unauthorized-actions">
          <Link to={dashboardLink} className="unauthorized-btn unauthorized-btn-primary">
            Go to Dashboard
          </Link>
          <Link to="/" className="unauthorized-btn unauthorized-btn-secondary">
            Back to Home
          </Link>
        </div>
      </div>
    </div>
  );
}
