import { useState, useEffect } from 'react';
import API, { BASE_URL } from '../api/axiosInstance';
import { toast } from 'react-toastify';
import StatusBadge from '../components/common/StatusBadge';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { HiOutlineCheckCircle, HiOutlineClock } from 'react-icons/hi';
import './PendingVehiclesPage.css';


export default function PendingVehiclesPage() {
  const [vehicles, setVehicles] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState(false);

  const fetchPending = async () => {
    setLoading(true);
    try {
      const res = await API.get('/pendingVehicles');
      setVehicles(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, []);

  const toggleSelect = (id) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]
    );
  };

  const selectAll = () => {
    if (selected.length === vehicles.length) {
      setSelected([]);
    } else {
      setSelected(vehicles.map((v) => v.id));
    }
  };

  const handleApprove = async () => {
    if (selected.length === 0) {
      toast.warning('Please select at least one vehicle.');
      return;
    }
    setApproving(true);
    try {
      await API.post('/vehicle/makeAvailable', selected);
      toast.success(`${selected.length} vehicle(s) approved successfully!`);
      setSelected([]);
      fetchPending();
    } catch (err) {
      toast.error('Failed to approve vehicles.');
    } finally {
      setApproving(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading pending vehicles..." />;

  return (
    <div className="page-container" id="pending-vehicles-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Pending Approvals</h1>
          <p className="page-subtitle">
            {vehicles.length} vehicle(s) awaiting approval
          </p>
        </div>
        {vehicles.length > 0 && (
          <div className="pending-actions">
            <button
              className="btn btn-secondary btn-sm"
              onClick={selectAll}
              id="select-all-btn"
            >
              {selected.length === vehicles.length ? 'Deselect All' : 'Select All'}
            </button>
            <button
              className="btn btn-success"
              onClick={handleApprove}
              disabled={selected.length === 0 || approving}
              id="approve-selected-btn"
            >
              <HiOutlineCheckCircle />
              {approving
                ? 'Approving...'
                : `Approve Selected (${selected.length})`}
            </button>
          </div>
        )}
      </div>

      {vehicles.length === 0 ? (
        <EmptyState
          title="No pending vehicles"
          message="All vehicles have been reviewed. Check back later!"
          icon={<HiOutlineClock />}
        />
      ) : (
        <div className="table-container">
          <table className="data-table" id="pending-vehicles-table">
            <thead>
              <tr>
                <th style={{ width: 40 }}>
                  <input
                    type="checkbox"
                    checked={selected.length === vehicles.length && vehicles.length > 0}
                    onChange={selectAll}
                    id="select-all-checkbox"
                  />
                </th>
                <th>Vehicle</th>
                <th>License Plate</th>
                <th>Category</th>
                <th>Year</th>
                <th>Daily Rate</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.map((v) => (
                <tr
                  key={v.id}
                  className={selected.includes(v.id) ? 'row-selected' : ''}
                >
                  <td>
                    <input
                      type="checkbox"
                      checked={selected.includes(v.id)}
                      onChange={() => toggleSelect(v.id)}
                      id={`select-vehicle-${v.id}`}
                    />
                  </td>
                  <td>
                    <div className="table-vehicle-cell">
                      <div className="table-vehicle-thumb">
                        {v.imageUrls && v.imageUrls.length > 0 ? (
                          <img src={`${BASE_URL}${v.imageUrls[0]}`} alt={v.model} />
                        ) : (
                          <span>🚗</span>
                        )}
                      </div>
                      <span className="table-vehicle-model">{v.model}</span>
                    </div>
                  </td>
                  <td><code className="plate-code">{v.licensePlate}</code></td>
                  <td>{v.categoryName || '—'}</td>
                  <td>{v.year}</td>
                  <td className="rate-cell">LKR {v.dailyRentalRate?.toLocaleString()}</td>
                  <td><StatusBadge status={v.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
