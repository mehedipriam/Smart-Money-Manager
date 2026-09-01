import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../services/authService.js';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import './AuthForm.css';

function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      await forgotPassword(email);
    } finally {
      setLoading(false);
      // Same generic confirmation regardless of outcome — the API never reveals whether the email exists.
      setSent(true);
    }
  }

  if (sent) {
    return (
      <div className="auth-form">
        <h1>Check your email</h1>
        <div className="auth-form__banner auth-form__banner--success">
          If an account with that email exists, we&apos;ve sent a password reset link.
        </div>
        <p className="auth-form__footer">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    );
  }

  return (
    <div className="auth-form">
      <h1>Forgot password</h1>
      <p className="subtitle">Enter your account email and we&apos;ll send you a reset link.</p>
      <form onSubmit={handleSubmit} noValidate>
        <Input
          label="Email"
          type="email"
          name="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Button type="submit" loading={loading}>
          Send reset link
        </Button>
      </form>
      <p className="auth-form__footer">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  );
}

export default ForgotPasswordPage;
