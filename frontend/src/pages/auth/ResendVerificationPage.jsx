import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { resendVerification } from '../../services/authService.js';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import './AuthForm.css';

function ResendVerificationPage() {
  const location = useLocation();
  const [email, setEmail] = useState(location.state?.email || '');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    try {
      await resendVerification(email);
    } finally {
      setLoading(false);
      // Always show the same generic confirmation — the API never reveals whether the email exists.
      setSent(true);
    }
  }

  if (sent) {
    return (
      <div className="auth-form">
        <h1>Check your email</h1>
        <div className="auth-form__banner auth-form__banner--success">
          If an account with that email exists and isn&apos;t verified yet, we&apos;ve sent a new verification link.
        </div>
        <p className="auth-form__footer">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    );
  }

  return (
    <div className="auth-form">
      <h1>Resend verification email</h1>
      <p className="subtitle">Enter your email and we&apos;ll send a fresh verification link.</p>
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
          Send link
        </Button>
      </form>
      <p className="auth-form__footer">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  );
}

export default ResendVerificationPage;
