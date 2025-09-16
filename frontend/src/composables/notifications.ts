import { ref, reactive } from 'vue';

interface Notification {
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  timeout?: number;
}

const notifications = ref<Notification[]>([]);

export function useNotifications() {
  const showNotification = (notification: Notification) => {
    const id = Date.now();
    const newNotification = {
      ...notification,
      id,
      timeout: notification.timeout || 5000
    };
    
    notifications.value.push(newNotification as any);
    
    // Auto-dismiss after timeout
    setTimeout(() => {
      notifications.value = notifications.value.filter(n => (n as any).id !== id);
    }, newNotification.timeout);
  };
  
  const success = (message: string, timeout = 3000) => {
    showNotification({ message, type: 'success', timeout });
  };
  
  const error = (message: string, timeout = 5000) => {
    showNotification({ message, type: 'error', timeout });
  };
  
  const info = (message: string, timeout = 3000) => {
    showNotification({ message, type: 'info', timeout });
  };
  
  const warning = (message: string, timeout = 4000) => {
    showNotification({ message, type: 'warning', timeout });
  };
  
  return {
    notifications,
    showNotification,
    success,
    error,
    info,
    warning
  };
}

// Create a global notifications instance
export const globalNotifications = reactive(useNotifications());
