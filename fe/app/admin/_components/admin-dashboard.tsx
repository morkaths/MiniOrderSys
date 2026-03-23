"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/auth-context";
import { 
  AdminProductPayload, 
  AdminVoucher, 
  AdminVoucherPayload, 
  Product 
} from "@/types";
import { formatVnd, extractErrorMessage, toMoneyValue } from "@/lib/utils";
import { toast } from "sonner";
import { Coffee, Ticket, Settings, ArrowLeft, LogOut, LayoutList, History } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { useAdminProducts } from "@/hooks/use-admin-products";
import { useAdminVouchers } from "@/hooks/use-admin-vouchers";
import { useAdminHistory } from "@/hooks/use-admin-history";
import { emptyTableForm, useAdminTables } from "@/hooks/use-admin-tables";

import AdminStatsCards from "./admin-stats-cards";
import DrinkManagement from "./drink-management";
import VoucherManagement from "./voucher-management";
import OrderManagement from "./order-management";
import InvoiceHistory from "./invoice-history";
import TableManagement from "./table-management";

type AdminTab = "drinks" | "vouchers" | "orders" | "history" | "tables";

interface ProductFormState {
  name: string;
  description: string;
  price: string;
  active: boolean;
}

interface VoucherFormState {
  code: string;
  name: string;
  description: string;
  discountPercent: string;
  active: boolean;
}

const emptyProductForm: ProductFormState = {
  name: "",
  description: "",
  price: "",
  active: true,
};

const emptyVoucherForm: VoucherFormState = {
  code: "",
  name: "",
  description: "",
  discountPercent: "10",
  active: true,
};

export default function AdminDashboard() {
  const { user, isLoggedIn, logout, hasHydrated } = useAuth();
  const router = useRouter();

  const [activeTab, setActiveTab] = useState<AdminTab>("orders");
  
  const {
    isLoadingProducts,
    products,
    productForm,
    setProductForm,
    editingProductId,
    setEditingProductId,
    isSavingProduct,
    loadProducts,
    handleProductSubmit,
    handleDeleteProduct,
  } = useAdminProducts();

  const {
    isLoadingVouchers,
    vouchers,
    voucherForm,
    setVoucherForm,
    editingVoucherId,
    setEditingVoucherId,
    isSavingVoucher,
    loadVouchers,
    handleVoucherSubmit,
    handleDeleteVoucher,
  } = useAdminVouchers();

  const {
   isLoadingTables,
   tables,
   tableForm,
   setTableForm,
   editingTableId,
   setEditingTableId,
   isSavingTable,
   loadTables,
   handleTableSubmit,
   handleDeleteTable,
   } = useAdminTables();

  const { 
    invoices, 
    fetchHistory,
    isLoading: isLoadingHistory 
  } = useAdminHistory();

  const loadAdminData = useCallback(async () => {
    // Tải đồng thời các dữ liệu admin
    await Promise.all([
       loadProducts(),
       loadVouchers(),
       fetchHistory(),
       loadTables()
    ]);
  }, [loadProducts, loadVouchers, fetchHistory, loadTables]);

  useEffect(() => {
    if (hasHydrated && (!isLoggedIn || user?.role !== "ADMIN")) {
      router.replace("/");
    } else if (hasHydrated && isLoggedIn) {
      loadAdminData();
    }
  }, [hasHydrated, isLoggedIn, user?.role, router, loadAdminData]);

  const stats = useMemo(() => {
    const today = new Date().toLocaleDateString("en-CA"); // YYYY-MM-DD
    
    const revenueToday = invoices
      .filter(inv => 
        inv.paymentStatus === "COMPLETED" && 
        new Date(inv.createdAt).toLocaleDateString("en-CA") === today
      )
      .reduce((sum, inv) => sum + inv.totalAmount, 0);

    return {
      activeProducts: products.filter(p => p.active).length,
      activeVouchers: vouchers.filter(v => v.active).length,
      revenueToday
    };
  }, [products, vouchers, invoices]);

  if (!hasHydrated) return null;
  if (!isLoggedIn || user?.role !== "ADMIN") return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4">
       <div className="h-12 w-12 border-4 border-primary border-t-transparent animate-spin rounded-full" />
       <p className="font-bold text-muted-foreground italic">Đang xác thực quyền Admin...</p>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
           <h1 className="text-3xl font-black tracking-tighter">Hệ thống quản trị</h1>
        </div>
      </div>

      {/* Stats Summary */}
      <AdminStatsCards
        productCount={products.length}
        activeProductCount={stats.activeProducts}
        voucherCount={vouchers.length}
        activeVoucherCount={stats.activeVouchers}
        todayRevenue={stats.revenueToday}
      />

      {/* Tabs Control */}
      <div className="flex items-center gap-2 p-1.5 bg-muted/50 rounded-2xl w-fit border border-muted flex-wrap">
         <Button 
            variant={activeTab === "orders" ? "default" : "ghost"} 
            className={`rounded-xl px-6 font-bold ${activeTab === "orders" ? "shadow-md" : ""}`}
            onClick={() => setActiveTab("orders")}
         >
            <LayoutList className="mr-2 h-4 w-4" />
            Đơn hàng
         </Button>
         <Button 
            variant={activeTab === "history" ? "default" : "ghost"} 
            className={`rounded-xl px-6 font-bold ${activeTab === "history" ? "shadow-md" : ""}`}
            onClick={() => setActiveTab("history")}
         >
            <History className="mr-2 h-4 w-4" />
            Lịch sử
         </Button>
         <Button 
            variant={activeTab === "drinks" ? "default" : "ghost"} 
            className={`rounded-xl px-6 font-bold ${activeTab === "drinks" ? "shadow-md" : ""}`}
            onClick={() => setActiveTab("drinks")}
         >
            <Coffee className="mr-2 h-4 w-4" />
            Đồ uống
         </Button>
         <Button 
            variant={activeTab === "vouchers" ? "default" : "ghost"} 
            className={`rounded-xl px-6 font-bold ${activeTab === "vouchers" ? "shadow-md" : ""}`}
            onClick={() => setActiveTab("vouchers")}
         >
            <Ticket className="mr-2 h-4 w-4" />
            Vouchers
         </Button>
         <Button 
            variant={activeTab === "tables" ? "default" : "ghost"} 
            className={`rounded-xl px-6 font-bold ${activeTab === "tables" ? "shadow-md" : ""}`}
            onClick={() => setActiveTab("tables")}
         >
            <Ticket className="mr-2 h-4 w-4" />
            Quản lý bàn
         </Button>
      </div>

      {/* Main Content Areas */}
      <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
         {activeTab === "orders" && (
            <OrderManagement token={user?.token} />
         )}
         {activeTab === "history" && (
            <InvoiceHistory />
         )}
         {activeTab === "drinks" && (
            <DrinkManagement
               products={products}
               isLoading={isSavingProduct}
               form={productForm}
               onFormChange={(val) => setProductForm(prev => ({ ...prev, ...val }))}
               onSubmit={handleProductSubmit}
               onEdit={(p) => {
                  setEditingProductId(p.id);
                  setProductForm({
                     name: p.name,
                     description: p.description,
                     price: String(toMoneyValue(p.price)),
                     active: p.active
                  });
               }}
               onDelete={handleDeleteProduct}
               onReset={() => { setEditingProductId(null); setProductForm(emptyProductForm); }}
               editingId={editingProductId}
            />
         )}
         {activeTab === "vouchers" && (
            <VoucherManagement
               vouchers={vouchers}
               isLoading={isSavingVoucher}
               form={voucherForm}
               onFormChange={(val) => setVoucherForm(prev => ({ ...prev, ...val }))}
               onSubmit={handleVoucherSubmit}
               onEdit={(v) => {
                  setEditingVoucherId(v.id);
                  setVoucherForm({
                     code: v.code,
                     name: v.name,
                     description: v.description,
                     discountPercent: String(v.discountPercent),
                     active: v.active
                  });
               }}
               onDelete={handleDeleteVoucher}
               onReset={() => { setEditingVoucherId(null); setVoucherForm(emptyVoucherForm); }}
               editingId={editingVoucherId}
            />
         )}
         {activeTab === "tables" && (
            <TableManagement
               tables={tables}
               isLoading={isSavingTable}
               form={tableForm}
               onFormChange={(val) => setTableForm(prev => ({ ...prev, ...val }))}
               onSubmit={handleTableSubmit}
               onEdit={(t) => {
                  setEditingTableId(t.id);
                  setTableForm({
                     tableNumber: t.tableNumber,
                     capacity: String(t.capacity),
                     status: t.status
                  });
               }}
               onDelete={handleDeleteTable}
               onReset={() => { setEditingTableId(null); setTableForm(emptyTableForm); }}
               editingId={editingTableId}
            />
         )}
      </div>
    </div>
  );
}
