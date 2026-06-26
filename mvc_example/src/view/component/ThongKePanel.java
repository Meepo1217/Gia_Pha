package view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ThongKePanel - Custom Component Panel Thống Kê Chuẩn Kiến Trúc OOP
 */
public class ThongKePanel extends ScrollPane implements ISearchablePanel {

    private Label lblTongSoThanhVien;
    private Label lblSoTheHe;
    private Label lblTyLeNamNu;
    private Label lblTitleSuKien;
    private HBox barGioiTinh;
    private FlowPane khungSuKien;

    private Runnable callbackChuyenDanhSach;
    private Runnable callbackChuyenCay;

    private List<FamilyTreeCanvas.ThanhVienNode> danhSachTong;

    public ThongKePanel() {
        khoiTaoGiaoDien();
    }

    public void setCallbackChuyenTab(Runnable chuyenDanhSach, Runnable chuyenCay) {
        this.callbackChuyenDanhSach = chuyenDanhSach;
        this.callbackChuyenCay = chuyenCay;
    }

    private void khoiTaoGiaoDien() {
        this.setStyle("-fx-background-color: #FAFAF8; -fx-background: #FAFAF8;");
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        VBox root = new VBox(28);
        root.setPadding(new Insets(35, 45, 45, 45));
        root.setStyle("-fx-background-color: #FAFAF8;");

        // 1. HEADER
        VBox header = new VBox(6);
        Label lblTitle = new Label("Tổng quan gia phả");
        lblTitle.setStyle("-fx-font-family: 'Segoe UI Semibold', 'Georgia', serif; -fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E1A17;");
        Label lblSubtitle = new Label("Khám phá và quản lý di sản gia đình bạn.");
        lblSubtitle.setStyle("-fx-font-size: 14.5px; -fx-text-fill: #73675C;");
        header.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. MIDDLE STATS SECTION
        HBox statsRow = new HBox(24);
        statsRow.setAlignment(Pos.TOP_LEFT);

        // --- CỘT TRÁI: Thẻ Tổng số thành viên (Bấm chuyển sang màn hình Danh sách) ---
        VBox leftBigCard = taoThietKeTheTrang();
        HBox.setHgrow(leftBigCard, Priority.ALWAYS);
        leftBigCard.setPrefWidth(550);
        leftBigCard.setSpacing(14);
        leftBigCard.setStyle(leftBigCard.getStyle() + " -fx-cursor: hand;");
        leftBigCard.setOnMouseClicked(e -> {
            if (callbackChuyenDanhSach != null) callbackChuyenDanhSach.run();
        });

        HBox lblTopLeft = new HBox(8);
        lblTopLeft.setAlignment(Pos.CENTER_LEFT);
        Label iconUsers = new Label("👥");
        iconUsers.setStyle("-fx-font-size: 18px;");
        Label txtUsers = new Label("Tổng số thành viên");
        txtUsers.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #3D352E;");
        lblTopLeft.getChildren().addAll(iconUsers, txtUsers);

        lblTongSoThanhVien = new Label("0");
        lblTongSoThanhVien.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #4E3629;");

        // Đã xóa bỏ phần chữ xanh "+3 thành viên tháng này" theo chỉ định

        leftBigCard.getChildren().addAll(lblTopLeft, lblTongSoThanhVien);

        // --- CỘT PHẢI ---
        VBox rightCol = new VBox(24);
        rightCol.setPrefWidth(380);

        // Thẻ trên: Thế hệ được ghi nhận (Bấm chuyển sang màn hình Cây gia phả)
        VBox genCard = taoThietKeTheTrang();
        genCard.setAlignment(Pos.CENTER);
        genCard.setSpacing(8);
        genCard.setStyle(genCard.getStyle() + " -fx-cursor: hand;");
        genCard.setOnMouseClicked(e -> {
            if (callbackChuyenCay != null) callbackChuyenCay.run();
        });

        Label iconGen = new Label("🏛");
        iconGen.setStyle("-fx-font-size: 24px; -fx-text-fill: #D97706;");
        lblSoTheHe = new Label("1");
        lblSoTheHe.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #2C241D;");
        Label txtGenTitle = new Label("Thế hệ được ghi nhận");
        txtGenTitle.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #3D352E;");
        Label txtGenSub = new Label("Trải dài qua các thế hệ gia tộc.");
        txtGenSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #8C827A; -fx-text-alignment: center;");
        txtGenSub.setWrapText(true);
        genCard.getChildren().addAll(iconGen, lblSoTheHe, txtGenTitle, txtGenSub);

        // Thẻ dưới: Tỷ lệ giới tính
        VBox genderCard = taoThietKeTheTrang();
        genderCard.setSpacing(12);
        HBox gTitleRow = new HBox(8);
        gTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label iconG = new Label("⚖");
        iconG.setStyle("-fx-font-size: 16px; -fx-text-fill: #5C3D1E;");
        Label txtG = new Label("Tỷ lệ Nam/Nữ");
        txtG.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #3D352E;");
        gTitleRow.getChildren().addAll(iconG, txtG);

        lblTyLeNamNu = new Label("Nam (50%)            Nữ (50%)");
        lblTyLeNamNu.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5C4B3E;");

        barGioiTinh = new HBox();
        barGioiTinh.setPrefHeight(12);
        barGioiTinh.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-overflow: hidden;");
        Rectangle rNam = new Rectangle(163, 12, Color.web("#4E3629"));
        Rectangle rNu = new Rectangle(163, 12, Color.web("#43A047"));
        rNam.setArcWidth(6); rNam.setArcHeight(6);
        rNu.setArcWidth(6); rNu.setArcHeight(6);
        barGioiTinh.getChildren().addAll(rNam, rNu);

        genderCard.getChildren().addAll(gTitleRow, lblTyLeNamNu, barGioiTinh);

        rightCol.getChildren().addAll(genCard, genderCard);
        statsRow.getChildren().addAll(leftBigCard, rightCol);

        // 3. EVENTS BOTTOM SECTION
        VBox eventsSection = new VBox(16);
        HBox evTitleBox = new HBox(8);
        evTitleBox.setAlignment(Pos.CENTER_LEFT);
        Label evIcon = new Label("📅");
        evIcon.setStyle("-fx-font-size: 18px;");
        int curMonth = LocalDate.now().getMonthValue();
        lblTitleSuKien = new Label("Sự kiện ghi nhận trong tháng " + curMonth + " (Theo lịch dương)");
        lblTitleSuKien.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C241D;");
        evTitleBox.getChildren().addAll(evIcon, lblTitleSuKien);

        khungSuKien = new FlowPane(20, 20);
        khungSuKien.prefWidthProperty().bind(root.widthProperty());

        eventsSection.getChildren().addAll(evTitleBox, khungSuKien);

        root.getChildren().addAll(header, statsRow, eventsSection);
        this.setContent(root);
    }

    private VBox taoThietKeTheTrang() {
        VBox card = new VBox();
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-radius: 16px; " +
                "-fx-border-color: #E6E1D6; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
        return card;
    }

    private HBox taoTheSuKien(String dateText, String badgeBg, String name, String desc, String genInfo) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setPrefWidth(310);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-radius: 12px; " +
                "-fx-border-color: #E6E1D6; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 2);");

        Label badge = new Label(dateText);
        badge.setStyle("-fx-background-color: " + (badgeBg.equals("#E4A11B") ? "#FEF3C7" : "#F5F5F5") + "; " +
                "-fx-text-fill: " + (badgeBg.equals("#E4A11B") ? "#B45309" : "#424242") + "; " +
                "-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-alignment: center; " +
                "-fx-padding: 8px 10px; -fx-background-radius: 8px;");
        badge.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox info = new VBox(3);
        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #2C241D;");
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #5C4B3E;");
        Label lblGen = new Label(genInfo);
        lblGen.setStyle("-fx-font-size: 11px; -fx-text-fill: #9E9E9E;");

        info.getChildren().addAll(lblName, lblDesc, lblGen);
        card.getChildren().addAll(badge, info);
        return card;
    }

    public void napDuLieu(List<FamilyTreeCanvas.ThanhVienNode> danhSachTong, int maxGen) {
        this.danhSachTong = danhSachTong;
        int curMonth = LocalDate.now().getMonthValue();
        if (lblTitleSuKien != null) {
            lblTitleSuKien.setText("Sự kiện ghi nhận trong tháng " + curMonth + " (Theo lịch dương)");
        }

        if (danhSachTong != null && !danhSachTong.isEmpty()) {
            int tongSo = danhSachTong.size();
            lblTongSoThanhVien.setText(String.valueOf(tongSo));
            lblSoTheHe.setText(String.valueOf(Math.max(1, maxGen)));

            long soNam = danhSachTong.stream().filter(n -> "nam".equalsIgnoreCase(n.getGioiTinh())).count();
            long soNu = tongSo - soNam;
            int ptNam = (int) Math.round((soNam * 100.0) / tongSo);
            int ptNu = 100 - ptNam;

            lblTyLeNamNu.setText("Nam (" + ptNam + "%)               Nữ (" + ptNu + "%)");
            barGioiTinh.getChildren().clear();
            double wTotal = 326;
            double wNam = (ptNam * wTotal) / 100.0;
            double wNu = wTotal - wNam;
            Rectangle rNam = new Rectangle(wNam, 12, Color.web("#4E3629"));
            Rectangle rNu = new Rectangle(wNu, 12, Color.web("#43A047"));
            barGioiTinh.getChildren().addAll(rNam, rNu);

            if (khungSuKien != null) {
                khungSuKien.getChildren().clear();
                List<FamilyTreeCanvas.ThanhVienNode> suKienThangNay = new ArrayList<>();
                for (FamilyTreeCanvas.ThanhVienNode node : danhSachTong) {
                    String ns = node.getNgaySinhDuongLich();
                    if (ns != null && ns.length() >= 10 && ns.contains("-")) {
                        try {
                            String[] parts = ns.split("-");
                            int m = Integer.parseInt(parts[1]);
                            if (m == curMonth) {
                                suKienThangNay.add(node);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (suKienThangNay.isEmpty()) {
                    Label lblEmpty = new Label("Hiện tại không có thành viên nào có sự kiện sinh nhật trong tháng " + curMonth + " này.");
                    lblEmpty.setStyle("-fx-font-size: 14.5px; -fx-font-style: italic; -fx-text-fill: #8C827A; -fx-padding: 10px 0;");
                    khungSuKien.getChildren().add(lblEmpty);
                } else {
                    for (FamilyTreeCanvas.ThanhVienNode node : suKienThangNay) {
                        String ns = node.getNgaySinhDuongLich();
                        String[] parts = ns.split("-");
                        String dateBadge = "DL\n" + parts[2] + "/" + parts[1];
                        String desc = "Sinh ngày: " + ns + " (Dương lịch)";
                        khungSuKien.getChildren().add(taoTheSuKien(dateBadge, "#E4A11B", node.getHoTen(), desc, "Sinh nhật trong tháng " + curMonth));
                    }
                }
            }
        }
    }

    @Override
    public boolean timKiem(String tuKhoa) {
        return danhSachTong != null && !danhSachTong.isEmpty();
    }
}
