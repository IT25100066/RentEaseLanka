import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import {
  HiOutlineClipboardCheck,
  HiOutlineRefresh,
  HiOutlineEye,
  HiOutlineSearch,
  HiX,
} from 'react-icons/hi';
import API, { extractErrorMessage } from '../../api/axiosInstance';
import { inspectVehicleReturn, getReturnInspection } from '../../api/returns';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import './VehicleReturnPage.css';

/* ─── Helper ─────────────────────────────────────────────── */
const fmt = (dt) =>
  dt
    ? new Date(dt).toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' })
    : '—';

const getBookingId = (b) => b?.bookingId || b?.BookingId || b?.id;

const FILTER_OPTIONS = ['ALL', 'ACTIVE', 'CONFIRMED'];

/* ════════════════════════════════════════════════════════════
   MODAL — Create Return Inspection
   ════════════════════════════════════════════════════════════ */
function CreateInspectionModal({ booking, staffId, onClose, onSaved }) {
  const [endingMileage, setEndingMileage] = useState('');
  const [hasDamage, setHasDamage] = useState(false);
  const [damageDescription, setDamageDescription] = useState('');
  const [damageAmount, setDamageAmount] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);

  const bId = getBookingId(booking);

  const submit = async () => {
    const mileage = Number(endingMileage);
    if (!Number.isFinite(mileage) || mileage < 0) {
      toast.warning('Enter a valid ending mileage.');
      return;
    }
    if (hasDamage && !damageDescription.trim()) {
      toast.warning('Please describe the vehicle damage found at return.');
      return;
    }
    if (hasDamage && (!Number.isFinite(Number(damageAmount)) || Number(damageAmount) <= 0)) {
      toast.warning('Enter a positive damage amount to deduct from the customer deposit.');
      return;
    }
    setBusy(true);
    try {
      await inspectVehicleReturn(bId, {
        staffId,
        endingMileage: mileage,
        hasDamage,
        damageDescription: hasDamage ? damageDescription.trim() : null,
        damageAmount: hasDamage ? Number(damageAmount) : null,
        notes: notes.trim() || null,
      });
      toast.success('Return inspection saved, deposit settled, and booking completed.');
      onSaved();
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Could not record vehicle return inspection.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="return-inspection-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon primary"><HiOutlineClipboardCheck /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Vehicle Return Inspection</p>
            <p className="ho-modal-subtitle">
              Booking #{bId} · {booking.model || `Vehicle ${booking.vehicleId}`}
            </p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>
        <div className="ho-modal-body ho-initiate-form">
          <p className="ho-info-text">
            Saving this inspection settles the security deposit automatically. If the rental
            invoice is fully paid, the booking will be completed.
          </p>

          {/* Booking summary */}
          <div className="ho-vehicle-panel">
            <div className="ho-vehicle-panel-item">
              <span className="ho-vehicle-panel-label">Vehicle</span>
              <span className="ho-vehicle-panel-value">{booking.model || `ID: ${booking.vehicleId}`}</span>
            </div>
            <div className="ho-vehicle-panel-item">
              <span className="ho-vehicle-panel-label">Customer</span>
              <span className="ho-vehicle-panel-value">{booking.customerName || `ID: ${booking.customerId}`}</span>
            </div>
            <div className="ho-vehicle-panel-item">
              <span className="ho-vehicle-panel-label">Pickup Date</span>
              <span className="ho-vehicle-panel-value">{fmt(booking.pickupDateTime)}</span>
            </div>
            <div className="ho-vehicle-panel-item">
              <span className="ho-vehicle-panel-label">Return Date</span>
              <span className="ho-vehicle-panel-value">{fmt(booking.returnDateTime)}</span>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Ending Mileage (km) *</label>
            <input
              id="ri-ending-mileage"
              className="form-input"
              type="number"
              min="0"
              placeholder="Enter ending odometer reading"
              value={endingMileage}
              onChange={(e) => setEndingMileage(e.target.value)}
            />
          </div>

          <label className="ho-check-row">
            <input
              type="checkbox"
              checked={hasDamage}
              onChange={(e) => setHasDamage(e.target.checked)}
            />
            <span>Damage found during return inspection</span>
          </label>

          {hasDamage && (
            <div className="form-group">
              <label className="form-label">Damage Description *</label>
              <textarea
                id="ri-damage-description"
                className="form-textarea"
                rows="3"
                placeholder="Describe the damage in detail..."
                value={damageDescription}
                onChange={(e) => setDamageDescription(e.target.value)}
              />
            </div>
          )}

          {hasDamage && (
            <div className="form-group">
              <label className="form-label">Damage Amount to Deduct from Deposit (LKR) *</label>
              <input
                id="ri-damage-amount"
                className="form-input"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="e.g. 5000"
                value={damageAmount}
                onChange={(e) => setDamageAmount(e.target.value)}
              />
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Return Notes</label>
            <textarea
              id="ri-notes"
              className="form-textarea"
              rows="3"
              placeholder="Any additional notes about the return..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>
        </div>
        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" disabled={busy} onClick={onClose}>
            Cancel
          </button>
          <button
            id="ri-submit-btn"
            className="btn btn-primary btn-sm"
            disabled={busy}
            onClick={submit}
          >
            {busy ? 'Saving…' : '📋 Save Return Inspection'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MODAL — View Return Inspection Details
   ════════════════════════════════════════════════════════════ */
function ViewInspectionModal({ inspection, onClose }) {
  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="view-inspection-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon emerald"><HiOutlineClipboardCheck /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Return Inspection Details</p>
            <p className="ho-modal-subtitle">
              Booking #{inspection.bookingId} · Inspection #{inspection.id}
            </p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>
        <div className="ho-modal-body">
          <div className="ri-detail-grid">
            <div className="ri-detail-item">
              <span className="ri-detail-label">Booking ID</span>
              <span className="ri-detail-value">#{inspection.bookingId}</span>
            </div>
            <div className="ri-detail-item">
              <span className="ri-detail-label">Vehicle ID</span>
              <span className="ri-detail-value">#{inspection.vehicleId}</span>
            </div>
            <div className="ri-detail-item">
              <span className="ri-detail-label">Staff ID</span>
              <span className="ri-detail-value">{inspection.staffId}</span>
            </div>
            <div className="ri-detail-item">
              <span className="ri-detail-label">Ending Mileage</span>
              <span className="ri-detail-value">{inspection.endingMileage?.toLocaleString()} km</span>
            </div>
            <div className="ri-detail-item">
              <span className="ri-detail-label">Damage Status</span>
              <span className="ri-detail-value">
                <span className={`ri-damage-badge ${inspection.hasDamage ? 'has-damage' : 'no-damage'}`}>
                  {inspection.hasDamage ? '⚠️ Damage Found' : '✅ No Damage'}
                </span>
              </span>
            </div>
            <div className="ri-detail-item">
              <span className="ri-detail-label">Inspected At</span>
              <span className="ri-detail-value">{fmt(inspection.inspectedAt)}</span>
            </div>
            {inspection.hasDamage && inspection.damageDescription && (
              <div className="ri-detail-item full-width">
                <span className="ri-detail-label">Damage Description</span>
                <span className="ri-detail-value">{inspection.damageDescription}</span>
              </div>
            )}
            {inspection.notes && (
              <div className="ri-detail-item full-width">
                <span className="ri-detail-label">Notes</span>
                <span className="ri-detail-value">{inspection.notes}</span>
              </div>
            )}
          </div>
        </div>
        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MAIN PAGE
   ════════════════════════════════════════════════════════════ */
export default function VehicleReturnPage() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [searchId, setSearchId] = useState('');
  const [modal, setModal] = useState(null);
  const [inspectionCache, setInspectionCache] = useState({});
  const [checkingInspections, setCheckingInspections] = useState(false);

  const defaultStaffId =
    user?.staffId || (user?.id ? `STAFF-${user.id}` : user?.email || 'STAFF');

  /* ─── Load bookings that can be inspected (ACTIVE / CONFIRMED) ─── */
  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await API.get('/booking/all');
      const all = res.data || [];
      // Filter to ACTIVE and CONFIRMED bookings (return inspection targets)
      const eligible = all.filter(
        (b) => b.status === 'ACTIVE' || b.status === 'CONFIRMED'
      );
      // Sort: ACTIVE first, then CONFIRMED, by ID descending
      eligible.sort((a, b) => {
        if (a.status === 'ACTIVE' && b.status !== 'ACTIVE') return -1;
        if (a.status !== 'ACTIVE' && b.status === 'ACTIVE') return 1;
        return (getBookingId(b) || 0) - (getBookingId(a) || 0);
      });
      setBookings(eligible);

      // Check which bookings already have inspections
      setCheckingInspections(true);
      const cache = {};
      await Promise.all(
        eligible.map(async (b) => {
          const bId = getBookingId(b);
          try {
            const inspection = await getReturnInspection(bId);
            cache[bId] = inspection;
          } catch {
            // No inspection yet — expected
            cache[bId] = null;
          }
        })
      );
      setInspectionCache(cache);
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to load bookings.'));
    } finally {
      setLoading(false);
      setCheckingInspections(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onSaved = () => {
    setModal(null);
    load();
  };

  const openCreate = (booking) => setModal({ type: 'create', booking });
  const openView = (inspection) => setModal({ type: 'view', inspection });
  const closeModal = () => setModal(null);

  /* ─── Search ─── */
  const handleSearch = async (e) => {
    e.preventDefault();
    const id = searchId.trim();
    if (!id || !/^\d+$/.test(id)) {
      toast.warning('Enter a valid booking ID.');
      return;
    }
    try {
      const inspection = await getReturnInspection(Number(id));
      setModal({ type: 'view', inspection });
    } catch (err) {
      toast.error(extractErrorMessage(err, `No return inspection found for booking #${id}.`));
    }
  };

  /* ─── Filtered bookings ─── */
  const filteredBookings =
    filter === 'ALL' ? bookings : bookings.filter((b) => b.status === filter);

  /* ─── Stats ─── */
  const stats = {
    total: bookings.length,
    active: bookings.filter((b) => b.status === 'ACTIVE').length,
    confirmed: bookings.filter((b) => b.status === 'CONFIRMED').length,
    inspected: Object.values(inspectionCache).filter(Boolean).length,
    pending: Object.values(inspectionCache).filter((v) => v === null).length,
  };

  return (
    <div className="page-container return-page" id="vehicle-return-page">
      {/* ── Header ── */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicle Return Inspections</h1>
          <p className="page-subtitle">
            Inspect returning vehicles and record condition, mileage, and damage
          </p>
        </div>
        <div className="return-toolbar">
          <button
            className="btn btn-secondary btn-sm"
            id="return-refresh-btn"
            onClick={load}
            title="Refresh"
          >
            <HiOutlineRefresh /> Refresh
          </button>
        </div>
      </div>

      {/* ── Search by Booking ID ── */}
      <form className="booking-search" onSubmit={handleSearch} style={{ marginBottom: 20 }}>
        <HiOutlineSearch className="booking-search-icon" aria-hidden="true" />
        <input
          type="search"
          value={searchId}
          onChange={(e) => setSearchId(e.target.value)}
          placeholder="Search inspection by booking ID..."
          aria-label="Search inspection by booking ID"
          id="return-inspection-search"
        />
        <button type="submit">View Inspection</button>
      </form>

      {/* ── Stats ── */}
      <div className="return-stats">
        {[
          { label: 'Total Bookings', value: stats.total, icon: '🗂️', cls: 'primary' },
          { label: 'Active Rentals', value: stats.active, icon: '🚗', cls: 'emerald' },
          { label: 'Confirmed', value: stats.confirmed, icon: '📋', cls: 'sky' },
          { label: 'Inspected', value: stats.inspected, icon: '✅', cls: 'emerald' },
          { label: 'Awaiting Inspection', value: stats.pending, icon: '⏳', cls: 'amber' },
        ].map((s) => (
          <div className="stat-card" key={s.label}>
            <div className={`stat-icon ${s.cls}`}>{s.icon}</div>
            <div className="stat-info">
              <h3>{s.value}</h3>
              <p>{s.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* ── Filter ── */}
      <div className="page-header" style={{ marginBottom: 20, marginTop: -8 }}>
        <div className="return-filter-group">
          {FILTER_OPTIONS.map((f) => (
            <button
              key={f}
              id={`return-filter-${f.toLowerCase()}`}
              className={`filter-chip ${filter === f ? 'active' : ''}`}
              onClick={() => setFilter(f)}
            >
              {f.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* ── Table ── */}
      {loading ? (
        <LoadingSpinner message="Loading bookings…" />
      ) : (
        <div className="table-container">
          <table className="data-table" id="return-inspections-table">
            <thead>
              <tr>
                <th>Booking #</th>
                <th>Vehicle</th>
                <th>Customer</th>
                <th>Pickup Date</th>
                <th>Return Date</th>
                <th>Total (LKR)</th>
                <th>Status</th>
                <th>Inspection</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredBookings.length === 0 ? (
                <tr>
                  <td colSpan={9}>
                    <div className="ri-empty">
                      <div className="ri-empty-icon">📋</div>
                      <h3>No bookings awaiting inspection</h3>
                      <p>
                        All eligible bookings with ACTIVE or CONFIRMED status will appear here
                        for return inspection.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredBookings.map((booking) => {
                  const bId = getBookingId(booking);
                  const inspection = inspectionCache[bId];
                  const hasInspection = !!inspection;

                  return (
                    <tr key={bId} id={`return-row-${bId}`}>
                      <td>
                        <strong>#{bId}</strong>
                      </td>
                      <td>
                        <div className="vehicle-cell-info">
                          <span className="vehicle-plate">
                            {booking.model || `Vehicle ID: ${booking.vehicleId}`}
                          </span>
                        </div>
                      </td>
                      <td>
                        <div className="handover-customer">
                          <span className="handover-cname">
                            {booking.customerName || '—'}
                          </span>
                          {booking.customerId && (
                            <span className="handover-cid">ID: {booking.customerId}</span>
                          )}
                        </div>
                      </td>
                      <td style={{ fontSize: '0.83rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>
                        {fmt(booking.pickupDateTime)}
                      </td>
                      <td style={{ fontSize: '0.83rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>
                        {fmt(booking.returnDateTime)}
                      </td>
                      <td className="rate-cell">
                        {booking.totalAmount?.toLocaleString()}
                      </td>
                      <td>
                        <StatusBadge status={booking.status} />
                      </td>
                      <td>
                        {checkingInspections ? (
                          <span style={{ fontSize: '0.78rem', color: 'var(--text-tertiary)' }}>
                            Checking…
                          </span>
                        ) : hasInspection ? (
                          <span className="ri-status-chip inspected">✅ Inspected</span>
                        ) : (
                          <span className="ri-status-chip pending">⏳ Pending</span>
                        )}
                      </td>
                      <td>
                        <div className="ri-action-group">
                          {!hasInspection && booking.status === 'ACTIVE' && (
                            <button
                              className="ri-btn ri-btn-inspect"
                              id={`btn-inspect-${bId}`}
                              onClick={() => openCreate(booking)}
                            >
                              <HiOutlineClipboardCheck /> Inspect
                            </button>
                          )}
                          {hasInspection && (
                            <button
                              className="ri-btn ri-btn-view"
                              id={`btn-view-inspection-${bId}`}
                              onClick={() => openView(inspection)}
                            >
                              <HiOutlineEye /> View
                            </button>
                          )}
                          {!hasInspection && booking.status !== 'ACTIVE' && (
                            <span
                              style={{
                                fontSize: '0.78rem',
                                color: 'var(--text-tertiary)',
                                fontStyle: 'italic',
                              }}
                            >
                              Activate first
                            </span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Modals ── */}
      {modal?.type === 'create' && (
        <CreateInspectionModal
          booking={modal.booking}
          staffId={defaultStaffId}
          onClose={closeModal}
          onSaved={onSaved}
        />
      )}
      {modal?.type === 'view' && (
        <ViewInspectionModal inspection={modal.inspection} onClose={closeModal} />
      )}
    </div>
  );
}
