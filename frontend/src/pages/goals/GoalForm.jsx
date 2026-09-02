import { useState } from 'react';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';

function GoalForm({ initialValues, onSubmit, onCancel, submitting, fieldErrors = {} }) {
  const isEdit = !!initialValues;
  const [form, setForm] = useState({
    goalName: initialValues?.goalName ?? '',
    targetAmount: initialValues?.targetAmount ?? '',
    targetDate: initialValues?.targetDate ?? '',
    description: initialValues?.description ?? '',
    status: initialValues?.status ?? 'ACTIVE',
  });

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      goalName: form.goalName,
      targetAmount: Number(form.targetAmount),
      targetDate: form.targetDate || undefined,
      description: form.description || undefined,
    };
    if (isEdit) payload.status = form.status;
    onSubmit(payload);
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        label="Goal name"
        name="goalName"
        required
        placeholder="e.g. Buy a Laptop"
        value={form.goalName}
        onChange={handleChange}
        error={fieldErrors.goalName}
      />
      <Input
        label="Target amount"
        name="targetAmount"
        type="number"
        min="0.01"
        step="0.01"
        required
        value={form.targetAmount}
        onChange={handleChange}
        error={fieldErrors.targetAmount}
      />
      <Input
        label="Target date (optional)"
        name="targetDate"
        type="date"
        value={form.targetDate}
        onChange={handleChange}
        error={fieldErrors.targetDate}
      />
      <Input
        label="Description (optional)"
        name="description"
        value={form.description}
        onChange={handleChange}
        error={fieldErrors.description}
      />

      {isEdit && (
        <div className="field">
          <label className="field__label" htmlFor="status">
            Status
          </label>
          <select id="status" name="status" className="field__input" value={form.status} onChange={handleChange}>
            <option value="ACTIVE">Active</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      )}

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Save changes' : 'Create goal'}
        </Button>
      </div>
    </form>
  );
}

export default GoalForm;
