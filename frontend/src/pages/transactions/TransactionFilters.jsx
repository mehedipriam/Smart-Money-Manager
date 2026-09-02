import './TransactionFilters.css';

function TransactionFilters({ filters, onChange, accounts, categories }) {
  function set(name, value) {
    onChange({ ...filters, [name]: value });
  }

  return (
    <div className="txn-filters card">
      <input
        type="text"
        placeholder="Search description or note…"
        className="field__input"
        value={filters.search}
        onChange={(e) => set('search', e.target.value)}
      />

      <select className="field__input" value={filters.type} onChange={(e) => set('type', e.target.value)}>
        <option value="">All types</option>
        <option value="INCOME">Income</option>
        <option value="EXPENSE">Expense</option>
      </select>

      <select className="field__input" value={filters.accountId} onChange={(e) => set('accountId', e.target.value)}>
        <option value="">All accounts</option>
        {accounts.map((account) => (
          <option key={account.id} value={account.id}>
            {account.accountName}
          </option>
        ))}
      </select>

      <select className="field__input" value={filters.categoryId} onChange={(e) => set('categoryId', e.target.value)}>
        <option value="">All categories</option>
        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.icon} {category.name}
          </option>
        ))}
      </select>

      <input
        type="date"
        className="field__input"
        value={filters.dateFrom}
        onChange={(e) => set('dateFrom', e.target.value)}
        aria-label="From date"
      />
      <input
        type="date"
        className="field__input"
        value={filters.dateTo}
        onChange={(e) => set('dateTo', e.target.value)}
        aria-label="To date"
      />

      <input
        type="number"
        placeholder="Min amount"
        className="field__input"
        value={filters.amountFrom}
        onChange={(e) => set('amountFrom', e.target.value)}
      />
      <input
        type="number"
        placeholder="Max amount"
        className="field__input"
        value={filters.amountTo}
        onChange={(e) => set('amountTo', e.target.value)}
      />
    </div>
  );
}

export default TransactionFilters;
