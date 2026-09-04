import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

/** Blocks non-admin users from the /admin subtree — sits inside ProtectedRoute, which already handles auth. */
function AdminRoute() {
  const { user } = useAuth();

  if (!user?.roles?.includes('ROLE_ADMIN')) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}

export default AdminRoute;
