import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import {
  HiOutlineCreditCard,
  HiOutlineShieldCheck,
  HiOutlineCurrencyDollar,
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
  HiOutlinePlusCircle,
  HiX,
  HiOutlineLockClosed,
  HiOutlineClock
} from 'react-icons/hi';
import paymentsApi from '../../api/payments';
import { extractErrorMessage } from '../../api/axiosInstance';
import './CheckoutModal.css';

export default function CheckoutModal({ invoice, booking, customerId, onClose, onPaymentSuccess }) {
  const [methods, setMethods] = useState([]);
  const [selectedMethodId, setSelectedMethodId] = useState(null);
  const [loadingMethods, setLoadingMethods] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [receipt, setReceipt] = useState(null);
  const [activeDepositHold, setActiveDepositHold] = useState(null);

  // New Card State
  const [showAddCard, setShowAddCard] = useState(false);
  const [newCardNumber, setNewCardNumber] = useState('');
  const [newCardBalance, setNewCardBalance] = useState('300000');
  const [newCardExpiry, setNewCardExpiry] = useState('2028-12-31');
  const [addingCard, setAddingCard] = useState(false);

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return 'LKR 0.00';
    return `LKR ${Number(val).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const remainingBalance = Number(invoice?.remainingBalance || 0);
  const resolvedBookingId = booking?.bookingId || booking?.BookingId || booking?.id || invoice?.vehicleReservationId;
  const bookingStatus = (booking?.status || 'PENDING').toUpperCase();
  const hasExistingHold = Boolean(activeDepositHold && activeDepositHold.status === 'HELD');
  const isDepositStage = bookingStatus === 'PENDING' && !hasExistingHold;
  const isPendingWithHold = bookingStatus === 'PENDING' && hasExistingHold;
  const securityDepositAmount = 25000;

  useEffect(() => {
    const fetchMethods = async () => {
      if (!customerId) return;
      try {
        setLoadingMethods(true);
        const data = await paymentsApi.getCustomerMethods(customerId);
        setMethods(data || []);
        if (data && data.length > 0) {
          setSelectedMethodId(data[0].id);
        }
        if (resolvedBookingId) {
          try {
            const hold = await paymentsApi.getActiveSecurityHold(resolvedBookingId);
            setActiveDepositHold(hold && hold.status === 'HELD' ? hold : null);
          } catch (_) {
            setActiveDepositHold(null);
          }
        }
      } catch (err) {
        toast.error('Could not load saved payment cards');
      } finally {
        setLoadingMethods(false);
      }
    };
    fetchMethods();
  }, [customerId, resolvedBookingId]);

  // Determine amount to charge
  const calculateChargeAmount = () => {
    return isDepositStage ? securityDepositAmount : remainingBalance;
  };

  const handleAddCard = async (e) => {
    e.preventDefault();
    if (!newCardNumber.trim()) {
      toast.warning('Please enter a card number');
      return;
    }
    try {
      setAddingCard(true);
      const added = await paymentsApi.addCustomerPaymentMethod(customerId, {
        cardNumber: newCardNumber.trim(),
        simulatedBalance: parseFloat(newCardBalance) || 250000,
        expiryDate: newCardExpiry,
      });
      toast.success('Payment method added successfully');
      setMethods((prev) => [...prev, added]);
      setSelectedMethodId(added.id);
      setShowAddCard(false);
      setNewCardNumber('');
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to add card'));
    } finally {
      setAddingCard(false);
    }
  };

  const handleQuickCardSelect = (cardNumber, balance, expiry) => {
    setNewCardNumber(cardNumber);
    setNewCardBalance(balance);
    setNewCardExpiry(expiry);
  };

  const handlePayNow = async () => {
    if (isPendingWithHold) {
      toast.info('Security deposit is already authorized. Rental fee payment will be accepted upon vehicle handover.');
      return;
    }
    if (!selectedMethodId) {
      toast.warning('Please select a payment card');
      return;
    }

    const amountToPay = calculateChargeAmount();
    if (amountToPay <= 0) {
      toast.warning('Payment amount must be greater than zero');
      return;
    }

    if (!isDepositStage && amountToPay > remainingBalance) {
      toast.warning(`Amount cannot exceed remaining balance of ${formatCurrency(remainingBalance)}`);
      return;
    }

    const idempotencyKey = `${isDepositStage ? 'HOLD' : 'PAY'}-${invoice.id}-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;

    try {
      setProcessing(true);
      const result = isDepositStage
        ? await paymentsApi.placeSecurityHold({ invoiceId: invoice.id, paymentMethodId: selectedMethodId, holdAmount: amountToPay, idempotencyKey })
        : await paymentsApi.processPayment({ invoiceId: invoice.id, paymentMethodId: selectedMethodId, amount: amountToPay, idempotencyKey });

      if (result.status === (isDepositStage ? 'HELD' : 'SUCCESS')) {
        toast.success(isDepositStage ? 'Security deposit authorized successfully!' : 'Rental payment processed successfully!');
        setReceipt({ ...result, isDepositHold: isDepositStage });
        if (onPaymentSuccess) {
          onPaymentSuccess(result);
        }
      } else {
        toast.error(`Payment failed: ${result.responseCode || 'Declined'}`);
      }
    } catch (err) {
      const msg = extractErrorMessage(err, 'Payment processing failed');
      toast.error(msg);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="checkout-modal-overlay" onClick={onClose}>
      <div className="checkout-modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="checkout-modal-header">
          <div className="checkout-header-title">
            <HiOutlineCreditCard className="checkout-title-icon" />
            <div>
              <h3>Secure Checkout & Payment</h3>
              <p>Booking #{resolvedBookingId} • Invoice #{invoice?.id}</p>
            </div>
          </div>
          <button className="checkout-close-btn" onClick={onClose}><HiX /></button>
        </div>

        {receipt ? (
          /* Receipt Screen */
          <div className="checkout-receipt-view">
            <div className="receipt-success-icon">
              <HiOutlineCheckCircle />
            </div>
            <h3>{receipt.isDepositHold ? 'Deposit Hold Authorized!' : 'Payment Successful!'}</h3>
            <p className="receipt-subtitle">
              {receipt.isDepositHold
                ? 'Your security deposit is verified. Staff may now complete the vehicle handover checks.'
                : 'Your rental payment has been securely processed and confirmed.'}
            </p>

            <div className="receipt-details-box">
              <div className="receipt-row">
                <span>Transaction Ref:</span>
                <strong><code>{receipt.txnReference}</code></strong>
              </div>
              <div className="receipt-row">
                <span>Payment ID:</span>
                <span>#{receipt.paymentId}</span>
              </div>
              <div className="receipt-row">
                <span>Status Code:</span>
                <span className="status-badge-approved">{receipt.responseCode}</span>
              </div>
              <div className="receipt-row">
                <span>Processed At:</span>
                <span>{new Date(receipt.processedAt).toLocaleString('en-LK')}</span>
              </div>
              <div className="receipt-divider" />
              <div className="receipt-row total-highlight">
                <span>{receipt.isDepositHold ? 'Rental Balance Due After Handover:' : 'Remaining Balance Due:'}</span>
                <strong className={receipt.remainingInvoiceBalance > 0 ? 'text-due' : 'text-cleared'}>
                  {formatCurrency(receipt.remainingInvoiceBalance)}
                </strong>
              </div>
            </div>

            <div className="checkout-receipt-actions">
              <button className="btn-checkout-primary" onClick={onClose}>
                Done / Back to Bookings
              </button>
            </div>
          </div>
        ) : (
          /* Checkout Payment Form */
          <div className="checkout-modal-body">
            {/* Invoice Summary Banner */}
            <div className="checkout-summary-card">
              <div className="summary-col">
                <span className="summary-label">Total Invoice</span>
                <span className="summary-val">{formatCurrency(invoice?.totalAmount)}</span>
              </div>
              <div className="summary-col">
                <span className="summary-label">Already Paid</span>
                <span className="summary-val text-green">{formatCurrency(invoice?.paidAmount)}</span>
              </div>
              <div className="summary-col highlight-balance">
                <span className="summary-label">Balance Due</span>
                <span className="summary-val text-red">{formatCurrency(remainingBalance)}</span>
              </div>
            </div>

            {/* Business Policy Banner */}
            <div className={`checkout-policy-banner ${isPendingWithHold ? 'hold-verified' : isDepositStage ? 'deposit-required' : 'full-required'}`}>
              <HiOutlineShieldCheck className="policy-icon" />
              <div>
                <strong>
                  {isPendingWithHold
                    ? 'Security Deposit Verified • Handover Pending'
                    : isDepositStage
                    ? 'Security Deposit Required Before Handover'
                    : 'Final Rental Payment Settlement'}
                </strong>
                <p>
                  {isPendingWithHold
                    ? `Your refundable security deposit of ${formatCurrency(securityDepositAmount)} is verified. Staff will now complete pickup checks. Rental fee balance of ${formatCurrency(remainingBalance)} is payable upon/after vehicle handover.`
                    : isDepositStage
                    ? `Authorize only the refundable security deposit of ${formatCurrency(securityDepositAmount)}. This does not pay the rental fee.`
                    : `The vehicle has been handed over. Pay the full remaining rental balance of ${formatCurrency(remainingBalance)} before booking completion.`}
                </p>
              </div>
            </div>

            {/* Payment Options (Deposit vs Full) */}
            <div className="checkout-section">
              <label className="section-title">Select Payment Amount</label>
              {isPendingWithHold ? (
                <div className="single-payment-notice">
                  <span>Refundable Security Deposit Status:</span>
                  <strong className="text-green">{formatCurrency(securityDepositAmount)} (Authorized & Held)</strong>
                  <small style={{ display: 'block', marginTop: '6px', color: 'var(--text-secondary)' }}>
                    Rental fee payment will be accepted once vehicle handover is complete.
                  </small>
                </div>
              ) : isDepositStage ? (
                <div className="single-payment-notice">
                  <span>Customer pays only the refundable security deposit before handover:</span>
                  <strong>{formatCurrency(securityDepositAmount)}</strong>
                </div>
              ) : (
                <div className="single-payment-notice">
                  <span>Pay Total Rental Balance:</span>
                  <strong>{formatCurrency(calculateChargeAmount())}</strong>
                </div>
              )}
            </div>

            {/* Payment Methods Section */}
            <div className="checkout-section">
              <div className="section-title-row">
                <label className="section-title">Select Payment Card</label>
                <button
                  type="button"
                  className="btn-link-toggle"
                  onClick={() => setShowAddCard(!showAddCard)}
                >
                  <HiOutlinePlusCircle /> {showAddCard ? 'Hide Card Form' : 'Add Test Card'}
                </button>
              </div>

              {/* Add New Mock Card Form */}
              {showAddCard && (
                <form className="add-card-form" onSubmit={handleAddCard}>
                  <h5>Add Simulated Card for Demo</h5>
                  <div className="quick-presets">
                    <span>Quick presets:</span>
                    <button
                      type="button"
                      className="preset-btn"
                      onClick={() => handleQuickCardSelect('4111 2222 3333 4242', '500000', '2028-12-31')}
                    >
                      Visa Gold (LKR 500k)
                    </button>
                    <button
                      type="button"
                      className="preset-btn"
                      onClick={() => handleQuickCardSelect('5500 0000 0000 8888', '150000', '2027-10-31')}
                    >
                      Mastercard (LKR 150k)
                    </button>
                    <button
                      type="button"
                      className="preset-btn text-amber"
                      onClick={() => handleQuickCardSelect('4000 0000 0000 1000', '1000', '2028-12-31')}
                    >
                      Low Balance (LKR 1k)
                    </button>
                  </div>

                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Card Number</label>
                      <input
                        type="text"
                        placeholder="4111 2222 3333 4444"
                        value={newCardNumber}
                        onChange={(e) => setNewCardNumber(e.target.value)}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Simulated Balance (LKR)</label>
                      <input
                        type="number"
                        placeholder="250000"
                        value={newCardBalance}
                        onChange={(e) => setNewCardBalance(e.target.value)}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Expiry Date</label>
                    <input
                      type="date"
                      value={newCardExpiry}
                      onChange={(e) => setNewCardExpiry(e.target.value)}
                      required
                    />
                  </div>

                  <div className="form-actions-right">
                    <button type="submit" className="btn-add-card-submit" disabled={addingCard}>
                      {addingCard ? 'Saving Card...' : 'Save & Select Card'}
                    </button>
                  </div>
                </form>
              )}

              {/* Cards List */}
              {loadingMethods ? (
                <p className="loading-text">Loading saved cards...</p>
              ) : methods.length === 0 ? (
                <div className="empty-cards-notice">
                  <p>No cards on file. Please add a test card above.</p>
                </div>
              ) : (
                <div className="cards-selection-list">
                  {methods.map((method) => {
                    const isSelected = selectedMethodId === method.id;
                    const isLowBalance = Number(method.availableBalance) < calculateChargeAmount();

                    return (
                      <div
                        key={method.id}
                        className={`card-item ${isSelected ? 'selected' : ''} ${isLowBalance ? 'insufficient-funds' : ''}`}
                        onClick={() => setSelectedMethodId(method.id)}
                      >
                        <div className="card-item-radio" />
                        <div className="card-item-details">
                          <div className="card-number-row">
                            <span className="card-masked">{method.maskedCardNum}</span>
                            <span className="card-expiry">Exp: {method.expiryDate}</span>
                          </div>
                          <div className="card-balance-row">
                            <span>Available Balance:</span>
                            <strong className={isLowBalance ? 'text-red' : 'text-green'}>
                              {formatCurrency(method.availableBalance)}
                            </strong>
                            {method.heldAmount > 0 && (
                              <small className="card-held-tag">
                                (Held: {formatCurrency(method.heldAmount)})
                              </small>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Total to Authorize & Pay Button */}
            <div className="checkout-action-footer">
              <div className="total-charge-display">
                <span>Total Charge Now:</span>
                <strong>{formatCurrency(calculateChargeAmount())}</strong>
              </div>

              <button
                type="button"
                className="btn-pay-now"
                onClick={handlePayNow}
                disabled={processing || loadingMethods || !selectedMethodId || isPendingWithHold}
              >
                {processing ? (
                  <span className="spinner-text">Processing Authorization...</span>
                ) : isPendingWithHold ? (
                  <>Deposit Authorized (Awaiting Handover)</>
                ) : (
                  <>
                    <HiOutlineLockClosed /> {isDepositStage ? `Authorize Deposit ${formatCurrency(calculateChargeAmount())}` : `Pay Rental ${formatCurrency(calculateChargeAmount())}`}
                  </>
                )}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
