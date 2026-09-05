import { useMemo, useState } from 'react';
import Button from '../../components/common/Button.jsx';
import Spinner from '../../components/common/Spinner.jsx';
import Table from '../../components/common/Table.jsx';
import * as importService from '../../services/importService.js';
import * as transactionService from '../../services/transactionService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import { formatCurrency } from '../../utils/formatCurrency.js';

function TransactionImportModal({ accounts, categories, onCancel, onImported }) {
  const toast = useToast();
  const [file, setFile] = useState(null);
  const [previewing, setPreviewing] = useState(false);
  const [preview, setPreview] = useState(null); // ImportPreviewResponse
  const [checked, setChecked] = useState({}); // index -> boolean
  const [accountId, setAccountId] = useState('');
  const [incomeCategoryId, setIncomeCategoryId] = useState('');
  const [expenseCategoryId, setExpenseCategoryId] = useState('');
  const [importing, setImporting] = useState(false);
  const [results, setResults] = useState(null); // { succeeded, failed: [{row, error}] }

  const incomeCategories = useMemo(() => categories.filter((c) => c.type === 'INCOME'), [categories]);
  const expenseCategories = useMemo(() => categories.filter((c) => c.type === 'EXPENSE'), [categories]);

  async function handlePreview() {
    if (!file) return;
    setPreviewing(true);
    try {
      const result = await importService.previewImport(file);
      setPreview(result);
      const initialChecked = {};
      result.rows.forEach((row, i) => {
        initialChecked[i] = !row.duplicate;
      });
      setChecked(initialChecked);
      setAccountId(accounts[0]?.id ?? '');
      setIncomeCategoryId(result.suggestedIncomeCategoryId ?? '');
      setExpenseCategoryId(result.suggestedExpenseCategoryId ?? '');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not read that file'));
    } finally {
      setPreviewing(false);
    }
  }

  if (!preview) {
    return (
      <div>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem', marginTop: 0 }}>
          Upload a sales-tracker export (currently supports the Daraz Sales Tracker template) and review the
          transactions it finds before anything is saved.
        </p>
        <input
          type="file"
          accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        {previewing && (
          <div style={{ display: 'grid', placeItems: 'center', padding: 24 }}>
            <Spinner />
          </div>
        )}
        <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
          <Button type="button" variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button type="button" loading={previewing} disabled={!file} onClick={handlePreview}>
            Preview
          </Button>
        </div>
      </div>
    );
  }

  const rowsWithIndex = preview.rows.map((row, i) => ({ ...row, _index: i }));
  const checkedRows = rowsWithIndex.filter((row) => checked[row._index]);
  const checkedHasIncome = checkedRows.some((r) => r.type === 'INCOME');
  const checkedHasExpense = checkedRows.some((r) => r.type === 'EXPENSE');
  const canImport =
    !importing &&
    checkedRows.length > 0 &&
    accountId &&
    (!checkedHasIncome || incomeCategoryId) &&
    (!checkedHasExpense || expenseCategoryId);

  const incomeTotal = checkedRows.filter((r) => r.type === 'INCOME').reduce((sum, r) => sum + r.amount, 0);
  const expenseTotal = checkedRows.filter((r) => r.type === 'EXPENSE').reduce((sum, r) => sum + r.amount, 0);

  function toggleRow(index) {
    setChecked((prev) => ({ ...prev, [index]: !prev[index] }));
  }

  async function handleImport() {
    setImporting(true);
    setResults(null);
    const succeeded = [];
    const failed = [];

    for (const row of checkedRows) {
      try {
        // eslint-disable-next-line no-await-in-loop -- must be sequential: each transaction updates the same account's balance.
        await transactionService.createTransaction({
          accountId: Number(accountId),
          categoryId: Number(row.type === 'INCOME' ? incomeCategoryId : expenseCategoryId),
          type: row.type,
          amount: row.amount,
          transactionDate: row.transactionDate,
          description: row.description,
        });
        succeeded.push(row);
      } catch (err) {
        failed.push({ row, error: getErrorMessage(err, 'Could not create this transaction') });
      }
    }

    setResults({ succeeded, failed });
    setImporting(false);
    await onImported();

    if (failed.length === 0) {
      toast.success(`Imported ${succeeded.length} transaction${succeeded.length === 1 ? '' : 's'}`);
      onCancel();
    } else {
      toast.error(`Imported ${succeeded.length}, failed ${failed.length}`);
    }
  }

  const columns = [
    {
      key: 'select',
      label: '',
      render: (row) => <input type="checkbox" checked={!!checked[row._index]} onChange={() => toggleRow(row._index)} />,
    },
    { key: 'transactionDate', label: 'Date' },
    { key: 'description', label: 'Description' },
    {
      key: 'type',
      label: 'Type',
      render: (row) => (
        <span style={{ color: row.type === 'INCOME' ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }}>
          {row.type === 'INCOME' ? 'Income' : 'Expense'}
        </span>
      ),
    },
    {
      key: 'amount',
      label: 'Amount',
      align: 'right',
      render: (row) => formatCurrency(row.amount, row.currency),
    },
    {
      key: 'duplicate',
      label: '',
      render: (row) => (row.duplicate ? <span style={{ color: 'var(--color-warning)', fontSize: '0.8rem' }}>possible duplicate</span> : null),
    },
  ];

  return (
    <div>
      <p style={{ marginTop: 0 }}>{preview.summary}</p>

      {preview.warnings.length > 0 && (
        <div
          style={{
            background: 'color-mix(in srgb, var(--color-warning) 12%, transparent)',
            border: '1px solid var(--color-warning)',
            borderRadius: 'var(--radius-sm)',
            padding: '10px 14px',
            marginBottom: 16,
          }}
        >
          {preview.warnings.map((w, i) => (
            <p key={i} style={{ margin: i === 0 ? 0 : '4px 0 0', fontSize: '0.85rem' }}>
              {w}
            </p>
          ))}
        </div>
      )}

      <Table columns={columns} rows={rowsWithIndex} rowKey="_index" emptyMessage="No transactions found in this file." />

      <div className="field" style={{ marginTop: 16 }}>
        <label className="field__label" htmlFor="importAccount">
          Account
        </label>
        <select id="importAccount" className="field__input" value={accountId} onChange={(e) => setAccountId(e.target.value)}>
          <option value="" disabled>
            Select an account
          </option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {a.accountName}
            </option>
          ))}
        </select>
      </div>

      {checkedHasIncome && (
        <div className="field">
          <label className="field__label" htmlFor="importIncomeCategory">
            Income category
          </label>
          <select
            id="importIncomeCategory"
            className="field__input"
            value={incomeCategoryId}
            onChange={(e) => setIncomeCategoryId(e.target.value)}
          >
            <option value="" disabled>
              Select a category
            </option>
            {incomeCategories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.icon} {c.name}
              </option>
            ))}
          </select>
        </div>
      )}

      {checkedHasExpense && (
        <div className="field">
          <label className="field__label" htmlFor="importExpenseCategory">
            Expense category
          </label>
          <select
            id="importExpenseCategory"
            className="field__input"
            value={expenseCategoryId}
            onChange={(e) => setExpenseCategoryId(e.target.value)}
          >
            <option value="" disabled>
              Select a category
            </option>
            {expenseCategories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.icon} {c.name}
              </option>
            ))}
          </select>
        </div>
      )}

      <p style={{ fontSize: '0.9rem' }}>
        You&apos;re about to import {checkedRows.length} transaction{checkedRows.length === 1 ? '' : 's'}
        {checkedHasIncome && <> — {formatCurrency(incomeTotal, 'BDT')} income</>}
        {checkedHasExpense && <> — {formatCurrency(expenseTotal, 'BDT')} expense</>}.
      </p>

      {results && results.failed.length > 0 && (
        <div
          style={{
            background: 'color-mix(in srgb, var(--color-danger) 10%, transparent)',
            border: '1px solid var(--color-danger)',
            borderRadius: 'var(--radius-sm)',
            padding: '10px 14px',
            marginBottom: 16,
          }}
        >
          <p style={{ margin: 0, fontSize: '0.85rem' }}>
            Imported: {results.succeeded.length} · Failed: {results.failed.length}
          </p>
          {results.failed.map(({ row, error }, i) => (
            <p key={i} style={{ margin: '4px 0 0', fontSize: '0.8rem' }}>
              {row.externalReference ? `Order #${row.externalReference}` : row.description}: {error}
            </p>
          ))}
        </div>
      )}

      <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
        <Button type="button" variant="secondary" onClick={onCancel}>
          {results ? 'Close' : 'Cancel'}
        </Button>
        <Button type="button" loading={importing} disabled={!canImport} onClick={handleImport}>
          Import {checkedRows.length} transaction{checkedRows.length === 1 ? '' : 's'}
        </Button>
      </div>
    </div>
  );
}

export default TransactionImportModal;
