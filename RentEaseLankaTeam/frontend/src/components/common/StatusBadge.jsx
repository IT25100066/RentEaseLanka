import './StatusBadge.css';

const statusConfig = {
  // Vehicle statuses
  AVAILABLE: { label: 'Available', className: 'badge-emerald' },
  PENDING: { label: 'Pending', className: 'badge-amber' },
  RENTED: { label: 'Rented', className: 'badge-sky' },
  UNDER_MAINTENANCE: { label: 'Under Maintenance', className: 'badge-violet' },
  OUT_OF_SERVICE: { label: 'Out of Service', className: 'badge-rose' },
  RESERVED: { label: 'Reserved', className: 'badge-primary' },
  // Maintenance statuses
  SCHEDULED: { label: 'Scheduled', className: 'badge-amber' },
  IN_PROGRESS: { label: 'In Progress', className: 'badge-sky' },
  COMPLETED: { label: 'Completed', className: 'badge-emerald' },
  CANCELLED: { label: 'Cancelled', className: 'badge-rose' },
};

export default function StatusBadge({ status }) {
  const config = statusConfig[status] || { label: status, className: 'badge-default' };

  return (
    <span className={`status-badge ${config.className}`}>
      <span className="badge-dot"></span>
      {config.label}
    </span>
  );
}
