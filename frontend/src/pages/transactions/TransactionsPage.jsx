import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import * as transactionService from '../../services/transactionService.js';
import * as accountService from '../../services/accountService.js';
import * as categoryService from '../../services/categoryService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import Table from '../../components/common/Table.jsx';
import TransactionFilters from './TransactionFilters.jsx';
import TransactionForm from './TransactionForm.jsx';
import TransactionImportModal from './TransactionImportModal.jsx';
import './TransactionsPage.css';

const EMPTY_FILTERS = {
  search: '',
  type: '',
  accountId: '',
  categoryId: '',
  dateFrom: '',
  dateTo: '',
  amountFrom: '',
  amountTo: '',
};

function buildParams(filters, sortBy, sortDir, page) {
  const params = { page, size: 20, sortBy, sortDir };
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== '' && value != null) params[key] = value;
  });
  return params;
}

function TransactionsPage() {
  const toast = useToast();
  const [accounts, setAccounts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [sortBy, setSortBy] = useState('transactionDate');
  const [sortDir, setSortDir] = useState('desc');
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit } | { delete }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const debounceRef = useRef(null);

  async function loadLookups() {
    const [accountList, categoryList] = await Promise.all([accountService.getAccounts(), categoryService.getCategories()]);
    setAccounts(accountList);
    setCategories(categoryList);
  }

  async function fetchTransactions() {
    try {
      const result = await transactionService.getTransactions(buildParams(filters, sortBy, sortDir, page));
      setPageData(result);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not load transactions'));
    }
  }

  useEffect(() => {
    setLoading(true);
    loadLookups()
      .then(fetchTransactions)
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(fetchTransactions, 300);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters, sortBy, sortDir, page]);

  function handleFiltersChange(next) {
    setFilters(next);
    setPage(0);
  }

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await transactionService.createTransaction(payload);
      await Promise.all([fetchTransactions(), accountService.getAccounts().then(setAccounts)]);
      closeModal();
      toast.success('Transaction added');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not add transaction'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(id, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await transactionService.updateTransaction(id, payload);
      await Promise.all([fetchTransactions(), accountService.getAccounts().then(setAccounts)]);
      closeModal();
      toast.success('Transaction updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update transaction'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(transaction) {
    setSubmitting(true);
    try {
      await transactionService.deleteTransaction(transaction.id);
      await Promise.all([fetchTransactions(), accountService.getAccounts().then(setAccounts)]);
      closeModal();
      toast.success('Transaction deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete transaction'));
    } finally {
      setSubmitting(false);
    }
  }

  const columns = [
    { key: 'transactionDate', label: 'Date' },
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
      align: 'right',
      render: (row) => (
        <span style={{ color: row.type === 'INCOME' ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }}>
          {row.type === 'INCOME' ? '+' : '-'}
          {formatCurrency(row.amount, row.account.currency)}
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
          <h1>Transactions</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', margin: '4px 0 0' }}>
            <Link to="/transactions/recurring">Manage recurring transactions →</Link>
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <Button
            variant="secondary"
            style={{ width: 'auto' }}
            onClick={() => setModal('import')}
            disabled={accounts.length === 0}
          >
            Import transactions
          </Button>
          <Button style={{ width: 'auto' }} onClick={() => setModal('create')} disabled={accounts.length === 0}>
            + Add transaction
          </Button>
        </div>
      </div>

      {accounts.length === 0 ? (
        <div className="card">
          <p>Create an account first to start recording transactions.</p>
        </div>
      ) : (
        <>
          <TransactionFilters filters={filters} onChange={handleFiltersChange} accounts={accounts} categories={categories} />

          <div className="txn-sort">
            <label>
              Sort by{' '}
              <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                <option value="transactionDate">Date</option>
                <option value="amount">Amount</option>
                <option value="createdAt">Created</option>
              </select>
            </label>
            <label>
              Order{' '}
              <select value={sortDir} onChange={(e) => setSortDir(e.target.value)}>
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </label>
          </div>

          <div className="card">
            <Table columns={columns} rows={pageData.content} emptyMessage="No transactions match these filters." />
          </div>

          {pageData.totalPages > 1 && (
            <div className="txn-pagination">
              <Button
                variant="secondary"
                style={{ width: 'auto' }}
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </Button>
              <span>
                Page {page + 1} of {pageData.totalPages} ({pageData.totalElements} total)
              </span>
              <Button
                variant="secondary"
                style={{ width: 'auto' }}
                disabled={page >= pageData.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}

      {modal === 'create' && (
        <Modal title="Add transaction" onClose={closeModal}>
          <TransactionForm
            accounts={accounts}
            categories={categories}
            onSubmit={handleCreate}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal === 'import' && (
        <Modal title="Import transactions" onClose={closeModal}>
          <TransactionImportModal
            accounts={accounts}
            categories={categories}
            onCancel={closeModal}
            onImported={() => Promise.all([fetchTransactions(), accountService.getAccounts().then(setAccounts)])}
          />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit transaction" onClose={closeModal}>
          <TransactionForm
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
        <Modal title="Delete transaction" onClose={closeModal}>
          <p>Delete this transaction? This cannot be undone and will adjust the account balance back.</p>
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

export default TransactionsPage;
