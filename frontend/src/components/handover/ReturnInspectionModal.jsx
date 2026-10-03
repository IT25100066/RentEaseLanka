import { useState } from 'react';
import { toast } from 'react-toastify';
import { HiOutlineClipboardCheck, HiX } from 'react-icons/hi';
import { inspectVehicleReturn } from '../../api/returns';
import { extractErrorMessage } from '../../api/axiosInstance';

export default function ReturnInspectionModal({ handover, staffId, onClose, onSaved }) {
  const [endingMileage, setEndingMileage] = useState(String(handover.vehicleCurrentOdometer ?? ''));
  const [hasDamage, setHasDamage] = useState(false);
  const [damageDescription, setDamageDescription] = useState('');
  const [damageAmount, setDamageAmount] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    const mileage = Number(endingMileage);
    if (!Number.isFinite(mileage) || mileage < 0) {
      toast.warning('Enter a valid ending mileage.');
      return;
    }
    if (hasDamage && !damageDescription.trim()) {
      toast.warning('Describe the vehicle damage found at return.');
      return;
    }
    if (hasDamage && (!Number.isFinite(Number(damageAmount)) || Number(damageAmount) <= 0)) {
      toast.warning('Enter the damage amount to deduct from the customer deposit.');
      return;
    }
    setBusy(true);
    try {
      await inspectVehicleReturn(handover.bookingId, {
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
      toast.error(extractErrorMessage(error, 'Could not record vehicle return.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="ho-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ho-modal">
        <div className="ho-modal-header">
          <div className="ho-modal-icon primary"><HiOutlineClipboardCheck /></div>
          <div className="ho-modal-title-group">
            <p className="ho-modal-title">Vehicle Return Inspection</p>
            <p className="ho-modal-subtitle">Booking #{handover.bookingId} · {handover.vehicleLicensePlate}</p>
          </div>
          <button className="ho-modal-close" onClick={onClose}><HiX /></button>
        </div>
        <div className="ho-modal-body ho-initiate-form">
          <p className="ho-info-text">Saving this inspection settles the deposit automatically and completes the booking. The full rental invoice must already be paid.</p>
          <div className="form-group">
            <label className="form-label">Ending Mileage *</label>
            <input className="form-input" type="number" min="0" value={endingMileage} onChange={(e) => setEndingMileage(e.target.value)} />
          </div>
          <label className="ho-check-row">
            <input type="checkbox" checked={hasDamage} onChange={(e) => setHasDamage(e.target.checked)} />
            <span>Damage found during return inspection</span>
          </label>
          {hasDamage && <div className="form-group">
            <label className="form-label">Damage Description *</label>
            <textarea className="form-textarea" rows="3" value={damageDescription} onChange={(e) => setDamageDescription(e.target.value)} />
          </div>}
          {hasDamage && <div className="form-group">
            <label className="form-label">Damage Amount to Deduct from Deposit (LKR) *</label>
            <input className="form-input" type="number" min="0.01" step="0.01" value={damageAmount} onChange={(e) => setDamageAmount(e.target.value)} />
          </div>}
          <div className="form-group">
            <label className="form-label">Return Notes</label>
            <textarea className="form-textarea" rows="3" value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
        </div>
        <div className="ho-modal-footer">
          <button className="btn btn-secondary btn-sm" disabled={busy} onClick={onClose}>Cancel</button>
          <button className="btn btn-primary btn-sm" disabled={busy} onClick={submit}>{busy ? 'Saving…' : 'Save Return Inspection'}</button>
        </div>
      </div>
    </div>
  );
}
