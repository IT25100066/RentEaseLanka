import React from 'react';
import { HiOutlinePrinter, HiX, HiOutlineDownload, HiOutlineCheckCircle, HiOutlineClock, HiOutlineExclamation } from 'react-icons/hi';
import './InvoiceDocument.css';

export default function InvoiceDocument({ invoice, booking, onClose }) {
  if (!invoice) return null;

  const formatCurrency = (val) => {
    if (val === undefined || val === null) return 'LKR 0.00';
    return `LKR ${Number(val).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatDate = (val) => {
    if (!val) return '—';
    const d = new Date(val);
    return Number.isNaN(d.getTime()) ? val : d.toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' });
  };

  const handlePrint = () => {
    window.print();
  };

  const isPaid = invoice.status === 'PAID';
  const isPartial = invoice.status === 'PARTIALLY_PAID';
  const isUnpaid = invoice.status === 'UNPAID';
  const isNoShow = invoice.status === 'NO_SHOW';

  const invoiceNumber = `INV-${new Date().getFullYear()}-${String(invoice.id).padStart(5, '0')}`;

  return (
    <div className="invoice-modal-overlay" onClick={onClose}>
      <div className="invoice-modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Modal Action Bar (Hidden in Print) */}
        <div className="invoice-actions-bar no-print">
          <div className="invoice-actions-left">
            <span className="invoice-actions-title">Customer Tax Invoice</span>
            <span className={`invoice-status-pill ${invoice.status?.toLowerCase()}`}>
              {invoice.status?.replace('_', ' ')}
            </span>
          </div>
          <div className="invoice-actions-right">
            <button className="btn-invoice-print" onClick={handlePrint}>
              <HiOutlinePrinter className="btn-icon" /> Print / Save as PDF
            </button>
            <button className="btn-invoice-close" onClick={onClose}>
              <HiX />
            </button>
          </div>
        </div>

        {/* Printable Invoice Page Canvas */}
        <div className="invoice-paper" id="printable-invoice">
          {/* Header */}
          <div className="invoice-header">
            <div className="invoice-brand">
              <div className="invoice-logo">
                <span className="logo-accent">RentEase</span>Lanka
              </div>
              <p className="invoice-brand-tagline">Premium Vehicle Rentals & Fleet Management</p>
              <p className="invoice-brand-address">
                No. 128, Galle Road, Colombo 03, Sri Lanka<br />
                Hotline: +94 11 234 5678 | Email: billing@renteaselanka.com<br />
                VAT / Tax Reg: LK-9928374-VAT
              </p>
            </div>

            <div className="invoice-meta">
              <div className="invoice-title-badge">TAX INVOICE</div>
              <table className="invoice-meta-table">
                <tbody>
                  <tr>
                    <th>Invoice No:</th>
                    <td><strong>{invoiceNumber}</strong></td>
                  </tr>
                  <tr>
                    <th>Invoice Date:</th>
                    <td>{new Date().toLocaleDateString('en-LK', { dateStyle: 'medium' })}</td>
                  </tr>
                  <tr>
                    <th>Booking Ref:</th>
                    <td>#{invoice.vehicleReservationId}</td>
                  </tr>
                  <tr>
                    <th>Pickup Deadline:</th>
                    <td>{formatDate(invoice.pickupDeadline)}</td>
                  </tr>
                  <tr>
                    <th>Payment Status:</th>
                    <td>
                      <span className={`invoice-stamp-badge ${invoice.status?.toLowerCase()}`}>
                        {invoice.status}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div className="invoice-divider" />

          {/* Customer & Booking Details */}
          <div className="invoice-parties">
            <div className="invoice-party-col">
              <h4>BILLED TO</h4>
              <p className="party-name">
                {booking?.user?.firstName || booking?.customerName || `Customer #${invoice.customerId}`}
                {booking?.user?.lastName ? ` ${booking.user.lastName}` : ''}
              </p>
              <p className="party-detail">{booking?.user?.email || 'Registered Customer'}</p>
              <p className="party-detail">Customer ID: {invoice.customerId}</p>
              {booking?.user?.phoneNumber && (
                <p className="party-detail">Phone: {booking.user.phoneNumber}</p>
              )}
            </div>

            <div className="invoice-party-col">
              <h4>RENTAL DETAILS</h4>
              <p className="party-name">{booking?.model || booking?.vehicleModel || `Vehicle Reservation #${invoice.vehicleReservationId}`}</p>
              <p className="party-detail">
                <strong>Rental Duration:</strong> {invoice.rentalDays || 1} {invoice.rentalDays === 1 ? 'Day' : 'Days'}
                {invoice.rentalDays <= 2 ? ' (Short-Term: 100% Upfront)' : ' (Standard Rental)'}
              </p>
              <p className="party-detail">
                <strong>Pickup:</strong> {formatDate(booking?.pickupDateTime)}
              </p>
              <p className="party-detail">
                <strong>Return:</strong> {formatDate(booking?.returnDateTime)}
              </p>
              {booking?.pickupLocation && (
                <p className="party-detail">
                  <strong>Location:</strong> {booking.pickupLocation}
                </p>
              )}
            </div>
          </div>

          {/* Line Items Table */}
          <table className="invoice-items-table">
            <thead>
              <tr>
                <th>Description</th>
                <th className="text-center">Days / Qty</th>
                <th className="text-right">Daily Rate</th>
                <th className="text-right">Total Amount</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>
                  <strong>Vehicle Rental Fee</strong>
                  <div className="item-subtext">
                    {booking?.model || 'Reserved Vehicle'} - {invoice.rentalDays || 1} day(s) reservation
                  </div>
                </td>
                <td className="text-center">{invoice.rentalDays || 1}</td>
                <td className="text-right">{formatCurrency(invoice.dailyRate)}</td>
                <td className="text-right">{formatCurrency(invoice.subtotal)}</td>
              </tr>
              {invoice.taxAmount && Number(invoice.taxAmount) > 0 ? (
                <tr>
                  <td>
                    <strong>Government Taxes & Service Charges (VAT)</strong>
                  </td>
                  <td className="text-center">1</td>
                  <td className="text-right">{formatCurrency(invoice.taxAmount)}</td>
                  <td className="text-right">{formatCurrency(invoice.taxAmount)}</td>
                </tr>
              ) : null}
            </tbody>
          </table>

          {/* Summary & Totals */}
          <div className="invoice-summary-section">
            <div className="invoice-notes-block">
              <h5>Payment Terms & Conditions</h5>
              <ul>
                <li>Short-term rentals (1–2 days) require 100% upfront settlement prior to key handover.</li>
                <li>Longer rentals (3+ days) require a minimum deposit of {formatCurrency(invoice.minimumDepositAmount)} (30% or 1 day rate).</li>
                <li>Pickup grace window is 2 hours from scheduled pickup time. Unclaimed reservations may be marked as No-Show with standard penalty calculation.</li>
                <li>Vehicles must be returned in the same condition with matched fuel levels.</li>
              </ul>
            </div>

            <div className="invoice-totals-block">
              <table className="invoice-totals-table">
                <tbody>
                  <tr>
                    <th>Subtotal:</th>
                    <td>{formatCurrency(invoice.subtotal)}</td>
                  </tr>
                  <tr>
                    <th>Taxes & Levies:</th>
                    <td>{formatCurrency(invoice.taxAmount || 0)}</td>
                  </tr>
                  <tr className="grand-total-row">
                    <th>Total Invoice Amount:</th>
                    <td>{formatCurrency(invoice.totalAmount)}</td>
                  </tr>
                  <tr className="paid-row">
                    <th>Total Paid:</th>
                    <td>{formatCurrency(invoice.paidAmount)}</td>
                  </tr>
                  <tr className="balance-row">
                    <th>Remaining Balance Due:</th>
                    <td>
                      <strong className={invoice.remainingBalance > 0 ? 'text-due' : 'text-cleared'}>
                        {formatCurrency(invoice.remainingBalance)}
                      </strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          {/* Payment Receipts History */}
          {invoice.paymentHistory && invoice.paymentHistory.length > 0 && (
            <div className="invoice-payments-history">
              <h4>Payment Transaction History</h4>
              <table className="invoice-history-table">
                <thead>
                  <tr>
                    <th>Txn Reference</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                    <th>Response Code</th>
                    <th className="text-right">Amount Paid</th>
                  </tr>
                </thead>
                <tbody>
                  {invoice.paymentHistory.map((p, idx) => (
                    <tr key={idx}>
                      <td><code>{p.txnReference || `TXN-${p.paymentId}`}</code></td>
                      <td>{formatDate(p.processedAt)}</td>
                      <td>
                        <span className={`status-tag-sm ${p.status?.toLowerCase()}`}>
                          {p.status}
                        </span>
                      </td>
                      <td>{p.responseCode || '200_OK'}</td>
                      <td className="text-right font-bold">
                        {p.status === 'SUCCESS' ? 'LKR ' + (invoice.paidAmount ? Number(invoice.paidAmount).toLocaleString('en-LK') : '—') : 'LKR 0.00'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Footer */}
          <div className="invoice-footer">
            <div className="invoice-signature">
              <div className="signature-line" />
              <p>Authorized Signature / Finance Dept.</p>
              <small>RentEase Lanka Management</small>
            </div>
            <div className="invoice-thank-you">
              <p>Thank you for choosing RentEase Lanka!</p>
              <small>This is a computer-generated tax invoice and requires no physical seal.</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
