package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import model.AuthService;

public class RegisterFormController extends BaseController {

    @FXML private TextField truongHoTen;
    @FXML private TextField truongSoDienThoai;
    @FXML private TextField truongEmail;
    @FXML private PasswordField truongMatKhau;
    @FXML private PasswordField truongXacNhanMatKhau;
    
    @FXML private ToggleGroup nhomVaiTro;
    @FXML private RadioButton rdoChuHo;
    @FXML private RadioButton rdoChuNha;
    @FXML private javafx.scene.layout.VBox thongTinChuHo;
    @FXML private javafx.scene.layout.VBox thongTinChuNha;
    
    @FXML private CheckBox hopDongY;
    @FXML private Button nutDangKy;

    private final AuthService authService = new AuthService();

    @Override
    public void khoiTao() {
        // Vẫn giữ tính năng click vào thẻ là tích chọn RadioButton
    }

    @FXML
    private void chonVaiTroChuHo() {
        rdoChuHo.setSelected(true);
    }

    @FXML
    private void chonVaiTroChuNha() {
        rdoChuNha.setSelected(true);
    }

    @FXML
    private void xuLyDangKy(ActionEvent event) {
        String thongBaoLoi = kiemTraHopLe();
        if (thongBaoLoi != null) {
            hienThongBaoLoi("Thông tin không hợp lệ", thongBaoLoi);
            return;
        }

        String hoTen = truongHoTen.getText().trim();
        String sdt = truongSoDienThoai.getText().trim();
        String email = truongEmail.getText().trim();
        String matKhau = truongMatKhau.getText();
        String vaiTro = (rdoChuHo != null && rdoChuHo.isSelected()) ? "Chủ_họ" : "Chủ_nhà";

        AuthService.KetQuaDangNhap ketQua = authService.dangKy(hoTen, sdt, email, matKhau, vaiTro);

        if (ketQua.isThanhCong()) {
            hienThongBaoThanhCong(ketQua.getThongBao());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HoanThienThongTin.fxml"));
                Stage stage = (Stage) nutDangKy.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Hoàn thiện thông tin - Sơ Đồ Gia Phả");
            } catch (Exception e) {
                hienThongBaoLoi("Lỗi", "Không thể nạp màn hình Hoàn thiện thông tin.");
                e.printStackTrace();
            }
        } else {
            hienThongBaoLoi("Đăng ký thất bại", ketQua.getThongBao());
        }
    }

    private String kiemTraHopLe() {
        if (truongHoTen.getText().trim().isEmpty()) return "Họ tên không được để trống.";
        String sdt = truongSoDienThoai.getText().trim();
        if (sdt.isEmpty()) return "Số điện thoại không được để trống.";
        if (!sdt.matches("^(0|\\+84)[0-9]{8,10}$")) return "Số điện thoại không đúng định dạng.";
        if (truongMatKhau.getText().isEmpty()) return "Mật khẩu không được để trống.";
        if (truongMatKhau.getText().length() < 6) return "Mật khẩu phải từ 6 ký tự trở lên.";
        if (!truongMatKhau.getText().equals(truongXacNhanMatKhau.getText())) {
            return "Mật khẩu xác nhận không khớp.";
        }
        if (!hopDongY.isSelected()) return "Bạn cần đồng ý với điều khoản dịch vụ.";
        return null;
    }

    @FXML
    private void xuLyQuayLaiDangNhap(ActionEvent event) {
        if (AuthLayoutController.getInstance() != null) {
            AuthLayoutController.getInstance().taiFormDangNhap();
        }
    }

    @FXML
    private void xuLyHienMatKhau(ActionEvent event) {
    }
}
