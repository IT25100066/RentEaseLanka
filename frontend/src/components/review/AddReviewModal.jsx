import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { HiOutlineStar, HiStar, HiOutlineX } from 'react-icons/hi';
import { submitReview } from '../../api/reviews';
import './AddReviewModal.css';

export default function AddReviewModal({ isOpen, onClose, onSuccess }) {
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [location, setLocation] = useState('');
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!comment.trim()) {
      toast.error('Please write a brief comment sharing your experience.');
      return;
    }

    try {
      setLoading(true);
      await submitReview({
        rating,
        location: location.trim() || 'Sri Lanka',
        comment: comment.trim(),
      });
      toast.success('Thank you! Your review has been submitted successfully.');
      if (onSuccess) onSuccess();
      onClose();
      // Reset form
      setRating(5);
      setLocation('');
      setComment('');
    } catch (err) {
      toast.error(err.userMessage || 'Failed to submit review. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="review-modal-overlay" onClick={onClose}>
      <div className="review-modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="review-modal-header">
          <div>
            <h3>Share Your Experience</h3>
            <p>Tell us how RentEaseLanka helped your journey</p>
          </div>
          <button className="review-modal-close" onClick={onClose} aria-label="Close modal">
            <HiOutlineX />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="review-modal-form">
          {/* Star Rating */}
          <div className="form-group text-center">
            <label className="rating-label">Overall Rating</label>
            <div className="star-picker">
              {[1, 2, 3, 4, 5].map((star) => {
                const activeStar = hoverRating ? star <= hoverRating : star <= rating;
                return (
                  <button
                    type="button"
                    key={star}
                    className={`star-btn ${activeStar ? 'active' : ''}`}
                    onClick={() => setRating(star)}
                    onMouseEnter={() => setHoverRating(star)}
                    onMouseLeave={() => setHoverRating(0)}
                    aria-label={`${star} star rating`}
                  >
                    {activeStar ? <HiStar /> : <HiOutlineStar />}
                  </button>
                );
              })}
            </div>
            <span className="rating-hint">
              {hoverRating || rating} out of 5 Stars ({
                (hoverRating || rating) === 5 ? 'Excellent' :
                (hoverRating || rating) === 4 ? 'Very Good' :
                (hoverRating || rating) === 3 ? 'Good' :
                (hoverRating || rating) === 2 ? 'Fair' : 'Poor'
              })
            </span>
          </div>

          {/* Location */}
          <div className="form-group">
            <label htmlFor="review-location">Your City / Location (Optional)</label>
            <input
              id="review-location"
              type="text"
              placeholder="e.g. Colombo, Kandy, Galle"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="review-input"
            />
          </div>

          {/* Comment */}
          <div className="form-group">
            <label htmlFor="review-comment">Your Review <span className="required-star">*</span></label>
            <textarea
              id="review-comment"
              rows="4"
              maxLength="1000"
              placeholder="Write your review here... How was the vehicle condition, service, and booking process?"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              className="review-textarea"
              required
            />
            <span className="char-count">{comment.length} / 1000 characters</span>
          </div>

          {/* Actions */}
          <div className="review-modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Review'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
