import { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-toastify';
import { getPendingCustomers, verifyCustomer, rejectCustomer, getNicFileBlob } from '../../api/users';
import Modal from '../../components/common/Modal';
import '../PanelPages.css';

/**
 * Loads a NIC image securely via the authenticated streaming endpoint.
 * Returns a Blob Object URL (must be revoked by the caller when no longer needed).
 */
async function loadSecureNicImage(fileName) {
  const blob = await getNicFileBlob(fileName);
  return URL.createObjectURL(blob);
}

export default function PendingCustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [modalImageUrl, setModalImageUrl] = useState(null);   // Object URL (Blob)
  const [loadingImage, setLoadingImage] = useState(false);
  const [imageError, setImageError] = useState(false);

  // ── Fetch pending customers ───────────────────────────────────────────────
  const fetchPending = async () => {
    try {
      const data = await getPendingCustomers();
      setCustomers(data);
    } catch {
      toast.error('Failed to load pending customers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPending(); }, []);

  // ── NIC modal ─────────────────────────────────────────────────────────────
  const handleOpenNicModal = useCallback(async (customer) => {
    setSelectedCustomer(customer);
    setImageError(false);
    setModalImageUrl(null);

    if (!customer.nicImageUrl) return;

    setLoadingImage(true);
    try {
      const objectUrl = await loadSecureNicImage(customer.nicImageUrl);
      setModalImageUrl(objectUrl);
    } catch {
      setImageError(true);
      toast.error('Could not load NIC document. Please try again.');
    } finally {
      setLoadingImage(false);
    }
  }, []);

  const handleCloseModal = useCallback(() => {
    // Revoke the Blob Object URL to free memory
    if (modalImageUrl) URL.revokeObjectURL(modalImageUrl);
    setSelectedCustomer(null);
    setModalImageUrl(null);
    setImageError(false);
  }, [modalImageUrl]);

  // ── Verify / Reject actions ───────────────────────────────────────────────
  const handleVerify = async (id) => {
    try {
      await verifyCustomer(id);
      toast.success('Customer verified successfully!');
      setCustomers((prev) => prev.filter((c) => c.id !== id));
      handleCloseModal();
    } catch {
      toast.error('Failed to verify customer');
    }
  };

  const handleReject = async (id) => {
    try {
      await rejectCustomer(id);
      toast.success('Customer rejected');
      setCustomers((prev) => prev.filter((c) => c.id !== id));
      handleCloseModal();
    } catch {
      toast.error('Failed to reject customer');
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="pending-page">
        <h1>Pending Customer Verifications</h1>
        <p>Loading…</p>
      </div>
    );
  }

  return (
    <div className="pending-page" id="pending-customers-page">
      <h1>Pending Customer Verifications</h1>
      <p>Review and verify customer NIC submissions to approve bookings</p>

      {customers.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">✅</div>
          <p>No pending verifications</p>
        </div>
      ) : (
        <div className="pending-table-wrapper">
          <table className="pending-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>NIC Document</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>#{customer.id}</td>
                  <td><strong>{customer.firstName} {customer.lastName}</strong></td>
                  <td>{customer.email}</td>
                  <td>
                    {customer.nicImageUrl ? (
                      <button
                        type="button"
                        className="nic-view-btn"
                        onClick={() => handleOpenNicModal(customer)}
                      >
                        🔍 View NIC Document
                      </button>
                    ) : (
                      <span className="nic-not-uploaded">Not uploaded</span>
                    )}
                  </td>
                  <td>
                    <div className="pending-actions">
                      <button className="action-btn verify" onClick={() => handleVerify(customer.id)}>
                        ✓ Verify
                      </button>
                      <button className="action-btn reject" onClick={() => handleReject(customer.id)}>
                        ✕ Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── NIC Document Preview Modal ── */}
      <Modal
        isOpen={!!selectedCustomer}
        onClose={handleCloseModal}
        title={
          selectedCustomer
            ? `NIC Document — ${selectedCustomer.firstName} ${selectedCustomer.lastName}`
            : 'NIC Document'
        }
        size="lg"
      >
        {selectedCustomer && (
          <div className="nic-modal-content">
            {/* Customer info row */}
            <div className="nic-modal-info">
              <span><strong>Email:</strong> {selectedCustomer.email}</span>
              <span><strong>Customer ID:</strong> #{selectedCustomer.id}</span>
            </div>

            {/* Image frame */}
            <div className="nic-modal-image-frame">
              {loadingImage && (
                <div className="nic-modal-loading">
                  <span className="nic-spinner" />
                  <span>Loading document…</span>
                </div>
              )}

              {!loadingImage && imageError && (
                <p className="nic-modal-error">⚠ Failed to load document image.</p>
              )}

              {!loadingImage && !imageError && modalImageUrl && (
                <img
                  src={modalImageUrl}
                  alt="Customer NIC"
                  className="nic-modal-img"
                />
              )}

              {!loadingImage && !imageError && !modalImageUrl && !selectedCustomer.nicImageUrl && (
                <p className="nic-modal-error">No NIC document uploaded by this customer.</p>
              )}
            </div>

            {/* Action buttons */}
            <div className="nic-modal-actions">
              <button
                className="action-btn reject"
                onClick={() => handleReject(selectedCustomer.id)}
              >
                ✕ Reject Customer
              </button>
              <button
                className="action-btn verify"
                onClick={() => handleVerify(selectedCustomer.id)}
              >
                ✓ Verify &amp; Approve
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
