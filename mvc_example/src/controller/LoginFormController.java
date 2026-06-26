package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginFormController extends BaseController {

    @FXML private TextField truongSoDienThoai;
    @FXML private PasswordField truongMatKhau;
    @FXML private CheckBox hopGhiNho;
    @FXML private Button nutDangNhap;

    private final model.AuthService authService = new model.AuthService();

    @Override
    public void khoiTao() {
        // Khởi tạo các giá trị ban đầu cho form Đăng nhập nếu cần
    }

    @FXML
    private void xuLyDangNhap(ActionEvent event) {
        String soDienThoai = truongSoDienThoai.getText().trim();
        String matKhau = truongMatKhau.getText();

        if (soDienThoai.isEmpty() || matKhau.isEmpty()) {
            hienThongBaoLoi("Đăng nhập thất bại", "Vui lòng nhập đầy đủ số điện thoại và mật khẩu.");
            return;
        }

        model.AuthService.KetQuaDangNhap ketQua = authService.dangNhap(soDienThoai, matKhau);

        if (ketQua.isThanhCong()) {
            hienThongBaoThanhCong(ketQua.getThongBao());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainDashboard.fxml"));
                Stage stage = (Stage) nutDangNhap.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Trang chủ Cây Gia Phả - Sơ Đồ Gia Phả");
            } catch (Exception e) {
                hienThongBaoLoi("Lỗi", "Không thể nạp màn hình chính.");
                e.printStackTrace();
            }
        } else {
            hienThongBaoLoi("Đăng nhập thất bại", ketQua.getThongBao());
        }
    }

    @FXML
    private void xuLyChuyenSangDangKy(ActionEvent event) {
        if (AuthLayoutController.getInstance() != null) {
            AuthLayoutController.getInstance().taiFormDangKy();
        }
    }

    @FXML
    private void xuLyQuenMatKhau(ActionEvent event) {
        hienThongBaoThongTin("Quên mật khẩu", "Tính năng cấp lại mật khẩu đang được phát triển.");
    }

    @FXML
    private void xuLyHienMatKhau(ActionEvent event) {
        // TODO: Chức năng ẩn hiện mật khẩu
    }
}
