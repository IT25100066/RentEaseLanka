import { useState, useEffect } from 'react';
import API from '../../api/axiosInstance';
import { toast } from 'react-toastify';
import { HiOutlineUpload, HiOutlineX } from 'react-icons/hi';
import './AddVehicleForm.css';

export default function AddVehicleForm({ onSuccess }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    licensePlate: '',
    model: '',
    dailyRate: '',
    year: '',
    currentOdometer: '',
    categoryId: '',
  });
  const [images, setImages] = useState([]);
  const [previews, setPreviews] = useState([]);

  useEffect(() => {
    API.get('/category')
      .then((res) => setCategories(res.data))
      .catch(() => toast.error('Failed to load categories'));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    setImages(files);
    const urls = files.map((file) => URL.createObjectURL(file));
    setPreviews(urls);
  };

  const removeImage = (index) => {
    const newImages = images.filter((_, i) => i !== index);
    const newPreviews = previews.filter((_, i) => i !== index);
    setImages(newImages);
    setPreviews(newPreviews);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData();
    formData.append('licensePlate', form.licensePlate);
    formData.append('model', form.model);
    formData.append('dailyRate', form.dailyRate);
    formData.append('year', form.year);
    formData.append('currentOdometer', form.currentOdometer);
    formData.append('categoryId', form.categoryId);
    images.forEach((img) => formData.append('images', img));

    try {
      await API.post('/vehicle/create', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      toast.success('Vehicle created successfully!');
      setForm({ licensePlate: '', model: '', dailyRate: '', year: '', currentOdometer: '', categoryId: '' });
      setImages([]);
      setPreviews([]);
      if (onSuccess) onSuccess();
    } catch (err) {
      toast.error('Failed to create vehicle. Please check the fields.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="add-vehicle-form" onSubmit={handleSubmit} id="add-vehicle-form">
      <div className="form-row">
        <div className="form-group">
          <label className="form-label">License Plate *</label>
          <input
            className="form-input"
            name="licensePlate"
            value={form.licensePlate}
            onChange={handleChange}
            placeholder="e.g. ABC-1234"
            required
            id="input-license-plate"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Model *</label>
          <input
            className="form-input"
            name="model"
            value={form.model}
            onChange={handleChange}
            placeholder="e.g. Toyota Corolla"
            required
            id="input-model"
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Daily Rate (LKR) *</label>
          <input
            className="form-input"
            type="number"
            name="dailyRate"
            value={form.dailyRate}
            onChange={handleChange}
            placeholder="e.g. 5000"
            min="1"
            step="0.01"
            required
            id="input-daily-rate"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Year *</label>
          <input
            className="form-input"
            type="number"
            name="year"
            value={form.year}
            onChange={handleChange}
            placeholder="e.g. 2023"
            min="1990"
            required
            id="input-year"
          />
        </div>
      </div>

      <div className="form-row">
        <div className="form-group">
          <label className="form-label">Current Odometer (km) *</label>
          <input
            className="form-input"
            type="number"
            name="currentOdometer"
            value={form.currentOdometer}
            onChange={handleChange}
            placeholder="e.g. 15000"
            min="0"
            required
            id="input-odometer"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Category *</label>
          <select
            className="form-select"
            name="categoryId"
            value={form.categoryId}
            onChange={handleChange}
            required
            id="select-category"
          >
            <option value="">Select a category</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Vehicle Images</label>
        <label className="image-upload-zone" htmlFor="vehicle-images-input">
          <HiOutlineUpload className="upload-icon" />
          <span>Click to upload images</span>
          <span className="upload-hint">PNG, JPG up to 10MB</span>
        </label>
        <input
          type="file"
          id="vehicle-images-input"
          multiple
          accept="image/*"
          onChange={handleImageChange}
          style={{ display: 'none' }}
        />
        {previews.length > 0 && (
          <div className="image-previews">
            {previews.map((url, idx) => (
              <div className="preview-item" key={idx}>
                <img src={url} alt={`Preview ${idx + 1}`} />
                <button
                  type="button"
                  className="preview-remove"
                  onClick={() => removeImage(idx)}
                >
                  <HiOutlineX />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <button
        type="submit"
        className="btn btn-primary btn-lg"
        disabled={loading}
        id="submit-vehicle-btn"
        style={{ width: '100%' }}
      >
        {loading ? 'Creating Vehicle...' : 'Create Vehicle'}
      </button>
    </form>
  );
}
