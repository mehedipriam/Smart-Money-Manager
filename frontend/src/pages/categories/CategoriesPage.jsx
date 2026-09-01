import { useEffect, useState } from 'react';
import * as categoryService from '../../services/categoryService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import CategoryForm from './CategoryForm.jsx';
import './CategoriesPage.css';

function CategoryGroup({ title, categories, onEdit, onDelete }) {
  return (
    <div className="card category-group">
      <h2>{title}</h2>
      <ul className="category-list">
        {categories.map((category) => (
          <li key={category.id} className="category-row">
            <span className="category-row__swatch" style={{ background: category.color || '#9ca3af' }} />
            <span className="category-row__icon">{category.icon}</span>
            <span className="category-row__name">{category.name}</span>
            {category.defaultCategory ? (
              <span className="category-row__badge">Default</span>
            ) : (
              <span className="category-row__actions">
                <button type="button" onClick={() => onEdit(category)}>
                  Edit
                </button>
                <button type="button" className="category-row__danger" onClick={() => onDelete(category)}>
                  Delete
                </button>
              </span>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function CategoriesPage() {
  const toast = useToast();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit } | { delete }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  async function refreshCategories() {
    try {
      setCategories(await categoryService.getCategories());
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not refresh categories'));
    }
  }

  useEffect(() => {
    setLoading(true);
    refreshCategories().finally(() => setLoading(false));
  }, []);

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await categoryService.createCategory(payload);
      await refreshCategories();
      closeModal();
      toast.success('Category created');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not create category'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(categoryId, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await categoryService.updateCategory(categoryId, payload);
      await refreshCategories();
      closeModal();
      toast.success('Category updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update category'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(category) {
    setSubmitting(true);
    try {
      await categoryService.deleteCategory(category.id);
      await refreshCategories();
      closeModal();
      toast.success('Category deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete category'));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
  const incomeCategories = categories.filter((c) => c.type === 'INCOME');

  return (
    <div className="categories-page">
      <div className="categories-page__header">
        <h1>Categories</h1>
        <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
          + Add category
        </Button>
      </div>

      <div className="categories-grid">
        <CategoryGroup
          title="Expense categories"
          categories={expenseCategories}
          onEdit={(c) => setModal({ edit: c })}
          onDelete={(c) => setModal({ delete: c })}
        />
        <CategoryGroup
          title="Income categories"
          categories={incomeCategories}
          onEdit={(c) => setModal({ edit: c })}
          onDelete={(c) => setModal({ delete: c })}
        />
      </div>

      {modal === 'create' && (
        <Modal title="Add category" onClose={closeModal}>
          <CategoryForm onSubmit={handleCreate} onCancel={closeModal} submitting={submitting} fieldErrors={fieldErrors} />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit category" onClose={closeModal}>
          <CategoryForm
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete category" onClose={closeModal}>
          <p>
            Delete <strong>{modal.delete.name}</strong>? This cannot be undone.
          </p>
          <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
            <Button variant="secondary" onClick={closeModal}>
              Cancel
            </Button>
            <Button variant="danger" loading={submitting} onClick={() => handleDelete(modal.delete)}>
              Delete
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
}

export default CategoriesPage;
