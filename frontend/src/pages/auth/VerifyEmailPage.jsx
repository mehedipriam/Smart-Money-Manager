import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { verifyEmail } from '../../services/authService.js';
import { getErrorMessage } from '../../utils/apiError.js';
import Spinner from '../../components/common/Spinner.jsx';
import { useQueryParam } from '../../hooks/useQueryParam.js';
import './AuthForm.css';

function VerifyEmailPage() {
  const token = useQueryParam('token');
  const [status, setStatus] = useState('loading'); // loading | success | error
  const [message, setMessage] = useState('');
  // The verification token is single-use, so React 18 StrictMode's dev-mode
  // double-invoke of this effect must not fire the request twice — the second
  // call would legitimately get "already used" and clobber the real result.
  const requestedRef = useRef(false);

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('This verification link is missing its token.');
      return;
    }
    if (requestedRef.current) return;
    requestedRef.current = true;

    verifyEmail(token)
      .then((successMessage) => {
        setStatus('success');
        setMessage(successMessage);
      })
      .catch((err) => {
        setStatus('error');
        setMessage(getErrorMessage(err, 'This verification link is invalid or has expired.'));
      });
  }, [token]);

  return (
    <div className="auth-form">
      <h1>Email verification</h1>

      {status === 'loading' && (
        <div style={{ display: 'grid', placeItems: 'center', padding: '24px 0' }}>
          <Spinner />
        </div>
      )}

      {status === 'success' && (
        <>
          <div className="auth-form__banner auth-form__banner--success">{message}</div>
          <Link to="/login">Continue to login</Link>
        </>
      )}

      {status === 'error' && (
        <>
          <div className="auth-form__banner auth-form__banner--error">{message}</div>
          <Link to="/resend-verification">Request a new verification link</Link>
        </>
      )}
    </div>
  );
}

export default VerifyEmailPage;
