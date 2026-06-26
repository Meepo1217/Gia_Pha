package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Lớp cha trừu tượng (Abstract Base Controller) chuẩn Template Method Pattern.
 * Đóng gói quy trình dùng chung đa phương tiện: chọn ảnh, video, tài liệu PDF/Word, kiểm tra lỗi và Lưu.
 */
public abstract class BaseHoSoController {

    protected String urlAnhCDN = "";     // Biến lưu URL ảnh chân dung
    protected String urlVideoCDN = "";   // Biến lưu URL video gia đình
    protected String urlTaiLieuCDN = ""; // Biến lưu URL tài liệu gia phả (.pdf, .docx)

    @FXML
    protected ImageView ivAvatarPreview; // Khung ngắm ảnh xem trước trên FXML

    // ===== NHÓM 3 SỰ KIỆN CHỌN FILE DÙNG CHUNG CHO MỌI FORM CON =====

    @FXML
    protected void xuLyChonAnh(ActionEvent event) {
        if (ivAvatarPreview != null && ivAvatarPreview.getScene() != null) {
            String url = ImagePickerHelper.chonVaUploadAnh(ivAvatarPreview, ivAvatarPreview.getScene().getWindow());
            if (url != null) this.urlAnhCDN = url;
        }
    }

    @FXML
    protected void xuLyChonVideo(ActionEvent event) {
        Window stage = (ivAvatarPreview != null && ivAvatarPreview.getScene() != null) 
                ? ivAvatarPreview.getScene().getWindow() : null;
        String url = ImagePickerHelper.chonVaUploadVideo(stage);
        if (url != null) {
            this.urlVideoCDN = url;
            hienThongBao("Đã tải Video", "✔ Video kỷ niệm đã được lưu lên Cloud:\n" + url);
        }
    }

    @FXML
    protected void xuLyChonTaiLieu(ActionEvent event) {
        Window stage = (ivAvatarPreview != null && ivAvatarPreview.getScene() != null) 
                ? ivAvatarPreview.getScene().getWindow() : null;
        String url = ImagePickerHelper.chonVaUploadTaiLieu(stage);
        if (url != null) {
            this.urlTaiLieuCDN = url;
            hienThongBao("Đã tải Tài liệu", "✔ Văn tự / Sách sử đã được lưu lên Cloud:\n" + url);
        }
    }

    // ===== KHUÔN MẪU QUY TRÌNH LƯU HỒ SƠ (Template Method) =====

    @FXML
    public final void xuLyBamLuu(ActionEvent event) {
        if (!kiemTraDuLieuHopLe()) {
            hienCanhBao("Thông tin chưa đầy đủ", "Vui lòng nhập đủ các trường thông tin bắt buộc.");
            return;
        }

        try {
            thucThiLuuVaoDatabase();
            hienThongBao("Thành công", "Đã cập nhật dữ liệu gia phả vào cơ sở dữ liệu.");
            dongCuaSo();
        } catch (Exception e) {
            hienLoi("Lỗi thực thi", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void xuLyHuy(ActionEvent event) {
        dongCuaSo();
    }

    protected void dongCuaSo() {
        if (ivAvatarPreview != null && ivAvatarPreview.getScene() != null) {
            ((Stage) ivAvatarPreview.getScene().getWindow()).close();
        }
    }

    protected void hienThongBao(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void hienCanhBao(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void hienLoi(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Đã xảy ra lỗi");
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ===== CÁC HÀM TRỪU TƯỢNG BẮT BUỘC LỚP CON PHẢI OVERRIDE (Đa hình) =====

    protected abstract boolean kiemTraDuLieuHopLe();
    protected abstract void thucThiLuuVaoDatabase() throws Exception;
}
