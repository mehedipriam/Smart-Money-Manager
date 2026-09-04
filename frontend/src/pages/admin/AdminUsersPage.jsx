import { useEffect, useRef, useState } from 'react';
import * as adminService from '../../services/adminService.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Modal from '../../components/common/Modal.jsx';
import Table from '../../components/common/Table.jsx';
import './AdminUsersPage.css';

const STATUS_TABS = [
  { value: '', label: 'All' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Disabled' },
];

function AdminUsersPage() {
  const { user: currentUser } = useAuth();
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [confirmTarget, setConfirmTarget] = useState(null);
  const [busyId, setBusyId] = useState(null);

  const debounceRef = useRef(null);

  async function fetchUsers() {
    try {
      const params = { page, size: 20 };
      if (search.trim()) params.search = search.trim();
      if (statusFilter !== '') params.enabled = statusFilter;
      setPageData(await adminService.getUsers(params));
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not load users'));
    }
  }

  useEffect(() => {
    setLoading(true);
    fetchUsers().finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(fetchUsers, 300);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, statusFilter, page]);

  function handleSearchChange(value) {
    setSearch(value);
    setPage(0);
  }

  function handleStatusChange(value) {
    setStatusFilter(value);
    setPage(0);
  }

  async function handleEnable(targetUser) {
    setBusyId(targetUser.id);
    try {
      await adminService.enableUser(targetUser.id);
      await fetchUsers();
      toast.success(`${targetUser.fullName} enabled`);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not enable user'));
    } finally {
      setBusyId(null);
    }
  }

  async function handleDisable(targetUser) {
    setBusyId(targetUser.id);
    try {
      await adminService.disableUser(targetUser.id);
      await fetchUsers();
      setConfirmTarget(null);
      toast.success(`${targetUser.fullName} disabled`);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not disable user'));
    } finally {
      setBusyId(null);
    }
  }

  const columns = [
    { key: 'fullName', label: 'Name' },
    { key: 'email', label: 'Email' },
    { key: 'roles', label: 'Roles', render: (row) => row.roles.map((r) => r.replace('ROLE_', '')).join(', ') },
    { key: 'emailVerified', label: 'Verified', render: (row) => (row.emailVerified ? '✅' : '—') },
    {
      key: 'enabled',
      label: 'Status',
      render: (row) => (
        <span style={{ color: row.enabled ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }}>
          {row.enabled ? 'Active' : 'Disabled'}
        </span>
      ),
    },
    { key: 'createdAt', label: 'Joined', render: (row) => new Date(row.createdAt).toLocaleDateString() },
    {
      key: 'actions',
      label: '',
      align: 'right',
      render: (row) => {
        const isSelf = row.id === currentUser?.id;
        return row.enabled ? (
          <button
            type="button"
            className="admin-users-page__danger"
            disabled={isSelf || busyId === row.id}
            title={isSelf ? 'You cannot disable your own account' : undefined}
            onClick={() => setConfirmTarget(row)}
          >
            Disable
          </button>
        ) : (
          <button type="button" disabled={busyId === row.id} onClick={() => handleEnable(row)}>
            Enable
          </button>
        );
      },
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
    <div className="admin-users-page">
      <div className="admin-users-page__header">
        <h1>Users</h1>
      </div>

      <div className="admin-users-page__toolbar">
        <input
          type="search"
          placeholder="Search by name or email…"
          value={search}
          onChange={(e) => handleSearchChange(e.target.value)}
        />
        <div className="admin-users-page__tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              type="button"
              className={`admin-users-page__tab${statusFilter === tab.value ? ' active' : ''}`}
              onClick={() => handleStatusChange(tab.value)}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="card">
        <Table columns={columns} rows={pageData.content} emptyMessage="No users match these filters." />
      </div>

      {pageData.totalPages > 1 && (
        <div className="admin-users-page__pagination">
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

      {confirmTarget && (
        <Modal title="Disable user" onClose={() => setConfirmTarget(null)}>
          <p>
            Disable <strong>{confirmTarget.fullName}</strong>? They will not be able to log in until re-enabled.
          </p>
          <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
            <Button variant="secondary" onClick={() => setConfirmTarget(null)}>
              Cancel
            </Button>
            <Button variant="danger" loading={busyId === confirmTarget.id} onClick={() => handleDisable(confirmTarget)}>
              Disable
            </Button>
          </div>
        </Modal>
      )}
    </div>
  );
}

export default AdminUsersPage;
