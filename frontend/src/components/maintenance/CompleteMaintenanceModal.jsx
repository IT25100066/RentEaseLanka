import { useState } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';

export default function CompleteMaintenanceModal({ record, onSuccess, onClose }) {
  const [loading, setLoading] = useState(false);
  const [cost, setCost] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.put(`/maintenance/${record.id}/completed`, {
        vehicleId: record.vehicle?.id || record.vehicleId,
        cost: Number(cost),
      });
      toast.success('Maintenance completed successfully!');
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    } catch (err) {
      toast.error('Failed to complete maintenance.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} id="complete-maintenance-form">
      <div className="form-group">
        <label className="form-label">Maintenance Record</label>
        <input className="form-input" value={`#${record.id} — ${record.type}`} disabled />
      </div>

      <div className="form-group">
        <label className="form-label">Total Cost (LKR) *</label>
        <input
          className="form-input"
          type="number"
          value={cost}
          onChange={(e) => setCost(e.target.value)}
          placeholder="Enter total maintenance cost"
          min="0"
          step="0.01"
          required
          id="complete-cost-input"
        />
      </div>

      <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={onClose}
          style={{ flex: 1 }}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn btn-success"
          disabled={loading}
          id="submit-complete-btn"
          style={{ flex: 1 }}
        >
          {loading ? 'Completing...' : 'Mark Complete'}
        </button>
      </div>
    </form>
  );
}
