import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as recurringService from '../../services/recurringTransactionService.js';
import * as accountService from '../../services/accountService.js';
import * as categoryService from '../../services/categoryService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import Table from '../../components/common/Table.jsx';
import RecurringTransactionForm from './RecurringTransactionForm.jsx';
import './TransactionsPage.css';

const FREQUENCY_LABELS = { DAILY: 'Daily', WEEKLY: 'Weekly', MONTHLY: 'Monthly', YEARLY: 'Yearly' };

function RecurringTransactionsPage() {
  const toast = useToast();
  const [recurring, setRecurring] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  async function refresh() {
    setRecurring(await recurringService.getRecurringTransactions());
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([refresh(), accountService.getAccounts().then(setAccounts), categoryService.getCategories().then(setCategories)])
      .catch((err) => toast.error(getErrorMessage(err, 'Could not load recurring transactions')))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await recurringService.createRecurringTransaction(payload);
      await refresh();
      closeModal();
      toast.success('Recurring transaction created');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not create recurring transaction'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(id, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await recurringService.updateRecurringTransaction(id, payload);
      await refresh();
      closeModal();
      toast.success('Recurring transaction updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update recurring transaction'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(item) {
    setSubmitting(true);
    try {
      await recurringService.deleteRecurringTransaction(item.id);
      await refresh();
      closeModal();
      toast.success('Recurring transaction deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete recurring transaction'));
    } finally {
      setSubmitting(false);
    }
  }

  const columns = [
    {
      key: 'category',
      label: 'Category',
      render: (row) => (
        <span>
          {row.category.icon} {row.category.name}
        </span>
      ),
    },
    { key: 'account', label: 'Account', render: (row) => row.account.accountName },
    { key: 'description', label: 'Description', render: (row) => row.description || '—' },
    {
      key: 'amount',
      label: 'Amount',
      render: (row) => (
        <span style={{ color: row.type === 'INCOME' ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }}>
          {row.type === 'INCOME' ? '+' : '-'}
          {formatCurrency(row.amount, row.account.currency)}
        </span>
      ),
    },
    { key: 'frequency', label: 'Repeats', render: (row) => FREQUENCY_LABELS[row.frequency] },
    { key: 'nextRunDate', label: 'Next run' },
    {
      key: 'active',
      label: 'Status',
      render: (row) => (
        <span style={{ color: row.active ? 'var(--color-success)' : 'var(--color-text-muted)' }}>
          {row.active ? 'Active' : 'Paused'}
        </span>
      ),
    },
    {
      key: 'actions',
      label: '',
      align: 'right',
      render: (row) => (
        <span className="txn-table__actions">
          <button type="button" onClick={() => setModal({ edit: row })}>
            Edit
          </button>
          <button type="button" className="txn-table__danger" onClick={() => setModal({ delete: row })}>
            Delete
          </button>
        </span>
      ),
    },
  ];

  if (loading) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className="transactions-page">
      <div className="transactions-page__header">
        <div>
          <h1>Recurring transactions</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', margin: '4px 0 0' }}>
            <Link to="/transactions">← Back to transactions</Link>
          </p>
        </div>
        <Button style={{ width: 'auto' }} onClick={() => setModal('create')} disabled={accounts.length === 0}>
          + Add recurring transaction
        </Button>
      </div>

      <div className="card">
        <Table
          columns={columns}
          rows={recurring}
          emptyMessage="No recurring transactions yet. Add one for things like a monthly salary or a subscription."
        />
      </div>

      {modal === 'create' && (
        <Modal title="Add recurring transaction" onClose={closeModal}>
          <RecurringTransactionForm
            accounts={accounts}
            categories={categories}
            onSubmit={handleCreate}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit recurring transaction" onClose={closeModal}>
          <RecurringTransactionForm
            accounts={accounts}
            categories={categories}
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete recurring transaction" onClose={closeModal}>
          <p>Delete this recurring transaction? Transactions it already generated are not affected.</p>
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

export default RecurringTransactionsPage;
