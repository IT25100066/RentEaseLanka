import { useState, useEffect, useMemo } from 'react';
import API from '../api/axiosInstance';
import { toast } from 'react-toastify';
import StatusBadge from '../components/common/StatusBadge';
import ScheduleMaintenanceForm from '../components/maintenance/ScheduleMaintenanceForm';
import CompleteMaintenanceModal from '../components/maintenance/CompleteMaintenanceModal';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import {
  HiOutlinePlus,
  HiOutlineCog,
  HiOutlineSearch,
  HiOutlineFilter,
  HiOutlineX,
  HiOutlinePlay,
  HiOutlineCheck,
  HiOutlineTrash,
  HiOutlineChevronUp,
  HiOutlineChevronDown,
} from 'react-icons/hi';
import './MaintenancePage.css';

const typeLabels = {
  ROUTINE_SERVICE: 'Routine Service',
  TIRE_CHANGE: 'Tire Change',
  ENGINE_REPAIR: 'Engine Repair',
  ACCIDENT_REPAIR: 'Accident Repair',
};

const maintenanceStatuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
const maintenanceTypes = ['ROUTINE_SERVICE', 'TIRE_CHANGE', 'ENGINE_REPAIR', 'ACCIDENT_REPAIR'];

export default function MaintenancePage() {
  const [vehicles, setVehicles] = useState([]);
  const [allRecords, setAllRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);

  // Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [filterVehicle, setFilterVehicle] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [sortField, setSortField] = useState('scheduledDate');
  const [sortDir, setSortDir] = useState('desc');

  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    setLoading(true);
    try {
      const vehiclesRes = await API.get('/vehicles');
      setVehicles(vehiclesRes.data);

      // Fetch records for all vehicles
      const recordPromises = vehiclesRes.data.map((v) =>
        API.get(`/maintenance/vehicle/${v.id}`)
          .then((res) =>
            res.data.map((r) => ({
              ...r,
              vehicleModel: v.model,
              vehiclePlate: v.licensePlate,
              vehicleId: v.id,
            }))
          )
          .catch(() => [])
      );
      const allResults = await Promise.all(recordPromises);
      setAllRecords(allResults.flat());
    } catch (err) {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleStartMaintenance = async (recordId) => {
    try {
      await API.put(`/maintenance/${recordId}/startMaintenance`);
      toast.success('Maintenance started!');
      loadAllData();
    } catch (err) {
      toast.error('Failed to start maintenance.');
    }
  };

  const handleCancelMaintenance = async (recordId) => {
    try {
      await API.delete(`/maintenance/${recordId}`);
      toast.success('Maintenance cancelled.');
      loadAllData();
    } catch (err) {
      toast.error('Failed to cancel maintenance.');
    }
  };

  const handleCompleteMaintenance = (record) => {
    setSelectedRecord(record);
    setShowCompleteModal(true);
  };

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDir('asc');
    }
  };

  const clearFilters = () => {
    setSearchQuery('');
    setFilterVehicle('');
    setFilterType('');
    setFilterStatus('');
  };

  const hasActiveFilters = searchQuery || filterVehicle || filterType || filterStatus;

  const filteredRecords = useMemo(() => {
    let records = [...allRecords];

    // Apply search
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      records = records.filter(
        (r) =>
          r.vehicleModel?.toLowerCase().includes(q) ||
          r.vehiclePlate?.toLowerCase().includes(q) ||
          r.description?.toLowerCase().includes(q) ||
          String(r.id).includes(q)
      );
    }

    // Apply filters
    if (filterVehicle) {
      records = records.filter((r) => String(r.vehicleId) === filterVehicle);
    }
    if (filterType) {
      records = records.filter((r) => r.type === filterType);
    }
    if (filterStatus) {
      records = records.filter((r) => r.status === filterStatus);
    }

    // Sort
    records.sort((a, b) => {
      let valA, valB;
      switch (sortField) {
        case 'id':
          valA = a.id;
          valB = b.id;
          break;
        case 'vehicleModel':
          valA = (a.vehicleModel || '').toLowerCase();
          valB = (b.vehicleModel || '').toLowerCase();
          break;
        case 'type':
          valA = a.type || '';
          valB = b.type || '';
          break;
        case 'status':
          valA = a.status || '';
          valB = b.status || '';
          break;
        case 'scheduledDate':
          valA = a.scheduledDate || '';
          valB = b.scheduledDate || '';
          break;
        case 'completionDate':
          valA = a.completionDate || '';
          valB = b.completionDate || '';
          break;
        case 'cost':
          valA = a.cost || 0;
          valB = b.cost || 0;
          break;
        default:
          return 0;
      }
      if (valA < valB) return sortDir === 'asc' ? -1 : 1;
      if (valA > valB) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });

    return records;
  }, [allRecords, searchQuery, filterVehicle, filterType, filterStatus, sortField, sortDir]);

  const SortIcon = ({ field }) => {
    if (sortField !== field) return null;
    return sortDir === 'asc' ? (
      <HiOutlineChevronUp className="sort-icon" />
    ) : (
      <HiOutlineChevronDown className="sort-icon" />
    );
  };

  if (loading) return <LoadingSpinner message="Loading maintenance records..." />;

  return (
    <div className="page-container" id="maintenance-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Maintenance Management</h1>
          <p className="page-subtitle">
            Track and manage all vehicle maintenance records ({allRecords.length} total)
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowScheduleModal(true)}
          id="schedule-new-btn"
        >
          <HiOutlinePlus />
          Schedule Maintenance
        </button>
      </div>

      {/* Filters Bar */}
      <div className="maintenance-filters" id="maintenance-filters">
        <div className="maint-filter-search">
          <HiOutlineSearch className="maint-filter-icon" />
          <input
            type="text"
            placeholder="Search records..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="maint-filter-input"
            id="maintenance-search"
          />
        </div>

        <div className="maint-filter-group">
          <select
            className="maint-filter-select"
            value={filterVehicle}
            onChange={(e) => setFilterVehicle(e.target.value)}
            id="filter-by-vehicle"
          >
            <option value="">All Vehicles</option>
            {vehicles.map((v) => (
              <option key={v.id} value={v.id}>
                {v.model} — {v.licensePlate}
              </option>
            ))}
          </select>

          <select
            className="maint-filter-select"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            id="filter-by-type"
          >
            <option value="">All Types</option>
            {maintenanceTypes.map((t) => (
              <option key={t} value={t}>
                {typeLabels[t] || t}
              </option>
            ))}
          </select>

          <select
            className="maint-filter-select"
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            id="filter-by-status"
          >
            <option value="">All Statuses</option>
            {maintenanceStatuses.map((s) => (
              <option key={s} value={s}>
                {s.replace(/_/g, ' ')}
              </option>
            ))}
          </select>

          {hasActiveFilters && (
            <button className="maint-filter-clear" onClick={clearFilters} id="clear-maint-filters">
              <HiOutlineX />
              Clear
            </button>
          )}
        </div>

        <div className="maint-filter-count">
          {filteredRecords.length} record{filteredRecords.length !== 1 ? 's' : ''}
        </div>
      </div>

      {/* Table */}
      {filteredRecords.length === 0 ? (
        <EmptyState
          title="No maintenance records found"
          message={
            hasActiveFilters
              ? 'Try adjusting your filters to find records.'
              : 'No maintenance records yet. Schedule one to get started.'
          }
          icon={<HiOutlineCog />}
        />
      ) : (
        <div className="table-container maintenance-table-container">
          <table className="data-table maintenance-table" id="maintenance-table">
            <thead>
              <tr>
                <th className="sortable-th" onClick={() => handleSort('id')}>
                  ID <SortIcon field="id" />
                </th>
                <th className="sortable-th" onClick={() => handleSort('vehicleModel')}>
                  Vehicle <SortIcon field="vehicleModel" />
                </th>
                <th className="sortable-th" onClick={() => handleSort('type')}>
                  Type <SortIcon field="type" />
                </th>
                <th className="sortable-th" onClick={() => handleSort('status')}>
                  Status <SortIcon field="status" />
                </th>
                <th className="sortable-th" onClick={() => handleSort('scheduledDate')}>
                  Scheduled <SortIcon field="scheduledDate" />
                </th>
                <th className="sortable-th" onClick={() => handleSort('completionDate')}>
                  Completed <SortIcon field="completionDate" />
                </th>
                <th>Odometer</th>
                <th className="sortable-th" onClick={() => handleSort('cost')}>
                  Cost <SortIcon field="cost" />
                </th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredRecords.map((record) => {
                const isScheduled = record.status === 'SCHEDULED';
                const isInProgress = record.status === 'IN_PROGRESS';
                const isTerminal = record.status === 'COMPLETED' || record.status === 'CANCELLED';

                return (
                  <tr key={record.id} id={`maint-row-${record.id}`}>
                    <td>
                      <code className="plate-code">#{record.id}</code>
                    </td>
                    <td>
                      <div className="maint-vehicle-cell">
                        <span className="maint-vehicle-model">{record.vehicleModel}</span>
                        <span className="maint-vehicle-plate">{record.vehiclePlate}</span>
                      </div>
                    </td>
                    <td>
                      <span className="maint-type-badge">
                        {typeLabels[record.type] || record.type}
                      </span>
                    </td>
                    <td>
                      <StatusBadge status={record.status} />
                    </td>
                    <td className="date-cell">
                      {record.scheduledDate
                        ? new Date(record.scheduledDate).toLocaleDateString()
                        : '—'}
                    </td>
                    <td className="date-cell">
                      {record.completionDate
                        ? new Date(record.completionDate).toLocaleDateString()
                        : '—'}
                    </td>
                    <td>
                      {record.odometerAtService != null
                        ? `${record.odometerAtService.toLocaleString()} km`
                        : '—'}
                    </td>
                    <td className="rate-cell">
                      {record.cost != null
                        ? `LKR ${record.cost.toLocaleString()}`
                        : '—'}
                    </td>
                    <td>
                      {!isTerminal && (
                        <div className="maint-actions">
                          {isScheduled && (
                            <>
                              <button
                                className="btn btn-warning btn-sm"
                                onClick={() => handleStartMaintenance(record.id)}
                                title="Start Maintenance"
                                id={`start-maint-${record.id}`}
                              >
                                <HiOutlinePlay />
                              </button>
                              <button
                                className="btn btn-danger btn-sm"
                                onClick={() => handleCancelMaintenance(record.id)}
                                title="Cancel"
                                id={`cancel-maint-${record.id}`}
                              >
                                <HiOutlineTrash />
                              </button>
                            </>
                          )}
                          {isInProgress && (
                            <button
                              className="btn btn-success btn-sm"
                              onClick={() => handleCompleteMaintenance(record)}
                              title="Complete"
                              id={`complete-maint-${record.id}`}
                            >
                              <HiOutlineCheck />
                            </button>
                          )}
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Schedule Modal */}
      <Modal
        isOpen={showScheduleModal}
        onClose={() => setShowScheduleModal(false)}
        title="Schedule Maintenance"
        size="md"
      >
        <ScheduleMaintenanceForm
          onSuccess={() => {
            setShowScheduleModal(false);
            loadAllData();
          }}
          onClose={() => setShowScheduleModal(false)}
        />
      </Modal>

      {/* Complete Modal */}
      <Modal
        isOpen={showCompleteModal}
        onClose={() => setShowCompleteModal(false)}
        title="Complete Maintenance"
        size="sm"
      >
        {selectedRecord && (
          <CompleteMaintenanceModal
            record={selectedRecord}
            onSuccess={() => {
              setShowCompleteModal(false);
              loadAllData();
            }}
            onClose={() => setShowCompleteModal(false)}
          />
        )}
      </Modal>
    </div>
  );
}
