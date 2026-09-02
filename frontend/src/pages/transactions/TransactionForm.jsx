import { useMemo, useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function TransactionForm({ accounts, categories, initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    accountId: initialValues?.account.id ?? accounts[0]?.id ?? '',
    categoryId: initialValues?.category.id ?? '',
    type: initialValues?.type ?? 'EXPENSE',
    amount: initialValues?.amount ?? '',
    transactionDate: initialValues?.transactionDate ?? todayIso(),
    description: initialValues?.description ?? '',
    note: initialValues?.note ?? '',
  });

  const categoriesForType = useMemo(
    () => categories.filter((category) => category.type === form.type),
    [categories, form.type],
  );

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => {
      const next = { ...prev, [name]: value };
      if (name === 'type') {
        // Switching type invalidates the previously selected category (it belongs to the other type).
        const stillValid = categories.some((c) => c.type === value && String(c.id) === String(prev.categoryId));
        if (!stillValid) next.categoryId = '';
      }
      return next;
    });
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      accountId: Number(form.accountId),
      categoryId: Number(form.categoryId),
      type: form.type,
      amount: Number(form.amount),
      transactionDate: form.transactionDate,
      description: form.description || undefined,
      note: form.note || undefined,
    });
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
      <Input
        label="Date"
        name="transactionDate"
        type="date"
        required
        value={form.transactionDate}
        onChange={handleChange}
        error={fieldErrors.transactionDate}
      />
      <Input
        label="Description (optional)"
        name="description"
        value={form.description}
        onChange={handleChange}
        error={fieldErrors.description}
      />
      <Input label="Note (optional)" name="note" value={form.note} onChange={handleChange} error={fieldErrors.note} />

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting} disabled={!form.categoryId}>
          {isEdit ? 'Save changes' : 'Add transaction'}
        </Button>
      </div>
    </form>
  );
}

export default TransactionForm;
