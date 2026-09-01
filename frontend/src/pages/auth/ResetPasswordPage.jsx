import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { resetPassword } from '../../services/authService.js';
import { getErrorMessage } from '../../utils/apiError.js';
import { useQueryParam } from '../../hooks/useQueryParam.js';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import './AuthForm.css';

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

function ResetPasswordPage() {
  const token = useQueryParam('token');
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldError, setFieldError] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setFieldError(null);

    if (!PASSWORD_PATTERN.test(newPassword)) {
      setFieldError('At least 8 characters, with a letter and a number');
      return;
    }
    if (newPassword !== confirmPassword) {
      setFieldError("Passwords don't match");
      return;
    }

    setLoading(true);
    try {
      await resetPassword({ token, newPassword });
      navigate('/login', { state: { resetSuccess: true } });
    } catch (err) {
      setError(getErrorMessage(err, 'This reset link is invalid or has expired.'));
    } finally {
      setLoading(false);
    }
  }

  if (!token) {
    return (
      <div className="auth-form">
        <h1>Reset password</h1>
        <div className="auth-form__banner auth-form__banner--error">This reset link is missing its token.</div>
        <Link to="/forgot-password">Request a new link</Link>
      </div>
    );
  }

  return (
    <div className="auth-form">
      <h1>Choose a new password</h1>

      {error && <div className="auth-form__banner auth-form__banner--error">{error}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="New password"
          type="password"
          autoComplete="new-password"
          required
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          error={fieldError}
        />
        <Input
          label="Confirm new password"
          type="password"
          autoComplete="new-password"
          required
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <Button type="submit" loading={loading}>
          Reset password
        </Button>
      </form>
    </div>
  );
}

export default ResetPasswordPage;
