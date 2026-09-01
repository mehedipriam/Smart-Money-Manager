import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/** Keeps an already-logged-in user off the login/register pages. */
function GuestOnlyRoute() {
  const { isAuthenticated, isBootstrapping } = useAuth();

  if (isBootstrapping) {
    return null;
  }

  if (isAuthenticated) {
    return <Navigate to="/profile" replace />;
  }

  return <Outlet />;
}

export default GuestOnlyRoute;
