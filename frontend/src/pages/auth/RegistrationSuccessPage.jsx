import { Link, useLocation } from 'react-router-dom';
import './AuthForm.css';

function RegistrationSuccessPage() {
  const location = useLocation();
  const email = location.state?.email;

  return (
    <div className="auth-form">
      <h1>Check your email</h1>
      <p className="subtitle">
        We sent a verification link{email ? ` to ${email}` : ''}. Open it to activate your account before logging in.
      </p>
      <p className="auth-form__footer">
        Didn&apos;t get it? <Link to="/resend-verification" state={{ email }}>Resend verification email</Link>
      </p>
      <p className="auth-form__footer">
        <Link to="/login">Back to login</Link>
      </p>
    </div>
  );
}

export default RegistrationSuccessPage;
