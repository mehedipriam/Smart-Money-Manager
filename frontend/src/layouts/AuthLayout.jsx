import { Outlet, Link } from 'react-router-dom';
import './AuthLayout.css';

function AuthLayout() {
  return (
    <div className="auth-layout">
      <div className="auth-layout__panel card">
        <Link to="/" className="auth-layout__brand">
          Smart Money Manager
        </Link>
        <Outlet />
      </div>
    </div>
  );
}

export default AuthLayout;
