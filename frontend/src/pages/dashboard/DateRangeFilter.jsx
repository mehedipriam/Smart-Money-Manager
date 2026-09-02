import './DateRangeFilter.css';

const RANGES = [
  { value: 'TODAY', label: 'Today' },
  { value: 'THIS_WEEK', label: 'This Week' },
  { value: 'THIS_MONTH', label: 'This Month' },
  { value: 'LAST_MONTH', label: 'Last Month' },
  { value: 'THIS_YEAR', label: 'This Year' },
  { value: 'CUSTOM', label: 'Custom' },
];

function DateRangeFilter({ range, customStart, customEnd, onChange }) {
  return (
    <div className="date-range-filter">
      <div className="date-range-filter__tabs">
        {RANGES.map((r) => (
          <button
            key={r.value}
            type="button"
            className={`date-range-filter__tab${range === r.value ? ' active' : ''}`}
            onClick={() => onChange({ range: r.value, customStart, customEnd })}
          >
            {r.label}
          </button>
        ))}
      </div>
      {range === 'CUSTOM' && (
        <div className="date-range-filter__custom">
          <input
            type="date"
            value={customStart}
            onChange={(e) => onChange({ range, customStart: e.target.value, customEnd })}
          />
          <span>to</span>
          <input
            type="date"
            value={customEnd}
            onChange={(e) => onChange({ range, customStart, customEnd: e.target.value })}
          />
        </div>
      )}
    </div>
  );
}

export default DateRangeFilter;
