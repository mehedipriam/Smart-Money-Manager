import './Table.css';

/**
 * Generic table: columns = [{ key, label, render?(row), align? }].
 * `render` lets a column show something other than a raw field value.
 */
function Table({ columns, rows, rowKey = 'id', emptyMessage = 'Nothing to show yet.' }) {
  if (rows.length === 0) {
    return <p className="table-empty">{emptyMessage}</p>;
  }

  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key} style={col.align ? { textAlign: col.align } : undefined}>
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row[rowKey]}>
              {columns.map((col) => (
                <td key={col.key} style={col.align ? { textAlign: col.align } : undefined}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Table;
