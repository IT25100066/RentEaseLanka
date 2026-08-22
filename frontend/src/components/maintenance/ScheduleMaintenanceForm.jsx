import { useState, useEffect } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';

const maintenanceTypes = [
  { value: 'ROUTINE_SERVICE', label: 'Routine Service' },
  { value: 'TIRE_CHANGE', label: 'Tire Change' },
  { value: 'ENGINE_REPAIR', label: 'Engine Repair' },
  { value: 'ACCIDENT_REPAIR', label: 'Accident Repair' },
];

export default function ScheduleMaintenanceForm({ vehicleId, onSuccess, onClose }) {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    vehicleId: vehicleId || '',
    type: '',
    description: '',
    odometerAtService: '',
    scheduledDate: '',
  });

  useEffect(() => {
    if (!vehicleId) {
      API.get('/vehicles')
        .then((res) => setVehicles(res.data))
        .catch(() => {});
    }
  }, [vehicleId]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/maintenance', {
        vehicleId: Number(form.vehicleId),
        type: form.type,
        description: form.description,
        odometerAtService: form.odometerAtService ? Number(form.odometerAtService) : null,
        scheduledDate: form.scheduledDate ? form.scheduledDate + 'T00:00:00' : null,
      });
      toast.success('Maintenance scheduled successfully!');
      setForm({ vehicleId: vehicleId || '', type: '', description: '', odometerAtService: '', scheduledDate: '' });
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    } catch (err) {
      const msg = err.response?.data || 'Failed to schedule maintenance.';
      toast.error(typeof msg === 'string' ? msg : 'Failed to schedule maintenance.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} id="schedule-maintenance-form">
      {!vehicleId && (
        <div className="form-group">
          <label className="form-label">Vehicle *</label>
          <select
            className="form-select"
            name="vehicleId"
            value={form.vehicleId}
            onChange={handleChange}
            required
            id="maintenance-vehicle-select"
          >
            <option value="">Select a vehicle</option>
            {vehicles.map((v) => (
              <option key={v.id} value={v.id}>
                {v.model} — {v.licensePlate}
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="form-group">
        <label className="form-label">Maintenance Type *</label>
        <select
          className="form-select"
          name="type"
          value={form.type}
          onChange={handleChange}
          required
          id="maintenance-type-select"
        >
          <option value="">Select type</option>
          {maintenanceTypes.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Description</label>
        <textarea
          className="form-textarea"
          name="description"
          value={form.description}
          onChange={handleChange}
          placeholder="Details about the maintenance..."
          id="maintenance-description"
        />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Odometer at Service (km)</label>
          <input
            className="form-input"
            type="number"
            name="odometerAtService"
            value={form.odometerAtService}
            onChange={handleChange}
            placeholder="e.g. 25000"
            min="0"
            id="maintenance-odometer"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Scheduled Date</label>
          <input
            className="form-input"
            type="date"
            name="scheduledDate"
            value={form.scheduledDate}
            onChange={handleChange}
            id="maintenance-date"
          />
        </div>
      </div>

      <button
        type="submit"
        className="btn btn-primary btn-lg"
        disabled={loading}
        id="submit-maintenance-btn"
        style={{ width: '100%' }}
      >
        {loading ? 'Scheduling...' : 'Schedule Maintenance'}
      </button>
    </form>
  );
}
