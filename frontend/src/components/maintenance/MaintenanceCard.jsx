import StatusBadge from '../common/StatusBadge';
import {
  HiOutlinePlay,
  HiOutlineCheck,
  HiOutlineTrash,
} from 'react-icons/hi';
import './MaintenanceCard.css';

const typeLabels = {
  ROUTINE_SERVICE: 'Routine Service',
  TIRE_CHANGE: 'Tire Change',
  ENGINE_REPAIR: 'Engine Repair',
  ACCIDENT_REPAIR: 'Accident Repair',
};

export default function MaintenanceCard({ record, onStart, onComplete, onCancel }) {
  const isScheduled = record.status === 'SCHEDULED';
  const isInProgress = record.status === 'IN_PROGRESS';
  const isTerminal = record.status === 'COMPLETED' || record.status === 'CANCELLED';

  return (
    <div className="maintenance-card card" id={`maintenance-card-${record.id}`}>
      <div className="mc-header">
        <div className="mc-type-info">
          <span className="mc-type">{typeLabels[record.type] || record.type}</span>
          <span className="mc-id">#{record.id}</span>
        </div>
        <StatusBadge status={record.status} />
      </div>

      <div className="mc-body">
        {record.description && (
          <p className="mc-description">{record.description}</p>
        )}

        <div className="mc-meta-grid">
          {record.scheduledDate && (
            <div className="mc-meta">
              <span className="mc-meta-label">Scheduled</span>
              <span className="mc-meta-value">
                {new Date(record.scheduledDate).toLocaleDateString()}
              </span>
            </div>
          )}
          {record.completionDate && (
            <div className="mc-meta">
              <span className="mc-meta-label">Completed</span>
              <span className="mc-meta-value">
                {new Date(record.completionDate).toLocaleDateString()}
              </span>
            </div>
          )}
          {record.odometerAtService != null && (
            <div className="mc-meta">
              <span className="mc-meta-label">Odometer</span>
              <span className="mc-meta-value">
                {record.odometerAtService?.toLocaleString()} km
              </span>
            </div>
          )}
          {record.cost != null && (
            <div className="mc-meta">
              <span className="mc-meta-label">Cost</span>
              <span className="mc-meta-value mc-cost">
                LKR {record.cost?.toLocaleString()}
              </span>
            </div>
          )}
        </div>
      </div>

      {!isTerminal && (
        <div className="mc-actions">
          {isScheduled && (
            <>
              <button
                className="btn btn-warning btn-sm"
                onClick={() => onStart(record.id)}
                id={`start-maintenance-${record.id}`}
              >
                <HiOutlinePlay />
                Start
              </button>
              <button
                className="btn btn-danger btn-sm"
                onClick={() => onCancel(record.id)}
                id={`cancel-maintenance-${record.id}`}
              >
                <HiOutlineTrash />
                Cancel
              </button>
            </>
          )}
          {isInProgress && (
            <button
              className="btn btn-success btn-sm"
              onClick={() => onComplete(record)}
              id={`complete-maintenance-${record.id}`}
            >
              <HiOutlineCheck />
              Complete
            </button>
          )}
        </div>
      )}
    </div>
  );
}
