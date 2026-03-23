// {project_dir}/fe/app/admin/_components/table-management.tsx
"use client";

import { Table } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Edit2, Trash2, LayoutGrid, X, Users } from "lucide-react";

export interface TableFormState {
  tableNumber: string;
  capacity: string;
  status: string;
}

interface TableManagementProps {
  tables: Table[];
  isLoading: boolean;
  form: TableFormState;
  onFormChange: (data: Partial<TableFormState>) => void;
  onSubmit: () => void;
  onEdit: (table: Table) => void;
  onDelete: (id: number) => void;
  onReset: () => void;
  editingId: number | null;
}

export default function TableManagement({
  tables,
  isLoading,
  form,
  onFormChange,
  onSubmit,
  onEdit,
  onDelete,
  onReset,
  editingId
}: TableManagementProps) {
  
  // Hàm phụ trợ để map trạng thái từ Backend sang tiếng Việt và màu sắc UI
  const getStatusDisplay = (status: string) => {
    switch (status) {
      case 'AVAILABLE': return { label: 'Sẵn sàng', variant: 'secondary' as const };
      case 'OCCUPIED': return { label: 'Đang có khách', variant: 'default' as const };
      case 'OUT_OF_SERVICE': return { label: 'Bảo trì', variant: 'destructive' as const };
      default: return { label: status, variant: 'outline' as const };
    }
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_1.5fr] items-start">
      {/* Cột Form thêm/sửa */}
      <Card className="border-none shadow-xl border-t-4 border-t-primary">
        <CardHeader className="pb-4">
          <div className="flex justify-between items-center bg-primary/5 rounded-xl px-4 py-2 mb-2">
             <CardTitle className="text-xl font-black">{editingId ? "Sửa thông tin bàn" : "Thêm bàn mới"}</CardTitle>
             {editingId && (
                <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full" onClick={onReset}>
                   <X className="h-4 w-4" />
                </Button>
             )}
          </div>
          <CardDescription>Quản lý danh sách bàn và chỗ ngồi cho khách.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>
             <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-muted-foreground">Mã bàn / Tên bàn</label>
                <Input 
                   value={form.tableNumber} 
                   onChange={e => onFormChange({ tableNumber: e.target.value })} 
                   placeholder="VD: Bàn 01, T-VIP..."
                   className="rounded-xl border-2 border-muted h-11"
                   maxLength={20}
                   required
                />
             </div>
             
             <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-muted-foreground">Số ghế (Sức chứa)</label>
                <Input 
                   type="number"
                   value={form.capacity} 
                   onChange={e => onFormChange({ capacity: e.target.value })} 
                   placeholder="VD: 4"
                   className="rounded-xl border-2 border-muted h-11 font-mono font-bold"
                   min="1"
                   required
                />
             </div>

             <div className="flex items-center gap-3 p-3 bg-muted/40 rounded-xl border border-muted/60 mt-6">
                <input 
                   type="checkbox" 
                   id="tableActive"
                   checked={form.status === 'AVAILABLE'} 
                   onChange={e => onFormChange({ status: e.target.checked ? 'AVAILABLE' : 'OUT_OF_SERVICE' })} 
                   className="h-5 w-5 accent-emerald-500 rounded-md"
                />
                <label htmlFor="tableActive" className="flex flex-col cursor-pointer select-none">
                   <span className="text-sm font-bold">Bàn đang mở (AVAILABLE)</span>
                   <span className="text-xs text-muted-foreground">Bỏ chọn nếu bàn đang hỏng hoặc cần bảo trì</span>
                </label>
             </div>

             <Button 
                className="w-full h-12 rounded-xl text-lg font-black shadow-lg shadow-primary/10 transition-transform active:scale-95 mt-4"
                disabled={isLoading}
                type="submit"
             >
                {isLoading ? "Đang lưu..." : editingId ? "Cập nhật ngay" : "Thêm bàn"}
             </Button>
          </form>
        </CardContent>
      </Card>

      {/* Cột hiển thị danh sách */}
      <div className="space-y-4">
         <div className="flex items-center gap-2 mb-2">
            <LayoutGrid className="h-5 w-5 text-primary" />
            <h3 className="text-xl font-bold uppercase tracking-tighter">Sơ đồ / Danh sách bàn ({tables.length})</h3>
         </div>
         
         <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-2 xl:grid-cols-3">
            {tables.map(t => {
               const isAvailable = t.status === 'AVAILABLE';
               const isOccupied = t.status === 'OCCUPIED';
               const statusConfig = getStatusDisplay(t.status);
               
               return (
               <Card key={t.id} className={`group overflow-hidden border-2 transition-all hover:shadow-lg ${isAvailable ? "border-emerald-100 bg-emerald-50/10" : isOccupied ? "border-amber-200 bg-amber-50/30" : "border-muted/60 grayscale-[50%]"}`}>
                  <CardContent className="p-4 flex flex-col h-full">
                     <div className="flex justify-between items-start mb-2">
                        <h4 className="font-bold text-lg leading-tight flex-1">{t.tableNumber}</h4>
                        <Badge variant={statusConfig.variant} className="text-[10px] h-5 px-1.5 uppercase font-black">
                           {statusConfig.label}
                        </Badge>
                     </div>
                     
                     <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-medium mb-4">
                        <Users className="h-3.5 w-3.5" />
                        <span>Tối đa {t.capacity} người</span>
                     </div>
                     
                     <div className="flex items-center justify-end mt-auto pt-3 border-t border-muted/60">
                        <div className="flex gap-1">
                           <Button variant="ghost" size="icon" className="h-8 w-8 rounded-lg text-blue-600 hover:bg-blue-50" onClick={() => onEdit(t)}>
                              <Edit2 className="h-4 w-4" />
                           </Button>
                           <Button variant="ghost" size="icon" className="h-8 w-8 rounded-lg text-rose-600 hover:bg-rose-50" onClick={() => onDelete(t.id)}>
                              <Trash2 className="h-4 w-4" />
                           </Button>
                        </div>
                     </div>
                  </CardContent>
               </Card>
            )})}
         </div>
         
         {tables.length === 0 && (
            <div className="text-center py-20 bg-muted/20 rounded-3xl border-2 border-dashed border-muted">
               <p className="text-muted-foreground text-sm font-medium">Chưa có bàn nào trong hệ thống.</p>
            </div>
         )}
      </div>
    </div>
  );
}