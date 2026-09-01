import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import { formatCurrency } from '../../utils/formatCurrency.js';

function TransferForm({ accounts, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const [form, setForm] = useState({
    fromAccountId: accounts[0]?.id ?? '',
    toAccountId: accounts[1]?.id ?? accounts[0]?.id ?? '',
    amount: '',
    note: '',
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      fromAccountId: Number(form.fromAccountId),
      toAccountId: Number(form.toAccountId),
      amount: Number(form.amount),
      note: form.note || undefined,
    });
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="field">
        <label className="field__label" htmlFor="fromAccountId">
          From account
        </label>
        <select id="fromAccountId" name="fromAccountId" className="field__input" value={form.fromAccountId} onChange={handleChange}>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.accountName} ({formatCurrency(account.currentBalance, account.currency)})
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="toAccountId">
          To account
        </label>
        <select id="toAccountId" name="toAccountId" className="field__input" value={form.toAccountId} onChange={handleChange}>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.accountName} ({formatCurrency(account.currentBalance, account.currency)})
            </option>
          ))}
        </select>
      </div>

      {form.fromAccountId === form.toAccountId && (
        <p style={{ color: 'var(--color-danger)', fontSize: '0.85rem', marginTop: -8 }}>
          Pick two different accounts.
        </p>
      )}

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
      <Input label="Note (optional)" name="note" value={form.note} onChange={handleChange} error={fieldErrors.note} />

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting} disabled={form.fromAccountId === form.toAccountId}>
          Transfer
        </Button>
      </div>
    </form>
  );
}

export default TransferForm;
