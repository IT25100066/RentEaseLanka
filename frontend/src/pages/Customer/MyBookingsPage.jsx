import React, { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-toastify';
import {
  HiOutlineCreditCard,
  HiOutlineDocumentText,
  HiOutlineDownload,
  HiOutlinePrinter,
  HiOutlineRefresh,
  HiOutlineShieldCheck,
  HiOutlineClock,
  HiOutlinePencilAlt
} from 'react-icons/hi';
import { useAuth } from '../../context/AuthContext';
import { getCustomerBookings, cancelBooking } from '../../api/bookings';
import paymentsApi from '../../api/payments';
import StatusBadge from '../../components/common/StatusBadge';
import CheckoutModal from '../../components/payments/CheckoutModal';
import InvoiceDocument from '../../components/payments/InvoiceDocument';
import AddReviewModal from '../../components/review/AddReviewModal';
import '../PanelPages.css';
import './MyBookingsPage.css';

const formatDateTime = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' });
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '—';
  return `LKR ${Number(val).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

export default function MyBookingsPage() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [invoicesMap, setInvoicesMap] = useState({});
  const [holdsMap, setHoldsMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);

  // Active Modals
  const [selectedCheckout, setSelectedCheckout] = useState(null); // { invoice, booking }
  const [selectedInvoiceDoc, setSelectedInvoiceDoc] = useState(null); // { invoice, booking }
  const [fetchingInvoiceForBookingId, setFetchingInvoiceForBookingId] = useState(null);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);

  const fetchBookingsAndInvoices = useCallback(async () => {
    if (!user?.id) return;
    try {
      setLoading(true);
      const bookingsData = await getCustomerBookings(user.id);
      setBookings(bookingsData || []);

      // Fetch corresponding invoices and security holds in parallel
      const map = {};
      const hMap = {};
      await Promise.allSettled(
        (bookingsData || []).map(async (b) => {
          const bId = b.bookingId || b.BookingId || b.id;
          if (!bId) return;
          try {
            const inv = await paymentsApi.getInvoiceByBookingId(bId);
            map[bId] = inv;
          } catch (e) {
            // Ignore if invoice not found yet
          }
          try {
            const hold = await paymentsApi.getActiveSecurityHold(bId);
            hMap[bId] = hold;
          } catch (e) {
            hMap[bId] = null;
          }
        })
      );
      setInvoicesMap(map);
      setHoldsMap(hMap);
    } catch (error) {
      toast.error('Failed to load your bookings');
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchBookingsAndInvoices();
  }, [fetchBookingsAndInvoices]);

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    setCancellingId(id);
    try {
      await cancelBooking(id);
      toast.success('Booking cancelled successfully');
      setBookings((prev) =>
        prev.map((booking) => {
          const bId = booking.bookingId || booking.BookingId || booking.id;
          return bId === id ? { ...booking, status: 'CANCELLED' } : booking;
        })
      );
    } catch (error) {
      toast.error('Failed to cancel booking');
    } finally {
      setCancellingId(null);
    }
  };

  const handleOpenCheckout = async (booking) => {
    const bId = booking.bookingId || booking.BookingId || booking.id;
    setFetchingInvoiceForBookingId(bId);
    try {
      let inv = invoicesMap[bId];
      if (!inv) {
        inv = await paymentsApi.getInvoiceByBookingId(bId);
        setInvoicesMap((prev) => ({ ...prev, [bId]: inv }));
      }
      setSelectedCheckout({ invoice: inv, booking });
    } catch (err) {
      toast.error('Could not load invoice for payment');
    } finally {
      setFetchingInvoiceForBookingId(null);
    }
  };

  const handleOpenInvoiceDoc = async (booking) => {
    const bId = booking.bookingId || booking.BookingId || booking.id;
    setFetchingInvoiceForBookingId(bId);
    try {
      let inv = invoicesMap[bId];
      if (!inv) {
        inv = await paymentsApi.getInvoiceByBookingId(bId);
        setInvoicesMap((prev) => ({ ...prev, [bId]: inv }));
      }
      setSelectedInvoiceDoc({ invoice: inv, booking });
    } catch (err) {
      toast.error('Could not load invoice document');
    } finally {
      setFetchingInvoiceForBookingId(null);
    }
  };

  const handlePaymentSuccess = async (receipt) => {
    // Refresh bookings & invoices
    await fetchBookingsAndInvoices();
  };

  if (loading) {
    return (
      <div className="customer-page">
        <h1 style={{ marginLeft: '12px', paddingTop: '48px' }}>My Bookings</h1>
        <p style={{ marginLeft: '12px' }}>Loading your reservations and payment records…</p>
      </div>
    );
  }

  return (
    <div className="customer-page" id="my-bookings-page">
      <div className="bookings-page-header">
        <div>
          <h1>My Bookings</h1>
          <p className="page-subtitle">View your reservations, security holds, and payment invoices</p>
        </div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <button
            className="btn-refresh-bookings"
            onClick={() => setIsReviewModalOpen(true)}
            style={{ background: '#2563eb', color: '#fff', border: 'none' }}
          >
            <HiOutlinePencilAlt /> Write a Review
          </button>
          <button
            className="btn-refresh-bookings"
            onClick={fetchBookingsAndInvoices}
            title="Refresh Bookings & Payments"
          >
            <HiOutlineRefresh /> Refresh
          </button>
        </div>
      </div>

      {bookings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <p>You don't have any bookings yet</p>
        </div>
      ) : (
        <div className="customer-bookings-table-container">
          <table className="data-table customer-bookings-table" id="my-bookings-table">
            <thead>
              <tr>
                <th>Booking</th>
                <th>Vehicle</th>
                <th>Pickup / Return</th>
                <th>Payment & Invoice</th>
                <th>Booking Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((booking) => {
                const bookingId = booking.bookingId || booking.BookingId || booking.id;
                const status = (booking.status || 'PENDING').toUpperCase();
                const invoice = invoicesMap[bookingId];
                const hold = holdsMap[bookingId];
                const hasActiveHold = hold && hold.status === 'HELD';
                const isPaid = invoice?.status === 'PAID';
                const isPartiallyPaid = invoice?.status === 'PARTIALLY_PAID';
                const isCancelled = status === 'CANCELLED';

                return (
                  <tr key={bookingId}>
                    <td data-label="Booking">
                      <strong>#{bookingId}</strong>
                    </td>

                    <td data-label="Vehicle">
                      <div className="booking-vehicle-name">
                        {booking.model || `Vehicle ID: ${booking.vehicleId}`}
                      </div>
                      <small className="booking-vehicle-sub">
                        Rate: {booking.vehicle?.dailyRentalRate ? formatCurrency(booking.vehicle.dailyRentalRate) + '/day' : 'Daily Rate'}
                      </small>
                    </td>

                    <td className="booking-date-cell" data-label="Pickup / Return">
                      <div className="pickup-date-line">
                        <strong>Pickup:</strong> {formatDateTime(booking.pickupDateTime)}
                      </div>
                      <div className="return-date-line">
                        <strong>Return:</strong> {formatDateTime(booking.returnDateTime)}
                      </div>
                    </td>

                    <td data-label="Payment & Invoice" className="payment-status-cell">
                      <div className="invoice-status-row">
                        <span className={`payment-pill ${invoice?.status?.toLowerCase() || 'unpaid'}`}>
                          {invoice?.status?.replace('_', ' ') || 'UNPAID'}
                        </span>
                        {hasActiveHold && (
                          <div className="deposit-hold-pill" title="LKR 25,000 security deposit pre-auth hold verified">
                            ✓ Deposit Held (LKR 25k)
                          </div>
                        )}
                      </div>

                      <div className="invoice-amount-breakdown">
                        <div className="amount-line">
                          <span>Total:</span>
                          <strong>{formatCurrency(invoice?.totalAmount || booking.totalAmount)}</strong>
                        </div>
                        {invoice && invoice.paidAmount > 0 && (
                          <div className="amount-line text-green">
                            <span>Paid:</span>
                            <span>{formatCurrency(invoice.paidAmount)}</span>
                          </div>
                        )}
                        {invoice && invoice.remainingBalance > 0 && (
                          <div className="amount-line text-due">
                            <span>Balance Due:</span>
                            <span>{formatCurrency(invoice.remainingBalance)}</span>
                          </div>
                        )}
                      </div>
                    </td>

                    <td data-label="Booking Status">
                      <StatusBadge status={status} />
                    </td>

                    <td data-label="Actions">
                      <div className="customer-booking-actions">
                        {/* Stage 1: PENDING and NO deposit hold -> Authorize Deposit */}
                        {status === 'PENDING' && !hasActiveHold && (
                          <button
                            className="btn-customer-pay btn-customer-deposit"
                            onClick={() => handleOpenCheckout(booking)}
                            disabled={fetchingInvoiceForBookingId === bookingId}
                            title="Pre-authorize refundable security deposit before handover"
                          >
                            <HiOutlineCreditCard />
                            Authorize Deposit (LKR 25,000)
                          </button>
                        )}

                        {/* Stage 2: PENDING and Deposit Hold is Active -> Handover pending */}
                        {status === 'PENDING' && hasActiveHold && (
                          <span
                            className="hold-pending-notice"
                            title="Security deposit hold verified. Vehicle pickup handover is pending with staff. Rental fee is due upon handover."
                          >
                            Deposit Verified • Awaiting Handover
                          </span>
                        )}

                        {/* Stage 3: Handover Complete (CONFIRMED or ACTIVE) and not fully paid -> Pay Rental Fee */}
                        {(status === 'CONFIRMED' || status === 'ACTIVE') && !isPaid && !isCancelled && (
                          <button
                            className="btn-customer-pay"
                            onClick={() => handleOpenCheckout(booking)}
                            disabled={fetchingInvoiceForBookingId === bookingId}
                            title="Pay remaining rental balance"
                          >
                            <HiOutlineCreditCard />
                            {isPartiallyPaid ? 'Pay Remaining Balance' : 'Pay Rental Fee'}
                          </button>
                        )}

                        {/* Download / Print Invoice Button */}
                        <button
                          className="btn-customer-invoice"
                          onClick={() => handleOpenInvoiceDoc(booking)}
                          disabled={fetchingInvoiceForBookingId === bookingId}
                          title="Download / Print Tax Invoice PDF"
                        >
                          <HiOutlineDocumentText />
                          Invoice
                        </button>

                        {/* Cancel Button if still PENDING */}
                        {status === 'PENDING' && (
                          <button
                            className="action-btn reject customer-booking-cancel"
                            onClick={() => handleCancel(bookingId)}
                            disabled={cancellingId === bookingId}
                          >
                            {cancellingId === bookingId ? 'Cancelling...' : 'Cancel'}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Checkout Modal */}
      {selectedCheckout && (
        <CheckoutModal
          invoice={selectedCheckout.invoice}
          booking={selectedCheckout.booking}
          customerId={user?.id}
          onClose={() => setSelectedCheckout(null)}
          onPaymentSuccess={handlePaymentSuccess}
        />
      )}

      {/* Printable Invoice Modal */}
      {selectedInvoiceDoc && (
        <InvoiceDocument
          invoice={selectedInvoiceDoc.invoice}
          booking={selectedInvoiceDoc.booking}
          onClose={() => setSelectedInvoiceDoc(null)}
        />
      )}

      {/* Customer Service Review Modal */}
      <AddReviewModal
        isOpen={isReviewModalOpen}
        onClose={() => setIsReviewModalOpen(false)}
        onSuccess={fetchBookingsAndInvoices}
      />
    </div>
  );
}
