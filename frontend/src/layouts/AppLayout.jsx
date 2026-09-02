import { Outlet, Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Button from '../components/common/Button.jsx';
import './AppLayout.css';

/**
 * Minimal authenticated shell (topbar + simple nav). The full dark sidebar
 * from spec section 14 is built out in Phase 7 once Budgets/Goals/Bills/
 * Reports all exist — until then a sidebar would be mostly dead links, so
 * this grows one real nav item per phase instead.
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
        <Link to="/accounts" className="app-layout__brand">
          Smart Money Manager
        </Link>
        <nav className="app-layout__nav">
          <NavLink to="/accounts" className={({ isActive }) => (isActive ? 'active' : undefined)}>
            Accounts
          </NavLink>
          <NavLink to="/categories" className={({ isActive }) => (isActive ? 'active' : undefined)}>
            Categories
          </NavLink>
          <NavLink to="/transactions" className={({ isActive }) => (isActive ? 'active' : undefined)}>
            Transactions
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : undefined)}>
            Profile
          </NavLink>
        </nav>
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
