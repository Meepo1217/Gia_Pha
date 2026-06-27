package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.geometry.Side;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.User;
import model.UserRepository;
import model.UserSession;
import view.component.*;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * MainDashboardController - Quản lý tương tác màn hình chính Trang chủ Cây Gia
 * Phả
 * Triển khai theo nguyên tắc OOP: Separation of Concerns + Delegation
 * Tách biệt hoàn toàn tầng hiển thị sơ đồ sang lớp riêng FamilyTreeCanvas
 * (Custom Panel)
 */
public class MainDashboardController extends BaseController implements Initializable {

    @FXML
    private Pane vungVeTree;
    @FXML
    private javafx.scene.control.Label lblMaSoDoSidebar;
    @FXML
    private javafx.scene.control.TextField txtTimKiemThanhVien;

    // Phần 4: Thanh công cụ nổi
    @FXML
    private HBox toolbarNoi;
    @FXML
    private javafx.scene.control.Button nutThem;
    @FXML
    private javafx.scene.control.Button nutSua;
    @FXML
    private javafx.scene.control.Button nutXoa;
    @FXML
    private javafx.scene.control.Button btnTabCay;
    @FXML
    private javafx.scene.control.Button btnTabDanhSach;
    @FXML
    private javafx.scene.control.Button btnTabThongKe;
    @FXML
    private javafx.scene.control.Button btnTabTaiLieu;
    @FXML
    private javafx.scene.control.Button nutVeBanThan;

    private FamilyTreeCanvas treeCanvas;
    private MemberListView memberListView;
    private view.component.ThongKePanel thongKePanel;
    private view.component.TaiLieuPanel taiLieuPanel;
    private ISearchablePanel manHinhDangHienThi;

    private String nodeDangChonTen = "";

    private void capNhatTrangThaiMenuSidebar(javafx.scene.control.Button activeBtn) {
        javafx.scene.control.Button[] allMenuButtons = {btnTabCay, btnTabDanhSach, btnTabThongKe, btnTabTaiLieu};
        for (javafx.scene.control.Button btn : allMenuButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("menu-item-active", "menu-item");
                btn.getStyleClass().add(btn == activeBtn ? "menu-item-active" : "menu-item");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        khoiTao();
    }

    @Override
    public void khoiTao() {
        if (toolbarNoi != null) {
            toolbarNoi.setVisible(false);
        }
        System.out.println("[MainDashboard] Đã nạp giao diện Trang chủ Cây Gia Phả.");

        // Cập nhật huy hiệu Mã sơ đồ dưới logo Sidebar
        capNhatMaSoDoSidebar();

        // Tự động đồng bộ sửa chữa tên tài khoản đăng nhập trong Supabase DB
        model.User curUser = model.UserSession.getInstance().getNguoiDungHienTai();
        if (curUser != null && curUser.getHoTen() != null && curUser.getHoTen().toLowerCase().contains("xc xc")) {
            curUser.setHoTen("XC XC");
            try (java.sql.Connection c = model.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement p = c.prepareStatement("UPDATE nguoi_dung SET ho_ten = 'XC XC' WHERE ho_ten ILIKE '%xc xc%'")) {
                p.executeUpdate();
            } catch (Exception ignored) {}
        }

        // Khởi tạo Custom Panel riêng biệt chuẩn kiến trúc OOP
        treeCanvas = new FamilyTreeCanvas(toolbarNoi);
        manHinhDangHienThi = treeCanvas;
        memberListView = new MemberListView();
        thongKePanel = new view.component.ThongKePanel();
        taiLieuPanel = new view.component.TaiLieuPanel();
        thongKePanel.setCallbackChuyenTab(() -> xuLyChuyenTabDanhSach(null), () -> xuLyChuyenTabCay(null));
        memberListView.setCallbackTaiLai(() -> {
            treeCanvas.taiVaVeCayGiaPha();
            memberListView.napDanhSach(treeCanvas.getDanhSachThanhVienTong());
        });

        treeCanvas.setOnNodeSelectedListener(ten -> {
            nodeDangChonTen = ten;
            User currentUser = UserSession.getInstance().getNguoiDungHienTai();
            boolean laChuHo = (currentUser != null && "Chủ_họ".equalsIgnoreCase(currentUser.getVaiTro()));
            if (nutThem != null) nutThem.setVisible(laChuHo);
            if (nutXoa != null) nutXoa.setVisible(laChuHo);
            if (nutSua != null) nutSua.setVisible(true);
        });

        // Nhúng Panel vào khung chứa trung tâm
        if (vungVeTree != null) {
            vungVeTree.getChildren().clear();
            vungVeTree.getChildren().add(treeCanvas);

            javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle();
            clipRect.widthProperty().bind(vungVeTree.widthProperty());
            clipRect.heightProperty().bind(vungVeTree.heightProperty());
            vungVeTree.setClip(clipRect);

            treeCanvas.taiVaVeCayGiaPha();
        }
    }

    @FXML
    public void xuLyChuyenTabCay(ActionEvent event) {
        if (vungVeTree != null && treeCanvas != null) {
            if (toolbarNoi != null) toolbarNoi.setVisible(false);
            vungVeTree.getChildren().clear();
            vungVeTree.getChildren().add(treeCanvas);
            manHinhDangHienThi = treeCanvas;
            capNhatTrangThaiMenuSidebar(btnTabCay);
            if (nutVeBanThan != null) nutVeBanThan.setVisible(true);
        }
    }

    @FXML
    public void xuLyChuyenTabDanhSach(ActionEvent event) {
        if (vungVeTree != null && memberListView != null) {
            if (toolbarNoi != null) toolbarNoi.setVisible(false);
            vungVeTree.getChildren().clear();
            memberListView.prefWidthProperty().bind(vungVeTree.widthProperty());
            memberListView.prefHeightProperty().bind(vungVeTree.heightProperty());
            vungVeTree.getChildren().add(memberListView);
            manHinhDangHienThi = memberListView;
            capNhatTrangThaiMenuSidebar(btnTabDanhSach);
            if (nutVeBanThan != null) nutVeBanThan.setVisible(false);

            if (treeCanvas != null) {
                memberListView.napDanhSach(treeCanvas.getDanhSachThanhVienTong());
            }
        }
    }

    @FXML
    public void xuLyChuyenTabThongKe(ActionEvent event) {
        if (vungVeTree != null && thongKePanel != null) {
            if (toolbarNoi != null) toolbarNoi.setVisible(false);
            vungVeTree.getChildren().clear();
            thongKePanel.prefWidthProperty().bind(vungVeTree.widthProperty());
            thongKePanel.prefHeightProperty().bind(vungVeTree.heightProperty());
            vungVeTree.getChildren().add(thongKePanel);
            manHinhDangHienThi = thongKePanel;
            capNhatTrangThaiMenuSidebar(btnTabThongKe);
            if (nutVeBanThan != null) nutVeBanThan.setVisible(false);

            if (treeCanvas != null) {
                thongKePanel.napDuLieu(treeCanvas.getDanhSachThanhVienTong(), treeCanvas.getMaxGen());
            }
        }
    }

    @FXML
    public void xuLyChuyenTabTaiLieu(ActionEvent event) {
        if (vungVeTree != null && taiLieuPanel != null) {
            if (toolbarNoi != null) toolbarNoi.setVisible(false);
            vungVeTree.getChildren().clear();
            taiLieuPanel.prefWidthProperty().bind(vungVeTree.widthProperty());
            taiLieuPanel.prefHeightProperty().bind(vungVeTree.heightProperty());
            vungVeTree.getChildren().add(taiLieuPanel);
            manHinhDangHienThi = taiLieuPanel;
            capNhatTrangThaiMenuSidebar(btnTabTaiLieu);
            if (nutVeBanThan != null) nutVeBanThan.setVisible(false);

            String maSoDo = layMaSoDoHienTai();
            taiLieuPanel.napDuLieu(maSoDo);
        }
    }

    private String layMaSoDoHienTai() {
        User cur = UserSession.getInstance().getNguoiDungHienTai();
        if (cur != null && cur.getMaNguoiDung() != null) {
            try (java.sql.Connection conn = model.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement st = conn.prepareStatement("SELECT tv.ma_so_do::text FROM thanh_vien_so_do tv WHERE tv.ma_nguoi_dung::text = ? LIMIT 1")) {
                st.setString(1, cur.getMaNguoiDung());
                java.sql.ResultSet rs = st.executeQuery();
                if (rs.next()) return rs.getString(1);
            } catch (Exception ignored) {}
        }
        try (java.sql.Connection conn = model.DatabaseConnection.getConnection();
             java.sql.PreparedStatement st = conn.prepareStatement("SELECT ma_so_do::text FROM so_do_gia_pha LIMIT 1")) {
            java.sql.ResultSet rs = st.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (Exception ignored) {}
        return "00000000-0000-0000-0000-000000000000";
    }

    @FXML
    public void xuLyVeBanThan(ActionEvent event) {
        if (treeCanvas != null) {
            treeCanvas.canChinhVeBanThan();
        }
    }

    public void capNhatMaSoDoSidebar() {
        if (lblMaSoDoSidebar == null) return;
        User cur = UserSession.getInstance().getNguoiDungHienTai();
        String maUserStr = (cur != null && cur.getMaNguoiDung() != null) ? cur.getMaNguoiDung() : null;

        String maThamGia = "GP2026";
        if (maUserStr != null) {
            try (java.sql.Connection conn = model.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement st = conn.prepareStatement("SELECT s.ma_tham_gia FROM so_do_gia_pha s JOIN thanh_vien_so_do tv ON s.ma_so_do::TEXT = tv.ma_so_do::TEXT WHERE tv.ma_nguoi_dung::TEXT = ? LIMIT 1")) {
                st.setString(1, maUserStr);
                java.sql.ResultSet rs = st.executeQuery();
                if (rs.next()) maThamGia = rs.getString(1);
            } catch (Exception ignored) {}
        }
        lblMaSoDoSidebar.setText("Mã Sơ Đồ: " + maThamGia);
    }

    // ===== CÁC CHỨC NĂNG PHẦN 4 =====

    @FXML
    private void xuLyThem(ActionEvent event) {
        if (nodeDangChonTen == null || nodeDangChonTen.isEmpty()) {
            hienThongBaoLoi("Chưa chọn thành viên", "Vui lòng click chọn một người trên cây gia phả để thêm người thân!");
            return;
        }

        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Thêm thành viên gia phả");
        choiceAlert.setHeaderText("Thêm người thân kết nối với: " + nodeDangChonTen);
        choiceAlert.setContentText("Bạn muốn thêm mối quan hệ gì vào nhánh phả hệ này?");

        ButtonType btnCon = new ButtonType("👶  Con ruột");
        ButtonType btnVoChong = new ButtonType("💍  Vợ / Chồng");
        ButtonType btnHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        choiceAlert.getButtonTypes().setAll(btnCon, btnVoChong, btnHuy);

        Optional<ButtonType> res = choiceAlert.showAndWait();
        if (res.isPresent() && res.get() != btnHuy) {
            String loaiQuanHe = (res.get() == btnCon) ? "con" : "vo_chong";
            new ThemThanhVienModal(nodeDangChonTen, loaiQuanHe, () -> {
                if (treeCanvas != null) treeCanvas.taiVaVeCayGiaPha();
                if (memberListView != null && treeCanvas != null) {
                    memberListView.napDanhSach(treeCanvas.getDanhSachThanhVienTong());
                }
            }).showAndWait();
        }
    }

    @FXML
    private void xuLySua(ActionEvent event) {
        if (nodeDangChonTen == null || nodeDangChonTen.isEmpty()) {
            hienThongBaoLoi("Chưa chọn thành viên", "Vui lòng chọn một người trên cây gia phả để chỉnh sửa!");
            return;
        }
        new SuaThanhVienModal(nodeDangChonTen, () -> {
            if (treeCanvas != null) treeCanvas.taiVaVeCayGiaPha();
        }).showAndWait();
    }

    @FXML
    private void xuLyXoa(ActionEvent event) {
        if (nodeDangChonTen == null || nodeDangChonTen.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa thành viên gia phả");
        confirm.setContentText("Bạn có chắc chắn muốn xóa thành viên '" + nodeDangChonTen + "' khỏi sơ đồ cây không?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (java.sql.Connection conn = model.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM nguoi_trong_gia_pha WHERE ho_ten = ?")) {
                    stmt.setString(1, nodeDangChonTen);
                    int deleted = stmt.executeUpdate();
                    if (deleted > 0) {
                        hienThongBaoThanhCong("Đã xóa thành viên '" + nodeDangChonTen + "' khỏi cơ sở dữ liệu!");
                        if (toolbarNoi != null) toolbarNoi.setVisible(false);
                        String deletedName = nodeDangChonTen;
                        nodeDangChonTen = "";
                        if (treeCanvas != null) {
                            treeCanvas.taiVaVeCayGiaPha();
                            if (memberListView != null) {
                                memberListView.napDanhSach(treeCanvas.getDanhSachThanhVienTong());
                            }
                        }
                    } else {
                        hienThongBaoLoi("Lỗi xóa", "Không tìm thấy thành viên để xóa trong CSDL!");
                    }
                } catch (Exception ex) {
                    hienThongBaoLoi("Lỗi CSDL", "Không thể thực thi lệnh xóa: " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void xuLyClickVungTrong(MouseEvent event) {
        if (toolbarNoi != null) toolbarNoi.setVisible(false);
    }

    @FXML
    private void xuLyTimKiemSoDo(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("GP2026");
        dialog.setTitle("Tham gia Sơ đồ Gia Phả");
        dialog.setHeaderText("Nhập Mã tham gia sơ đồ gia phả (ví dụ: GP2026)");
        dialog.setContentText("Mã sơ đồ:");

        dialog.showAndWait().ifPresent(ma -> {
            if (ma.trim().isEmpty()) {
                hienThongBaoLoi("Lỗi nhập liệu", "Vui lòng nhập mã sơ đồ cần tìm!");
                return;
            }
            try {
                User currentUser = UserSession.getInstance().getNguoiDungHienTai();
                String ketQua = new UserRepository().timKiemVaThamGiaSoDo(ma.trim(), currentUser);
                if (ketQua != null) {
                    hienThongBaoThanhCong("Chúc mừng! Bạn đã tham gia vào sơ đồ gia phả thành công:\n\n" + ketQua);
                    if (treeCanvas != null)
                        treeCanvas.taiVaVeCayGiaPha(); // Tải lại cây canvas
                    capNhatMaSoDoSidebar(); // Đồng bộ lại huy hiệu mã sơ đồ trên sidebar trái
                } else {
                    hienThongBaoLoi("Không tìm thấy",
                            "Không tồn tại sơ đồ gia phả nào khớp với mã '" + ma.toUpperCase() + "'!");
                }
            } catch (Exception ex) {
                hienThongBaoLoi("Không thể tham gia sơ đồ", ex.getMessage());
            }
        });
    }

    /**
     * Triển khai chuẩn OOP Đa hình (Polymorphism): Ủy quyền tìm kiếm xuống giao diện đang hiển thị.
     */
    @FXML
    private void xuLyTimKiemThanhVien(ActionEvent event) {
        if (txtTimKiemThanhVien == null || manHinhDangHienThi == null) return;
        String tuKhoa = txtTimKiemThanhVien.getText();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            manHinhDangHienThi.timKiem("");
            return;
        }

        boolean timThay = manHinhDangHienThi.timKiem(tuKhoa);
        if (!timThay) {
        }
    }

    @FXML
    private void xuLyClickAvatar(MouseEvent event) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem logoutItem = new MenuItem("🚪   Đăng xuất");
        logoutItem.setStyle("-fx-font-size: 13.5px; -fx-padding: 6px 18px; -fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        logoutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận đăng xuất");
            alert.setHeaderText("Đăng xuất tài khoản gia phả");
            alert.setContentText("Bạn có chắc chắn muốn quay lại màn hình đăng nhập không?");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    UserSession.getInstance().dangXuat();
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AuthLayout.fxml"));
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(loader.load(), 900, 600));
                        stage.setTitle("Sơ Đồ Gia Phả - Xác thực");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
        contextMenu.getItems().add(logoutItem);
        contextMenu.show((Node) event.getSource(), Side.BOTTOM, 0, 8);
    }

    @FXML
    private void xuLyChiaSe(ActionEvent event) {
        String ma = layMaSoDoHienTai();
        String shareContent = "Mã tham gia Sơ đồ Gia Phả gia đình: " + ma + "\nLink truy cập: https://giapha.app/join/" + ma;
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(shareContent);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        hienThongBaoThanhCong("✔ Đã sao chép mã sơ đồ (" + ma + ") cùng liên kết chia sẻ vào bộ nhớ tạm!\n\nBạn có thể dán (Ctrl+V) gửi cho người thân qua Zalo, Messenger.");
    }

    @FXML
    private void xuLyInAn(ActionEvent event) {
        if (vungVeTree == null || treeCanvas == null) return;

        int selectedMode = 3;
        ChoiceDialog<String> modeDialog = new ChoiceDialog<>("2a: Có ảnh - Có cả vợ chồng (Mặc định)",
                "1a: Không ảnh - Có cả vợ chồng",
                "1b: Không ảnh - Chỉ có con ruột (dừng thế hệ sau ở con gái)",
                "2a: Có ảnh - Có cả vợ chồng (Mặc định)",
                "2b: Có ảnh - Chỉ có con ruột (dừng thế hệ sau ở con gái)"
        );
        modeDialog.setTitle("Xuất file Sơ đồ Gia Phả (.PNG A0 / .PDF)");
        modeDialog.setHeaderText("Bước 1: Chọn bố cục phả ký trước khi xuất");
        modeDialog.setContentText("Vui lòng chọn bố cục sơ đồ cây:");

        Optional<String> modeRes = modeDialog.showAndWait();
        if (modeRes.isEmpty()) return;

        String choice = modeRes.get();
        if (choice.startsWith("1a")) selectedMode = 1;
        else if (choice.startsWith("1b")) selectedMode = 2;
        else if (choice.startsWith("2a")) selectedMode = 3;
        else if (choice.startsWith("2b")) selectedMode = 4;

        treeCanvas.setDangXuatFile(true);
        treeCanvas.thietLapCheDoInAn(selectedMode);
        treeCanvas.taiVaVeCayGiaPha();

        Alert formatDialog = new Alert(Alert.AlertType.CONFIRMATION);
        formatDialog.setTitle("Xuất file Sơ đồ Gia Phả (.PNG A0 / .PDF)");
        formatDialog.setHeaderText("Bước 2: Chọn định dạng tệp kết xuất");
        formatDialog.setContentText("Vui lòng chọn định dạng bạn muốn xuất ra để xem:");

        ButtonType btnA0 = new ButtonType("📜 Xuất ảnh khổ lớn A0 (.PNG)", ButtonBar.ButtonData.YES);
        ButtonType btnPdf = new ButtonType("📄 Xuất tài liệu PDF (.PDF)", ButtonBar.ButtonData.NO);
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        formatDialog.getButtonTypes().setAll(btnA0, btnPdf, btnCancel);

        Optional<ButtonType> fmtRes = formatDialog.showAndWait();
        if (fmtRes.isPresent() && fmtRes.get() != btnCancel) {
            double oldTX = treeCanvas.getTranslateX();
            double oldTY = treeCanvas.getTranslateY();
            double oldSX = treeCanvas.getScaleX();
            double oldSY = treeCanvas.getScaleY();

            // Đặt lại tọa độ chuẩn về (0,0) và tỷ lệ 1:1 để chụp toàn bộ nội dung cây
            treeCanvas.setTranslateX(0);
            treeCanvas.setTranslateY(0);
            treeCanvas.setScaleX(1.0);
            treeCanvas.setScaleY(1.0);

            if (fmtRes.get() == btnA0) {
                try {
                    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                    params.setTransform(javafx.scene.transform.Transform.scale(4, 4));
                    params.setFill(javafx.scene.paint.Color.web("#FAFAF8"));
                    javafx.scene.image.WritableImage snapshot = treeCanvas.snapshot(params, null);

                    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                    fc.setTitle("Lưu sơ đồ gia phả chuẩn in giấy A0 (Toàn bộ cây)");
                    fc.setInitialFileName("SoDoGiaPha_ToanBoCay_A0_" + layMaSoDoHienTai() + "_" + System.currentTimeMillis() + ".png");
                    fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Ảnh PNG toàn cảnh (*.png)", "*.png"));
                    java.io.File dest = fc.showSaveDialog(vungVeTree.getScene().getWindow());
                    if (dest != null) {
                        javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "png", dest);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Xuất file A0 thành công");
                        successAlert.setHeaderText("✔ Đã kết xuất toàn bộ Sơ đồ Cây Gia Phả (khổ A0)!");
                        successAlert.setContentText("Tệp đã lưu tại:\n" + dest.getAbsolutePath() + "\n\nKích thước toàn cảnh: " + (int)snapshot.getWidth() + " x " + (int)snapshot.getHeight() + " pixels.\n\nBạn có muốn mở ảnh toàn bộ cây lên xem ngay bây giờ không?");
                        ButtonType btnOpen = new ButtonType("👁️ Mở xem ngay", ButtonBar.ButtonData.OK_DONE);
                        ButtonType btnClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
                        successAlert.getButtonTypes().setAll(btnOpen, btnClose);

                        successAlert.showAndWait().ifPresent(btn -> {
                            if (btn == btnOpen) {
                                try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}
                            }
                        });
                    }
                } catch (Exception ex) {
                    hienThongBaoLoi("Lỗi xuất khổ A0", ex.getMessage());
                }
            } else if (fmtRes.get() == btnPdf) {
                try {
                    javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
                    if (job != null) {
                        // Thiết lập trang in quay ngang (Landscape) và căn lề trang
                        javafx.print.PageLayout pageLayout = job.getPrinter().createPageLayout(
                                javafx.print.Paper.A4, javafx.print.PageOrientation.LANDSCAPE, javafx.print.Printer.MarginType.DEFAULT
                        );
                        job.getJobSettings().setPageLayout(pageLayout);

                        boolean proceed = job.showPrintDialog(vungVeTree.getScene().getWindow());
                        if (proceed) {
                            // Tính tỷ lệ thu nhỏ để lấp toàn bộ cây vào vừa khít 1 trang ngang PDF
                            double pW = pageLayout.getPrintableWidth();
                            double pH = pageLayout.getPrintableHeight();
                            double cW = treeCanvas.getBoundsInLocal().getWidth();
                            double cH = treeCanvas.getBoundsInLocal().getHeight();
                            double fitScale = Math.min(pW / cW, pH / cH) * 0.95;

                            treeCanvas.setScaleX(fitScale);
                            treeCanvas.setScaleY(fitScale);

                            boolean printed = job.printPage(treeCanvas);
                            if (printed) {
                                job.endJob();
                                hienThongBaoThanhCong("✔ Đã kết xuất toàn bộ cây gia phả ra tệp PDF trang ngang (Landscape) thành công!");
                            } else {
                                hienThongBaoLoi("Lỗi in ấn", "Hệ thống không thể hoàn tất lệnh in trang.");
                            }
                        }
                    } else {
                        hienThongBaoLoi("Không tìm thấy máy in", "Hệ thống chưa cài đặt trình điều khiển máy in hoặc Microsoft Print to PDF.");
                    }
                } catch (Exception ex) {
                    hienThongBaoLoi("Lỗi xuất PDF", ex.getMessage());
                }
            }

            treeCanvas.setTranslateX(oldTX);
            treeCanvas.setTranslateY(oldTY);
            treeCanvas.setScaleX(oldSX);
            treeCanvas.setScaleY(oldSY);
        }

        treeCanvas.setDangXuatFile(false);
        treeCanvas.thietLapCheDoInAn(3);
        treeCanvas.taiVaVeCayGiaPha();
    }
}
