import { useState, useEffect } from 'react';
import API from '../api/axiosInstance';
import CategoryCard from '../components/category/CategoryCard';
import AddCategoryForm from '../components/category/AddCategoryForm';
import EditCategoryModal from '../components/category/EditCategoryModal';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { HiOutlinePlus } from 'react-icons/hi';

export default function CategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editCategory, setEditCategory] = useState(null);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await API.get('/category');
      setCategories(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  return (
    <div className="page-container" id="categories-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Category Management</h1>
          <p className="page-subtitle">
            Manage vehicle categories and pricing ({categories.length} categories)
          </p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowAddModal(true)}
          id="add-category-btn"
        >
          <HiOutlinePlus />
          Add Category
        </button>
      </div>

      {loading ? (
        <LoadingSpinner message="Loading categories..." />
      ) : categories.length === 0 ? (
        <EmptyState
          title="No categories found"
          message="Create your first vehicle category to get started."
        />
      ) : (
        <div className="grid-cards">
          {categories.map((cat) => (
            <CategoryCard
              key={cat.id}
              category={cat}
              onEdit={(c) => setEditCategory(c)}
            />
          ))}
        </div>
      )}

      {/* Add Category Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Create Category"
        size="sm"
      >
        <AddCategoryForm
          onSuccess={() => {
            setShowAddModal(false);
            fetchCategories();
          }}
        />
      </Modal>

      {/* Edit Category Modal */}
      <Modal
        isOpen={!!editCategory}
        onClose={() => setEditCategory(null)}
        title="Update Category Pricing"
        size="sm"
      >
        {editCategory && (
          <EditCategoryModal
            category={editCategory}
            onSuccess={fetchCategories}
            onClose={() => setEditCategory(null)}
          />
        )}
      </Modal>
    </div>
  );
}
