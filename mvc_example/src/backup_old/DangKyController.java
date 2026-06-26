package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * DangKyController - Điều khiển màn hình Tạo tài khoản mới
 *
 * Nguyên tắc OOP:
 * - Kế thừa  : extends BaseController
 * - Đa hình  : implements Initializable
 * - Đóng gói : field private, validate riêng
 *
 * TODO Backend:
 * - Kết nối IUserRepository.dangKy() để lưu tài khoản
 * - Hash mật khẩu (qua pgcrypto trên Supabase)
 */
public class DangKyController extends BaseController implements Initializable {

    // ===== FXML =====
    @FXML private ImageView anhCuTo;
    @FXML private TextField truongHoTen;
    @FXML private TextField truongEmail;
    @FXML private PasswordField truongMatKhau;
    @FXML private PasswordField truongXacNhanMatKhau;
    @FXML private ToggleGroup nhomVaiTro;   // Phải có để khớp fx:define trong FXML
    @FXML private RadioButton rdoChuHo;
    @FXML private RadioButton rdoChuNha;
    @FXML private CheckBox hopDongY;
    @FXML private Button nutDangKy;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        khoiTao();
    }

    @Override
    public void khoiTao() {
        // Nhấn Enter ở trường cuối → Submit
        truongXacNhanMatKhau.setOnAction(e -> xuLyDangKy(null));
    }

    // =========================================================
    // SỰ KIỆN
    // =========================================================

    @FXML
    private void xuLyDangKy(ActionEvent event) {
        // [GIAO DIỆN] Validate trước khi gọi backend
        String thongBaoLoi = kiemTraHopLe();
        if (thongBaoLoi != null) {
            hienThongBaoLoi("Thông tin không hợp lệ", thongBaoLoi);
            return;
        }

        // [BACKEND] TODO: Gọi Service đăng ký
        // RegisterService.dangKy(hoTen, email, matKhau, vaiTro)
        System.out.println("[DangKyController] Đăng ký tài khoản...");
        // hienThongBaoThanhCong("Tạo tài khoản thành công!\n(TODO: Kết nối backend)");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HoanThienThongTin.fxml"));
            Stage stage = (Stage) nutDangKy.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Hoàn thiện thông tin - Sơ Đồ Gia Phả");
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi", "Không thể chuyển sang màn hình Hoàn thiện thông tin.");
            e.printStackTrace();
        }
    }

    @FXML
    private void xuLyHienMatKhau(ActionEvent event) {
        // TODO: Toggle hiện/ẩn mật khẩu
    }

    @FXML
    private void xuLyDieuKhoan(ActionEvent event) {
        hienThongBaoThongTin("Điều khoản dịch vụ", "Nội dung điều khoản đang được cập nhật.");
    }

    @FXML
    private void xuLyChinhSach(ActionEvent event) {
        hienThongBaoThongTin("Chính sách bảo mật", "Nội dung chính sách đang được cập nhật.");
    }

    @FXML
    private void xuLyQuayLaiDangNhap(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/Login.fxml")
            );
            Stage stage = (Stage) nutDangKy.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng nhập - Sơ Đồ Gia Phả");
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi", "Không thể quay lại màn hình đăng nhập.");
        }
    }

    // =========================================================
    // VALIDATE (Đóng gói logic kiểm tra)
    // =========================================================

    private String kiemTraHopLe() {
        if (truongHoTen.getText().trim().isEmpty())
            return "Vui lòng nhập họ và tên!";
        if (truongHoTen.getText().trim().length() < 2)
            return "Họ và tên phải có ít nhất 2 ký tự!";
        if (truongMatKhau.getText().isEmpty())
            return "Vui lòng nhập mật khẩu!";
        if (truongMatKhau.getText().length() < 6)
            return "Mật khẩu phải có ít nhất 6 ký tự!";
        if (!truongMatKhau.getText().equals(truongXacNhanMatKhau.getText()))
            return "Mật khẩu xác nhận không khớp!";
        if (!hopDongY.isSelected())
            return "Vui lòng đồng ý với Điều khoản dịch vụ!";
        return null; // Hợp lệ
    }
}
