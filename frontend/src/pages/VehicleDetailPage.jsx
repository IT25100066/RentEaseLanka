import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import API, { BASE_URL } from '../api/axiosInstance';
import { toast } from 'react-toastify';
import StatusBadge from '../components/common/StatusBadge';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EditVehicleModal from '../components/fleet/EditVehicleModal';
import MaintenanceCard from '../components/maintenance/MaintenanceCard';
import ScheduleMaintenanceForm from '../components/maintenance/ScheduleMaintenanceForm';
import CompleteMaintenanceModal from '../components/maintenance/CompleteMaintenanceModal';
import EmptyState from '../components/common/EmptyState';
import {
  HiOutlinePencil,
  HiOutlinePlus,
  HiOutlineArrowLeft,
} from 'react-icons/hi';
import './VehicleDetailPage.css';


export default function VehicleDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [vehicle, setVehicle] = useState(null);
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [activeImage, setActiveImage] = useState(0);

  const fetchVehicle = async () => {
    try {
      const res = await API.get(`/vehicle/${id}`);
      setVehicle(res.data);
    } catch (err) {
      toast.error('Vehicle not found');
      navigate('/admin/fleet');
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
        onClick={() => navigate('/admin/fleet')}
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
                src={`${BASE_URL}${images[activeImage]}`}
                alt={vehicle.model}
                id="vehicle-main-image"
              />
            ) : (
              <div className="detail-no-image">
                <span>🚗</span>
                <p>No images available</p>
              </div>
            )}
          </div>
          {images.length > 1 && (
            <div className="detail-thumbnails">
              {images.map((url, idx) => (
                <button
                  key={idx}
                  className={`thumb ${idx === activeImage ? 'thumb-active' : ''}`}
                  onClick={() => setActiveImage(idx)}
                >
                  <img src={`${BASE_URL}${url}`} alt={`Thumb ${idx + 1}`} />
                </button>
              ))}
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
            <button
              className="btn btn-secondary"
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
