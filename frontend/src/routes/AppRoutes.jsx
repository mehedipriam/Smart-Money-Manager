import { Routes, Route } from 'react-router-dom';
import Home from '../pages/Home.jsx';
import ProfilePage from '../pages/ProfilePage.jsx';
import DashboardPage from '../pages/dashboard/DashboardPage.jsx';
import AccountsPage from '../pages/accounts/AccountsPage.jsx';
import CategoriesPage from '../pages/categories/CategoriesPage.jsx';
import TransactionsPage from '../pages/transactions/TransactionsPage.jsx';
import RecurringTransactionsPage from '../pages/transactions/RecurringTransactionsPage.jsx';
import BudgetsPage from '../pages/budgets/BudgetsPage.jsx';
import GoalsPage from '../pages/goals/GoalsPage.jsx';
import BillsPage from '../pages/bills/BillsPage.jsx';
import ReportsPage from '../pages/reports/ReportsPage.jsx';
import LoginPage from '../pages/auth/LoginPage.jsx';
import RegisterPage from '../pages/auth/RegisterPage.jsx';
import RegistrationSuccessPage from '../pages/auth/RegistrationSuccessPage.jsx';
import VerifyEmailPage from '../pages/auth/VerifyEmailPage.jsx';
import ResendVerificationPage from '../pages/auth/ResendVerificationPage.jsx';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage.jsx';
import ResetPasswordPage from '../pages/auth/ResetPasswordPage.jsx';
import AuthLayout from '../layouts/AuthLayout.jsx';
import AppLayout from '../layouts/AppLayout.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import GuestOnlyRoute from './GuestOnlyRoute.jsx';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />

      {/* Login/register only make sense for a logged-out visitor. */}
      <Route element={<GuestOnlyRoute />}>
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/registration-success" element={<RegistrationSuccessPage />} />
        </Route>
      </Route>

      {/* Token-driven flows: valid regardless of whether the browser happens to hold a session. */}
      <Route element={<AuthLayout />}>
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/resend-verification" element={<ResendVerificationPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/accounts" element={<AccountsPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/transactions/recurring" element={<RecurringTransactionsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/budgets" element={<BudgetsPage />} />
          <Route path="/goals" element={<GoalsPage />} />
          <Route path="/bills" element={<BillsPage />} />
          <Route path="/reports" element={<ReportsPage />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default AppRoutes;
