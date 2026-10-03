import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import {
  HiOutlineShieldCheck,
  HiOutlineCreditCard,
  HiOutlineExclamationCircle,
  HiOutlineCheckCircle,
  HiOutlineCash,
  HiOutlineRefresh,
  HiX,
  HiOutlineLockClosed
} from 'react-icons/hi';
import paymentsApi from '../../api/payments';
import { extractErrorMessage } from '../../api/axiosInstance';
import './HandoverPaymentModal.css';

export default function HandoverPaymentModal({ handover, onClose, onActionSuccess }) {
  const [invoice, setInvoice] = useState(null);
  const [methods, setMethods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedMethodId, setSelectedMethodId] = useState(null);
  const [activeHold, setActiveHold] = useState(null);
  const [releasingHold, setReleasingHold] = useState(false);

  // Security Hold Form
  const [holdAmount, setHoldAmount] = useState('25000');
  const [placingHold, setPlacingHold] = useState(false);

  // Damage Capture Form
  const [damageAmount, setDamageAmount] = useState('15000');
  const [damageDesc, setDamageDesc] = useState(handover?.damageDescription || 'Minor bumper scratch / exterior repair');
  const [chargingDamage, setChargingDamage] = useState(false);

  // Active tab: 'HOLD', 'DAMAGE', 'OVERVIEW'
  const [activeTab, setActiveTab] = useState(handover?.hasDamage ? 'DAMAGE' : 'HOLD');

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return 'LKR 0.00';
    return `LKR ${Number(val).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const bookingId = handover?.bookingId;
  const customerId = handover?.customerId || 1;

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        if (bookingId) {
          const inv = await paymentsApi.getInvoiceByBookingId(bookingId);
          setInvoice(inv);
          try {
            const hold = await paymentsApi.getActiveSecurityHold(bookingId);
            setActiveHold(hold && hold.status === 'HELD' ? hold : null);
          } catch (_) {
            setActiveHold(null);
          }
        }
        if (customerId) {
          const cards = await paymentsApi.getCustomerMethods(customerId);
          setMethods(cards || []);
          if (cards && cards.length > 0) {
            setSelectedMethodId(cards[0].id);
          }
        }
      } catch (err) {
        console.error('Failed to load invoice/methods for handover:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [bookingId, customerId]);

  const handleReleaseHold = async () => {
    if (!activeHold?.paymentId) return;
    try {
      setReleasingHold(true);
      await paymentsApi.releaseSecurityHold(activeHold.paymentId, 'Vehicle returned and deposit released');
      setActiveHold(null);
      toast.success('Security deposit hold released to the customer.');
      if (onActionSuccess) onActionSuccess();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'The deposit can be released after the return inspection is complete.'));
    } finally {
      setReleasingHold(false);
    }
  };

  const handlePlaceHold = async () => {
    if (!invoice?.id) {
      toast.warning('No active invoice found for this booking.');
      return;
    }
    if (!selectedMethodId) {
      toast.warning('Please select a customer payment card.');
      return;
    }
    const amt = parseFloat(holdAmount);
    if (!amt || amt <= 0) {
      toast.warning('Please enter a valid hold amount.');
      return;
    }

    const idempotencyKey = `HOLD-${invoice.id}-${Date.now()}`;

    try {
      setPlacingHold(true);
      const res = await paymentsApi.placeSecurityHold({
        invoiceId: invoice.id,
        paymentMethodId: selectedMethodId,
        holdAmount: amt,
        idempotencyKey,
      });

      if (res.status === 'HELD') {
        toast.success(`Security Pre-Auth Hold of ${formatCurrency(amt)} placed successfully!`);
        setActiveHold(res);
        const updated = await paymentsApi.getInvoiceDetails(invoice.id);
        setInvoice(updated);
        if (onActionSuccess) onActionSuccess();
      } else {
        toast.error(`Hold authorization failed: ${res.responseCode}`);
      }
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to place security hold'));
    } finally {
      setPlacingHold(false);
    }
  };

  const handleChargeDamage = async () => {
    if (!invoice?.id) {
      toast.warning('No active invoice found for this booking.');
      return;
    }
    if (!selectedMethodId) {
      toast.warning('Please select a customer payment card.');
      return;
    }
    const amt = parseFloat(damageAmount);
    if (!amt || amt <= 0) {
      toast.warning('Please enter a valid damage recovery amount.');
      return;
    }
    if (!damageDesc.trim()) {
      toast.warning('Please enter a damage assessment description.');
      return;
    }

    const idempotencyKey = `DMG-${invoice.id}-${Date.now()}`;

    try {
      setChargingDamage(true);
      const res = await paymentsApi.chargeDamage({
        invoiceId: invoice.id,
        paymentMethodId: selectedMethodId,
        damageAmount: amt,
        damageDescription: damageDesc.trim(),
        idempotencyKey,
      });

      if (res.status === 'SUCCESS') {
        toast.success(`Damage recovery of ${formatCurrency(amt)} charged successfully! (Txn: ${res.txnReference})`);
        const updated = await paymentsApi.getInvoiceDetails(invoice.id);
        setInvoice(updated);
        if (onActionSuccess) onActionSuccess();
      } else {
        toast.error(`Damage charge failed: ${res.responseCode}`);
      }
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to charge damage recovery'));
    } finally {
      setChargingDamage(false);
    }
  };

  return (
    <div className="handover-payment-modal-overlay" onClick={onClose}>
      <div className="handover-payment-modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="handover-payment-modal-header">
          <div className="hp-header-title">
            <HiOutlineShieldCheck className="hp-title-icon" />
            <div>
              <h3>Payment & Security Hold Management</h3>
              <p>Handover #{handover.id} • Vehicle: {handover.vehicleLicensePlate} • Booking #{handover.bookingId || '—'}</p>
            </div>
          </div>
          <button className="hp-close-btn" onClick={onClose}><HiX /></button>
        </div>

        {/* Tab Navigation */}
        <div className="hp-tab-bar">
          <button
            className={`hp-tab-btn ${activeTab === 'HOLD' ? 'active' : ''}`}
            onClick={() => setActiveTab('HOLD')}
          >
            <HiOutlineLockClosed /> Pre-Auth Security Hold
          </button>
          <button
            className={`hp-tab-btn ${activeTab === 'DAMAGE' ? 'active' : ''}`}
            onClick={() => setActiveTab('DAMAGE')}
          >
            <HiOutlineExclamationCircle /> Damage Recovery Capture
          </button>
          <button
            className={`hp-tab-btn ${activeTab === 'OVERVIEW' ? 'active' : ''}`}
            onClick={() => setActiveTab('OVERVIEW')}
          >
            <HiOutlineCash /> Invoice Overview
          </button>
        </div>

        <div className="hp-modal-body">
          {loading ? (
            <p className="loading-text">Loading invoice and payment details...</p>
          ) : !invoice ? (
            <div className="hp-empty-notice">
              <p>No linked invoice found for this booking ID #{bookingId}.</p>
            </div>
          ) : (
            <>
              {/* Financial Quick Status */}
              <div className="hp-quick-status-grid">
                <div className="hp-status-card">
                  <span className="hp-label">Invoice Status</span>
                  <span className={`hp-badge ${invoice.status?.toLowerCase()}`}>{invoice.status}</span>
                </div>
                <div className="hp-status-card">
                  <span className="hp-label">Total Rental</span>
                  <strong>{formatCurrency(invoice.totalAmount)}</strong>
                </div>
                <div className="hp-status-card">
                  <span className="hp-label">Paid Amount</span>
                  <strong className="text-green">{formatCurrency(invoice.paidAmount)}</strong>
                </div>
                <div className="hp-status-card">
                  <span className="hp-label">Remaining Balance</span>
                  <strong className={invoice.remainingBalance > 0 ? 'text-red' : 'text-green'}>
                    {formatCurrency(invoice.remainingBalance)}
                  </strong>
                </div>
              </div>

              {/* TAB 1: Pre-Auth Hold */}
              {activeTab === 'HOLD' && (
                <div className="hp-tab-content">
                  {activeHold ? (
                    <div className="hp-info-box" style={{ background: '#ecfdf5', borderColor: '#a7f3d0' }}>
                      <HiOutlineShieldCheck style={{ color: '#059669' }} />
                      <div>
                        <strong style={{ color: '#065f46' }}>Deposit Hold Active ({formatCurrency(activeHold.holdAmount || 25000)})</strong>
                        <p style={{ color: '#047857' }}>
                          A refundable security deposit hold is authorized for this booking. Handover checks may proceed.
                        </p>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="hp-info-box">
                        <HiOutlineShieldCheck />
                        <div>
                          <strong>Security Pre-Authorization Hold (Vehicle Handover)</strong>
                          <p>
                            Place an authorized hold on the customer's credit line before releasing the keys.
                            Funds are not captured until return inspection or damage assessment.
                          </p>
                        </div>
                      </div>

                      <div className="hp-form-group">
                        <label>Select Customer Card for Pre-Auth Hold:</label>
                        <select
                          className="hp-select"
                          value={selectedMethodId || ''}
                          onChange={(e) => setSelectedMethodId(Number(e.target.value))}
                        >
                          {methods.map((m) => (
                            <option key={m.id} value={m.id}>
                              {m.maskedCardNum} (Avail: {formatCurrency(m.availableBalance)} | Held: {formatCurrency(m.heldAmount)})
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="hp-form-group">
                        <label>Security Hold Amount (LKR):</label>
                        <input
                          type="number"
                          className="hp-input"
                          value={holdAmount}
                          onChange={(e) => setHoldAmount(e.target.value)}
                          placeholder="25000"
                        />
                      </div>

                      <div className="hp-action-row">
                        <button
                          className="hp-btn-primary"
                          onClick={handlePlaceHold}
                          disabled={placingHold || !selectedMethodId}
                        >
                          {placingHold ? 'Authorizing Hold...' : `Authorize Hold of ${formatCurrency(holdAmount)}`}
                        </button>
                      </div>
                    </>
                  )}
                </div>
              )}

              {/* TAB 2: Damage Recovery */}
              {activeTab === 'DAMAGE' && (
                <div className="hp-tab-content">
                  <div className="hp-info-box warning">
                    <HiOutlineExclamationCircle />
                    <div>
                      <strong>Vehicle Return Damage Recovery Charge</strong>
                      <p>
                        Charge repair or damage assessment fees to the customer. If an active security hold exists,
                        the amount will be captured from the hold; otherwise, the card is billed directly.
                      </p>
                    </div>
                  </div>

                  <div className="hp-form-group">
                    <label>Select Card to Charge / Capture:</label>
                    <select
                      className="hp-select"
                      value={selectedMethodId || ''}
                      onChange={(e) => setSelectedMethodId(Number(e.target.value))}
                    >
                      {methods.map((m) => (
                        <option key={m.id} value={m.id}>
                          {m.maskedCardNum} (Avail: {formatCurrency(m.availableBalance)} | Active Hold: {formatCurrency(m.heldAmount)})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="hp-form-group">
                    <label>Assessed Damage Amount (LKR):</label>
                    <input
                      type="number"
                      className="hp-input"
                      value={damageAmount}
                      onChange={(e) => setDamageAmount(e.target.value)}
                      placeholder="15000"
                    />
                  </div>

                  <div className="hp-form-group">
                    <label>Damage Assessment Description:</label>
                    <textarea
                      className="hp-textarea"
                      rows="3"
                      value={damageDesc}
                      onChange={(e) => setDamageDesc(e.target.value)}
                      placeholder="Detailed damage note and repair estimate..."
                    />
                  </div>

                  <div className="hp-action-row">
                    <button
                      className="hp-btn-danger"
                      onClick={handleChargeDamage}
                      disabled={chargingDamage || !selectedMethodId}
                    >
                      {chargingDamage ? 'Processing Capture...' : `Charge Damage Fee (${formatCurrency(damageAmount)})`}
                    </button>
                  </div>
                </div>
              )}

              {/* TAB 3: Overview & Transaction History */}
              {activeTab === 'OVERVIEW' && (
                <div className="hp-tab-content">
                  <h5>Payment & Transaction Records</h5>
                  {activeHold && (
                    <div className="hp-info-box">
                      <HiOutlineShieldCheck />
                      <div>
                        <strong>Active security deposit hold</strong>
                        <p>After the separate vehicle return inspection, release the unused security deposit to the customer.</p>
                        <button className="hp-btn-primary" onClick={handleReleaseHold} disabled={releasingHold}>
                          {releasingHold ? 'Releasing Deposit...' : 'Release Security Deposit'}
                        </button>
                      </div>
                    </div>
                  )}
                  {invoice.paymentHistory && invoice.paymentHistory.length > 0 ? (
                    <table className="hp-table">
                      <thead>
                        <tr>
                          <th>Txn Reference</th>
                          <th>Status</th>
                          <th>Response</th>
                          <th>Date</th>
                        </tr>
                      </thead>
                      <tbody>
                        {invoice.paymentHistory.map((p, idx) => (
                          <tr key={idx}>
                            <td><code>{p.txnReference || `TXN-${p.paymentId}`}</code></td>
                            <td>
                              <span className={`hp-tag ${p.status?.toLowerCase()}`}>{p.status}</span>
                            </td>
                            <td>{p.responseCode}</td>
                            <td>{new Date(p.processedAt).toLocaleString('en-LK')}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="no-records">No transactions recorded yet for this invoice.</p>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
