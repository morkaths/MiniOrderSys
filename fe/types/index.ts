export interface Product {
  id: number;
  name: string;
  description: string;
  price: string;
  active: boolean;
}

export interface AdminProductPayload {
  name: string;
  description: string;
  price: number;
  active: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  username: string;
  role: string;
}

export interface VoucherDrawResponse {
  message: string;
  code?: string;
  name?: string;
  discountPercent?: string;
}

export interface AdminVoucher {
  id: number;
  code: string;
  name: string;
  description: string;
  discountPercent: number;
  active: boolean;
}

export interface AdminVoucherPayload {
  code: string;
  name: string;
  description: string;
  discountPercent: number;
  active: boolean;
}

export interface OrderItemPayload {
  productId: number;
  quantity: number;
}

export interface OrderCreatePayload {
  tableNumber: string;
  items: OrderItemPayload[];
  voucherCode?: string;
  paymentMethod: 'CASH' | 'VNPAY';
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderResponse {
  orderId: number;
  tableNumber: string;
  status: string;
  orderedBy: string;
  totalAmount: number;
  discountPercent: number | null;
  discountAmount: number | null;
  createdAt: string;
  items: OrderItemResponse[];
}

export interface InvoiceResponse {
  id: number;
  orderId: number;
  tableNumber: string;
  totalAmount: number;
  paymentMethod: 'CASH' | 'TRANSFER' | 'VNPAY';
  paymentStatus: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  paidAt: string | null;
  createdAt: string;
  paymentUrl: string | null;
}

export interface TableResponse {
  id: number;
  tableNumber: string;
  capacity: number;
  status: string;
}

export interface Table { // display on UI
  id: number;
  tableNumber: string;
  capacity: number;
  status: string; // Enum từ backend, ví dụ: 'AVAILABLE', 'OCCUPIED', 'OUT_OF_SERVICE'
}

export interface AdminTablePayload { // TableRequest
  tableNumber: string;
  capacity: number;
  status?: string;
}

export interface ApiError {
  message?: string;
  error?: string;
}
