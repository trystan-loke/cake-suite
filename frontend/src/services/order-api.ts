import { auth } from '../firebase';

// Get the API base URL from environment variables
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * API service for handling HTTP requests
 */
export async function apiRequest<T>(
  url: string,
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET',
  body?: any
): Promise<T> {
  // Get current user's ID token for authentication
  const token = await auth.currentUser?.getIdToken();
  
  if (!token) {
    throw new Error('Authentication required');
  }
  
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
  
  const options: RequestInit = {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  };

  const response = await fetch(`${API_BASE_URL}${url}`, options);  
  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }
  
  // For DELETE requests that return 204 No Content
  if (response.status === 204) {
    return {} as T;
  }
  
  return await response.json() as T;
}

// Type definitions for Order
export interface Order {
  id?: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  orderSummary: string;
  orderDetails: string;
  totalAmount: number | null;
  deposit: number | null;
  tip: number;
  isDelivery: boolean;
  deliveryFee: number;
  pickupDate: string;
  orderDate: string;
  status: string;
  images: { url: string; path: string }[];
}

// Order API Service
export const OrderAPI = {
  getAllOrders: (from?: string, to?: string) => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const queryString = params.toString();
    return apiRequest<Order[]>(queryString ? `/orders?${queryString}` : '/orders');
  },
  
  getOrderById: (id: string) => apiRequest<Order>(`/orders/${id}`),
  
  createOrder: (order: Order) => apiRequest<Order>('/orders', 'POST', order),
  
  updateOrder: (id: string, order: Order) => apiRequest<Order>(`/orders/${id}`, 'PUT', order),
  
  deleteOrder: (id: string) => apiRequest<void>(`/orders/${id}`, 'DELETE'),

  exportOrders: async (from?: string, to?: string): Promise<void> => {
    const params = new URLSearchParams();
    if (from) params.append('from', new Date(from).toISOString());
    if (to) params.append('to', new Date(to).toISOString());
    
    const token = await auth.currentUser?.getIdToken();
    const response = await fetch(
      `${API_BASE_URL}/orders/export?${params.toString()}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    
    if (!response.ok) throw new Error('Export failed');
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `orders_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }
};
