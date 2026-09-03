import { useEffect, useRef, useState } from 'react';
import * as notificationService from '../../services/notificationService.js';
import { useToast } from '../../context/ToastContext.jsx';
import { getErrorMessage } from '../../utils/apiError.js';
import Spinner from '../common/Spinner.jsx';
import './NotificationBell.css';

const POLL_INTERVAL_MS = 30000;

const TYPE_ICON = {
  BUDGET_WARNING: '⚠️',
  BUDGET_EXCEEDED: '🚨',
  BILL_DUE_REMINDER: '🧾',
  GOAL_COMPLETED: '🎯',
  RECURRING_TRANSACTION_ADDED: '🔁',
};

function timeAgo(isoString) {
  const seconds = Math.floor((Date.now() - new Date(isoString).getTime()) / 1000);
  if (seconds < 60) return 'just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

function NotificationBell() {
  const toast = useToast();
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const containerRef = useRef(null);

  async function refreshUnreadCount() {
    try {
      setUnreadCount(await notificationService.getUnreadCount());
    } catch {
      // Silent — the badge just stays at its last known value until the next successful poll.
    }
  }

  useEffect(() => {
    refreshUnreadCount();
    const interval = setInterval(refreshUnreadCount, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function togglePanel() {
    const next = !open;
    setOpen(next);
    if (next) {
      setLoading(true);
      try {
        setNotifications(await notificationService.getNotifications());
      } catch (err) {
        toast.error(getErrorMessage(err, 'Could not load notifications'));
      } finally {
        setLoading(false);
      }
    }
  }

  async function handleItemClick(notification) {
    if (notification.read) return;
    try {
      await notificationService.markAsRead(notification.id);
      setNotifications((current) => current.map((n) => (n.id === notification.id ? { ...n, read: true } : n)));
      setUnreadCount((count) => Math.max(0, count - 1));
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not mark notification as read'));
    }
  }

  async function handleDelete(event, notification) {
    event.stopPropagation();
    try {
      await notificationService.deleteNotification(notification.id);
      setNotifications((current) => current.filter((n) => n.id !== notification.id));
      if (!notification.read) setUnreadCount((count) => Math.max(0, count - 1));
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not delete notification'));
    }
  }

  async function handleMarkAllAsRead() {
    try {
      await notificationService.markAllAsRead();
      setNotifications((current) => current.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Could not mark all as read'));
    }
  }

  return (
    <div className="notification-bell" ref={containerRef}>
      <button
        type="button"
        className="app-topbar__icon-btn notification-bell__trigger"
        aria-label="Notifications"
        onClick={togglePanel}
      >
        🔔
        {unreadCount > 0 && <span className="notification-bell__badge">{unreadCount > 99 ? '99+' : unreadCount}</span>}
      </button>

      {open && (
        <div className="notification-bell__panel">
          <div className="notification-bell__header">
            <h3>Notifications</h3>
            {unreadCount > 0 && (
              <button type="button" onClick={handleMarkAllAsRead}>
                Mark all as read
              </button>
            )}
          </div>

          {loading ? (
            <div style={{ display: 'grid', placeItems: 'center', padding: '24px 0' }}>
              <Spinner size={22} />
            </div>
          ) : notifications.length === 0 ? (
            <p className="notification-bell__empty">No notifications yet.</p>
          ) : (
            <ul className="notification-bell__list">
              {notifications.map((notification) => (
                <li
                  key={notification.id}
                  className={`notification-bell__item${notification.read ? '' : ' unread'}`}
                  onClick={() => handleItemClick(notification)}
                >
                  <span className="notification-bell__icon">{TYPE_ICON[notification.type] || '🔔'}</span>
                  <div className="notification-bell__body">
                    <span className="notification-bell__title">{notification.title}</span>
                    <span className="notification-bell__message">{notification.message}</span>
                    <span className="notification-bell__time">{timeAgo(notification.createdAt)}</span>
                  </div>
                  <button
                    type="button"
                    className="notification-bell__delete"
                    aria-label="Delete notification"
                    onClick={(event) => handleDelete(event, notification)}
                  >
                    ×
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
