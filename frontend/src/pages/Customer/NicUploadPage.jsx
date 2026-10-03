import { useState, useRef, useCallback } from 'react';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { uploadNic } from '../../api/users';
import '../PanelPages.css';

// ── Status configuration ──────────────────────────────────────────────────────
const STATUS_CONFIG = {
  VERIFIED: {
    icon: '✅',
    label: 'Identity Verified',
    description: 'Your NIC has been reviewed and approved. You can now make vehicle bookings.',
    variant: 'verified',
  },
  PENDING: {
    icon: '⏳',
    label: 'Under Review',
    description: "Your NIC has been submitted and is pending staff review. We'll notify you once it's processed.",
    variant: 'pending',
  },
  REJECTED: {
    icon: '❌',
    label: 'Verification Rejected',
    description: 'Your previous submission was rejected. Please re-upload a clear image of your NIC.',
    variant: 'rejected',
  },
};

const MAX_FILE_SIZE_MB = 5;

export default function NicUploadPage() {
  const { user, updateUser } = useAuth();
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef(null);

  const status = user?.verificationStatus || 'PENDING';
  const hasUploaded = !!user?.nicImageUrl;
  // Show upload form only for REJECTED users or first-time (no NIC yet)
  const canUpload = status === 'REJECTED' || (!hasUploaded && status !== 'VERIFIED');

  // ── File selection helpers ──────────────────────────────────────────────────
  const validateAndSetFile = useCallback((selected) => {
    if (!selected) return;
    if (!selected.type.startsWith('image/')) {
      toast.error('Please upload an image file (PNG, JPG, JPEG)');
      return;
    }
    if (selected.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      toast.error(`File is too large. Maximum allowed size is ${MAX_FILE_SIZE_MB} MB.`);
      return;
    }
    const objectUrl = URL.createObjectURL(selected);
    if (preview) URL.revokeObjectURL(preview);
    setPreview(objectUrl);
    setFile(selected);
  }, [preview]);

  const handleFileChange = (e) => validateAndSetFile(e.target.files?.[0]);

  // ── Drag-and-drop handlers ──────────────────────────────────────────────────
  const handleDragOver = (e) => { e.preventDefault(); setDragOver(true); };
  const handleDragLeave = () => setDragOver(false);
  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    validateAndSetFile(e.dataTransfer.files?.[0]);
  };

  // ── Upload ──────────────────────────────────────────────────────────────────
  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) { toast.error('Please select an NIC image first'); return; }
    setLoading(true);
    try {
      const updatedUser = await uploadNic(file);
      if (updateUser && updatedUser) updateUser(updatedUser);
      toast.success('NIC uploaded successfully! Your verification is now pending staff review.');
      setFile(null);
      if (preview) { URL.revokeObjectURL(preview); setPreview(null); }
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (error) {
      const errMsg =
        error.response?.data?.message ||
        error.response?.data ||
        'Failed to upload NIC. Please try again.';
      toast.error(errMsg);
    } finally {
      setLoading(false);
    }
  };

  const statusConfig = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;

  return (
    <div className="customer-page" id="nic-upload-page">
      <div className="nic-page-header">
        <h1>NIC Verification</h1>
        <p>Upload your National Identity Card to get verified for booking vehicles.</p>
      </div>

      {/* ── Status Banner ── */}
      <div className={`nic-status-banner nic-status-banner--${statusConfig.variant}`}>
        <span className="nic-status-banner__icon">{statusConfig.icon}</span>
        <div className="nic-status-banner__body">
          <strong className="nic-status-banner__label">{statusConfig.label}</strong>
          <p className="nic-status-banner__desc">{statusConfig.description}</p>
        </div>
      </div>

      {/* ── VERIFIED: locked card ── */}
      {status === 'VERIFIED' && (
        <div className="nic-verified-card">
          <div className="nic-verified-card__badge">✓</div>
          <p className="nic-verified-card__text">
            Your identity has been confirmed. No further action is required.
          </p>
        </div>
      )}

      {/* ── PENDING: review notice ── */}
      {status === 'PENDING' && hasUploaded && (
        <div className="nic-pending-notice">
          <span className="nic-pending-notice__icon">🔒</span>
          <p>Your NIC document is locked for editing while it is under review.</p>
        </div>
      )}

      {/* ── REJECTED / First-time: upload form ── */}
      {canUpload && (
        <form onSubmit={handleUpload} className="nic-upload-form">
          {/* Drop zone */}
          <div
            className={`upload-area${dragOver ? ' drag-over' : ''}`}
            onClick={() => fileInputRef.current?.click()}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && fileInputRef.current?.click()}
            aria-label="Click or drag and drop to select NIC image"
          >
            {preview ? (
              <div className="nic-preview-wrapper">
                <img src={preview} alt="NIC Preview" className="nic-preview-img" />
                <span className="nic-preview-filename">{file?.name} — click to change</span>
              </div>
            ) : (
              <>
                <div className="upload-icon">📄</div>
                <p className="upload-area__main-text">Click or drag &amp; drop your NIC image</p>
                <p className="upload-area__sub-text">PNG, JPG, JPEG — max {MAX_FILE_SIZE_MB} MB</p>
              </>
            )}
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept="image/*"
              className="nic-file-input"
            />
          </div>

          <button
            type="submit"
            className="create-form-btn nic-upload-btn"
            disabled={loading || !file}
          >
            {loading ? (
              <><span className="nic-spinner" />Uploading NIC...</>
            ) : (
              <><span>📤</span> Upload NIC Document</>
            )}
          </button>
        </form>
      )}
    </div>
  );
}
