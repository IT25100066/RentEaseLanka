import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../common/StatusBadge';
import { BASE_URL } from '../../api/axiosInstance';
import { HiOutlineEye, HiOutlineLocationMarker } from 'react-icons/hi';
import './VehicleCard.css';

export default function VehicleCard({ vehicle }) {
  const navigate = useNavigate();
  const { role } = useAuth();

  const firstImage = vehicle.imageUrls && vehicle.imageUrls.length > 0 ? vehicle.imageUrls[0] : null;
  const imageUrl = firstImage
    ? `${BASE_URL}${firstImage.startsWith('/') ? '' : '/'}${firstImage}`
    : null;

  const handleView = () => {
    let basePath = 'staff';
    if (role === 'ADMIN') basePath = 'admin';
    else if (role === 'MANAGER') basePath = 'manager';
    navigate(`/${basePath}/vehicle/${vehicle.id}`);
  };

  return (
    <div className="vehicle-card card" id={`vehicle-card-${vehicle.id}`}>
      <div className="vehicle-card-image">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={vehicle.model}
            loading="lazy"
            onError={(e) => {
              e.target.style.display = 'none';
              if (e.target.nextSibling) e.target.nextSibling.style.display = 'flex';
            }}
          />
        ) : null}
        <div className="vehicle-no-image" style={{ display: imageUrl ? 'none' : 'flex' }}>
          <span>🚗</span>
          <p>No Image</p>
        </div>
        <div >
          <StatusBadge status={vehicle.status} />
        </div>
      </div>

      <div className="vehicle-card-body">
        <div className="vehicle-card-top">
          <h3 className="vehicle-model">{vehicle.model}</h3>
          <span className="vehicle-year">{vehicle.year}</span>
        </div>

        <p className="vehicle-plate">
          <HiOutlineLocationMarker />
          {vehicle.licensePlate}
        </p>

        <div className="vehicle-card-meta">
          <div className="meta-item">
            <span className="meta-label">Category</span>
            <span className="meta-value">{vehicle.categoryName || '—'}</span>
          </div>
          <div className="meta-item">
            <span className="meta-label">Odometer</span>
            <span className="meta-value">{vehicle.currentOdometer?.toLocaleString()} km</span>
          </div>
        </div>

        <div className="vehicle-card-footer">
          <div className="vehicle-price">
            <span className="price-amount">LKR {vehicle.dailyRentalRate?.toLocaleString()}</span>
            <span className="price-label">/ day</span>
          </div>
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleView}
            id={`view-vehicle-${vehicle.id}`}
          >
            <HiOutlineEye />
            View
          </button>
        </div>
      </div>
    </div>
  );
}
