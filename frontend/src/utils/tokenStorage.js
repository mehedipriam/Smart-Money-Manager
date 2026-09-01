const STORAGE_KEY = 'smm.auth';

function read() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function getSession() {
  return read();
}

export function getAccessToken() {
  return read()?.accessToken ?? null;
}

export function getRefreshToken() {
  return read()?.refreshToken ?? null;
}

export function getStoredUser() {
  return read()?.user ?? null;
}

export function setSession({ accessToken, refreshToken, user }) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ accessToken, refreshToken, user }));
}

export function setStoredUser(user) {
  const current = read();
  if (!current) return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...current, user }));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}
