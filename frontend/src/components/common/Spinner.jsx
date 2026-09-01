import './Spinner.css';

function Spinner({ size = 32 }) {
  return (
    <div
      className="spinner"
      style={{ width: size, height: size }}
      role="status"
      aria-label="Loading"
    />
  );
}

export default Spinner;
