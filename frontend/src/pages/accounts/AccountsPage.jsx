import { useEffect, useState } from 'react';
import * as accountService from '../../services/accountService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import { ACCOUNT_TYPE_LABELS, formatCurrency } from '../../utils/formatCurrency.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import AccountForm from './AccountForm.jsx';
import TransferForm from './TransferForm.jsx';
import './AccountsPage.css';

function AccountsPage() {
  const toast = useToast();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null); // 'create' | { edit: account } | 'transfer' | { delete: account }
  const [submitting, setSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  // Fetches accounts without touching the page-level spinner — used to refresh the
  // grid after a mutation, so a create/edit/delete/transfer doesn't flash the whole
  // page back to a loading state while the grid it's updating is still on screen.
  async function refreshAccounts() {
    try {
      setAccounts(await accountService.getAccounts());
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not refresh accounts'));
    }
  }

  useEffect(() => {
    setLoading(true);
    refreshAccounts().finally(() => setLoading(false));
  }, []);

  function closeModal() {
    setModal(null);
    setFieldErrors({});
  }

  async function handleCreate(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await accountService.createAccount(payload);
      await refreshAccounts();
      closeModal();
      toast.success('Account created');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not create account'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleUpdate(accountId, payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await accountService.updateAccount(accountId, payload);
      await refreshAccounts();
      closeModal();
      toast.success('Account updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update account'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(account) {
    setSubmitting(true);
    try {
      await accountService.deleteAccount(account.id);
      await refreshAccounts();
      closeModal();
      toast.success('Account deleted');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete account'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleTransfer(payload) {
    setSubmitting(true);
    setFieldErrors({});
    try {
      await accountService.transfer(payload);
      await refreshAccounts();
      closeModal();
      toast.success('Transfer completed');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Transfer failed'));
      setFieldErrors(getFieldErrors(err));
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
    <div className="accounts-page">
      <div className="accounts-page__header">
        <h1>Accounts</h1>
        <div style={{ display: 'flex', gap: 12 }}>
          {accounts.length >= 2 && (
            <Button variant="secondary" style={{ width: 'auto' }} onClick={() => setModal('transfer')}>
              Transfer
            </Button>
          )}
          <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
            + Add account
          </Button>
        </div>
      </div>

      {accounts.length === 0 ? (
        <div className="card accounts-page__empty">
          <p>You don&apos;t have any accounts yet.</p>
          <Button style={{ width: 'auto' }} onClick={() => setModal('create')}>
            Create your first account
          </Button>
        </div>
      ) : (
        <div className="accounts-grid">
          {accounts.map((account) => (
            <div key={account.id} className="card account-card">
              <span className="account-card__type">{ACCOUNT_TYPE_LABELS[account.accountType]}</span>
              <h3>{account.accountName}</h3>
              <p className="account-card__balance">{formatCurrency(account.currentBalance, account.currency)}</p>
              <div className="account-card__actions">
                <button type="button" onClick={() => setModal({ edit: account })}>
                  Edit
                </button>
                <button type="button" className="account-card__danger" onClick={() => setModal({ delete: account })}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modal === 'create' && (
        <Modal title="Add account" onClose={closeModal}>
          <AccountForm onSubmit={handleCreate} onCancel={closeModal} submitting={submitting} fieldErrors={fieldErrors} />
        </Modal>
      )}

      {modal?.edit && (
        <Modal title="Edit account" onClose={closeModal}>
          <AccountForm
            initialValues={modal.edit}
            onSubmit={(payload) => handleUpdate(modal.edit.id, payload)}
            onCancel={closeModal}
            submitting={submitting}
            fieldErrors={fieldErrors}
          />
        </Modal>
      )}

      {modal === 'transfer' && (
        <Modal title="Transfer money" onClose={closeModal}>
          <TransferForm accounts={accounts} onSubmit={handleTransfer} onCancel={closeModal} submitting={submitting} fieldErrors={fieldErrors} />
        </Modal>
      )}

      {modal?.delete && (
        <Modal title="Delete account" onClose={closeModal}>
          <p>
            Delete <strong>{modal.delete.accountName}</strong>? This cannot be undone.
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

export default AccountsPage;
