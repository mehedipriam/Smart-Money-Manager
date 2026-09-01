import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Button from '../components/common/Button.jsx';
import './AppLayout.css';

/**
 * Minimal authenticated shell for Phase 3 (just a topbar). The full dark
 * sidebar with Transactions/Accounts/Budgets/... links (spec section 14)
 * is built out in Phase 7 once those pages actually exist — until then a
 * sidebar would only contain dead links.
 */
function AppLayout() {
  const { user, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    toast.info('Logged out');
    navigate('/login', { replace: true });
  }

  return (
    <div className="app-layout">
      <header className="app-layout__topbar">
        <Link to="/profile" className="app-layout__brand">
          Smart Money Manager
        </Link>
        <div className="app-layout__user">
          <span>{user?.fullName}</span>
          <Button variant="ghost" onClick={handleLogout} style={{ width: 'auto' }}>
            Log out
          </Button>
        </div>
      </header>
      <main className="app-layout__content">
        <Outlet />
      </main>
    </div>
  );
}

export default AppLayout;
