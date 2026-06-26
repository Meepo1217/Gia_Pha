package controller;

import java.io.File;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.CloudflareStorageService;

/**
 * Lớp trợ lý tiện ích tĩnh đa phương tiện (Multimedia Helper).
 * Tái sử dụng chọn & tải lên: Ảnh chân dung, Video gia đình (.mp4), Tài liệu gia phả (.pdf, .docx).
 */
public class ImagePickerHelper {

    private static final long MAX_IMG_SIZE = 5L * 1024 * 1024;   // 5 MB
    private static final long MAX_VID_SIZE = 50L * 1024 * 1024;  // 50 MB
    private static final long MAX_DOC_SIZE = 20L * 1024 * 1024;  // 20 MB

    public static String chonVaUploadAnh(ImageView imgPreview, Window ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện thành viên");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ảnh gia phả (JPEG, PNG)", "*.jpg", "*.jpeg", "*.png", "*.webp"));

        File file = fileChooser.showOpenDialog(ownerStage);
        if (file == null) return null;

        if (file.length() > MAX_IMG_SIZE) {
            hienCanhBao("Dung lượng quá lớn", "Bức ảnh nặng vượt mức 5MB. Vui lòng chọn ảnh nhẹ hơn.");
            return null;
        }

        String url = CloudflareStorageService.getInstance().uploadFile(file, "avatars");
        if (url == null || url.contains("DIEN_")) {
            url = file.toURI().toString();
        }
        if (url != null && imgPreview != null) {
            try {
                imgPreview.setImage(new Image(file.toURI().toString()));
                double w = imgPreview.getFitWidth() > 0 ? imgPreview.getFitWidth() : 160;
                double h = imgPreview.getFitHeight() > 0 ? imgPreview.getFitHeight() : 160;
                Rectangle clip = new Rectangle(w, h);
                clip.setArcWidth(20); clip.setArcHeight(20);
                imgPreview.setClip(clip);

                if (imgPreview.getParent() instanceof javafx.scene.layout.StackPane sp) {
                    for (javafx.scene.Node child : sp.getChildren()) {
                        if (child instanceof javafx.scene.control.Label lbl && "👤".equals(lbl.getText())) {
                            lbl.setVisible(false);
                        }
                        if (child instanceof javafx.scene.control.Button btn) {
                            btn.toFront();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return url;
    }

    /**
     * Chọn và upload kỷ niệm Video gia đình (.mp4, .mov).
     */
    public static String chonVaUploadVideo(Window ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Video kỷ niệm gia đình");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video gia đình (MP4, MOV)", "*.mp4", "*.mov", "*.avi"));

        File file = fileChooser.showOpenDialog(ownerStage);
        if (file == null) return null;

        if (file.length() > MAX_VID_SIZE) {
            hienCanhBao("Video quá dài", "Dung lượng video vượt ngưỡng 50MB. Vui lòng nén bớt trước khi tải.");
            return null;
        }

        return CloudflareStorageService.getInstance().uploadFile(file, "videos");
    }

    /**
     * Chọn và upload Tài liệu phả ký (.pdf, .docx, .doc).
     */
    public static String chonVaUploadTaiLieu(Window ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Tài liệu phả ký / Văn tự cổ");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tài liệu phả ký (PDF, DOCX)", "*.pdf", "*.docx", "*.doc"));

        File file = fileChooser.showOpenDialog(ownerStage);
        if (file == null) return null;

        if (file.length() > MAX_DOC_SIZE) {
            hienCanhBao("File quá lớn", "Tài liệu vượt ngưỡng 20MB.");
            return null;
        }

        return CloudflareStorageService.getInstance().uploadFile(file, "docs");
    }

    private static void hienCanhBao(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
