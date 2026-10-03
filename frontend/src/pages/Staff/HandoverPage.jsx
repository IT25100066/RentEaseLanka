import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  HiOutlineTruck,
  HiOutlineChartBar,
  HiOutlineShieldCheck,
  HiOutlineCheckCircle,
  HiOutlineXCircle,
  HiOutlinePlusCircle,
  HiOutlineRefresh,
  HiX,
  HiOutlineClipboardCheck,
  HiOutlineExclamationCircle,
  HiOutlineLockClosed,
  HiOutlineCash,
} from 'react-icons/hi';
import {
  getAllHandovers,
  initiateHandover,
  recordMileage,
  recordDamage,
  completeHandover,
  cancelHandover,
} from '../../api/handovers';
import paymentsApi from '../../api/payments';
import { extractErrorMessage } from '../../api/axiosInstance';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import HandoverPaymentModal from '../../components/payments/HandoverPaymentModal';
import ReturnInspectionModal from '../../components/handover/ReturnInspectionModal';
import './HandoverPage.css';

/* ─── Helper ───────────────────────────────────────────────── */
const fmt = (dt) => (dt ? new Date(dt).toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' }) : '—');

const STATUS_FILTERS = ['ALL', 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

/* ─── Sub-components ───────────────────────────────────────── */

/** Compact vehicle + booking info panel shown inside every modal */
function VehiclePanel({ h }) {
  return (
    <div className="ho-vehicle-panel">
      <div className="ho-vehicle-panel-item">
        <span className="ho-vehicle-panel-label">License Plate</span>
        <span className="ho-vehicle-panel-value">{h.vehicleLicensePlate ?? '—'}</span>
      </div>
      <div className="ho-vehicle-panel-item">
        <span className="ho-vehicle-panel-label">Model / Year</span>
        <span className="ho-vehicle-panel-value">{h.vehicleModel} ({h.vehicleYear})</span>
      </div>
      <div className="ho-vehicle-panel-item">
        <span className="ho-vehicle-panel-label">Current Odometer</span>
        <span className="ho-vehicle-panel-value">{h.vehicleCurrentOdometer?.toLocaleString()} km</span>
      </div>
      <div className="ho-vehicle-panel-item">
        <span className="ho-vehicle-panel-label">Vehicle Status</span>
        <span className="ho-vehicle-panel-value">{h.vehicleStatus}</span>
      </div>
      {h.customerName && (
        <div className="ho-vehicle-panel-item">
          <span className="ho-vehicle-panel-label">Customer</span>
          <span className="ho-vehicle-panel-value">{h.customerName}</span>
        </div>
      )}
      {h.bookingId && (
        <div className="ho-vehicle-panel-item">
          <span className="ho-vehicle-panel-label">Booking #</span>
          <span className="ho-vehicle-panel-value">{h.bookingId}</span>
        </div>
      )}
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MODAL — Initiate New Handover
   ════════════════════════════════════════════════════════════ */
function InitiateModal({ onClose, onSaved, initialValues = {} }) {
  const { user } = useAuth();
  const defaultStaff = user?.staffId || (user?.id ? `STAFF-${user.id}` : (user?.email || 'STAFF'));

  const [form, setForm] = useState({
    vehicleId: '',
    bookingId: '',
    customerId: '',
    customerName: '',
    staffId: defaultStaff,
    initialNotes: '',
    ...initialValues,
  });
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = async () => {
    if (!form.vehicleId && !form.bookingId) {
      toast.warning('Please enter either a Vehicle ID or a Booking ID.');
      return;
    }
    if (!form.staffId || !form.staffId.trim()) {
      toast.warning('Staff ID is required.');
      return;
    }

    setBusy(true);
    try {
      const payload = {
        staffId: form.staffId.trim(),
        vehicleId: form.vehicleId ? Number(form.vehicleId) : null,
        bookingId: form.bookingId ? Number(form.bookingId) : null,
        customerId: form.customerId ? Number(form.customerId) : null,
        customerName: form.customerName ? form.customerName.trim() : null,
        initialNotes: form.initialNotes ? form.initialNotes.trim() : null,
      };
      await initiateHandover(payload);
      toast.success('Handover initiated successfully!');
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to initiate handover.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="initiate-handover-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon primary"><HiOutlinePlusCircle /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Initiate Vehicle Handover</p>
            <p className="ho-modal-subtitle">Start a new vehicle handover process</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>

        <div className="ho-modal-body ho-initiate-form">
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Booking ID <span style={{ color: 'var(--emerald-600)', fontWeight: 500 }}>(Auto-fills vehicle & customer)</span></label>
              <input id="initiate-bookingId" className="form-input" type="number" placeholder="e.g. 5 (or leave blank for walk-ins)"
                value={form.bookingId} onChange={set('bookingId')} />
            </div>
            <div className="form-group">
              <label className="form-label">Vehicle ID <span style={{ color: 'var(--text-tertiary)', fontWeight: 400 }}>(Optional if Booking ID given)</span></label>
              <input id="initiate-vehicleId" className="form-input" type="number" placeholder="e.g. 12"
                value={form.vehicleId} onChange={set('vehicleId')} />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Staff ID *</label>
              <input id="initiate-staffId" className="form-input" type="text" placeholder="e.g. STAFF-1"
                value={form.staffId} onChange={set('staffId')} />
            </div>
            <div className="form-group">
              <label className="form-label">Customer ID <span style={{ color: 'var(--text-tertiary)', fontWeight: 400 }}>(Auto-filled if booking provided)</span></label>
              <input id="initiate-customerId" className="form-input" type="number" placeholder="Optional"
                value={form.customerId} onChange={set('customerId')} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Customer Name <span style={{ color: 'var(--text-tertiary)', fontWeight: 400 }}>(Auto-filled if booking provided)</span></label>
            <input id="initiate-customerName" className="form-input" type="text" placeholder="Full name of the customer"
              value={form.customerName} onChange={set('customerName')} />
          </div>

          <div className="form-group">
            <label className="form-label">Initial Notes</label>
            <textarea id="initiate-notes" className="form-textarea" rows={3}
              placeholder="Any remarks before handover starts..."
              value={form.initialNotes} onChange={set('initialNotes')} />
          </div>
        </div>

        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose} disabled={busy}>Cancel</button>
          <button id="initiate-submit-btn" className="btn btn-primary btn-sm" onClick={submit} disabled={busy}>
            {busy ? 'Initiating…' : '🚗 Initiate Handover'}
          </button>
        </div>
      </div>
    </div>
  );
}



/* ════════════════════════════════════════════════════════════
   MODAL — Record Mileage
   ════════════════════════════════════════════════════════════ */
function MileageModal({ handover, onClose, onSaved }) {
  const [mileage, setMileage] = useState(
    handover?.startingMileage != null ? String(handover.startingMileage) : ''
  );
  const [busy, setBusy]       = useState(false);

  const submit = async () => {
    const val = parseFloat(mileage);
    if (isNaN(val) || val < 0) { toast.warning('Enter a valid non-negative mileage value.'); return; }
    setBusy(true);
    try {
      await recordMileage(handover.id, { startingMileage: val });
      toast.success('Starting mileage recorded!');
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to record mileage.'));
    } finally { setBusy(false); }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="mileage-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon sky"><HiOutlineChartBar /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Record Starting Mileage</p>
            <p className="ho-modal-subtitle">Handover #{handover.id}</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>

        <div className="ho-modal-body">
          <VehiclePanel h={handover} />
          <div className="form-group">
            <label className="form-label">
              Starting Odometer Reading (km)
              <span style={{ marginLeft: 8, fontWeight: 400, color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                Vehicle current: {handover.vehicleCurrentOdometer?.toLocaleString()} km
              </span>
            </label>
            <input id="mileage-input" className="form-input" type="number" min={handover.vehicleCurrentOdometer ?? 0}
              placeholder={`Minimum ${handover.vehicleCurrentOdometer ?? 0} km`}
              value={mileage} onChange={(e) => setMileage(e.target.value)} />
          </div>
        </div>

        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose} disabled={busy}>Cancel</button>
          <button id="mileage-submit-btn" className="btn btn-primary btn-sm" style={{ background: 'var(--sky-500)', color: '#fff', border: 'none' }}
            onClick={submit} disabled={busy}>
            {busy ? 'Saving…' : '📏 Save Mileage'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MODAL — Record Damage
   ════════════════════════════════════════════════════════════ */
function DamageModal({ handover, onClose, onSaved }) {
  const [hasDamage, setHasDamage]         = useState(Boolean(handover?.hasDamage));
  const [description, setDescription]     = useState(handover?.damageDescription || '');
  const [busy, setBusy]                   = useState(false);

  const submit = async () => {
    if (hasDamage && !description.trim()) {
      toast.warning('Please describe the damage found.');
      return;
    }
    setBusy(true);
    try {
      await recordDamage(handover.id, {
        hasDamage,
        damageDescription: description.trim() || 'Confirmed no new damage',
      });
      toast.success('Damage inspection recorded!');
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to record damage.'));
    } finally { setBusy(false); }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="damage-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon rose"><HiOutlineShieldCheck /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Damage Inspection</p>
            <p className="ho-modal-subtitle">Handover #{handover.id}</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>

        <div className="ho-modal-body">
          <VehiclePanel h={handover} />

          <div className="form-group">
            <label className="form-label">Damage Found?</label>
            <div className="ho-radio-group">
              <label className={`ho-radio-option ${!hasDamage ? 'selected-no' : ''}`}>
                <input id="damage-no" type="radio" name="damage" checked={!hasDamage} onChange={() => setHasDamage(false)} />
                <HiOutlineCheckCircle /> No Damage
              </label>
              <label className={`ho-radio-option ${hasDamage ? 'selected-yes' : ''}`}>
                <input id="damage-yes" type="radio" name="damage" checked={hasDamage} onChange={() => setHasDamage(true)} />
                <HiOutlineExclamationCircle /> Damage Found
              </label>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              {hasDamage ? 'Damage Description *' : 'Notes (optional)'}
            </label>
            <textarea id="damage-description" className="form-textarea" rows={3}
              placeholder={hasDamage ? 'Describe the damage in detail...' : 'Confirmed no damage — any notes?'}
              value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
        </div>

        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose} disabled={busy}>Cancel</button>
          <button id="damage-submit-btn"
            className={`btn btn-sm ${hasDamage ? 'btn-danger' : 'btn-success'}`}
            onClick={submit} disabled={busy}>
            {busy ? 'Saving…' : hasDamage ? '⚠️ Save Damage Report' : '✅ Confirm No Damage'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MODAL — Complete Handover
   ════════════════════════════════════════════════════════════ */
function CompleteModal({ handover, onClose, onSaved, onOpenPayment, onOpenMileage, onOpenDamage }) {
  const [busy, setBusy] = useState(false);
  const [hasDepositHold, setHasDepositHold] = useState(null);
  const [checkingHold, setCheckingHold] = useState(false);

  useEffect(() => {
    const checkDeposit = async () => {
      if (!handover?.bookingId) {
        setHasDepositHold(true);
        return;
      }
      try {
        setCheckingHold(true);
        const hold = await paymentsApi.getActiveSecurityHold(handover.bookingId);
        setHasDepositHold(Boolean(hold && hold.status === 'HELD'));
      } catch (_) {
        setHasDepositHold(false);
      } finally {
        setCheckingHold(false);
      }
    };
    checkDeposit();
  }, [handover?.bookingId]);

  const mileagePass = handover.startingMileage != null;
  const damagePass  = handover.damageDescription != null || handover.hasDamage !== null;
  const depositPass = handover.bookingId ? hasDepositHold === true : true;

  const checks = [
    { key: 'mileage', label: 'Starting mileage entered', pass: mileagePass, action: onOpenMileage, actionLabel: 'Set Mileage' },
    { key: 'damage',  label: 'Damage inspection done',   pass: damagePass,  action: onOpenDamage,  actionLabel: 'Inspect Damage' },
    {
      key: 'deposit',
      label: checkingHold ? 'Checking security deposit hold...' : 'Security deposit hold authorized',
      pass: depositPass,
      action: onOpenPayment,
      actionLabel: 'Authorize Hold'
    },
  ];
  const allPass = checks.every((c) => c.pass) && !checkingHold;

  const submit = async () => {
    setBusy(true);
    try {
      await completeHandover(handover.id);
      toast.success('🎉 Handover completed! Vehicle is now RENTED and Booking is ACTIVE.');
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to complete handover.'));
    } finally { setBusy(false); }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="complete-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon emerald"><HiOutlineClipboardCheck /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Complete Handover</p>
            <p className="ho-modal-subtitle">Handover #{handover.id} — pre-completion checklist</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>

        <div className="ho-modal-body">
          <VehiclePanel h={handover} />

          <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 8 }}>
            All requirements must be satisfied before completing handover:
          </p>
          <ul className="ho-checklist">
            {checks.map((c) => (
              <li key={c.key} className={c.pass ? 'check-pass' : 'check-fail'} style={{ justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span className="icon">{c.pass ? '✅' : '❌'}</span>
                  <span>{c.label}</span>
                </div>
                {!c.pass && c.action && (
                  <button
                    type="button"
                    className="btn btn-xs btn-secondary"
                    style={{ padding: '3px 9px', fontSize: '0.75rem', borderRadius: 'var(--radius-sm)' }}
                    onClick={c.action}
                  >
                    {c.actionLabel}
                  </button>
                )}
              </li>
            ))}
          </ul>

          {handover.bookingId && !hasDepositHold && !checkingHold && (
            <div style={{ marginTop: 12, padding: '10px 14px', background: '#fffbeb', border: '1px solid #fde68a', borderRadius: 'var(--radius-md)' }}>
              <p style={{ fontSize: '0.82rem', color: '#92400e', margin: '0 0 8px 0' }}>
                <strong>Deposit Missing:</strong> A refundable security deposit must be authorized before completing handover.
              </p>
              {onOpenPayment && (
                <button
                  type="button"
                  className="btn btn-sm"
                  style={{ background: 'var(--amber-600, #d97706)', color: '#fff' }}
                  onClick={onOpenPayment}
                >
                  <HiOutlineLockClosed /> Authorize Deposit Now
                </button>
              )}
            </div>
          )}

          {allPass && (
            <p style={{ fontSize: '0.82rem', background: 'var(--emerald-50)', color: 'var(--emerald-700)',
              padding: '10px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--emerald-100)', marginTop: 12 }}>
              <strong>Inspection Verified:</strong> Completing handover will confirm and activate the booking, transition the vehicle to <strong>RENTED</strong>, and release the keys to the customer.
            </p>
          )}
        </div>

        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose} disabled={busy}>Cancel</button>
          <button id="complete-submit-btn" className="btn btn-success btn-sm"
            onClick={submit} disabled={busy || !allPass}
            title={!allPass ? 'Complete all checklist requirements first' : ''}>
            {busy ? 'Completing…' : '🚀 Complete Handover'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MODAL — Cancel Handover
   ════════════════════════════════════════════════════════════ */
function CancelModal({ handover, onClose, onSaved }) {
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setBusy(true);
    try {
      await cancelHandover(handover.id);
      toast.success('Handover cancelled. Booking and vehicle restored.');
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to cancel handover.'));
    } finally { setBusy(false); }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal" id="cancel-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon rose"><HiOutlineXCircle /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Cancel Handover</p>
            <p className="ho-modal-subtitle">Handover #{handover.id}</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>

        <div className="ho-modal-body">
          <VehiclePanel h={handover} />
          <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', background: 'var(--rose-50)',
            padding: '12px 16px', borderRadius: 'var(--radius-md)', border: '1px solid var(--rose-100)', lineHeight: 1.6 }}>
            ⚠️ Cancelling this handover will also <strong>cancel the linked booking</strong> (if any) and
            restore the vehicle status back to <strong>AVAILABLE</strong>.
          </p>
        </div>

        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" onClick={onClose} disabled={busy}>Go Back</button>
          <button id="cancel-submit-btn" className="btn btn-danger btn-sm" onClick={submit} disabled={busy}>
            {busy ? 'Cancelling…' : '✕ Confirm Cancel'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════
   MAIN PAGE
   ════════════════════════════════════════════════════════════ */
export default function HandoverPage() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [handovers, setHandovers] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [filter, setFilter]       = useState('ALL');
  const [modal, setModal]         = useState(null); // { type, handover? }

  const defaultStaffId = user?.staffId || (user?.id ? `STAFF-${user.id}` : (user?.email || 'STAFF'));

  useEffect(() => {
    const bookingId = searchParams.get('bookingId');
    if (!bookingId) return;

    setModal({
      type: 'initiate',
      initialValues: {
        bookingId,
        vehicleId: searchParams.get('vehicleId') ?? '',
        customerId: searchParams.get('customerId') ?? '',
        customerName: searchParams.get('customerName') ?? '',
        staffId: String(defaultStaffId),
      },
    });
    setSearchParams({}, { replace: true });
  }, [defaultStaffId, searchParams, setSearchParams]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const status = filter === 'ALL' ? null : filter;
      const data   = await getAllHandovers(status);
      setHandovers(data.sort((a, b) => b.id - a.id));
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to load handovers.'));
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { load(); }, [load]);

  const onSaved = () => { setModal(null); load(); };
  const open    = (type, handover = null) => setModal({ type, handover });
  const close   = () => setModal(null);

  /* ─── Stats ─── */
  const stats = {
    total:      handovers.length,
    pending:    handovers.filter((h) => h.status === 'PENDING').length,
    inProgress: handovers.filter((h) => h.status === 'IN_PROGRESS').length,
    completed:  handovers.filter((h) => h.status === 'COMPLETED').length,
    cancelled:  handovers.filter((h) => h.status === 'CANCELLED').length,
  };

  /* ─── Damage chip ─── */
  const DamageChip = ({ h }) => {
    if (h.damageDescription == null)
      return <span className="damage-chip not-inspected">⏳ Not Inspected</span>;
    if (h.hasDamage)
      return <span className="damage-chip has-damage" title={h.damageDescription}>⚠️ Damage</span>;
    return <span className="damage-chip no-damage">✓ Clean</span>;
  };

  /* ─── Active status check ─── */
  const isActive = (h) => h.status === 'PENDING' || h.status === 'IN_PROGRESS';

  return (
    <div className="page-container handover-page" id="handover-page">

      {/* ── Header ── */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicle Handover</h1>
          <p className="page-subtitle">Manage vehicle pickup handovers step-by-step</p>
        </div>
        <div className="handover-toolbar">
          <button className="btn btn-secondary btn-sm" id="handover-refresh-btn" onClick={load} title="Refresh">
            <HiOutlineRefresh /> Refresh
          </button>
          <button className="btn btn-primary btn-sm" id="initiate-handover-btn"
            onClick={() => open('initiate')}>
            <HiOutlinePlusCircle /> Initiate Handover
          </button>
        </div>
      </div>

      {/* ── Stats ── */}
      <div className="handover-stats">
        {[
          { label: 'Total', value: stats.total,      icon: '🗂️', cls: 'primary' },
          { label: 'Pending',   value: stats.pending,    icon: '⏳', cls: 'amber'   },
          { label: 'In Progress', value: stats.inProgress, icon: '🔄', cls: 'sky'   },
          { label: 'Completed', value: stats.completed,  icon: '✅', cls: 'emerald' },
          { label: 'Cancelled', value: stats.cancelled,  icon: '✕',  cls: 'rose'   },
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

      {/* ── Filter Chips ── */}
      <div className="page-header" style={{ marginBottom: 20, marginTop: -8 }}>
        <div className="handover-filter-group">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f}
              id={`filter-${f.toLowerCase()}`}
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
        <LoadingSpinner message="Loading handovers…" />
      ) : (
        <div className="table-container">
          <table className="data-table" id="handovers-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Vehicle</th>
                <th>Customer</th>
                <th>Staff</th>
                <th>Mileage</th>
                <th>Damage</th>
                <th>Status</th>
                <th>Handover Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {handovers.length === 0 ? (
                <tr>
                  <td colSpan={9}>
                    <div className="ho-empty">
                      <div className="ho-empty-icon">🚗</div>
                      <h3>No handovers found</h3>
                      <p>Click "Initiate Handover" to start a new one.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                handovers.map((h) => (
                  <tr key={h.id} id={`handover-row-${h.id}`}>
                    <td><strong>#{h.id}</strong></td>

                    {/* Vehicle */}
                    <td>
                      <div className="vehicle-cell-info">
                        <span className="vehicle-plate">{h.vehicleLicensePlate ?? `ID: ${h.vehicleId}`}</span>
                        <span className="vehicle-model-year">{h.vehicleModel} {h.vehicleYear}</span>
                      </div>
                    </td>

                    {/* Customer */}
                    <td>
                      <div className="handover-customer">
                        <span className="handover-cname">{h.customerName ?? '—'}</span>
                        {h.customerId && <span className="handover-cid">ID: {h.customerId}</span>}
                      </div>
                    </td>

                    {/* Staff */}
                    <td><span className="handover-staff">{h.staffId}</span></td>

                    {/* Mileage */}
                    <td>
                      {h.startingMileage != null
                        ? <span className="mileage-chip">📏 {h.startingMileage.toLocaleString()} km</span>
                        : <span style={{ color: 'var(--text-tertiary)', fontSize: '0.8rem' }}>—</span>
                      }
                    </td>

                    {/* Damage */}
                    <td><DamageChip h={h} /></td>

                    {/* Status */}
                    <td><StatusBadge status={h.status} /></td>

                    {/* Date */}
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>
                      {fmt(h.handoverDateTime)}
                    </td>

                    {/* Actions */}
                    <td>
                      <div className="ho-action-group">
                        {/* Payment & Security Hold Modal Trigger */}
                        <button className="ho-btn ho-btn-pay" id={`btn-payment-${h.id}`}
                          onClick={() => open('payment', h)}
                          title="Security Pre-Auth Hold & Damage Recovery">
                          <HiOutlineLockClosed /> Payment & Hold
                        </button>

                        {isActive(h) && (
                          <button className="ho-btn ho-btn-mile" id={`btn-mileage-${h.id}`}
                            onClick={() => open('mileage', h)}
                            title={h.startingMileage != null ? `Starting Mileage: ${h.startingMileage} km` : 'Record starting mileage'}>
                            <HiOutlineChartBar /> {h.startingMileage != null ? 'Mileage ✓' : 'Mileage'}
                          </button>
                        )}
                        {isActive(h) && (
                          <button className="ho-btn ho-btn-dmg" id={`btn-damage-${h.id}`}
                            onClick={() => open('damage', h)}
                            title={h.damageDescription ? `Damage: ${h.damageDescription}` : 'Inspect vehicle damage'}>
                            <HiOutlineShieldCheck /> {h.damageDescription != null ? 'Damage ✓' : 'Damage'}
                          </button>
                        )}
                        {isActive(h) && (
                          <button className="ho-btn ho-btn-done" id={`btn-complete-${h.id}`}
                            onClick={() => open('complete', h)}
                            title="Complete vehicle handover">
                            <HiOutlineCheckCircle /> Complete
                          </button>
                        )}
                        {isActive(h) && (
                          <button className="ho-btn ho-btn-cancel" id={`btn-cancel-${h.id}`}
                            onClick={() => open('cancel', h)}
                            title="Cancel vehicle handover">
                            <HiOutlineXCircle /> Cancel
                          </button>
                        )}
                        {h.status === 'COMPLETED' && h.bookingId && (
                          <button className="ho-btn ho-btn-done" id={`btn-return-inspect-${h.id}`}
                            onClick={() => open('returnInspect', h)}
                            title="Record vehicle return inspection">
                            <HiOutlineClipboardCheck /> Return Inspect
                          </button>
                        )}
                        {!isActive(h) && !h.bookingId && h.status !== 'COMPLETED' && (
                          <span style={{ color: 'var(--text-tertiary)', fontSize: '0.8rem' }}>—</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Modals ── */}
      {modal?.type === 'initiate'  && <InitiateModal onClose={close} onSaved={onSaved} initialValues={modal.initialValues ?? { staffId: String(defaultStaffId) }} />}
      {modal?.type === 'mileage'   && <MileageModal  handover={modal.handover} onClose={close} onSaved={onSaved} />}
      {modal?.type === 'damage'    && <DamageModal   handover={modal.handover} onClose={close} onSaved={onSaved} />}
      {modal?.type === 'complete'  && (
        <CompleteModal
          handover={modal.handover}
          onClose={close}
          onSaved={onSaved}
          onOpenPayment={() => open('payment', modal.handover)}
          onOpenMileage={() => open('mileage', modal.handover)}
          onOpenDamage={() => open('damage', modal.handover)}
        />
      )}
      {modal?.type === 'cancel'    && <CancelModal   handover={modal.handover} onClose={close} onSaved={onSaved} />}
      {modal?.type === 'payment'   && <HandoverPaymentModal handover={modal.handover} onClose={close} onActionSuccess={onSaved} />}
      {modal?.type === 'returnInspect' && <ReturnInspectionModal handover={modal.handover} staffId={defaultStaffId} onClose={close} onSaved={onSaved} />}
    </div>
  );
}
