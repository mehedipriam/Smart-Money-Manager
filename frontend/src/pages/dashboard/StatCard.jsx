import { formatCurrency } from '../../utils/formatCurrency.js';
import './StatCard.css';

function ChangeBadge({ percent }) {
  if (percent === null || percent === undefined) {
    return <span className="stat-card__change stat-card__change--neutral">New</span>;
  }
  const isUp = Number(percent) >= 0;
  return (
    <span className={`stat-card__change ${isUp ? 'stat-card__change--up' : 'stat-card__change--down'}`}>
      {isUp ? '▲' : '▼'} {Math.abs(percent)}%
    </span>
  );
}

function StatCard({ label, amount, currency, changePercent }) {
  return (
    <div className="card stat-card">
      <span className="stat-card__label">{label}</span>
      <span className="stat-card__amount">{formatCurrency(amount, currency)}</span>
      {changePercent !== undefined && <ChangeBadge percent={changePercent} />}
    </div>
  );
}

export default StatCard;
