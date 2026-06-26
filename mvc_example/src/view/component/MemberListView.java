package view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MemberListView - Custom Component Panel theo chuẩn kiến trúc OOP
 * Kế thừa trực tiếp từ BorderPane, chuyên trách hiển thị danh sách thành viên dạng lưới Thẻ (Grid Card)
 * Đóng gói (Encapsulation) logic phân trang 8 người/trang và lọc tìm kiếm Đa hình.
 */
public class MemberListView extends BorderPane implements ISearchablePanel {

    private GridPane gridPane;
    private Button btnTruoc;
    private Button btnSau;
    private Label lblThongTinTrang;

    private List<FamilyTreeCanvas.ThanhVienNode> danhSachTong = new ArrayList<>();
    private List<FamilyTreeCanvas.ThanhVienNode> danhSachLoc = new ArrayList<>();

    private Runnable callbackTaiLai;

    public void setCallbackTaiLai(Runnable cb) { this.callbackTaiLai = cb; }

    private int trangHienTai = 1;
    private final int kichThuocTrang = 8; // 4 cột x 2 hàng

    public MemberListView() {
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        this.setStyle("-fx-background-color: #FAFAF8;");

        // Center: Lưới chứa các Thẻ thành viên
        gridPane = new GridPane();
        gridPane.setHgap(25);
        gridPane.setVgap(25);
        gridPane.setPadding(new Insets(30, 40, 30, 40));
        gridPane.setAlignment(Pos.TOP_CENTER);
        this.setCenter(gridPane);

        // Bottom: Thanh điều hướng phân trang bên dưới
        HBox paginationBar = new HBox(15);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(15, 20, 30, 20));

        btnTruoc = new Button("◀  Trước");
        btnTruoc.setStyle("-fx-background-color: white; -fx-border-color: #D6D1C4; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 6px 16px; -fx-font-weight: bold; -fx-text-fill: #5C3D1E;");
        btnTruoc.setOnAction(e -> {
            if (trangHienTai > 1) {
                trangHienTai--;
                capNhatGiaoDienTrang();
            }
        });

        lblThongTinTrang = new Label("Trang 1 / 1");
        lblThongTinTrang.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #3D2E1E; -fx-padding: 0 10px;");

        btnSau = new Button("Sau  ▶");
        btnSau.setStyle("-fx-background-color: white; -fx-border-color: #D6D1C4; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 6px 16px; -fx-font-weight: bold; -fx-text-fill: #5C3D1E;");
        btnSau.setOnAction(e -> {
            int tongTrang = (int) Math.ceil((double) danhSachLoc.size() / kichThuocTrang);
            if (trangHienTai < tongTrang) {
                trangHienTai++;
                capNhatGiaoDienTrang();
            }
        });

        paginationBar.getChildren().addAll(btnTruoc, lblThongTinTrang, btnSau);
        this.setBottom(paginationBar);
    }

    /**
     * Nạp dữ liệu danh sách tổng vào giao diện
     * @param danhSach danh sách các ThanhVienNode
     */
    public void napDanhSach(List<FamilyTreeCanvas.ThanhVienNode> danhSach) {
        if (danhSach != null) {
            this.danhSachTong = new ArrayList<>(danhSach);
            this.danhSachLoc = new ArrayList<>(danhSach);
            this.trangHienTai = 1;
            capNhatGiaoDienTrang();
        }
    }

    private void capNhatGiaoDienTrang() {
        gridPane.getChildren().clear();

        int tongSoTrang = (int) Math.ceil((double) danhSachLoc.size() / kichThuocTrang);
        if (tongSoTrang == 0) tongSoTrang = 1;
        if (trangHienTai > tongSoTrang) trangHienTai = tongSoTrang;

        lblThongTinTrang.setText("Trang " + trangHienTai + " / " + tongSoTrang);
        btnTruoc.setDisable(trangHienTai <= 1);
        btnSau.setDisable(trangHienTai >= tongSoTrang);

        int batDau = (trangHienTai - 1) * kichThuocTrang;
        int ketThuc = Math.min(batDau + kichThuocTrang, danhSachLoc.size());

        int col = 0;
        int row = 0;
        for (int i = batDau; i < ketThuc; i++) {
            FamilyTreeCanvas.ThanhVienNode data = danhSachLoc.get(i);
            StackPane theUI = taoTheThanhVienUI(data);
            gridPane.add(theUI, col, row);

            col++;
            if (col >= 4) { // 4 thẻ 1 hàng
                col = 0;
                row++;
            }
        }
    }

    private StackPane taoTheThanhVienUI(FamilyTreeCanvas.ThanhVienNode data) {
        StackPane container = new StackPane();
        container.setPrefSize(210, 235);

        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(18, 15, 18, 15));
        card.setPrefSize(210, 235);

        boolean isSelf = data.isLaBanThan();
        String borderColor = isSelf ? "#2E7D32" : "#E6E1D6";
        String borderWidth = isSelf ? "2px" : "1px";

        card.setStyle("-fx-background-color: white; -fx-background-radius: 14px; -fx-border-radius: 14px; " +
                      "-fx-border-color: " + borderColor + "; -fx-border-width: " + borderWidth + "; " +
                      "-fx-cursor: hand; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        card.setOnMouseClicked(e -> {
            new SuaThanhVienModal(data.getHoTen(), callbackTaiLai).showAndWait();
        });

        // Khung Avatar chuẩn 3:4 chân dung
        StackPane avtFrame = new StackPane();
        avtFrame.setPrefSize(90, 120);
        Rectangle bgRect = new Rectangle(90, 120);
        bgRect.setArcWidth(22);
        bgRect.setArcHeight(22);
        bgRect.setFill(Color.web("#EDEBE4"));
        bgRect.setStroke(Color.web("#D6D1C4"));

        boolean loaded = false;
        String urlAnh = data.getUrlAnh();
        if (urlAnh != null && !urlAnh.isEmpty() && (urlAnh.startsWith("http") || urlAnh.startsWith("file:"))) {
            try {
                ImageView iv = new ImageView(new Image(urlAnh, true));
                iv.setFitWidth(90);
                iv.setFitHeight(120);
                iv.setPreserveRatio(false);
                Rectangle clip = new Rectangle(90, 120);
                clip.setArcWidth(22); clip.setArcHeight(22);
                iv.setClip(clip);
                avtFrame.getChildren().addAll(bgRect, iv);
                loaded = true;
            } catch (Exception ignored) {}
        }

        if (!loaded) {
            String defName = "nu".equalsIgnoreCase(data.getGioiTinh()) ? "src/view/default_nu.png" : "src/view/default_nam.png";
            File f = new File(defName);
            if (f.exists()) {
                try {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(90);
                    iv.setFitHeight(120);
                    iv.setPreserveRatio(false);
                    Rectangle clip = new Rectangle(90, 120);
                    clip.setArcWidth(22); clip.setArcHeight(22);
                    iv.setClip(clip);
                    avtFrame.getChildren().addAll(bgRect, iv);
                    loaded = true;
                } catch (Exception ignored) {}
            }
        }

        if (!loaded) {
            Label placeholder = new Label("👤");
            placeholder.setStyle("-fx-font-size: 42px;");
            avtFrame.getChildren().addAll(bgRect, placeholder);
        }

        // Tên
        Label lblTen = new Label(data.getHoTen());
        lblTen.setStyle("-fx-font-family: 'Segoe UI Semibold', 'Georgia', serif; -fx-font-size: 15.5px; -fx-text-fill: #3D2E1E; -fx-font-weight: bold;");
        lblTen.setWrapText(true);
        lblTen.setAlignment(Pos.CENTER);

        // Năm sinh - mất
        Label lblNam = new Label(data.getNamHienThi());
        lblNam.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C827A;");

        card.getChildren().addAll(avtFrame, lblTen, lblNam);
        container.getChildren().add(card);

        if (isSelf) {
            Label star = new Label("★");
            star.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 18px; -fx-font-weight: bold;");
            StackPane.setAlignment(star, Pos.TOP_RIGHT);
            StackPane.setMargin(star, new Insets(10, 15, 0, 0));
            container.getChildren().add(star);
        }

        return container;
    }

    @Override
    public boolean timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            this.danhSachLoc = new ArrayList<>(danhSachTong);
        } else {
            String query = tuKhoa.trim().toLowerCase();
            this.danhSachLoc = danhSachTong.stream()
                    .filter(n -> n.getHoTen() != null && n.getHoTen().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }
        this.trangHienTai = 1;
        capNhatGiaoDienTrang();
        return !danhSachLoc.isEmpty();
    }
}
