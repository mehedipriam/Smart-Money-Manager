import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { useTheme } from '../context/ThemeContext.jsx';
import './AppLayout.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/transactions', icon: '💳', label: 'Transactions' },
  { to: '/accounts', icon: '🏦', label: 'Accounts' },
  { to: '/budgets', icon: '🧮', label: 'Budgets' },
  { to: '/goals', icon: '🎯', label: 'Goals' },
  { to: '/bills', icon: '🧾', label: 'Bills & Reminders' },
  { to: '/reports', icon: '📈', label: 'Reports' },
  { to: '/categories', icon: '🏷️', label: 'Categories' },
  { to: '/profile', icon: '⚙️', label: 'Settings' },
];

function AppLayout() {
  const { user, logout } = useAuth();
  const toast = useToast();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    toast.info('Logged out');
    navigate('/login', { replace: true });
  }

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="app-sidebar__brand">Smart Money Manager</div>
        <nav className="app-sidebar__nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/dashboard'}
              className={({ isActive }) => `app-sidebar__link${isActive ? ' active' : ''}`}
            >
              <span className="app-sidebar__icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="app-main">
        <header className="app-topbar">
          <div />
          <div className="app-topbar__actions">
            <button
              type="button"
              className="app-topbar__icon-btn"
              aria-label="Notifications"
              onClick={() => toast.info('Notifications are coming in a later phase.')}
            >
              🔔
            </button>
            <button
              type="button"
              className="app-topbar__icon-btn"
              aria-label="Toggle dark mode"
              onClick={toggleTheme}
            >
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>
            <NavLink to="/profile" className="app-topbar__user">
              {user?.fullName}
            </NavLink>
            <button type="button" className="app-topbar__logout" onClick={handleLogout}>
              Log out
            </button>
          </div>
        </header>
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
