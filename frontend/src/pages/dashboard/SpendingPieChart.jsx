import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { formatCurrency } from '../../utils/formatCurrency.js';

const FALLBACK_COLOR = '#9ca3af';

function SpendingPieChart({ data, currency }) {
  if (data.length === 0) {
    return <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '32px 0' }}>No expenses in this period yet.</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={data}
          dataKey="amount"
          nameKey="categoryName"
          innerRadius={60}
          outerRadius={95}
          paddingAngle={2}
        >
          {data.map((entry) => (
            <Cell key={entry.categoryId} fill={entry.color || FALLBACK_COLOR} stroke="var(--color-surface)" strokeWidth={2} />
          ))}
        </Pie>
        <Tooltip
          formatter={(value, _name, item) => [formatCurrency(value, currency), item.payload.categoryName]}
          contentStyle={{ borderRadius: 8, border: '1px solid var(--color-border)' }}
        />
        <Legend verticalAlign="bottom" height={36} formatter={(value) => <span style={{ color: 'var(--color-text)' }}>{value}</span>} />
      </PieChart>
    </ResponsiveContainer>
  );
}

export default SpendingPieChart;
