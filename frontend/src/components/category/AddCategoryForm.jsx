import { useState } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';

export default function AddCategoryForm({ onSuccess }) {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    name: '',
    description: '',
    basePrice: '',
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/category/create', {
        name: form.name,
        description: form.description,
        basePrice: Number(form.basePrice),
      });
      toast.success('Category created successfully!');
      setForm({ name: '', description: '', basePrice: '' });
      if (onSuccess) onSuccess();
    } catch (err) {
      toast.error('Failed to create category.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} id="add-category-form">
      <div className="form-group">
        <label className="form-label">Category Name *</label>
        <input
          className="form-input"
          name="name"
          value={form.name}
          onChange={handleChange}
          placeholder="e.g. SUV"
          required
          id="input-category-name"
        />
      </div>

      <div className="form-group">
        <label className="form-label">Description</label>
        <textarea
          className="form-textarea"
          name="description"
          value={form.description}
          onChange={handleChange}
          placeholder="Brief description of the category"
          id="input-category-description"
        />
      </div>

      <div className="form-group">
        <label className="form-label">Base Price (LKR) *</label>
        <input
          className="form-input"
          type="number"
          name="basePrice"
          value={form.basePrice}
          onChange={handleChange}
          placeholder="e.g. 3000"
          min="1"
          step="0.01"
          required
          id="input-base-price"
        />
      </div>

      <button
        type="submit"
        className="btn btn-primary btn-lg"
        disabled={loading}
        id="submit-category-btn"
        style={{ width: '100%' }}
      >
        {loading ? 'Creating...' : 'Create Category'}
      </button>
    </form>
  );
}
