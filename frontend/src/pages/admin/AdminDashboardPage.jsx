import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as adminService from '../../services/adminService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import './AdminDashboardPage.css';

function AdminDashboardPage() {
  const toast = useToast();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService
      .getStats()
      .then(setStats)
      .catch((err) => toast.error(getErrorMessage(err, 'Could not load system statistics')))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  if (!stats) {
    return null;
  }

  const tiles = [
    { label: 'Total Users', value: stats.totalUsers },
    { label: 'Active Users', value: stats.activeUsers },
    { label: 'Disabled Users', value: stats.disabledUsers },
    { label: 'Verified Users', value: stats.verifiedUsers },
    { label: 'New Users This Month', value: stats.newUsersThisMonth },
    { label: 'Total Transactions', value: stats.totalTransactions },
    { label: 'Total Accounts', value: stats.totalAccounts },
    { label: 'Total Budgets', value: stats.totalBudgets },
    { label: 'Total Goals', value: stats.totalGoals },
    { label: 'Total Bills', value: stats.totalBills },
  ];

  return (
    <div className="admin-dashboard-page">
      <div className="admin-dashboard-page__header">
        <h1>Admin Dashboard</h1>
        <Link to="/admin/users">
          <Button style={{ width: 'auto' }}>Manage users</Button>
        </Link>
      </div>

      <div className="admin-stats-grid">
        {tiles.map((tile) => (
          <div className="card admin-stat" key={tile.label}>
            <span className="admin-stat__label">{tile.label}</span>
            <span className="admin-stat__value">{tile.value.toLocaleString()}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminDashboardPage;
