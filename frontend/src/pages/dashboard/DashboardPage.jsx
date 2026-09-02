import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import * as dashboardService from '../../services/dashboardService.js';
import * as transactionService from '../../services/transactionService.js';
import * as accountService from '../../services/accountService.js';
import * as budgetService from '../../services/budgetService.js';
import * as billService from '../../services/billService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Spinner from '../../components/common/Spinner.jsx';
import Table from '../../components/common/Table.jsx';
import ProgressBar from '../../components/common/ProgressBar.jsx';
import DateRangeFilter from './DateRangeFilter.jsx';
import StatCard from './StatCard.jsx';
import SpendingPieChart from './SpendingPieChart.jsx';
import CashFlowChart from './CashFlowChart.jsx';
import './DashboardPage.css';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function DashboardPage() {
  const toast = useToast();
  const [filter, setFilter] = useState({ range: 'THIS_MONTH', customStart: todayIso(), customEnd: todayIso() });
  const [dashboard, setDashboard] = useState(null);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [upcomingBills, setUpcomingBills] = useState([]);
  const [currency, setCurrency] = useState('BDT');
  const [loading, setLoading] = useState(true);
  const debounceRef = useRef(null);

  async function load() {
    if (filter.range === 'CUSTOM' && (!filter.customStart || !filter.customEnd)) {
      return;
    }
    try {
      const params = { range: filter.range };
      if (filter.range === 'CUSTOM') {
        params.startDate = filter.customStart;
        params.endDate = filter.customEnd;
      }
      // Budget Overview and Upcoming Bills are always "right now" widgets, independent of the
      // dashboard's own date filter — a budget is inherently monthly and a bill's due date is
      // what it is regardless of which historical period the user is currently viewing.
      const [dashboardData, txnPage, accounts, currentBudgets, bills] = await Promise.all([
        dashboardService.getDashboard(params),
        transactionService.getTransactions({ page: 0, size: 5, sortBy: 'transactionDate', sortDir: 'desc' }),
        accountService.getAccounts(),
        budgetService.getBudgets({}),
        billService.getUpcomingBills(5),
      ]);
      setDashboard(dashboardData);
      setRecentTransactions(txnPage.content);
      setBudgets(currentBudgets);
      setUpcomingBills(bills);
      if (accounts.length > 0) setCurrency(accounts[0].currency);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not load dashboard'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(load, 200);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter]);

  async function handleMarkBillPaid(bill) {
    try {
      await billService.markAsPaid(bill.id);
      setUpcomingBills(await billService.getUpcomingBills(5));
      toast.success('Bill marked as paid');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not mark bill as paid'));
    }
  }

  if (loading && !dashboard) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  const recentColumns = [
    { key: 'transactionDate', label: 'Date' },
    { key: 'category', label: 'Category', render: (row) => `${row.category.icon} ${row.category.name}` },
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
  ];

  return (
    <div className="dashboard-page">
      <div className="dashboard-page__header">
        <h1>Dashboard</h1>
      </div>

      <DateRangeFilter range={filter.range} customStart={filter.customStart} customEnd={filter.customEnd} onChange={setFilter} />

      {dashboard && (
        <>
          <div className="dashboard-stats">
            <StatCard label="Total Balance" amount={dashboard.summary.totalBalance} currency={currency} />
            <StatCard
              label="Total Income"
              amount={dashboard.summary.totalIncome}
              currency={currency}
              changePercent={dashboard.summary.incomeChangePercent}
            />
            <StatCard
              label="Total Expenses"
              amount={dashboard.summary.totalExpenses}
              currency={currency}
              changePercent={dashboard.summary.expenseChangePercent}
            />
            <StatCard
              label="Savings This Month"
              amount={dashboard.summary.monthlySavings}
              currency={currency}
              changePercent={dashboard.summary.savingsChangePercent}
            />
          </div>

          <div className="dashboard-grid">
            <div className="card">
              <h2>Spending Overview</h2>
              <SpendingPieChart data={dashboard.spendingByCategory} currency={currency} />
            </div>
            <div className="card">
              <h2>Cash Flow</h2>
              <CashFlowChart data={dashboard.cashFlow} currency={currency} />
            </div>
          </div>

          <div className="dashboard-grid">
            <div className="card">
              <div className="dashboard-page__section-header">
                <h2>Recent Transactions</h2>
                <Link to="/transactions">View all →</Link>
              </div>
              <Table columns={recentColumns} rows={recentTransactions} emptyMessage="No transactions yet." />
            </div>

            <div className="card">
              <div className="dashboard-page__section-header">
                <h2>Budget Overview</h2>
                <Link to="/budgets">Manage →</Link>
              </div>
              {budgets.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
                  No budgets set for this month yet. <Link to="/budgets">Create one</Link> to track spending against a limit.
                </p>
              ) : (
                <div className="dashboard-budgets">
                  {budgets.map((budget) => (
                    <div key={budget.id} className="dashboard-budgets__row">
                      <div className="dashboard-budgets__label">
                        <span>
                          {budget.category.icon} {budget.category.name}
                        </span>
                        <span>{formatCurrency(budget.usedAmount, currency)} / {formatCurrency(budget.budgetAmount, currency)}</span>
                      </div>
                      <ProgressBar percentage={budget.usagePercentage} nearLimit={budget.nearLimit} exceeded={budget.exceeded} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="dashboard-grid">
            <div className="card">
              <div className="dashboard-page__section-header">
                <h2>Upcoming Bills</h2>
                <Link to="/bills">Manage →</Link>
              </div>
              {upcomingBills.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
                  No unpaid bills. <Link to="/bills">Add one</Link> to get reminders here.
                </p>
              ) : (
                <ul className="dashboard-bills">
                  {upcomingBills.map((bill) => (
                    <li key={bill.id} className="dashboard-bills__row">
                      <div>
                        <span className="dashboard-bills__name">{bill.billName}</span>
                        <span
                          className="dashboard-bills__due"
                          style={{ color: bill.paymentStatus === 'OVERDUE' ? 'var(--color-danger)' : 'var(--color-text-muted)' }}
                        >
                          Due {bill.dueDate} {bill.paymentStatus === 'OVERDUE' && '· Overdue'}
                        </span>
                      </div>
                      <div className="dashboard-bills__right">
                        <span>{formatCurrency(bill.amount, currency)}</span>
                        <button type="button" onClick={() => handleMarkBillPaid(bill)}>
                          Mark paid
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default DashboardPage;
