import { useEffect, useRef, useState } from 'react';
import * as reportService from '../../services/reportService.js';
import * as accountService from '../../services/accountService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';
import Spinner from '../../components/common/Spinner.jsx';
import Button from '../../components/common/Button.jsx';
import DateRangeFilter from '../dashboard/DateRangeFilter.jsx';
import StatCard from '../dashboard/StatCard.jsx';
import CashFlowChart from '../dashboard/CashFlowChart.jsx';
import SpendingPieChart from '../dashboard/SpendingPieChart.jsx';
import SavingsTrendChart from './SavingsTrendChart.jsx';
import '../dashboard/DashboardPage.css';
import './ReportsPage.css';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function ReportsPage() {
  const toast = useToast();
  const [filter, setFilter] = useState({ range: 'THIS_MONTH', customStart: todayIso(), customEnd: todayIso() });
  const [report, setReport] = useState(null);
  const [categoryView, setCategoryView] = useState('EXPENSE');
  const [currency, setCurrency] = useState('BDT');
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(null); // 'csv' | 'pdf' | null
  const debounceRef = useRef(null);

  function buildParams() {
    const params = { range: filter.range };
    if (filter.range === 'CUSTOM') {
      params.startDate = filter.customStart;
      params.endDate = filter.customEnd;
    }
    return params;
  }

  async function load() {
    if (filter.range === 'CUSTOM' && (!filter.customStart || !filter.customEnd)) {
      return;
    }
    try {
      const [reportData, accounts] = await Promise.all([
        reportService.getReportSummary(buildParams()),
        accountService.getAccounts(),
      ]);
      setReport(reportData);
      if (accounts.length > 0) setCurrency(accounts[0].currency);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not load report'));
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

  async function handleExport(format) {
    setExporting(format);
    try {
      if (format === 'csv') {
        await reportService.exportCsv(buildParams());
      } else {
        await reportService.exportPdf(buildParams());
      }
      toast.success(`Report exported as ${format.toUpperCase()}`);
    } catch (err) {
      toast.error(getErrorMessage(err, `Could not export ${format.toUpperCase()}`));
    } finally {
      setExporting(null);
    }
  }

  if (loading && !report) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', padding: '48px 0' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className="reports-page">
      <div className="reports-page__header">
        <h1>Reports &amp; Analytics</h1>
        <div className="reports-page__export">
          <Button variant="secondary" style={{ width: 'auto' }} loading={exporting === 'csv'} onClick={() => handleExport('csv')}>
            Export CSV
          </Button>
          <Button style={{ width: 'auto' }} loading={exporting === 'pdf'} onClick={() => handleExport('pdf')}>
            Export PDF
          </Button>
        </div>
      </div>

      <DateRangeFilter range={filter.range} customStart={filter.customStart} customEnd={filter.customEnd} onChange={setFilter} />

      {report && (
        <>
          <div className="dashboard-stats">
            <StatCard label="Total Income" amount={report.totalIncome} currency={currency} changePercent={report.incomeChangePercent} />
            <StatCard label="Total Expenses" amount={report.totalExpenses} currency={currency} changePercent={report.expenseChangePercent} />
            <StatCard label="Net Savings" amount={report.netSavings} currency={currency} changePercent={report.savingsChangePercent} />
          </div>

          <div className="reports-insights">
            <div className="card reports-insight">
              <span className="reports-insight__label">Savings Rate</span>
              <span className="reports-insight__value">{report.savingsRate === null ? 'N/A' : `${report.savingsRate}%`}</span>
            </div>
            <div className="card reports-insight">
              <span className="reports-insight__label">Average Monthly Expense</span>
              <span className="reports-insight__value">{formatCurrency(report.averageMonthlyExpense, currency)}</span>
            </div>
            <div className="card reports-insight">
              <span className="reports-insight__label">Highest Expense Category</span>
              <span className="reports-insight__value">
                {report.highestExpenseCategory
                  ? `${report.highestExpenseCategory.icon || ''} ${report.highestExpenseCategory.categoryName}`
                  : 'N/A'}
              </span>
            </div>
          </div>

          <div className="dashboard-grid">
            <div className="card">
              <h2>Income vs Expense</h2>
              <CashFlowChart data={report.cashFlow} currency={currency} />
            </div>

            <div className="card">
              <div className="reports-page__section-header">
                <h2>Category Spending</h2>
                <div className="reports-page__tabs">
                  <button
                    type="button"
                    className={`reports-page__tab${categoryView === 'EXPENSE' ? ' active' : ''}`}
                    onClick={() => setCategoryView('EXPENSE')}
                  >
                    Expense
                  </button>
                  <button
                    type="button"
                    className={`reports-page__tab${categoryView === 'INCOME' ? ' active' : ''}`}
                    onClick={() => setCategoryView('INCOME')}
                  >
                    Income
                  </button>
                </div>
              </div>
              <SpendingPieChart
                data={categoryView === 'EXPENSE' ? report.expenseByCategory : report.incomeByCategory}
                currency={currency}
              />
            </div>
          </div>

          <div className="dashboard-grid">
            <div className="card">
              <h2>Monthly Cash Flow</h2>
              <CashFlowChart data={report.monthlyTrend} currency={currency} />
            </div>

            <div className="card">
              <h2>Savings Trend</h2>
              <SavingsTrendChart data={report.monthlyTrend} currency={currency} />
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default ReportsPage;
