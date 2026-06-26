package view.component;

import controller.SuaThanhVienController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

/**
 * SuaThanhVienModal - Cửa sổ Modal popup sang trọng chỉnh sửa thông tin thành viên
 * Dựng trực tiếp bằng JavaFX chuẩn OOP MVC khớp 100% bản thiết kế đính kèm.
 */
public class SuaThanhVienModal extends Stage {

    private final String hoTenThanhVien;
    private final Runnable callbackVeLaiCay;

    private ImageView anhAvatar;
    private TextField txtHoTen;
    private RadioButton rdoNam, rdoNu;
    private DatePicker dpNgaySinh, dpNgayMat;
    private TextField txtSoDienThoai, txtEmail, txtDiaChi;
    private TextField txtChaMe, txtVoChong;
    private Button btnLuu;

    private SuaThanhVienController controller;

    public SuaThanhVienModal(String hoTenThanhVien, Runnable callbackVeLaiCay) {
        this.hoTenThanhVien = hoTenThanhVien;
        this.callbackVeLaiCay = callbackVeLaiCay;

        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Chỉnh sửa thông tin thành viên gia phả");
        this.setResizable(false);

        khoiTaoGiaoDien();
        controller = new SuaThanhVienController(this, hoTenThanhVien, callbackVeLaiCay);
        controller.taiDuLieuCu();
    }

    private void khoiTaoGiaoDien() {
        HBox root = new HBox(35);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #FAFAF6;");
        root.setAlignment(Pos.CENTER);

        // ===== CỘT TRÁI: AVATAR & CAMERA =====
        VBox leftCol = new VBox(15);
        leftCol.setAlignment(Pos.TOP_CENTER);
        leftCol.setPrefWidth(240);

        StackPane avtBox = new StackPane();
        avtBox.setPrefSize(165, 220);

        Rectangle bgAvatar = new Rectangle(165, 220);
        bgAvatar.setArcWidth(26);
        bgAvatar.setArcHeight(26);
        bgAvatar.setFill(Color.web("#EDEBE4"));
        bgAvatar.setStroke(Color.web("#D6D1C4"));

        anhAvatar = new ImageView();
        anhAvatar.setFitWidth(165);
        anhAvatar.setFitHeight(220);
        anhAvatar.setPreserveRatio(false);
        Rectangle clipAvt = new Rectangle(165, 220);
        clipAvt.setArcWidth(26); clipAvt.setArcHeight(26);
        anhAvatar.setClip(clipAvt);

        // Nút Camera nhỏ ở góc dưới bên phải
        Button btnCamera = new Button("📷");
        btnCamera.setStyle("-fx-background-color: #E0F2FE; -fx-border-color: #0284C7; -fx-border-width: 1.5px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4px 8px; -fx-font-size: 14px;");
        btnCamera.setOnAction(e -> controller.chonVaTaiAnhLen());

        StackPane.setAlignment(btnCamera, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnCamera, new Insets(0, -8, -8, 0));

        avtBox.getChildren().addAll(bgAvatar, anhAvatar, btnCamera);

        Label lblTitleAvt = new Label("Tải lên ảnh chân dung");
        lblTitleAvt.setStyle("-fx-font-size: 15.5px; -fx-font-weight: bold; -fx-text-fill: #5C3D1E;");

        Label lblSubAvt = new Label("Sử dụng ảnh rõ mặt để mọi người\ndễ dàng nhận ra");
        lblSubAvt.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #8C827A; -fx-text-alignment: center;");
        lblSubAvt.setWrapText(true);
        lblSubAvt.setAlignment(Pos.CENTER);

        leftCol.getChildren().addAll(avtBox, lblTitleAvt, lblSubAvt);

        // ===== CỘT PHẢI: FORM NHẬP LIỆU =====
        VBox formCard = new VBox(18);
        formCard.setPadding(new Insets(28));
        formCard.setPrefWidth(490);
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-border-radius: 20px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        String lblStyle = "-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #3D2E1E;";
        String fieldStyle = "-fx-background-color: #FAFAF8; -fx-border-color: #D6D1C4; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-font-size: 13.5px;";

        // 1. Họ và tên
        VBox boxTen = new VBox(6);
        Label lblTen = new Label("Họ và tên *");
        lblTen.setStyle(lblStyle);
        txtHoTen = new TextField();
        txtHoTen.setStyle(fieldStyle);
        boxTen.getChildren().addAll(lblTen, txtHoTen);

        // 2. Giới tính
        VBox boxGT = new VBox(6);
        Label lblGT = new Label("Giới tính");
        lblGT.setStyle(lblStyle);
        HBox gtGroup = new HBox(25);
        gtGroup.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup tgGT = new ToggleGroup();
        rdoNam = new RadioButton("Nam");
        rdoNam.setToggleGroup(tgGT);
        rdoNam.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #3D2E1E;");
        rdoNu = new RadioButton("Nữ");
        rdoNu.setToggleGroup(tgGT);
        rdoNu.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #3D2E1E;");
        gtGroup.getChildren().addAll(rdoNam, rdoNu);
        boxGT.getChildren().addAll(lblGT, gtGroup);

        // 3. Ngày sinh - Ngày mất
        HBox rowNgay = new HBox(20);
        VBox boxNgaySinh = new VBox(6);
        HBox.setHgrow(boxNgaySinh, Priority.ALWAYS);
        Label lblNS = new Label("Ngày sinh");
        lblNS.setStyle(lblStyle);
        dpNgaySinh = new DatePicker();
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setStyle(fieldStyle);
        boxNgaySinh.getChildren().addAll(lblNS, dpNgaySinh);

        VBox boxNgayMat = new VBox(6);
        HBox.setHgrow(boxNgayMat, Priority.ALWAYS);
        Label lblNM = new Label("Ngày mất (nếu có)");
        lblNM.setStyle(lblStyle);
        dpNgayMat = new DatePicker();
        dpNgayMat.setMaxWidth(Double.MAX_VALUE);
        dpNgayMat.setStyle(fieldStyle);
        boxNgayMat.getChildren().addAll(lblNM, dpNgayMat);
        rowNgay.getChildren().addAll(boxNgaySinh, boxNgayMat);

        // 4. Số điện thoại
        VBox boxSDT = new VBox(6);
        Label lblSDT = new Label("Số điện thoại");
        lblSDT.setStyle(lblStyle);
        txtSoDienThoai = new TextField();
        txtSoDienThoai.setStyle(fieldStyle);
        boxSDT.getChildren().addAll(lblSDT, txtSoDienThoai);

        // 5. Email - Địa chỉ
        HBox rowLienHe = new HBox(20);
        VBox boxEmail = new VBox(6);
        HBox.setHgrow(boxEmail, Priority.ALWAYS);
        Label lblEmail = new Label("Email cá nhân");
        lblEmail.setStyle(lblStyle);
        txtEmail = new TextField();
        txtEmail.setPromptText("example@mail.com");
        txtEmail.setStyle(fieldStyle);
        boxEmail.getChildren().addAll(lblEmail, txtEmail);

        VBox boxDiaChi = new VBox(6);
        HBox.setHgrow(boxDiaChi, Priority.ALWAYS);
        Label lblDiaChi = new Label("Địa chỉ");
        lblDiaChi.setStyle(lblStyle);
        txtDiaChi = new TextField();
        txtDiaChi.setPromptText("Số nhà, tên đường...");
        txtDiaChi.setStyle(fieldStyle);
        boxDiaChi.getChildren().addAll(lblDiaChi, txtDiaChi);
        rowLienHe.getChildren().addAll(boxEmail, boxDiaChi);

        // 6. Cha mẹ - Vợ chồng
        HBox rowQuanHe = new HBox(20);
        VBox boxChaMe = new VBox(6);
        HBox.setHgrow(boxChaMe, Priority.ALWAYS);
        Label lblChaMe = new Label("Con của ai (Cha/Mẹ)");
        lblChaMe.setStyle(lblStyle);
        txtChaMe = new TextField();
        txtChaMe.setPromptText("... Tìm tên cha hoặc mẹ...");
        txtChaMe.setStyle(fieldStyle);
        boxChaMe.getChildren().addAll(lblChaMe, txtChaMe);

        VBox boxVoChong = new VBox(6);
        HBox.setHgrow(boxVoChong, Priority.ALWAYS);
        Label lblVoChong = new Label("Vợ / Chồng");
        lblVoChong.setStyle(lblStyle);
        txtVoChong = new TextField();
        txtVoChong.setPromptText("... Tìm tên vợ hoặc chồng...");
        txtVoChong.setStyle(fieldStyle);
        boxVoChong.getChildren().addAll(lblVoChong, txtVoChong);
        rowQuanHe.getChildren().addAll(boxChaMe, boxVoChong);

        // 7. Nút Lưu
        btnLuu = new Button("Lưu và bắt đầu khám phá  ➔");
        btnLuu.setMaxWidth(Double.MAX_VALUE);
        btnLuu.setStyle("-fx-background-color: #3D2E1E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-background-radius: 10px; -fx-padding: 13px; -fx-cursor: hand;");
        btnLuu.setOnAction(e -> controller.xuLyLuuThongTin());

        formCard.getChildren().addAll(boxTen, boxGT, rowNgay, boxSDT, rowLienHe, rowQuanHe, btnLuu);

        root.getChildren().addAll(leftCol, formCard);

        Scene scene = new Scene(root);
        this.setScene(scene);
    }

    // ===== GETTERS DÀNH CHO CONTROLLER =====
    public ImageView getAnhAvatar() { return anhAvatar; }
    public TextField getTxtHoTen() { return txtHoTen; }
    public RadioButton getRdoNam() { return rdoNam; }
    public RadioButton getRdoNu() { return rdoNu; }
    public DatePicker getDpNgaySinh() { return dpNgaySinh; }
    public DatePicker getDpNgayMat() { return dpNgayMat; }
    public TextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public TextField getTxtEmail() { return txtEmail; }
    public TextField getTxtDiaChi() { return txtDiaChi; }
    public TextField getTxtChaMe() { return txtChaMe; }
    public TextField getTxtVoChong() { return txtVoChong; }

    public void hienThiAnhMacDinh(String gioiTinh) {
        String def = "nu".equalsIgnoreCase(gioiTinh) ? "src/view/default_nu.png" : "src/view/default_nam.png";
        File f = new File(def);
        if (f.exists()) {
            anhAvatar.setImage(new Image(f.toURI().toString()));
        }
    }
}
