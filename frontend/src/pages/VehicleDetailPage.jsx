import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import API, { BASE_URL } from '../api/axiosInstance';
import { toast } from 'react-toastify';
import StatusBadge from '../components/common/StatusBadge';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EditVehicleModal from '../components/fleet/EditVehicleModal';
import MaintenanceCard from '../components/maintenance/MaintenanceCard';
import ScheduleMaintenanceForm from '../components/maintenance/ScheduleMaintenanceForm';
import CompleteMaintenanceModal from '../components/maintenance/CompleteMaintenanceModal';
import AddVehicleImagesModal from '../components/fleet/AddVehicleImagesModal';
import EmptyState from '../components/common/EmptyState';
import {
  HiOutlinePencil,
  HiOutlinePlus,
  HiOutlineArrowLeft,
  HiOutlinePhotograph,
} from 'react-icons/hi';
import './VehicleDetailPage.css';

export default function VehicleDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { role } = useAuth();
  const [vehicle, setVehicle] = useState(null);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [showAddImagesModal, setShowAddImagesModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [activeImage, setActiveImage] = useState(0);

  const canManageImages = role === 'MANAGER' || role === 'ADMIN';

  const getFleetPath = () => {
    if (role === 'ADMIN') return '/admin/fleet';
    if (role === 'STAFF') return '/staff/fleet';
    return '/manager/fleet';
  };

  const fetchVehicle = async () => {
    try {
      const res = await API.get(`/vehicle/${id}`);
      setVehicle(res.data);
    } catch (err) {
      toast.error('Vehicle not found');
      navigate(getFleetPath());
    }
  };

  const fetchRecords = async () => {
    try {
      const res = await API.get(`/maintenance/vehicle/${id}`);
      setRecords(res.data);
    } catch (err) {
      setRecords([]);
    }
  };

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      await Promise.all([fetchVehicle(), fetchRecords()]);
      setLoading(false);
    };
    load();
  }, [id]);

  const handleStartMaintenance = async (recordId) => {
    try {
      await API.put(`/maintenance/${recordId}/startMaintenance`);
      toast.success('Maintenance started!');
      fetchRecords();
    } catch (err) {
      toast.error('Failed to start maintenance.');
    }
  };

  const handleCancelMaintenance = async (recordId) => {
    try {
      await API.delete(`/maintenance/${recordId}`);
      toast.success('Maintenance cancelled.');
      fetchRecords();
      fetchVehicle();
    } catch (err) {
      toast.error('Failed to cancel maintenance.');
    }
  };

  const handleCompleteMaintenance = (record) => {
    setSelectedRecord(record);
    setShowCompleteModal(true);
  };

  if (loading) return <LoadingSpinner message="Loading vehicle details..." />;
  if (!vehicle) return null;

  const images = vehicle.imageUrls || [];

  return (
    <div className="page-container" id="vehicle-detail-page">
      <button
        className="btn btn-ghost back-btn"
        onClick={() => navigate(getFleetPath())}
        id="back-to-fleet"
      >
        <HiOutlineArrowLeft />
        Back to Fleet
      </button>

      <div className="detail-layout">
        {/* Left Column — Images */}
        <div className="detail-images">
          <div className="detail-main-image">
            {images.length > 0 ? (
              <img
                src={`${BASE_URL}${images[activeImage].startsWith('/') ? '' : '/'}${images[activeImage]}`}
                alt={vehicle.model}
                id="vehicle-main-image"
                onError={(e) => {
                  e.target.style.display = 'none';
                  if (e.target.nextSibling) e.target.nextSibling.style.display = 'flex';
                }}
              />
            ) : null}
            <div className="detail-no-image" style={{ display: images.length > 0 ? 'none' : 'flex' }}>
              <span>🚗</span>
              <p>No images available</p>
              {canManageImages && (
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  style={{ marginTop: '8px' }}
                  onClick={() => setShowAddImagesModal(true)}
                  id="empty-add-images-btn"
                >
                  <HiOutlinePhotograph />
                  Upload Images
                </button>
              )}
            </div>
          </div>
          {(images.length > 1 || canManageImages) && (
            <div className="detail-thumbnails">
              {images.map((url, idx) => (
                <button
                  key={idx}
                  className={`thumb ${idx === activeImage ? 'thumb-active' : ''}`}
                  onClick={() => setActiveImage(idx)}
                >
                  <img src={`${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`} alt={`Thumb ${idx + 1}`} />
                </button>
              ))}
              {canManageImages && (
                <button
                  type="button"
                  className="thumb thumb-add"
                  onClick={() => setShowAddImagesModal(true)}
                  id="thumb-add-image-btn"
                  title="Add More Vehicle Images"
                >
                  <HiOutlinePlus style={{ fontSize: '1.1rem' }} />
                  <span>Add</span>
                </button>
              )}
            </div>
          )}
        </div>

        {/* Right Column — Info */}
        <div className="detail-info">
          <div className="detail-info-header">
            <div>
              <h1 className="detail-model">{vehicle.model}</h1>
              <p className="detail-plate">{vehicle.licensePlate}</p>
            </div>
            <StatusBadge status={vehicle.status} />
          </div>

          <div className="detail-price-box">
            <span className="detail-price-amount">
              LKR {vehicle.dailyRentalRate?.toLocaleString()}
            </span>
            <span className="detail-price-per">per day</span>
          </div>

          <div className="detail-specs">
            <div className="spec-item">
              <span className="spec-label">Year</span>
              <span className="spec-value">{vehicle.year}</span>
            </div>
            <div className="spec-item">
              <span className="spec-label">Category</span>
              <span className="spec-value">{vehicle.categoryName || '—'}</span>
            </div>
            <div className="spec-item">
              <span className="spec-label">Odometer</span>
              <span className="spec-value">{vehicle.currentOdometer?.toLocaleString()} km</span>
            </div>
            <div className="spec-item">
              <span className="spec-label">Added</span>
              <span className="spec-value">
                {vehicle.createdAt ? new Date(vehicle.createdAt).toLocaleDateString() : '—'}
              </span>
            </div>
          </div>

          <div className="detail-actions">
            <button
              className="btn btn-primary"
              onClick={() => setShowEditModal(true)}
              id="edit-vehicle-detail-btn"
            >
              <HiOutlinePencil />
              Edit Vehicle
            </button>
            {canManageImages && (
              <button
                className="btn btn-secondary"
                onClick={() => setShowAddImagesModal(true)}
                id="add-vehicle-images-btn"
              >
                <HiOutlinePhotograph />
                Add Images
              </button>
            )}
            <button
              className="btn btn-ghost"
              onClick={() => setShowScheduleModal(true)}
              id="schedule-maintenance-btn"
            >
              <HiOutlinePlus />
              Schedule Maintenance
            </button>
          </div>
        </div>
      </div>

      {/* Maintenance History */}
      <div className="detail-section">
        <h2 className="section-title">Maintenance History</h2>
        {records.length === 0 ? (
          <EmptyState
            title="No maintenance records"
            message="This vehicle has no maintenance history yet."
          />
        ) : (
          <div className="grid-cards">
            {records.map((record) => (
              <MaintenanceCard
                key={record.id}
                record={record}
                onStart={handleStartMaintenance}
                onComplete={handleCompleteMaintenance}
                onCancel={handleCancelMaintenance}
              />
            ))}
          </div>
        )}
      </div>

      {/* Edit Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Vehicle"
      >
        <EditVehicleModal
          vehicle={vehicle}
          onSuccess={() => {
            fetchVehicle();
          }}
          onClose={() => setShowEditModal(false)}
        />
      </Modal>

      {/* Add Images Modal (Manager Feature) */}
      <AddVehicleImagesModal
        vehicle={vehicle}
        isOpen={showAddImagesModal}
        onClose={() => setShowAddImagesModal(false)}
        onSuccess={() => {
          fetchVehicle();
        }}
      />

      {/* Schedule Maintenance Modal */}
      <Modal
        isOpen={showScheduleModal}
        onClose={() => setShowScheduleModal(false)}
        title="Schedule Maintenance"
      >
        <ScheduleMaintenanceForm
          vehicleId={vehicle.id}
          onSuccess={() => {
            fetchRecords();
            fetchVehicle();
          }}
          onClose={() => setShowScheduleModal(false)}
        />
      </Modal>

      {/* Complete Maintenance Modal */}
      <Modal
        isOpen={showCompleteModal}
        onClose={() => setShowCompleteModal(false)}
        title="Complete Maintenance"
        size="sm"
      >
        {selectedRecord && (
          <CompleteMaintenanceModal
            record={selectedRecord}
            onSuccess={() => {
              fetchRecords();
              fetchVehicle();
            }}
            onClose={() => setShowCompleteModal(false)}
          />
        )}
      </Modal>
    </div>
  );
}
