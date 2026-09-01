import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import './AuthForm.css';

function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [needsVerification, setNeedsVerification] = useState(false);
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setNeedsVerification(false);
    setLoading(true);
    try {
      await login(form);
      toast.success('Welcome back!');
      const redirectTo = location.state?.from?.pathname || '/accounts';
      navigate(redirectTo, { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, 'Invalid email or password');
      setError(message);
      if (err.response?.status === 403) {
        setNeedsVerification(true);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-form">
      <h1>Welcome back</h1>
      <p className="subtitle">Log in to manage your money.</p>

      {error && (
        <div className="auth-form__banner auth-form__banner--error">
          {error}
          {needsVerification && (
            <>
              {' '}
              <Link to="/resend-verification" state={{ email: form.email }}>
                Resend verification email
              </Link>
            </>
          )}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          required
          value={form.email}
          onChange={handleChange}
        />
        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          required
          value={form.password}
          onChange={handleChange}
        />
        <div style={{ textAlign: 'right', marginBottom: 16 }}>
          <Link to="/forgot-password" style={{ fontSize: '0.85rem' }}>
            Forgot password?
          </Link>
        </div>
        <Button type="submit" loading={loading}>
          Log in
        </Button>
      </form>

      <p className="auth-form__footer">
        Don&apos;t have an account? <Link to="/register">Sign up</Link>
      </p>
    </div>
  );
}

export default LoginPage;
