import { useState, useEffect } from 'react';
import API from '../../api/axiosInstance';
import { HiOutlineFilter } from 'react-icons/hi';
import './VehicleFilters.css';

const vehicleStatuses = [
  'AVAILABLE',
  'PENDING',
  'RENTED',
  'UNDER_MAINTENANCE',
  'OUT_OF_SERVICE',
  'RESERVED',
];

export default function VehicleFilters({ onFilter, onClear }) {
  const [categories, setCategories] = useState([]);
  const [filterType, setFilterType] = useState('');
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');

  useEffect(() => {
    API.get('/category')
      .then((res) => setCategories(res.data))
      .catch(() => {});
  }, []);

  const handleApply = () => {
    if (filterType === 'category' && category) {
      onFilter('category', category);
    } else if (filterType === 'status' && status) {
      onFilter('status', status);
    }
  };

  const handleClear = () => {
    setFilterType('');
    setCategory('');
    setStatus('');
    onClear();
  };

  return (
    <div className="vehicle-filters" id="vehicle-filters">
      <div className="filter-row">
        <div className="filter-group">
          <label className="filter-label">
            <HiOutlineFilter />
            Filter by
          </label>
          <select
            className="form-select filter-select"
            value={filterType}
            onChange={(e) => {
              setFilterType(e.target.value);
              setCategory('');
              setStatus('');
            }}
            id="filter-type-select"
          >
            <option value="">No filter</option>
            <option value="category">Category</option>
            <option value="status">Status</option>
          </select>
        </div>

        {filterType === 'category' && (
          <div className="filter-group">
            <label className="filter-label">Category</label>
            <select
              className="form-select filter-select"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              id="filter-category-select"
            >
              <option value="">Select category</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.name}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {filterType === 'status' && (
          <div className="filter-group">
            <label className="filter-label">Status</label>
            <select
              className="form-select filter-select"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              id="filter-status-select"
            >
              <option value="">Select status</option>
              {vehicleStatuses.map((s) => (
                <option key={s} value={s}>
                  {s.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="filter-actions">
          <button
            className="btn btn-primary btn-sm"
            onClick={handleApply}
            disabled={!filterType || (filterType === 'category' && !category) || (filterType === 'status' && !status)}
            id="apply-filter-btn"
          >
            Apply
          </button>
          {filterType && (
            <button
              className="btn btn-ghost btn-sm"
              onClick={handleClear}
              id="clear-filter-btn"
            >
              Clear
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
