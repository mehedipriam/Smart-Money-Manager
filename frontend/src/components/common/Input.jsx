import './Input.css';

function Input({ label, error, id, ...rest }) {
  const inputId = id || rest.name;
  return (
    <div className="field">
      {label && (
        <label className="field__label" htmlFor={inputId}>
          {label}
        </label>
      )}
      <input id={inputId} className={`field__input ${error ? 'field__input--error' : ''}`} {...rest} />
      {error && <p className="field__error">{error}</p>}
    </div>
  );
}

export default Input;
