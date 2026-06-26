package view.component;

/**
 * ISearchablePanel - Giao diện chuẩn mực (Interface) theo thiết kế OOP
 * Định nghĩa hành vi tìm kiếm chung cho các Custom Panel (Cây gia phả, Danh sách, Thống kê...)
 * Giúp Controller Trang chủ tương tác thông qua tính Đa hình (Polymorphism)
 */
public interface ISearchablePanel {
    /**
     * Tìm kiếm thành viên trên giao diện hiện tại
     * @param tuKhoa từ khóa họ tên cần tìm
     * @return true nếu tìm thấy/có kết quả, false nếu không tìm thấy
     */
    boolean timKiem(String tuKhoa);
}
