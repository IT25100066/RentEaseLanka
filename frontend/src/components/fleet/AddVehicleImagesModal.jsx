import { useState, useRef } from 'react';
import { toast } from 'react-toastify';
import { HiOutlineUpload, HiOutlineX, HiOutlinePhotograph } from 'react-icons/hi';
import { addVehicleImages } from '../../api/fleet';
import Modal from '../common/Modal';
import LoadingSpinner from '../common/LoadingSpinner';
import './AddVehicleImagesModal.css';

export default function AddVehicleImagesModal({ vehicle, isOpen, onClose, onSuccess }) {
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef(null);

  if (!isOpen || !vehicle) return null;

  const handleFiles = (files) => {
    const validImageFiles = Array.from(files).filter((file) =>
      file.type.startsWith('image/')
    );

    if (validImageFiles.length === 0) {
      toast.warning('Please select image files (JPEG, PNG, WEBP, etc.)');
      return;
    }

    const newPreviews = validImageFiles.map((file) => ({
      file,
      url: URL.createObjectURL(file),
      name: file.name,
      size: (file.size / (1024 * 1024)).toFixed(2) + ' MB',
    }));

    setSelectedFiles((prev) => [...prev, ...validImageFiles]);
    setPreviews((prev) => [...prev, ...newPreviews]);
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleFileInputChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(e.target.files);
    }
  };

  const removeImage = (index) => {
    URL.revokeObjectURL(previews[index].url);
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
    setPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const clearAll = () => {
    previews.forEach((p) => URL.revokeObjectURL(p.url));
    setSelectedFiles([]);
    setPreviews([]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (selectedFiles.length === 0) {
      toast.warning('Please select at least one image to upload.');
      return;
    }

    setLoading(true);
    try {
      await addVehicleImages(vehicle.id, selectedFiles);
      toast.success(
        `Successfully added ${selectedFiles.length} image${selectedFiles.length > 1 ? 's' : ''} to ${vehicle.model}!`
      );
      clearAll();
      if (onSuccess) onSuccess();
      if (onClose) onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Failed to upload vehicle images.';
      toast.error(typeof msg === 'string' ? msg : 'Failed to upload vehicle images.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => {
        if (!loading) {
          clearAll();
          onClose();
        }
      }}
      title="Add Vehicle Images"
      size="md"
    >
      <form onSubmit={handleSubmit} className="add-images-container" id="add-vehicle-images-form">
        <div className="add-images-header-info">
          <div className="add-images-vehicle-name">
            🚗 {vehicle.model} ({vehicle.year})
          </div>
          <div className="add-images-vehicle-sub">
            Plate: <strong>{vehicle.licensePlate}</strong> &bull; Existing Images: {vehicle.imageUrls?.length || 0}
          </div>
        </div>

        {/* Dropzone */}
        <div
          className={`add-images-dropzone ${dragActive ? 'drag-active' : ''}`}
          onClick={() => fileInputRef.current?.click()}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          id="vehicle-images-dropzone"
        >
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileInputChange}
            multiple
            accept="image/*"
            style={{ display: 'none' }}
            id="vehicle-images-file-input"
          />
          <HiOutlineUpload className="add-images-dropzone-icon" />
          <div className="add-images-dropzone-title">
            Click to browse or drag and drop images here
          </div>
          <div className="add-images-dropzone-hint">
            Supports PNG, JPG, JPEG, WEBP. You can select multiple images at once.
          </div>
        </div>

        {/* Previews */}
        {previews.length > 0 && (
          <div className="add-images-preview-section">
            <div className="add-images-preview-header">
              <span>Selected Images ({previews.length})</span>
              <button
                type="button"
                className="btn btn-ghost btn-xs text-danger"
                onClick={clearAll}
                disabled={loading}
              >
                Clear all
              </button>
            </div>
            <div className="add-images-grid">
              {previews.map((item, idx) => (
                <div className="add-image-card" key={idx}>
                  <img src={item.url} alt={`Upload preview ${idx + 1}`} />
                  <button
                    type="button"
                    className="add-image-card-remove"
                    onClick={(e) => {
                      e.stopPropagation();
                      removeImage(idx);
                    }}
                    disabled={loading}
                    title="Remove"
                  >
                    <HiOutlineX />
                  </button>
                  <div className="add-image-card-badge">{item.size}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="add-images-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => {
              clearAll();
              onClose();
            }}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading || selectedFiles.length === 0}
            id="submit-add-images-btn"
          >
            {loading ? (
              <>
                <LoadingSpinner size="sm" inline />
                Uploading...
              </>
            ) : (
              <>
                <HiOutlinePhotograph />
                Upload {selectedFiles.length > 0 ? `${selectedFiles.length} ` : ''}Image{selectedFiles.length === 1 ? '' : 's'}
              </>
            )}
          </button>
        </div>
      </form>
    </Modal>
  );
}
