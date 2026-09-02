import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function BudgetForm({ expenseCategories, month, year, initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    categoryId: initialValues?.category.id ?? expenseCategories[0]?.id ?? '',
    budgetAmount: initialValues?.budgetAmount ?? '',
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (isEdit) {
      onSubmit({ budgetAmount: Number(form.budgetAmount) });
    } else {
      onSubmit({ categoryId: Number(form.categoryId), budgetAmount: Number(form.budgetAmount), month, year });
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', marginTop: -4 }}>
        {MONTH_NAMES[month - 1]} {year}
      </p>

      {isEdit ? (
        <p style={{ fontWeight: 600 }}>
          {initialValues.category.icon} {initialValues.category.name}
        </p>
      ) : (
        <div className="field">
          <label className="field__label" htmlFor="categoryId">
            Category
          </label>
          <select id="categoryId" name="categoryId" className="field__input" value={form.categoryId} onChange={handleChange} required>
            {expenseCategories.length === 0 && (
              <option value="" disabled>
                No expense categories available
              </option>
            )}
            {expenseCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.icon} {category.name}
              </option>
            ))}
          </select>
        </div>
      )}

      <Input
        label="Budget amount"
        name="budgetAmount"
        type="number"
        min="0.01"
        step="0.01"
        required
        value={form.budgetAmount}
        onChange={handleChange}
        error={fieldErrors.budgetAmount}
      />

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting} disabled={!isEdit && !form.categoryId}>
          {isEdit ? 'Save changes' : 'Create budget'}
        </Button>
      </div>
    </form>
  );
}

export default BudgetForm;
