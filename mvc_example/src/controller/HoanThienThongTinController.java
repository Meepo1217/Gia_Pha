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

public class HoanThienThongTinController extends BaseController implements Initializable {

    @FXML private ImageView anhChanDung;
    @FXML private Button nutTaiAnh;
    
    @FXML private TextField truongHoTen;
    @FXML private ToggleGroup nhomGioiTinh;
    @FXML private RadioButton rdoNam;
    @FXML private RadioButton rdoNu;
    
    @FXML private DatePicker truongNgaySinh;
    @FXML private DatePicker truongNgayMat;
    
    @FXML private TextField truongSoDienThoai;
    @FXML private TextField truongEmail;
    @FXML private TextField truongDiaChi;
    
    @FXML private TextField truongChaMe;
    @FXML private TextField truongVoChong;
    
    @FXML private Button nutLuu;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        khoiTao();
    }

    @Override
    public void khoiTao() {
        if (nutTaiAnh != null) {
            nutTaiAnh.setOnAction(e -> xuLyTaiAnh());
        }

        // Tự động điền Họ tên, Số điện thoại và Email vừa đăng ký ở trang trước
        model.User user = model.UserSession.getInstance().getNguoiDungHienTai();
        if (user != null) {
            if (truongHoTen != null && user.getHoTen() != null) {
                truongHoTen.setText(user.getHoTen());
            }
            if (truongSoDienThoai != null && user.getSoDienThoai() != null) {
                truongSoDienThoai.setText(user.getSoDienThoai());
            }
            if (truongEmail != null && user.getEmail() != null) {
                truongEmail.setText(user.getEmail());
            }
        }

        capNhatAnhAvatarMacDinh();
        if (nhomGioiTinh != null) {
            nhomGioiTinh.selectedToggleProperty().addListener((obs, o, n) -> capNhatAnhAvatarMacDinh());
        }
    }

    private void capNhatAnhAvatarMacDinh() {
        if (anhChanDung != null && (urlAnhAvatar == null || urlAnhAvatar.isEmpty())) {
            boolean isNu = rdoNu != null && rdoNu.isSelected();
            java.io.File defFile = new java.io.File(isNu ? "src/view/default_nu.png" : "src/view/default_nam.png");
            if (defFile.exists()) {
                anhChanDung.setImage(new javafx.scene.image.Image(defFile.toURI().toString()));
                if (anhChanDung.getParent() instanceof javafx.scene.layout.StackPane sp) {
                    for (javafx.scene.Node n : sp.getChildren()) {
                        if (n instanceof Label lbl && "👤".equals(lbl.getText())) lbl.setVisible(false);
                    }
                }
            }
        }
    }

    private String urlAnhAvatar = "";

    private void xuLyTaiAnh() {
        if (anhChanDung != null && nutTaiAnh != null && nutTaiAnh.getScene() != null) {
            String url = ImagePickerHelper.chonVaUploadAnh(anhChanDung, nutTaiAnh.getScene().getWindow());
            if (url != null) {
                this.urlAnhAvatar = url;
            }
        }
    }

    @FXML
    private void xuLyLuuThongTin(ActionEvent event) {
        String hoTen = truongHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            hienThongBaoLoi("Thiếu thông tin", "Vui lòng nhập Họ và tên!");
            return;
        }

        String gioiTinh = (rdoNam != null && rdoNam.isSelected()) ? "nam" : "nu";
        String ngaySinh = (truongNgaySinh != null && truongNgaySinh.getValue() != null) ? truongNgaySinh.getValue().toString() : "";
        String diaChi = (truongDiaChi != null) ? truongDiaChi.getText().trim() : "";

        model.User currentUser = model.UserSession.getInstance().getNguoiDungHienTai();
        if (currentUser != null) {
            currentUser.setHoTen(hoTen);
            try {
                new model.UserRepository().capNhatThongTinHoanThien(currentUser, gioiTinh, ngaySinh, diaChi, this.urlAnhAvatar);
                System.out.println("[HoanThienThongTin] Đã cập nhật hồ sơ vào Supabase DB cho: " + hoTen);
            } catch (Exception ex) {
                System.err.println("[HoanThienThongTin] Lỗi cập nhật DB: " + ex.getMessage());
            }
        }
        
        // Chuyển sang màn hình chính Dashboard Cây Gia Phả
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainDashboard.fxml"));
            Stage stage = (Stage) nutLuu.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Trang chủ Cây Gia Phả - Sơ Đồ Gia Phả");
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi", "Không thể tải màn hình chính.");
            e.printStackTrace();
        }
    }
}
