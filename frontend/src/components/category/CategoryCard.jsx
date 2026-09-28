import { HiOutlinePencil, HiOutlineCurrencyDollar } from 'react-icons/hi';
import './CategoryCard.css';

export default function CategoryCard({ category, onEdit }) {
  return (
    <div className="category-card card" id={`category-card-${category.id}`}>
      <div className="category-card-icon">
        <HiOutlineCurrencyDollar />
      </div>
      <div className="category-card-info">
        <h3 className="category-name">{category.name}</h3>
        <p className="category-description">{category.description || 'No description available'}</p>
        <div className="category-price">
          <span className="price-tag">Base Price</span>
          <span className="price-value">LKR {category.price?.toLocaleString()}</span>
        </div>
      </div>
      <button
        className="btn btn-ghost btn-sm category-edit-btn"
        onClick={() => onEdit(category)}
        id={`edit-category-${category.id}`}
      >
        <HiOutlinePencil />
        Edit Price
      </button>
    </div>
  );
}
