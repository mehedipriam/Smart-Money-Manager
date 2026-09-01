import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

/** Same form for create and edit — type is fixed once created, so it's only shown (not editable) in edit mode. */
function CategoryForm({ initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    name: initialValues?.name || '',
    type: initialValues?.type || 'EXPENSE',
    icon: initialValues?.icon || '',
    color: initialValues?.color || '#16a34a',
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (isEdit) {
      onSubmit({ name: form.name, icon: form.icon || undefined, color: form.color });
    } else {
      onSubmit({ name: form.name, type: form.type, icon: form.icon || undefined, color: form.color });
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        label="Category name"
        name="name"
        required
        value={form.name}
        onChange={handleChange}
        error={fieldErrors.name}
      />

      {isEdit ? (
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', marginTop: -8 }}>
          Type: {form.type === 'INCOME' ? 'Income' : 'Expense'} (fixed after creation)
        </p>
      ) : (
        <div className="field">
          <label className="field__label" htmlFor="type">
            Type
          </label>
          <select id="type" name="type" className="field__input" value={form.type} onChange={handleChange}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
        </div>
      )}

      <Input
        label="Icon (emoji, optional)"
        name="icon"
        maxLength={4}
        placeholder="🎯"
        value={form.icon}
        onChange={handleChange}
        error={fieldErrors.icon}
      />

      <div className="field">
        <label className="field__label" htmlFor="color">
          Color
        </label>
        <input
          id="color"
          name="color"
          type="color"
          className="field__input"
          style={{ height: 42, padding: 4 }}
          value={form.color}
          onChange={handleChange}
        />
      </div>

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Save changes' : 'Create category'}
        </Button>
      </div>
    </form>
  );
}

export default CategoryForm;
