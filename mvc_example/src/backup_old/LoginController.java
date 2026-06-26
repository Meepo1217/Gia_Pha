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
import model.AuthService;
import model.AuthService.KetQuaDangNhap;
import model.IAuthService;

/**
 * LoginController - Điều khiển màn hình Đăng nhập
 *
 * Nguyên tắc OOP: TÍNH KẾA THỮA (Inheritance)
 * - extends BaseController: kế thừa các phương thức hiển thị thông báo
 *   (hienThongBaoLoi, hienThongBaoThanhCong)
 * - Không cần viết lại, dùng lại từ class cha!
 *
 * Nguyên tắc OOP: TÍNH ĐA HÌNH (Polymorphism)
 * - implements Initializable: JavaFX gọi initialize() đa hình
 * - Dùng IAuthService thay vì AuthService cụ thể
 */
public class LoginController extends BaseController implements Initializable {

    // ===== FXML COMPONENTS =====
    @FXML private TextField truongSoDienThoai;
    @FXML private PasswordField truongMatKhau;
    @FXML private CheckBox hopGhiNho;
    @FXML private Button nutDangNhap;
    @FXML private Button nutHienMatKhau;   // Nút hiện/ẩn mật khẩu
    @FXML private ImageView anhCuTo;        // Ảnh cụ tổ bên trái

    // ===== SERVICE INJECTION =====
    // Dùng IAuthService (interface) thay vì AuthService (class cụ thể)
    // → Tính Đa Hình: có thể hoán đổi bằng MockAuthService khi test
    private final IAuthService authService = new AuthService();

    // Trạng thái hiện/ẩn mật khẩu
    private boolean dangHienMatKhau = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Thiết lập ban đầu: cho phép Enter để đăng nhập
        truongMatKhau.setOnAction(event -> xuLyDangNhap(null));
        truongSoDienThoai.setOnAction(event -> truongMatKhau.requestFocus());
    }

    // Triển khai phương thức trừa tượng từ BaseController
    @Override
    public void khoiTao() {
        truongMatKhau.setOnAction(event -> xuLyDangNhap(null));
    }

    // =========================================================
    // SỰ KIỆN ĐĂNG NHẬP
    // =========================================================

    /**
     * Bước 4 trong Sequence Diagram: Bấm "Đăng nhập"
     * → Gọi AuthService.dangNhap()
     * → Nhận KetQuaDangNhap
     * → Cập nhật UI
     */
    @FXML
    private void xuLyDangNhap(ActionEvent event) {
        String soDienThoai = truongSoDienThoai.getText().trim();
        String matKhau = truongMatKhau.getText();

        // Disable nút, đổi text trong lúc chờ
        datTrangThaiDangXuLy(true);

        // Chạy trên luồng riêng để không đóng băng UI
        new Thread(() -> {

            // Gọi AuthService → Service gọi Repository → Repository gọi DB
            KetQuaDangNhap ketQua = authService.dangNhap(soDienThoai, matKhau);

            // Quay về luồng JavaFX để cập nhật UI
            javafx.application.Platform.runLater(() -> {
                datTrangThaiDangXuLy(false);

                if (ketQua.isThanhCong()) {
                    xuLyDangNhapThanhCong(ketQua);
                } else {
                    hienThongBaoLoi("Đăng nhập thất bại", ketQua.getThongBao());
                }
            });

        }).start();
    }

    /**
     * Xử lý sau khi đăng nhập thành công
     * Bước 11-13 trong Sequence Diagram
     */
    private void xuLyDangNhapThanhCong(KetQuaDangNhap ketQua) {
        System.out.println("[LoginController] Đăng nhập thành công: "
                + ketQua.getNguoiDung().getHoTen());

        hienThongBaoThanhCong(ketQua.getThongBao());

        // TODO: Chuyển sang màn hình chính FamilyTree.fxml
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/FamilyTree.fxml"));
        // Stage stage = (Stage) nutDangNhap.getScene().getWindow();
        // stage.setScene(new Scene(loader.load()));
    }

    // =========================================================
    // CÁC SỰ KIỆN KHÁC
    // =========================================================

    @FXML
    private void xuLyHienMatKhau(ActionEvent event) {
        // TODO: Toggle hiện/ẩn mật khẩu
        System.out.println("Toggle mật khẩu...");
    }

    @FXML
    private void xuLyQuenMatKhau(ActionEvent event) {
        hienThongBaoThongTin("Thông báo", "Tính năng đặt lại mật khẩu đang được phát triển.");
        // TODO: Mở màn hình ForgotPassword.fxml
    }

    @FXML
    private void xuLyTaoTaiKhoan(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/DangKy.fxml")
            );
            Stage stage = (Stage) nutDangNhap.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Tạo tài khoản - Sơ Đồ Gia Phả");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết ra console
            hienThongBaoLoi("Lỗi", "Không thể mở màn hình đăng ký:\n" + e.getMessage());
        }
    }

    // =========================================================
    // PHƯƠNG THỨC HỖ TRỢ UI
    // =========================================================

    /** Bật/tắt trạng thái đang xử lý */
    private void datTrangThaiDangXuLy(boolean dangXuLy) {
        nutDangNhap.setDisable(dangXuLy);
        nutDangNhap.setText(dangXuLy ? "Đang đăng nhập..." : "Đăng nhập");
    }

    // Kế THừA từ BaseController → KHÔNG cần viết lại hienThongBaoLoi()
    // và hienThongBaoThanhCong() — dùng thẳng từ class cha!
}
