import { useState } from 'react';
import { toast } from 'react-toastify';
import { createDriver } from '../../api/drivers';
import '../PanelPages.css';

const initialForm = { firstName: '', lastName: '', email: '', password: '' };

export default function CreateDriverPage() {
  const [formData, setFormData] = useState(initialForm);
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await createDriver(formData);
      toast.success('Driver created successfully.');
      setFormData(initialForm);
    } catch (error) {
      const message = error.response?.data?.message || error.response?.data || 'Failed to create driver';
      toast.error(typeof message === 'string' ? message : 'Failed to create driver');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-form-page" id="create-driver-page">
      <div className="create-form-card">
        <h1>Create Driver</h1>
        <p>Create a driver account for booking assignments.</p>
        <form className="create-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="driver-firstName">First Name</label>
              <input type="text" id="driver-firstName" name="firstName" value={formData.firstName} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label htmlFor="driver-lastName">Last Name</label>
              <input type="text" id="driver-lastName" name="lastName" value={formData.lastName} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-group">
            <label htmlFor="driver-email">Email</label>
            <input type="email" id="driver-email" name="email" value={formData.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="driver-password">Temporary Password</label>
            <input type="password" id="driver-password" name="password" value={formData.password} onChange={handleChange} required minLength={6} />
          </div>
          <button type="submit" className="create-form-btn" disabled={loading}>
            {loading ? 'Creating...' : 'Create Driver'}
          </button>
        </form>
      </div>
    </div>
  );
}
