import { useState } from 'react';
import { toast } from 'react-toastify';
import { createStaff } from '../../api/users';
import '../PanelPages.css';

export default function CreateStaffPage() {
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
      await createStaff(formData);
      toast.success('Staff member created successfully!');
      setFormData({ firstName: '', lastName: '', email: '', password: '' });
    } catch (error) {
      const msg = error.response?.data?.message || error.response?.data || 'Failed to create staff';
      toast.error(typeof msg === 'string' ? msg : 'Failed to create staff');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-form-page" id="create-staff-page">
      <div className="create-form-card">
        <h1>Create Staff Member</h1>
        <p>Add a new staff member to the team</p>

        <form className="create-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="staff-firstName">First Name</label>
              <input type="text" id="staff-firstName" name="firstName" value={formData.firstName} onChange={handleChange} required placeholder="Jane" />
            </div>
            <div className="form-group">
              <label htmlFor="staff-lastName">Last Name</label>
              <input type="text" id="staff-lastName" name="lastName" value={formData.lastName} onChange={handleChange} required placeholder="Smith" />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="staff-email">Email</label>
            <input type="email" id="staff-email" name="email" value={formData.email} onChange={handleChange} required placeholder="staff@renteaselanka.com" />
          </div>

          <div className="form-group">
            <label htmlFor="staff-password">Password</label>
            <input type="password" id="staff-password" name="password" value={formData.password} onChange={handleChange} required placeholder="Enter password" />
          </div>

          <button type="submit" className="create-form-btn" disabled={loading}>
            {loading ? 'Creating…' : 'Create Staff'}
          </button>
        </form>
      </div>
    </div>
  );
}
