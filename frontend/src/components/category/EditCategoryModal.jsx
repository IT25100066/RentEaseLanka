import { useState } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';

export default function EditCategoryModal({ category, onSuccess, onClose }) {
  const [loading, setLoading] = useState(false);
  const [basePrice, setBasePrice] = useState(category?.basePrice || '');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/category/update', {
        id: category.id,
        basePrice: Number(basePrice),
      });
      toast.success('Category pricing updated!');
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    } catch (err) {
      toast.error('Failed to update category pricing.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} id="edit-category-form">
      <div className="form-group">
        <label className="form-label">Category</label>
        <input className="form-input" value={category?.name || ''} disabled />
      </div>

      <div className="form-group">
        <label className="form-label">New Base Price (LKR)</label>
        <input
          className="form-input"
          type="number"
          value={basePrice}
          onChange={(e) => setBasePrice(e.target.value)}
          min="1"
          step="0.01"
          required
          id="edit-base-price"
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
          className="btn btn-primary"
          disabled={loading}
          id="submit-edit-category"
          style={{ flex: 1 }}
        >
          {loading ? 'Updating...' : 'Update Price'}
        </button>
      </div>
    </form>
  );
}
