import { useMemo, useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function RecurringTransactionForm({ accounts, categories, initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    accountId: initialValues?.account.id ?? accounts[0]?.id ?? '',
    categoryId: initialValues?.category.id ?? '',
    type: initialValues?.type ?? 'EXPENSE',
    amount: initialValues?.amount ?? '',
    description: initialValues?.description ?? '',
    note: initialValues?.note ?? '',
    frequency: initialValues?.frequency ?? 'MONTHLY',
    startDate: initialValues?.startDate ?? todayIso(),
    endDate: initialValues?.endDate ?? '',
    active: initialValues?.active ?? true,
  });

  const categoriesForType = useMemo(
    () => categories.filter((category) => category.type === form.type),
    [categories, form.type],
  );

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    const newValue = type === 'checkbox' ? checked : value;
    setForm((prev) => {
      const next = { ...prev, [name]: newValue };
      if (name === 'type') {
        const stillValid = categories.some((c) => c.type === value && String(c.id) === String(prev.categoryId));
        if (!stillValid) next.categoryId = '';
      }
      return next;
    });
  }

  function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      accountId: Number(form.accountId),
      categoryId: Number(form.categoryId),
      type: form.type,
      amount: Number(form.amount),
      description: form.description || undefined,
      note: form.note || undefined,
      frequency: form.frequency,
      endDate: form.endDate || undefined,
    };
    if (isEdit) {
      payload.active = form.active;
    } else {
      payload.startDate = form.startDate;
    }
    onSubmit(payload);
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="field">
        <label className="field__label" htmlFor="type">
          Type
        </label>
        <select id="type" name="type" className="field__input" value={form.type} onChange={handleChange}>
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="accountId">
          Account
        </label>
        <select id="accountId" name="accountId" className="field__input" value={form.accountId} onChange={handleChange} required>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.accountName}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="categoryId">
          Category
        </label>
        <select id="categoryId" name="categoryId" className="field__input" value={form.categoryId} onChange={handleChange} required>
          <option value="" disabled>
            Select a category
          </option>
          {categoriesForType.map((category) => (
            <option key={category.id} value={category.id}>
              {category.icon} {category.name}
            </option>
          ))}
        </select>
      </div>

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

      <div className="field">
        <label className="field__label" htmlFor="frequency">
          Repeats
        </label>
        <select id="frequency" name="frequency" className="field__input" value={form.frequency} onChange={handleChange}>
          <option value="DAILY">Daily</option>
          <option value="WEEKLY">Weekly</option>
          <option value="MONTHLY">Monthly</option>
          <option value="YEARLY">Yearly</option>
        </select>
      </div>

      {!isEdit && (
        <Input
          label="Start date"
          name="startDate"
          type="date"
          required
          value={form.startDate}
          onChange={handleChange}
          error={fieldErrors.startDate}
        />
      )}
      <Input
        label="End date (optional — leave blank to repeat indefinitely)"
        name="endDate"
        type="date"
        value={form.endDate}
        onChange={handleChange}
        error={fieldErrors.endDate}
      />
      <Input
        label="Description (optional)"
        name="description"
        value={form.description}
        onChange={handleChange}
        error={fieldErrors.description}
      />

      {isEdit && (
        <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16, fontSize: '0.9rem' }}>
          <input type="checkbox" name="active" checked={form.active} onChange={handleChange} />
          Active
        </label>
      )}

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting} disabled={!form.categoryId}>
          {isEdit ? 'Save changes' : 'Create recurring transaction'}
        </Button>
      </div>
    </form>
  );
}

export default RecurringTransactionForm;
