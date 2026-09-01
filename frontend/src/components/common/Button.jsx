import './Button.css';

function Button({ children, variant = 'primary', loading = false, disabled, type = 'button', ...rest }) {
  return (
    <button
      type={type}
      className={`btn btn--${variant}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...rest}
    >
      {loading ? 'Please wait…' : children}
    </button>
  );
}

export default Button;
