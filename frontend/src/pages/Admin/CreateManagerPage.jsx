import { useState } from 'react';
import { toast } from 'react-toastify';
import { createManager } from '../../api/users';
import '../PanelPages.css';

export default function CreateManagerPage() {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await createManager(formData);
      toast.success('Manager created successfully!');
      setFormData({ firstName: '', lastName: '', email: '', password: '' });
    } catch (error) {
      const msg = error.response?.data?.message || error.response?.data || 'Failed to create manager';
      toast.error(typeof msg === 'string' ? msg : 'Failed to create manager');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-form-page" id="create-manager-page">
      <div className="create-form-card">
        <h1>Create Manager</h1>
        <p>Add a new manager to the platform</p>

        <form className="create-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="mgr-firstName">First Name</label>
              <input type="text" id="mgr-firstName" name="firstName" value={formData.firstName} onChange={handleChange} required placeholder="John" />
            </div>
            <div className="form-group">
              <label htmlFor="mgr-lastName">Last Name</label>
              <input type="text" id="mgr-lastName" name="lastName" value={formData.lastName} onChange={handleChange} required placeholder="Doe" />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="mgr-email">Email</label>
            <input type="email" id="mgr-email" name="email" value={formData.email} onChange={handleChange} required placeholder="manager@renteaselanka.com" />
          </div>

          <div className="form-group">
            <label htmlFor="mgr-password">Password</label>
            <input type="password" id="mgr-password" name="password" value={formData.password} onChange={handleChange} required placeholder="Enter password" />
          </div>

          <button type="submit" className="create-form-btn" disabled={loading}>
            {loading ? 'Creating…' : 'Create Manager'}
          </button>
        </form>
      </div>
    </div>
  );
}
