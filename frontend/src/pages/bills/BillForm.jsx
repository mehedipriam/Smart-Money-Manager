import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function BillForm({ expenseCategories, initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    billName: initialValues?.billName ?? '',
    amount: initialValues?.amount ?? '',
    dueDate: initialValues?.dueDate ?? todayIso(),
    categoryId: initialValues?.category?.id ?? '',
    recurringType: initialValues?.recurringType ?? '',
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      billName: form.billName,
      amount: Number(form.amount),
      dueDate: form.dueDate,
      categoryId: form.categoryId ? Number(form.categoryId) : undefined,
      recurringType: form.recurringType || undefined,
    });
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        label="Bill name"
        name="billName"
        required
        placeholder="e.g. Electricity Bill"
        value={form.billName}
        onChange={handleChange}
        error={fieldErrors.billName}
      />
      <Input
        label="Amount"
        name="amount"
        type="number"
        min="0.01"
        step="0.01"
        required
        value={form.amount}
        onChange={handleChange}
        error={fieldErrors.amount}
      />
      <Input
        label="Due date"
        name="dueDate"
        type="date"
        required
        value={form.dueDate}
        onChange={handleChange}
        error={fieldErrors.dueDate}
      />

      <div className="field">
        <label className="field__label" htmlFor="categoryId">
          Category (optional)
        </label>
        <select id="categoryId" name="categoryId" className="field__input" value={form.categoryId} onChange={handleChange}>
          <option value="">No category</option>
          {expenseCategories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.icon} {category.name}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="recurringType">
          Repeats (optional)
        </label>
        <select id="recurringType" name="recurringType" className="field__input" value={form.recurringType} onChange={handleChange}>
          <option value="">One-time bill</option>
          <option value="DAILY">Daily</option>
          <option value="WEEKLY">Weekly</option>
          <option value="MONTHLY">Monthly</option>
          <option value="YEARLY">Yearly</option>
        </select>
      </div>

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Save changes' : 'Add bill'}
        </Button>
      </div>
    </form>
  );
}

export default BillForm;
