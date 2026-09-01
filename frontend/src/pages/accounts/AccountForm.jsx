import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import { ACCOUNT_TYPES, ACCOUNT_TYPE_LABELS, CURRENCIES, formatCurrency } from '../../utils/formatCurrency.js';

/** Same form for create and edit — in edit mode the balance is shown read-only, since it only moves via transactions/transfers. */
function AccountForm({ initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    accountName: initialValues?.accountName || '',
    accountType: initialValues?.accountType || ACCOUNT_TYPES[0],
    initialBalance: initialValues?.initialBalance ?? '0',
    currency: initialValues?.currency || CURRENCIES[0],
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (isEdit) {
      onSubmit({ accountName: form.accountName, accountType: form.accountType, currency: form.currency });
    } else {
      onSubmit({ ...form, initialBalance: Number(form.initialBalance) });
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        label="Account name"
        name="accountName"
        required
        value={form.accountName}
        onChange={handleChange}
        error={fieldErrors.accountName}
      />

      <div className="field">
        <label className="field__label" htmlFor="accountType">
          Account type
        </label>
        <select id="accountType" name="accountType" className="field__input" value={form.accountType} onChange={handleChange}>
          {ACCOUNT_TYPES.map((type) => (
            <option key={type} value={type}>
              {ACCOUNT_TYPE_LABELS[type]}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="currency">
          Currency
        </label>
        <select id="currency" name="currency" className="field__input" value={form.currency} onChange={handleChange}>
          {CURRENCIES.map((currency) => (
            <option key={currency} value={currency}>
              {currency}
            </option>
          ))}
        </select>
      </div>

      {isEdit ? (
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>
          Current balance: {formatCurrency(initialValues.currentBalance, initialValues.currency)} (only changes
          through transfers)
        </p>
      ) : (
        <Input
          label="Initial balance"
          name="initialBalance"
          type="number"
          min="0"
          step="0.01"
          required
          value={form.initialBalance}
          onChange={handleChange}
          error={fieldErrors.initialBalance}
        />
      )}

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Save changes' : 'Create account'}
        </Button>
      </div>
    </form>
  );
}

export default AccountForm;
