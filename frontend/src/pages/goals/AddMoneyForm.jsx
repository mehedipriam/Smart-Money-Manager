import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function AddMoneyForm({ goal, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const [form, setForm] = useState({ amount: '', contributionDate: todayIso(), note: '' });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({ amount: Number(form.amount), contributionDate: form.contributionDate, note: form.note || undefined });
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', marginTop: -4 }}>
        Adding to <strong>{goal.goalName}</strong>
      </p>
      <Input
        label="Amount"
        name="amount"
        type="number"
        min="0.01"
        step="0.01"
        required
        autoFocus
        value={form.amount}
        onChange={handleChange}
        error={fieldErrors.amount}
      />
      <Input
        label="Date"
        name="contributionDate"
        type="date"
        required
        value={form.contributionDate}
        onChange={handleChange}
        error={fieldErrors.contributionDate}
      />
      <Input label="Note (optional)" name="note" value={form.note} onChange={handleChange} error={fieldErrors.note} />

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          Add money
        </Button>
      </div>
    </form>
  );
}

export default AddMoneyForm;
