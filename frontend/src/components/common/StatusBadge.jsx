import './StatusBadge.css';

const statusConfig = {
  // Vehicle statuses
  AVAILABLE: { label: 'Available', className: 'badge-emerald' },
  PENDING: { label: 'Pending', className: 'badge-amber' },
  CONFIRMED: { label: 'Confirmed', className: 'badge-sky' },
  ACTIVE: { label: 'Active', className: 'badge-emerald' },
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
  const normalizedStatus = status
    ? String(status).toUpperCase().trim().replace(/_/g, ' ')
    : '';

  const config = statusConfig[normalizedStatus] || {
    label: status ? String(status).replace(/_/g, ' ') : 'Unknown',
    className: 'badge-default',
  };

  return (
    <span className={`status-badge ${config.className}`}>
      <span className="badge-dot"></span>
      {config.label}
    </span>
  );
}
