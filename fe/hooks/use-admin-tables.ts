import { useState, useCallback } from "react";
import { useAuth } from "@/contexts/auth-context";
import { Table } from "@/types";
import { extractErrorMessage } from "@/lib/utils";
import { toast } from "sonner";

export interface TableFormState {
  tableNumber: string;
  capacity: string;
  status: string;
}

export const emptyTableForm: TableFormState = {
  tableNumber: "",
  capacity: "4", // default: 4
  status: "AVAILABLE",
};

export function useAdminTables() {
  const { user } = useAuth();

  const [isLoadingTables, setIsLoadingTables] = useState(false);
  const [tables, setTables] = useState<Table[]>([]);
  const [tableForm, setTableForm] = useState<TableFormState>(emptyTableForm);
  const [editingTableId, setEditingTableId] = useState<number | null>(null);
  const [isSavingTable, setIsSavingTable] = useState(false);

  const loadTables = useCallback(async () => {
    if (!user?.token) return;
    setIsLoadingTables(true);
    try {
      const res = await fetch("/api/admin/tables", {
        headers: { Authorization: `Bearer ${user.token}` },
      });
      const data = await res.json();
      if (!res.ok) throw new Error(extractErrorMessage(data, "Lỗi tải danh sách bàn"));
      setTables(data);
    } catch (err: any) {
      toast.error(err.message || "Lỗi kết nối server");
    } finally {
      setIsLoadingTables(false);
    }
  }, [user?.token]);

  const handleTableSubmit = async () => {
    const capacityInt = parseInt(tableForm.capacity);
    if (!tableForm.tableNumber.trim() || isNaN(capacityInt) || capacityInt < 1) {
      toast.warning("Tên bàn và số ghế không hợp lệ");
      return;
    }

    setIsSavingTable(true);
    try {
      const isEdit = editingTableId !== null;
      const url = isEdit ? `/api/admin/tables/${editingTableId}` : "/api/admin/tables";
      
      const payload = {
        tableNumber: tableForm.tableNumber.trim(),
        capacity: capacityInt,
        status: tableForm.status
      };

      const res = await fetch(url, {
        method: isEdit ? "PUT" : "POST",
        headers: { 
          Authorization: `Bearer ${user?.token}`, 
          "Content-Type": "application/json" 
        },
        body: JSON.stringify(payload)
      });

      const data = await res.json();
      if (!res.ok) throw new Error(extractErrorMessage(data, "Lỗi lưu thông tin bàn"));

      toast.success(isEdit ? "Cập nhật bàn thành công!" : "Thêm bàn mới thành công!");
      setEditingTableId(null);
      setTableForm(emptyTableForm);
      loadTables();
    } catch (err: any) {
      toast.error(err.message);
    } finally {
      setIsSavingTable(false);
    }
  };

  const handleDeleteTable = async (id: number) => {
    if (!confirm("Bạn có chắc chắn muốn xóa bàn này?")) return;
    try {
      const res = await fetch(`/api/admin/tables/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${user?.token}` }
      });
      if (!res.ok) throw new Error("Xóa bàn thất bại");
      toast.success("Đã xóa bàn");
      loadTables();
    } catch (err: any) {
      toast.error(err.message);
    }
  };

  return {
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
  };
}