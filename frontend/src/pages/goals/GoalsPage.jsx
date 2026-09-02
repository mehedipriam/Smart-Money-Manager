import { useEffect, useState } from 'react';
import * as goalService from '../../services/goalService.js';
import * as accountService from '../../services/accountService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import ProgressBar from '../../components/common/ProgressBar.jsx';
import GoalForm from './GoalForm.jsx';
import AddMoneyForm from './AddMoneyForm.jsx';
import ContributionsList from './ContributionsList.jsx';
import './GoalsPage.css';

const STATUS_TABS = [
  { value: '', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const STATUS_BADGE = {
  ACTIVE: { label: 'Active', color: 'var(--color-primary-dark)' },
  COMPLETED: { label: 'Completed', color: 'var(--color-success)' },
  CANCELLED: { label: 'Cancelled', color: 'var(--color-text-muted)' },
};

function GoalsPage() {
  const toast = useToast();
  const [statusFilter, setStatusFilter] = useState('');
  const [goals, setGoals] = useState([]);
  const [currency, setCurrency] = useState('BDT');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit } | { delete } | { addMoney } | { history, contributions }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  async function refreshGoals() {
    setGoals(await goalService.getGoals(statusFilter || undefined));
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([refreshGoals(), accountService.getAccounts().then((accounts) => {
      if (accounts.length > 0) setCurrency(accounts[0].currency);
    })])
      .catch((err) => toast.error(getErrorMessage(err, 'Could not load goals')))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await goalService.createGoal(payload);
      await refreshGoals();
      closeModal();
      toast.success('Goal created');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not create goal'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(id, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await goalService.updateGoal(id, payload);
      await refreshGoals();
      closeModal();
      toast.success('Goal updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update goal'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(goal) {
    setSubmitting(true);
    try {
      await goalService.deleteGoal(goal.id);
      await refreshGoals();
      closeModal();
      toast.success('Goal deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete goal'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAddMoney(goalId, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      const updated = await goalService.addContribution(goalId, payload);
      await refreshGoals();
      closeModal();
      toast.success(updated.status === 'COMPLETED' ? '🎉 Goal completed!' : 'Money added to goal');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not add money'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function openHistory(goal) {
    setModal({ history: goal, contributions: null });
    try {
      const contributions = await goalService.getContributions(goal.id);
      setModal({ history: goal, contributions });
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not load contribution history'));
      closeModal();
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className="goals-page">
      <div className="goals-page__header">
        <h1>Savings Goals</h1>
        <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
          + Add goal
        </Button>
      </div>

      <div className="goals-page__tabs">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            type="button"
            className={`goals-page__tab${statusFilter === tab.value ? ' active' : ''}`}
            onClick={() => setStatusFilter(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {goals.length === 0 ? (
        <div className="card goals-page__empty">
          <p>No goals here yet.</p>
          <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
            Create your first goal
          </Button>
        </div>
      ) : (
        <div className="goals-grid">
          {goals.map((goal) => (
            <div key={goal.id} className="card goal-card">
              <div className="goal-card__header">
                <h3>{goal.goalName}</h3>
                <span className="goal-card__badge" style={{ color: STATUS_BADGE[goal.status].color }}>
                  {STATUS_BADGE[goal.status].label}
                </span>
              </div>
              {goal.description && <p className="goal-card__description">{goal.description}</p>}

              <ProgressBar percentage={goal.progressPercentage} nearLimit={false} exceeded={false} />
              <div className="goal-card__figures">
                <span>{formatCurrency(goal.currentSavedAmount, currency)} of {formatCurrency(goal.targetAmount, currency)}</span>
                <span>{goal.progressPercentage}%</span>
              </div>
              {goal.targetDate && <p className="goal-card__date">Target date: {goal.targetDate}</p>}

              <div className="goal-card__actions">
                {goal.status === 'ACTIVE' && (
                  <button type="button" onClick={() => setModal({ addMoney: goal })}>
                    Add money
                  </button>
                )}
                <button type="button" onClick={() => openHistory(goal)}>
                  History
                </button>
                <button type="button" onClick={() => setModal({ edit: goal })}>
                  Edit
                </button>
                <button type="button" className="goal-card__danger" onClick={() => setModal({ delete: goal })}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modal === 'create' && (
        <Modal title="Add goal" onClose={closeModal}>
          <GoalForm onSubmit={handleCreate} onCancel={closeModal} submitting={submitting} fieldErrors={fieldErrors} />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit goal" onClose={closeModal}>
          <GoalForm
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.addMoney && (
        <Modal title="Add money" onClose={closeModal}>
          <AddMoneyForm
            goal={modal.addMoney}
            onSubmit={(payload) => handleAddMoney(modal.addMoney.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.history && (
        <Modal title={`${modal.history.goalName} — history`} onClose={closeModal}>
          {modal.contributions === null ? (
            <div style={{ display: 'grid', placeItems: 'center', padding: '24px 0' }}>
              <Spinner size={24} />
            </div>
          ) : (
            <ContributionsList contributions={modal.contributions} currency={currency} />
          )}
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete goal" onClose={closeModal}>
          <p>
            Delete <strong>{modal.delete.goalName}</strong>? This also removes its contribution history. This cannot be undone.
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

export default GoalsPage;
