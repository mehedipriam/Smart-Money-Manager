import './ProgressBar.css';

function ProgressBar({ percentage, nearLimit, exceeded }) {
  const clamped = Math.min(Number(percentage), 100);
  const tone = exceeded ? 'danger' : nearLimit ? 'warning' : 'ok';

  return (
    <div className={`progress-bar progress-bar--${tone}`}>
      <div className="progress-bar__fill" style={{ width: `${clamped}%` }} />
    </div>
  );
}

export default ProgressBar;
