import { formatCurrency } from '../../utils/formatCurrency.js';

function ContributionsList({ contributions, currency }) {
  if (contributions.length === 0) {
    return <p style={{ color: 'var(--color-text-muted)' }}>No contributions yet.</p>;
  }

  return (
    <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexDirection: 'column', gap: 10 }}>
      {contributions.map((c) => (
        <li
          key={c.id}
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            borderBottom: '1px solid var(--color-border)',
            paddingBottom: 8,
          }}
        >
          <span>
            {c.contributionDate}
            {c.note && <span style={{ color: 'var(--color-text-muted)' }}> — {c.note}</span>}
          </span>
          <span style={{ fontWeight: 600, color: 'var(--color-success)' }}>+{formatCurrency(c.amount, currency)}</span>
        </li>
      ))}
    </ul>
  );
}

export default ContributionsList;
