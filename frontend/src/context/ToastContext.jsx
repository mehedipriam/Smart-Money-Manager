import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import './ToastContext.css';

const ToastContext = createContext(null);

let nextId = 1;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    (message, type = 'info', duration = 4000) => {
      const id = nextId++;
      setToasts((current) => [...current, { id, message, type }]);
      if (duration > 0) {
        setTimeout(() => removeToast(id), duration);
      }
      return id;
    },
    [removeToast],
  );

  const value = useMemo(
    () => ({
      showToast,
      success: (message, duration) => showToast(message, 'success', duration),
      error: (message, duration) => showToast(message, 'error', duration),
      info: (message, duration) => showToast(message, 'info', duration),
    }),
    [showToast],
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast--${toast.type}`} onClick={() => removeToast(toast.id)}>
            {toast.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
