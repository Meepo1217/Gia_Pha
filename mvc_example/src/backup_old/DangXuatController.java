package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.AuthService;
import model.IAuthService;
import model.User;
import model.UserSession;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

// DangXuatController - Điều khiển màn hình Tài khoản & Đăng xuất
public class DangXuatController extends BaseController implements Initializable {

    // ===== FXML — CỘT TRÁI =====
    @FXML private ImageView anhDaiDien;
    @FXML private Label tenNguoiDung;
    @FXML private Label soDienThoaiHienThi;
    @FXML private Label soSoDo;
    @FXML private Label soThanhVien;
    @FXML private Label soNgayDung;

    // ===== FXML — CỘT PHẢI =====
    @FXML private Label hienHoTen;
    @FXML private Label hienSoDienThoai;
    @FXML private Label hienEmail;
    @FXML private Label hienNgonNgu;
    @FXML private Label thoiGianDangNhap;
    @FXML private Button nutDangXuat;

    // ===== SERVICE (Tầng nghiệp vụ) =====
    // Dùng Interface → Tính Đa Hình
    private final IAuthService authService = new AuthService();

    // ===== KHỞI TẠO =====

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        khoiTao();
    }

    /**
     * Ghi đè phương thức trừu tượng từ BaseController (Kế Thừa)
     * Controller chỉ LẤY DỮ LIỆU từ Model → hiển thị lên View
     */
    @Override
    public void khoiTao() {
        User nguoiDung = UserSession.getInstance().getNguoiDungHienTai();

        if (nguoiDung != null) {
            // Có session thật → hiển thị dữ liệu thật
            hienThiThongTinNguoiDung(nguoiDung);
        } else {
            // Chưa đăng nhập (xem thử UI) → hiển thị dữ liệu mẫu
            hienThiDuLieuMau();
        }
    }

    /** Dữ liệu mẫu để xem trước giao diện (chưa đăng nhập thật) */
    private void hienThiDuLieuMau() {
        tenNguoiDung.setText("Nguyễn Văn A");
        soDienThoaiHienThi.setText("📞  0912 345 678");
        soSoDo.setText("2");
        soThanhVien.setText("15");
        soNgayDung.setText("30");
        hienHoTen.setText("Nguyễn Văn A");
        hienSoDienThoai.setText("0912 345 678");
        hienEmail.setText("nguyen.a@email.com");
        hienNgonNgu.setText("Tiếng Việt (vi)");
        thoiGianDangNhap.setText(
            java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))
        );
    }

    // =========================================================
    // [PHẦN GIAO DIỆN] — Hiển thị dữ liệu từ Model lên View
    // Controller chỉ đọc từ UserSession, KHÔNG gọi DB trực tiếp
    // =========================================================

    /**
     * Hiển thị thông tin User lên các Label trong View
     * Dữ liệu lấy từ UserSession (đã được AuthService lưu khi đăng nhập)
     */
    private void hienThiThongTinNguoiDung(User nguoiDung) {
        // --- Cột trái ---
        tenNguoiDung.setText(nguoiDung.getHoTen());
        soDienThoaiHienThi.setText("📞  " + nguoiDung.getSoDienThoai());

        // Thống kê (TODO: lấy từ Repository sau)
        soSoDo.setText("--");
        soThanhVien.setText("--");
        soNgayDung.setText("--");

        // --- Cột phải ---
        hienHoTen.setText(nguoiDung.getHoTen());
        hienSoDienThoai.setText(nguoiDung.getSoDienThoai());

        // Email có thể null
        String email = nguoiDung.getEmail();
        hienEmail.setText((email != null && !email.isEmpty()) ? email : "(Chưa cập nhật)");

        // Ngôn ngữ
        String ngonNgu = "vi".equals(nguoiDung.getNgonNguMacDinh())
                ? "Tiếng Việt (vi)" : "English (en)";
        hienNgonNgu.setText(ngonNgu);

        // Thời gian đăng nhập
        thoiGianDangNhap.setText(
            java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))
        );
    }

    // =========================================================
    // [PHẦN SỰ KIỆN] — Nhận sự kiện từ View → Gọi Service
    // Controller KHÔNG tự xử lý logic — ủy quyền cho Service
    // =========================================================

    /**
     * [SỰ KIỆN] Người dùng bấm "ĐĂNG XUẤT"
     *
     * Controller làm:
     *  1. Hiện hộp thoại xác nhận (UI logic)
     *  2. Nếu đồng ý → gọi authService.dangXuat() (Backend)
     *  3. Chuyển về màn hình đăng nhập (UI logic)
     */
    @FXML
    private void xuLyDangXuat(ActionEvent event) {
        // [GIAO DIỆN] Hỏi xác nhận
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận đăng xuất");
        xacNhan.setHeaderText("Bạn có chắc muốn đăng xuất?");
        xacNhan.setContentText(
            "Phiên làm việc hiện tại sẽ kết thúc.\n" +
            "Bạn cần đăng nhập lại để tiếp tục."
        );

        ButtonType nutDongY = new ButtonType("Đăng xuất", ButtonBar.ButtonData.OK_DONE);
        ButtonType nutHuy   = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        xacNhan.getButtonTypes().setAll(nutDongY, nutHuy);

        xacNhan.showAndWait().ifPresent(ketQua -> {
            if (ketQua == nutDongY) {

                // [BACKEND] Gọi Service xử lý đăng xuất
                // Controller KHÔNG tự xóa session — ủy quyền cho AuthService
                authService.dangXuat();

                // [GIAO DIỆN] Chuyển về màn hình đăng nhập
                chuyenVeManhHinhDangNhap();
            }
        });
    }

    /**
     * [SỰ KIỆN] Bấm "Chỉnh sửa thông tin"
     * TODO Backend: Mở màn hình ChinhSuaThongTin.fxml
     */
    @FXML
    private void xuLyChinhSua(ActionEvent event) {
        hienThongBaoThongTin("Đang phát triển",
                "Tính năng chỉnh sửa thông tin đang được phát triển.");
    }

    /**
     * [SỰ KIỆN] Bấm "Đổi mật khẩu"
     * TODO Backend: Mở màn hình DoiMatKhau.fxml
     */
    @FXML
    private void xuLyDoiMatKhau(ActionEvent event) {
        hienThongBaoThongTin("Đang phát triển",
                "Tính năng đổi mật khẩu đang được phát triển.");
    }

    /**
     * [SỰ KIỆN] Bấm "Quay lại ứng dụng"
     * TODO: Chuyển về MainView.fxml khi có màn hình chính
     */
    @FXML
    private void xuLyQuayLai(ActionEvent event) {
        System.out.println("[DangXuatController] Quay lại màn hình chính...");
        // TODO: stage.setScene(new Scene(loader.load("/view/MainView.fxml")))
    }

    // =========================================================
    // PHƯƠNG THỨC HỖ TRỢ UI (private)
    // =========================================================

    /**
     * Chuyển về màn hình đăng nhập
     * Gọi sau khi đăng xuất thành công
     */
    private void chuyenVeManhHinhDangNhap() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/Login.fxml")
            );
            Stage stage = (Stage) nutDangXuat.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Đăng nhập - Sơ Đồ Gia Phả");
        } catch (Exception e) {
            System.err.println("[DangXuatController] Lỗi chuyển màn hình: " + e.getMessage());
            hienThongBaoLoi("Lỗi", "Không thể quay về màn hình đăng nhập.");
        }
    }
}
