import { HiOutlinePencil, HiOutlineCurrencyDollar } from 'react-icons/hi';
import './CategoryCard.css';

export default function CategoryCard({ category, onEdit }) {
  if (!category) return null;

  const categoryName = (category.name || '').toUpperCase();
  const price = category.basePrice ?? category.price;

  return (
    <div className="category-card card" id={`category-card-${category.id}`}>
      <div className="category-card-icon">
        <HiOutlineCurrencyDollar />
      </div>
      <div className="category-card-info">
        <h3 className="category-name">{categoryName}</h3>
        <p className="category-description">{category.description || 'No description available'}</p>
        <div className="category-price">
          <span className="price-tag">Base Price</span>
          <span className="price-value">
            {price != null ? `LKR ${price.toLocaleString()}` : 'LKR 0'}
          </span>
        </div>
      </div>
      <button
        className="btn btn-ghost btn-sm category-edit-btn"
        onClick={() => onEdit && onEdit(category)}
        id={`edit-category-${category.id}`}
      >
        <HiOutlinePencil />
        Edit Price
      </button>
    </div>
  );
}
