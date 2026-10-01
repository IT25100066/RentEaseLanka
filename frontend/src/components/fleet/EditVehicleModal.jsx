import { useState, useEffect } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';

export default function EditVehicleModal({ vehicle, onSuccess, onClose }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    id: vehicle?.id || '',
    model: vehicle?.model || '',
    dailyRentalRate: vehicle?.dailyRentalRate || '',
    categoryId: vehicle?.categoryId || '',
    year: vehicle?.year || '',
  });

  useEffect(() => {
    API.get('/category')
      .then((res) => setCategories(res.data))
      .catch(() => toast.error('Failed to load categories'));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/vehicle/update', {
        id: Number(form.id),
        model: form.model,
        dailyRentalRate: Number(form.dailyRentalRate),
        categoryId: Number(form.categoryId),
        year: Number(form.year),
      });
      toast.success('Vehicle updated successfully!');
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    } catch (err) {
      toast.error('Failed to update vehicle.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} id="edit-vehicle-form">
      <div className="form-group">
        <label className="form-label">Model</label>
        <input
          className="form-input"
          name="model"
          value={form.model}
          onChange={handleChange}
          required
          id="edit-model"
        />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Daily Rate (LKR)</label>
          <input
            className="form-input"
            type="number"
            name="dailyRentalRate"
            value={form.dailyRentalRate}
            onChange={handleChange}
            min="1"
            step="0.01"
            required
            id="edit-daily-rate"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Year</label>
          <input
            className="form-input"
            type="number"
            name="year"
            value={form.year}
            onChange={handleChange}
            min="1990"
            required
            id="edit-year"
          />
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Category</label>
        <select
          className="form-select"
          name="categoryId"
          value={form.categoryId}
          onChange={handleChange}
          required
          id="edit-category"
        >
          <option value="">Select a category</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
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
          className="btn btn-primary"
          disabled={loading}
          id="submit-edit-vehicle"
          style={{ flex: 1 }}
        >
          {loading ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
}
