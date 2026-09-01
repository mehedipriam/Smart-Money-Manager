import { useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import * as userService from '../services/userService.js';
import { getErrorMessage, getFieldErrors } from '../utils/apiError.js';
import Input from '../components/common/Input.jsx';
import Button from '../components/common/Button.jsx';
import './ProfilePage.css';

const CURRENCIES = ['BDT', 'USD', 'EUR', 'GBP'];
const LANGUAGES = [
  { value: 'EN', label: 'English' },
  { value: 'BN', label: 'Bangla' },
];
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

function ProfileForm() {
  const { user, updateLocalUser } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({
    fullName: user.fullName,
    phone: user.phone || '',
    profileImageUrl: user.profileImageUrl || '',
    defaultCurrency: user.defaultCurrency,
    preferredLanguage: user.preferredLanguage,
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setFieldErrors({});
    try {
      const updated = await userService.updateProfile(form);
      updateLocalUser(updated);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not update profile'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="card" onSubmit={handleSubmit} noValidate>
      <h2>Profile</h2>
      <Input label="Email" value={user.email} disabled />
      <Input
        label="Full name"
        name="fullName"
        required
        value={form.fullName}
        onChange={handleChange}
        error={fieldErrors.fullName}
      />
      <Input label="Phone" name="phone" value={form.phone} onChange={handleChange} error={fieldErrors.phone} />

      <div className="field">
        <label className="field__label" htmlFor="defaultCurrency">
          Default currency
        </label>
        <select
          id="defaultCurrency"
          name="defaultCurrency"
          className="field__input"
          value={form.defaultCurrency}
          onChange={handleChange}
        >
          {CURRENCIES.map((currency) => (
            <option key={currency} value={currency}>
              {currency}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label className="field__label" htmlFor="preferredLanguage">
          Language
        </label>
        <select
          id="preferredLanguage"
          name="preferredLanguage"
          className="field__input"
          value={form.preferredLanguage}
          onChange={handleChange}
        >
          {LANGUAGES.map((lang) => (
            <option key={lang.value} value={lang.value}>
              {lang.label}
            </option>
          ))}
        </select>
      </div>

      <Button type="submit" loading={saving}>
        Save changes
      </Button>
    </form>
  );
}

function ChangePasswordForm() {
  const toast = useToast();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setFieldErrors({});

    if (!PASSWORD_PATTERN.test(form.newPassword)) {
      setFieldErrors({ newPassword: 'At least 8 characters, with a letter and a number' });
      return;
    }
    if (form.newPassword !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: "Passwords don't match" });
      return;
    }

    setSaving(true);
    try {
      await userService.changePassword(form);
      toast.success('Password changed');
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not change password'));
      setFieldErrors(getFieldErrors(err));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="card" onSubmit={handleSubmit} noValidate>
      <h2>Change password</h2>
      <Input
        label="Current password"
        type="password"
        name="currentPassword"
        autoComplete="current-password"
        required
        value={form.currentPassword}
        onChange={handleChange}
        error={fieldErrors.currentPassword}
      />
      <Input
        label="New password"
        type="password"
        name="newPassword"
        autoComplete="new-password"
        required
        value={form.newPassword}
        onChange={handleChange}
        error={fieldErrors.newPassword}
      />
      <Input
        label="Confirm new password"
        type="password"
        name="confirmPassword"
        autoComplete="new-password"
        required
        value={form.confirmPassword}
        onChange={handleChange}
        error={fieldErrors.confirmPassword}
      />
      <Button type="submit" loading={saving}>
        Update password
      </Button>
    </form>
  );
}

function ProfilePage() {
  return (
    <div className="profile-page">
      <ProfileForm />
      <ChangePasswordForm />
    </div>
  );
}

export default ProfilePage;
