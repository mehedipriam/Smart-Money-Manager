import { useEffect, useState } from 'react';
import * as budgetService from '../../services/budgetService.js';
import * as categoryService from '../../services/categoryService.js';
import * as accountService from '../../services/accountService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import ProgressBar from '../../components/common/ProgressBar.jsx';
import BudgetForm from './BudgetForm.jsx';
import './BudgetsPage.css';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function BudgetsPage() {
  const toast = useToast();
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [budgets, setBudgets] = useState([]);
  const [expenseCategories, setExpenseCategories] = useState([]);
  const [currency, setCurrency] = useState('BDT');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit } | { delete }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  async function refreshBudgets(alertOnLoad) {
    const list = await budgetService.getBudgets({ month, year });
    setBudgets(list);
    if (alertOnLoad) {
      const exceeded = list.filter((b) => b.exceeded);
      const nearLimit = list.filter((b) => b.nearLimit);
      if (exceeded.length > 0) {
        toast.error(`${exceeded.length} budget${exceeded.length > 1 ? 's are' : ' is'} over limit this month.`);
      } else if (nearLimit.length > 0) {
        toast.info(`${nearLimit.length} budget${nearLimit.length > 1 ? 's have' : ' has'} reached 80% usage.`);
      }
    }
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([refreshBudgets(true), categoryService.getCategories('EXPENSE').then(setExpenseCategories)])
      .catch((err) => toast.error(getErrorMessage(err, 'Could not load budgets')))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month, year]);

  useEffect(() => {
    // Just to infer the currency to display amounts in; falls back to BDT if the user has no accounts yet.
    accountService
      .getAccounts()
      .then((accounts) => {
        if (accounts.length > 0) setCurrency(accounts[0].currency);
      })
      .catch(() => {});
  }, []);

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  function goToPreviousMonth() {
    if (month === 1) {
      setMonth(12);
      setYear((y) => y - 1);
    } else {
      setMonth((m) => m - 1);
    }
  }

  function goToNextMonth() {
    if (month === 12) {
      setMonth(1);
      setYear((y) => y + 1);
    } else {
      setMonth((m) => m + 1);
    }
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await budgetService.createBudget(payload);
      await refreshBudgets(false);
      closeModal();
      toast.success('Budget created');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not create budget'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(id, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await budgetService.updateBudget(id, payload);
      await refreshBudgets(false);
      closeModal();
      toast.success('Budget updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update budget'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(budget) {
    setSubmitting(true);
    try {
      await budgetService.deleteBudget(budget.id);
      await refreshBudgets(false);
      closeModal();
      toast.success('Budget deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete budget'));
    } finally {
      setSubmitting(false);
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
    <div className="budgets-page">
      <div className="budgets-page__header">
        <h1>Budgets</h1>
        <Button style={{ width: 'auto' }} onClick={() => setModal('create')} disabled={expenseCategories.length === 0}>
          + Add budget
        </Button>
      </div>

      <div className="budgets-page__month-nav">
        <button type="button" onClick={goToPreviousMonth} aria-label="Previous month">
          ←
        </button>
        <span>
          {MONTH_NAMES[month - 1]} {year}
        </span>
        <button type="button" onClick={goToNextMonth} aria-label="Next month">
          →
        </button>
      </div>

      {budgets.length === 0 ? (
        <div className="card budgets-page__empty">
          <p>No budgets set for {MONTH_NAMES[month - 1]} {year}.</p>
          <Button style={{ width: 'auto' }} onClick={() => setModal('create')} disabled={expenseCategories.length === 0}>
            Create your first budget
          </Button>
        </div>
      ) : (
        <div className="budgets-grid">
          {budgets.map((budget) => (
            <div key={budget.id} className="card budget-card">
              <div className="budget-card__header">
                <span>
                  {budget.category.icon} {budget.category.name}
                </span>
                <span className="budget-card__actions">
                  <button type="button" onClick={() => setModal({ edit: budget })}>
                    Edit
                  </button>
                  <button type="button" className="budget-card__danger" onClick={() => setModal({ delete: budget })}>
                    Delete
                  </button>
                </span>
              </div>

              <ProgressBar percentage={budget.usagePercentage} nearLimit={budget.nearLimit} exceeded={budget.exceeded} />

              <div className="budget-card__figures">
                <span>
                  {formatCurrency(budget.usedAmount, currency)} of {formatCurrency(budget.budgetAmount, currency)}
                </span>
                <span
                  style={{
                    color: budget.exceeded ? 'var(--color-danger)' : budget.nearLimit ? 'var(--color-warning)' : 'var(--color-text-muted)',
                    fontWeight: 600,
                  }}
                >
                  {budget.usagePercentage}%
                </span>
              </div>
              {budget.exceeded && <p className="budget-card__alert budget-card__alert--danger">Budget exceeded</p>}
              {budget.nearLimit && <p className="budget-card__alert budget-card__alert--warning">Approaching limit</p>}
            </div>
          ))}
        </div>
      )}

      {modal === 'create' && (
        <Modal title="Add budget" onClose={closeModal}>
          <BudgetForm
            expenseCategories={expenseCategories}
            month={month}
            year={year}
            onSubmit={handleCreate}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit budget" onClose={closeModal}>
          <BudgetForm
            expenseCategories={expenseCategories}
            month={month}
            year={year}
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete budget" onClose={closeModal}>
          <p>
            Delete the {modal.delete.category.name} budget for {MONTH_NAMES[month - 1]} {year}?
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

export default BudgetsPage;
