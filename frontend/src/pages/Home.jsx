import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import Button from '../components/common/Button.jsx';

function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <div
      style={{
        minHeight: '100svh',
        display: 'grid',
        placeItems: 'center',
        padding: 24,
        textAlign: 'center',
      }}
    >
      <div>
        <h1>Smart Money Manager</h1>
        <p style={{ color: 'var(--color-text-muted)', maxWidth: 420, margin: '0 auto 24px' }}>
          Track income, expenses, budgets, and savings goals in one place.
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
          {isAuthenticated ? (
            <Link to="/profile">
              <Button>Go to profile</Button>
            </Link>
          ) : (
            <>
              <Link to="/login">
                <Button variant="secondary">Log in</Button>
              </Link>
              <Link to="/register">
                <Button>Sign up</Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default Home;
