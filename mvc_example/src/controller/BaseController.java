package controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * BaseController - Lớp cha dùng chung cho mọi Controller
 *
 * Nguyên tắc OOP: TÍNH KẾ THỪA (Inheritance)
 * - Các Controller con (LoginController, RegisterController, v.v.)
 *   kế thừa các phương thức hiển thị thông báo từ lớp này
 * - Tránh lặp code giống nhau ở nhiều màn hình
 */
public abstract class BaseController {

    /**
     * Hiển thị thông báo lỗi
     * Dùng chung cho mọi màn hình trong ứng dụng
     */
    protected void hienThongBaoLoi(String tieuDe, String noiDung) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo thành công
     */
    protected void hienThongBaoThanhCong(String noiDung) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo thông tin
     */
    protected void hienThongBaoThongTin(String tieuDe, String noiDung) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo cảnh báo
     */
    protected void hienThongBaoCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * Phương thức khởi tạo — class con PHẢI ghi đè (Override)
     * Nguyên tắc OOP: TÍNH ĐA HÌNH — mỗi màn hình có initialize() riêng
     */
    public abstract void khoiTao();
}
