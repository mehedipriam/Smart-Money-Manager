import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import { getErrorMessage, getFieldErrors } from '../../utils/apiError.js';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import './AuthForm.css';

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

function validate(form) {
  const errors = {};
  if (!form.fullName.trim()) errors.fullName = 'Full name is required';
  if (!/^\S+@\S+\.\S+$/.test(form.email)) errors.email = 'Enter a valid email address';
  if (!PASSWORD_PATTERN.test(form.password)) {
    errors.password = 'At least 8 characters, with a letter and a number';
  }
  return errors;
}

function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ fullName: '', email: '', password: '', phone: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    const errors = validate(form);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setLoading(true);
    try {
      await register(form);
      navigate('/registration-success', { state: { email: form.email } });
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-form">
      <h1>Create your account</h1>
      <p className="subtitle">Start tracking your money in minutes.</p>

      {error && <div className="auth-form__banner auth-form__banner--error">{error}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="Full name"
          name="fullName"
          autoComplete="name"
          required
          value={form.fullName}
          onChange={handleChange}
          error={fieldErrors.fullName}
        />
        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          required
          value={form.email}
          onChange={handleChange}
          error={fieldErrors.email}
        />
        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="new-password"
          required
          value={form.password}
          onChange={handleChange}
          error={fieldErrors.password}
        />
        <Input
          label="Phone (optional)"
          type="tel"
          name="phone"
          autoComplete="tel"
          value={form.phone}
          onChange={handleChange}
          error={fieldErrors.phone}
        />
        <Button type="submit" loading={loading}>
          Sign up
        </Button>
      </form>

      <p className="auth-form__footer">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}

export default RegisterPage;
