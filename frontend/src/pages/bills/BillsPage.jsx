import { useEffect, useState } from 'react';
import * as billService from '../../services/billService.js';
import * as categoryService from '../../services/categoryService.js';
import * as accountService from '../../services/accountService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import Table from '../../components/common/Table.jsx';
import BillForm from './BillForm.jsx';
import './BillsPage.css';

const STATUS_TABS = [
  { value: '', label: 'All' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'OVERDUE', label: 'Overdue' },
  { value: 'PAID', label: 'Paid' },
];

const STATUS_STYLE = {
  PENDING: 'var(--color-text-muted)',
  OVERDUE: 'var(--color-danger)',
  PAID: 'var(--color-success)',
};

const FREQUENCY_LABELS = { DAILY: 'Daily', WEEKLY: 'Weekly', MONTHLY: 'Monthly', YEARLY: 'Yearly' };

function BillsPage() {
  const toast = useToast();
  const [statusFilter, setStatusFilter] = useState('');
  const [bills, setBills] = useState([]);
  const [expenseCategories, setExpenseCategories] = useState([]);
  const [currency, setCurrency] = useState('BDT');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit } | { delete }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  async function refreshBills() {
    setBills(await billService.getBills(statusFilter || undefined));
  }

  useEffect(() => {
    setLoading(true);
    Promise.all([
      refreshBills(),
      categoryService.getCategories('EXPENSE').then(setExpenseCategories),
      accountService.getAccounts().then((accounts) => {
        if (accounts.length > 0) setCurrency(accounts[0].currency);
      }),
    ])
      .catch((err) => toast.error(getErrorMessage(err, 'Could not load bills')))
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
      await billService.createBill(payload);
      await refreshBills();
      closeModal();
      toast.success('Bill added');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not add bill'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(id, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await billService.updateBill(id, payload);
      await refreshBills();
      closeModal();
      toast.success('Bill updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update bill'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(bill) {
    setSubmitting(true);
    try {
      await billService.deleteBill(bill.id);
      await refreshBills();
      closeModal();
      toast.success('Bill deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete bill'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleMarkAsPaid(bill) {
    try {
      await billService.markAsPaid(bill.id);
      await refreshBills();
      toast.success(bill.recurringType ? 'Marked as paid — next bill created' : 'Marked as paid');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not mark bill as paid'));
    }
  }

  const columns = [
    { key: 'dueDate', label: 'Due date' },
    { key: 'billName', label: 'Bill' },
    { key: 'category', label: 'Category', render: (row) => (row.category ? `${row.category.icon} ${row.category.name}` : '—') },
    { key: 'recurringType', label: 'Repeats', render: (row) => (row.recurringType ? FREQUENCY_LABELS[row.recurringType] : '—') },
    { key: 'amount', label: 'Amount', align: 'right', render: (row) => formatCurrency(row.amount, currency) },
    {
      key: 'paymentStatus',
      label: 'Status',
      render: (row) => <span style={{ color: STATUS_STYLE[row.paymentStatus], fontWeight: 600 }}>{row.paymentStatus}</span>,
    },
    {
      key: 'actions',
      label: '',
      align: 'right',
      render: (row) => (
        <span className="bills-page__actions">
          {row.paymentStatus !== 'PAID' && (
            <button type="button" onClick={() => handleMarkAsPaid(row)}>
              Mark as paid
            </button>
          )}
          <button type="button" onClick={() => setModal({ edit: row })}>
            Edit
          </button>
          <button type="button" className="bills-page__danger" onClick={() => setModal({ delete: row })}>
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
    <div className="bills-page">
      <div className="bills-page__header">
        <h1>Bills & Reminders</h1>
        <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
          + Add bill
        </Button>
      </div>

      <div className="bills-page__tabs">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            type="button"
            className={`bills-page__tab${statusFilter === tab.value ? ' active' : ''}`}
            onClick={() => setStatusFilter(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="card">
        <Table columns={columns} rows={bills} emptyMessage="No bills here yet." />
      </div>

      {modal === 'create' && (
        <Modal title="Add bill" onClose={closeModal}>
          <BillForm
            expenseCategories={expenseCategories}
            onSubmit={handleCreate}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit bill" onClose={closeModal}>
          <BillForm
            expenseCategories={expenseCategories}
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete bill" onClose={closeModal}>
          <p>
            Delete <strong>{modal.delete.billName}</strong>? This cannot be undone.
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

export default BillsPage;
