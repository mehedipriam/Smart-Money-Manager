import { ComposedChart, Bar, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { formatCurrency } from '../../utils/formatCurrency.js';

function CashFlowChart({ data, currency }) {
  if (data.length === 0) {
    return <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '32px 0' }}>No activity in this period yet.</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={280}>
      <ComposedChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
        <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
        <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-muted)' }} />
        <Tooltip
          formatter={(value) => formatCurrency(value, currency)}
          contentStyle={{ borderRadius: 8, border: '1px solid var(--color-border)' }}
        />
        <Legend />
        <Bar dataKey="income" name="Income" fill="var(--color-success)" radius={[3, 3, 0, 0]} />
        <Bar dataKey="expense" name="Expense" fill="var(--color-danger)" radius={[3, 3, 0, 0]} />
        <Line dataKey="savings" name="Savings" stroke="var(--color-primary-dark)" strokeWidth={2} dot={{ r: 3 }} />
      </ComposedChart>
    </ResponsiveContainer>
  );
}

export default CashFlowChart;
