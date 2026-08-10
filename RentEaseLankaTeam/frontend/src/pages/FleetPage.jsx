import { useState, useEffect, useCallback } from 'react';
import API from '../api/axiosInstance';
import VehicleCard from '../components/fleet/VehicleCard';
import VehicleFilters from '../components/fleet/VehicleFilters';
import AddVehicleForm from '../components/fleet/AddVehicleForm';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { HiOutlinePlus } from 'react-icons/hi';

export default function FleetPage() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);

  const fetchAllVehicles = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get('/vehicles');
      setVehicles(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAllVehicles();
  }, [fetchAllVehicles]);

  const handleFilter = async (filterType, value) => {
    setLoading(true);
    try {
      const params = { filterBy: filterType };
      if (filterType === 'category') params.category = value;
      if (filterType === 'status') params.status = value;
      const res = await API.get('/vehicle', { params });
      setVehicles(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilter = () => {
    fetchAllVehicles();
  };

  return (
    <div className="page-container" id="fleet-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Fleet Management</h1>
          <p className="page-subtitle">
            Manage all vehicles in your fleet ({vehicles.length} vehicles)
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowAddModal(true)}
          id="add-vehicle-btn"
        >
          <HiOutlinePlus />
          Add Vehicle
        </button>
      </div>

      <VehicleFilters onFilter={handleFilter} onClear={handleClearFilter} />

      {loading ? (
        <LoadingSpinner message="Loading vehicles..." />
      ) : vehicles.length === 0 ? (
        <EmptyState
          title="No vehicles found"
          message="Add your first vehicle to get started or adjust your filters."
        />
      ) : (
        <div className="grid-cards">
          {vehicles.map((vehicle) => (
            <VehicleCard key={vehicle.id} vehicle={vehicle} />
          ))}
        </div>
      )}

      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Vehicle"
        size="lg"
      >
        <AddVehicleForm
          onSuccess={() => {
            setShowAddModal(false);
            fetchAllVehicles();
          }}
        />
      </Modal>
    </div>
  );
}
