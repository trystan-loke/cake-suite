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
  imagePaths: string[] | null;
}

// Order API Service
export const OrderAPI = {
  getAllOrders: (from?: string) => apiRequest<Order[]>(from ? `/orders?from=${from}` : '/orders'),
  
  getOrderById: (id: string) => apiRequest<Order>(`/orders/${id}`),
  
  createOrder: (order: Order) => apiRequest<Order>('/orders', 'POST', order),
  
  updateOrder: (id: string, order: Order) => apiRequest<Order>(`/orders/${id}`, 'PUT', order),
  
  deleteOrder: (id: string) => apiRequest<void>(`/orders/${id}`, 'DELETE'),
};
