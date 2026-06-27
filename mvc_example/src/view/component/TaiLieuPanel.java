package view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ArchiveRepository;
import model.CloudflareStorageService;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * TaiLieuPanel - Thư viện Kho lưu trữ, Upload, Xem chi tiết Album, Xem tất cả Album & Xem tất cả Tài liệu toàn phần
 * Triển khai chuẩn OOP View Switching Pattern (5 Màn hình riêng biệt)
 */
public class TaiLieuPanel extends ScrollPane implements ISearchablePanel {

    private String currentMaSoDo = "";
    private final ArchiveRepository repo = new ArchiveRepository();

    // Năm màn hình chính chuyển đổi qua lại trong StackPane
    private VBox screenLibrary;
    private VBox screenUpload;
    private VBox screenAlbumDetail;
    private VBox screenAllAlbums;
    private VBox screenAllDocs;

    // Thành phần màn hình Thư viện
    private FlowPane gridAlbums;
    private VBox listDocsContainer;

    // Thành phần màn hình Xem chi tiết Album riêng biệt
    private Label lblDetailTitle;
    private Label lblDetailSub;
    private FlowPane fullGridMediaDetail;
    private String currentViewingAlbumName = "";

    // Thành phần màn hình Xem tất cả Album toàn phần
    private FlowPane gridAllAlbumsFull;

    // Thành phần màn hình Xem tất cả Tài liệu toàn phần
    private VBox listAllDocsFullContainer;

    // Thành phần màn hình Tải lên
    private ComboBox<String> cbUploadAlbums;
    private Label lblMediaSelectedCount;
    private List<File> pickedMediaFiles = new ArrayList<>();

    private TextField txtUploadDocName;
    private Label lblDocSelectedName;
    private File pickedDocFile = null;

    public TaiLieuPanel() {
        khoiTaoGiaoDien();
    }

    public void napDuLieu(String maSoDo) {
        this.currentMaSoDo = maSoDo != null ? maSoDo : "";
        repo.chuanHoaLinkCdnCu();
        taiDanhSachAlbums();
        taiDanhSachTaiLieu();
        capNhatDanhSachComboBoxAlbum();
    }

    private void khoiTaoGiaoDien() {
        this.setStyle("-fx-background-color: #FAFAF8; -fx-background: #FAFAF8;");
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        StackPane rootSwitcher = new StackPane();
        rootSwitcher.setStyle("-fx-background-color: #FAFAF8;");
        rootSwitcher.setPadding(new Insets(30, 40, 40, 40));

        // =========================================================
        // MÀN HÌNH 1: THƯ VIỆN KHO LƯU TRỮ GIA ĐÌNH
        // =========================================================
        screenLibrary = new VBox(24);

        HBox libHeader = new HBox(20);
        libHeader.setAlignment(Pos.CENTER_LEFT);

        VBox libTitleBox = new VBox(4);
        Label lblTitle = new Label("Kho lưu trữ gia đình");
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #5C3D1E; -fx-font-family: 'Segoe UI', Arial;");
        Label lblSub = new Label("Quản lý và bảo tồn các văn bản, chứng nhận và hình ảnh quan trọng qua các thế hệ.");
        lblSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #78716C;");
        libTitleBox.getChildren().addAll(lblTitle, lblSub);

        Region libSpacer = new Region();
        HBox.setHgrow(libSpacer, Priority.ALWAYS);

        Button btnSwitchToUpload = new Button("⬆   Tải lên");
        btnSwitchToUpload.setStyle("-fx-background-color: #43281C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 22px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnSwitchToUpload.setOnAction(e -> switchScreen(screenUpload));

        libHeader.getChildren().addAll(libTitleBox, libSpacer, btnSwitchToUpload);

        HBox mainCols = new HBox(24);
        mainCols.setAlignment(Pos.TOP_LEFT);

        // Cột Trái: Albums (65%)
        VBox boxLeftContainer = new VBox(16);
        HBox.setHgrow(boxLeftContainer, Priority.ALWAYS);
        boxLeftContainer.setStyle("-fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 2);");

        HBox leftHeader = new HBox(10); leftHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblLeftTitle = new Label("Hình ảnh gia đình");
        lblLeftTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #43281C;");
        Region leftSpacer = new Region(); HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Button btnViewAllAlbums = new Button("Xem tất cả");
        btnViewAllAlbums.setStyle("-fx-background-color: transparent; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13.5px;");
        btnViewAllAlbums.setOnAction(e -> moManHinhDanhSachAlbumToanPhan());
        leftHeader.getChildren().addAll(lblLeftTitle, leftSpacer, btnViewAllAlbums);

        gridAlbums = new FlowPane(18, 18);
        boxLeftContainer.getChildren().addAll(leftHeader, gridAlbums);

        // Cột Phải: Tài liệu quan trọng
        VBox boxRightContainer = new VBox(16);
        boxRightContainer.setPrefWidth(360); boxRightContainer.setMinWidth(330);
        boxRightContainer.setStyle("-fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 24px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 2);");

        Label lblRightTitle = new Label("Tài liệu quan trọng");
        lblRightTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #43281C;");

        ComboBox<String> cbFilter = new ComboBox<>();
        cbFilter.getItems().addAll("Tất cả thành viên", "Tài liệu chung dòng họ", "Văn tự cổ");
        cbFilter.setValue("Tất cả thành viên");
        cbFilter.setMaxWidth(Double.MAX_VALUE);
        cbFilter.setStyle("-fx-background-color: #F8F7F4; -fx-border-color: #D6D3CD; -fx-border-radius: 6px; -fx-padding: 2px;");

        listDocsContainer = new VBox(12);

        Button btnViewAllDocs = new Button("Xem toàn bộ tài liệu");
        btnViewAllDocs.setMaxWidth(Double.MAX_VALUE);
        btnViewAllDocs.setStyle("-fx-background-color: transparent; -fx-border-color: #D6D3CD; -fx-border-radius: 8px; -fx-padding: 8px; -fx-text-fill: #5C3D1E; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13.5px;");
        btnViewAllDocs.setOnAction(e -> moManHinhDanhSachTaiLieuToanPhan());

        boxRightContainer.getChildren().addAll(lblRightTitle, cbFilter, listDocsContainer, btnViewAllDocs);
        mainCols.getChildren().addAll(boxLeftContainer, boxRightContainer);
        screenLibrary.getChildren().addAll(libHeader, mainCols);

        // =========================================================
        // MÀN HÌNH 2: MÀN HÌNH RIÊNG TẢI TỆP LÊN
        // =========================================================
        screenUpload = new VBox(28);
        HBox upHeader = new HBox(18); upHeader.setAlignment(Pos.CENTER_LEFT);
        Button btnBackToLib = new Button("⬅   Trở về thư viện");
        btnBackToLib.setStyle("-fx-background-color: #E7E5E4; -fx-text-fill: #292524; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnBackToLib.setOnAction(e -> switchScreen(screenLibrary));

        VBox upTitleBox = new VBox(3);
        Label lblUpTitle = new Label("Tải tệp lên Kho lưu trữ gia đình"); lblUpTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #43281C;");
        Label lblUpSub = new Label("Chọn phân loại phương tiện bên dưới để tải lên hệ thống lưu trữ đám mây Cloudflare R2."); lblUpSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #78716C;");
        upTitleBox.getChildren().addAll(lblUpTitle, lblUpSub);
        upHeader.getChildren().addAll(btnBackToLib, upTitleBox);

        HBox uploadCards = new HBox(24); uploadCards.setAlignment(Pos.TOP_LEFT);

        VBox cardA = new VBox(18); HBox.setHgrow(cardA, Priority.ALWAYS);
        cardA.setStyle("-fx-background-color: white; -fx-border-color: #BAE6FD; -fx-border-width: 1.5px; -fx-border-radius: 14px; -fx-background-radius: 14px; -fx-padding: 28px; -fx-effect: dropshadow(three-pass-box, rgba(3,105,161,0.06), 10, 0, 0, 4);");
        Label lblTitleA = new Label("1. Tải lên Hình ảnh / Video gia đình"); lblTitleA.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0369A1;");

        VBox boxA1 = new VBox(6);
        Label lblA1 = new Label("a. Chọn Album lưu trữ (hoặc thêm tên Album mới):"); lblA1.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        cbUploadAlbums = new ComboBox<>(); cbUploadAlbums.setEditable(true); cbUploadAlbums.setMaxWidth(Double.MAX_VALUE);
        cbUploadAlbums.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-padding: 4px;");
        boxA1.getChildren().addAll(lblA1, cbUploadAlbums);

        VBox boxA2 = new VBox(8);
        Label lblA2 = new Label("b. Chọn các tệp ảnh / video (Cho phép chọn nhiều file cùng lúc):"); lblA2.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        Button btnPickMedia = new Button("Chọn các tệp ảnh / video từ máy..."); btnPickMedia.setMaxWidth(Double.MAX_VALUE);
        btnPickMedia.setStyle("-fx-background-color: #F0F9FF; -fx-border-color: #7DD3FC; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 24px; -fx-text-fill: #0284C7; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        lblMediaSelectedCount = new Label("Chưa chọn tệp tin nào."); lblMediaSelectedCount.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
        btnPickMedia.setOnAction(e -> chonNhiêuFileMedia());
        boxA2.getChildren().addAll(lblA2, btnPickMedia, lblMediaSelectedCount);

        Region spacerA = new Region(); VBox.setVgrow(spacerA, Priority.ALWAYS);
        Button btnSubmitA = new Button("⬆   Bắt đầu tải ảnh & video lên Cloud"); btnSubmitA.setMaxWidth(Double.MAX_VALUE);
        btnSubmitA.setStyle("-fx-background-color: #0284C7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-padding: 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnSubmitA.setOnAction(e -> thucHienUploadMedia());
        cardA.getChildren().addAll(lblTitleA, boxA1, boxA2, spacerA, btnSubmitA);

        VBox cardB = new VBox(18); cardB.setPrefWidth(400); cardB.setMinWidth(360);
        cardB.setStyle("-fx-background-color: white; -fx-border-color: #FDE68A; -fx-border-width: 1.5px; -fx-border-radius: 14px; -fx-background-radius: 14px; -fx-padding: 28px; -fx-effect: dropshadow(three-pass-box, rgba(217,119,6,0.06), 10, 0, 0, 4);");
        Label lblTitleB = new Label("2. Tải lên Tài liệu quan trọng"); lblTitleB.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        VBox boxB1 = new VBox(6);
        Label lblB1 = new Label("a. Đặt tên tài liệu / chứng nhận:"); lblB1.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        txtUploadDocName = new TextField(); txtUploadDocName.setPromptText("Ví dụ: Giấy khai sinh cụ ông, Phả ký gốc DOCX...");
        txtUploadDocName.setStyle("-fx-background-color: #FFFBEB; -fx-border-color: #FCD34D; -fx-border-radius: 6px; -fx-padding: 8px;");
        boxB1.getChildren().addAll(lblB1, txtUploadDocName);

        VBox boxB2 = new VBox(8);
        Label lblB2 = new Label("b. Chọn tệp tài liệu (PDF, Word DOC, DOCX):"); lblB2.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
        Button btnPickDoc = new Button("Chọn file tài liệu từ máy..."); btnPickDoc.setMaxWidth(Double.MAX_VALUE);
        btnPickDoc.setStyle("-fx-background-color: #FFFBEB; -fx-border-color: #FCD34D; -fx-border-style: dashed; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 24px; -fx-text-fill: #B45309; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        lblDocSelectedName = new Label("Chưa chọn tài liệu."); lblDocSelectedName.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
        btnPickDoc.setOnAction(e -> chonFileTaiLieu());
        boxB2.getChildren().addAll(lblB2, btnPickDoc, lblDocSelectedName);

        Region spacerB = new Region(); VBox.setVgrow(spacerB, Priority.ALWAYS);
        Button btnSubmitB = new Button("⬆   Bắt đầu tải tài liệu lên Cloud"); btnSubmitB.setMaxWidth(Double.MAX_VALUE);
        btnSubmitB.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14.5px; -fx-padding: 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnSubmitB.setOnAction(e -> thucHienUploadTaiLieu());
        cardB.getChildren().addAll(lblTitleB, boxB1, boxB2, spacerB, btnSubmitB);

        uploadCards.getChildren().addAll(cardA, cardB);
        screenUpload.getChildren().addAll(upHeader, uploadCards);

        // =========================================================
        // MÀN HÌNH 3: MÀN HÌNH RIÊNG XEM CHI TIẾT ALBUM
        // =========================================================
        screenAlbumDetail = new VBox(24);
        HBox detailHeader = new HBox(18); detailHeader.setAlignment(Pos.CENTER_LEFT);
        detailHeader.setStyle("-fx-padding: 0 0 16px 0; -fx-border-color: #E6E1D6; -fx-border-width: 0 0 1.5px 0;");

        Button btnBackFromDetail = new Button("⬅   Trở về thư viện");
        btnBackFromDetail.setStyle("-fx-background-color: #E7E5E4; -fx-text-fill: #292524; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnBackFromDetail.setOnAction(e -> switchScreen(screenLibrary));

        VBox detTitleBox = new VBox(3);
        lblDetailTitle = new Label("Album: Tên Album"); lblDetailTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #43281C;");
        lblDetailSub = new Label("Toàn bộ các tệp ảnh và video đã lưu trữ trong album."); lblDetailSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #78716C;");
        detTitleBox.getChildren().addAll(lblDetailTitle, lblDetailSub);

        Region detSpacer = new Region(); HBox.setHgrow(detSpacer, Priority.ALWAYS);
        Button btnAddMoreToAlb = new Button("⬆   Tải thêm vào Album này");
        btnAddMoreToAlb.setStyle("-fx-background-color: #0284C7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnAddMoreToAlb.setOnAction(e -> { switchScreen(screenUpload); if(currentViewingAlbumName!=null) cbUploadAlbums.setValue(currentViewingAlbumName); });

        detailHeader.getChildren().addAll(btnBackFromDetail, detTitleBox, detSpacer, btnAddMoreToAlb);
        fullGridMediaDetail = new FlowPane(20, 20);
        screenAlbumDetail.getChildren().addAll(detailHeader, fullGridMediaDetail);

        // =========================================================
        // MÀN HÌNH 4: MÀN HÌNH RIÊNG XEM TẤT CẢ ALBUM THEO KIỂU LƯỚI
        // =========================================================
        screenAllAlbums = new VBox(26);
        HBox allAlbHeader = new HBox(18); allAlbHeader.setAlignment(Pos.CENTER_LEFT);
        allAlbHeader.setStyle("-fx-padding: 0 0 16px 0; -fx-border-color: #E6E1D6; -fx-border-width: 0 0 1.5px 0;");

        Button btnBackFromAllAlb = new Button("⬅   Trở về thư viện");
        btnBackFromAllAlb.setStyle("-fx-background-color: #E7E5E4; -fx-text-fill: #292524; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnBackFromAllAlb.setOnAction(e -> switchScreen(screenLibrary));

        VBox allAlbTitleBox = new VBox(3);
        Label lblAllAlbTitle = new Label("Toàn bộ Album gia đình"); lblAllAlbTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #43281C;");
        Label lblAllAlbSub = new Label("Hiển thị toàn bộ các bộ sưu tập hình ảnh và video gia phả theo bố cục lưới."); lblAllAlbSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #78716C;");
        allAlbTitleBox.getChildren().addAll(lblAllAlbTitle, lblAllAlbSub);

        Region allAlbSpacer = new Region(); HBox.setHgrow(allAlbSpacer, Priority.ALWAYS);
        Button btnAddNewAlbFull = new Button("⬆   Tải lên Album mới");
        btnAddNewAlbFull.setStyle("-fx-background-color: #43281C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 22px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnAddNewAlbFull.setOnAction(e -> switchScreen(screenUpload));

        allAlbHeader.getChildren().addAll(btnBackFromAllAlb, allAlbTitleBox, allAlbSpacer, btnAddNewAlbFull);
        gridAllAlbumsFull = new FlowPane(24, 24);
        screenAllAlbums.getChildren().addAll(allAlbHeader, gridAllAlbumsFull);

        // =========================================================
        // MÀN HÌNH 5: MÀN HÌNH RIÊNG XEM TẤT CẢ TÀI LIỆU TOÀN PHẦN
        // =========================================================
        screenAllDocs = new VBox(24);
        HBox allDocHeader = new HBox(18); allDocHeader.setAlignment(Pos.CENTER_LEFT);
        allDocHeader.setStyle("-fx-padding: 0 0 16px 0; -fx-border-color: #E6E1D6; -fx-border-width: 0 0 1.5px 0;");

        Button btnBackFromAllDoc = new Button("⬅   Trở về thư viện");
        btnBackFromAllDoc.setStyle("-fx-background-color: #E7E5E4; -fx-text-fill: #292524; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnBackFromAllDoc.setOnAction(e -> switchScreen(screenLibrary));

        VBox allDocTitleBox = new VBox(3);
        Label lblAllDocTitle = new Label("Toàn bộ Tài liệu quan trọng"); lblAllDocTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #43281C;");
        Label lblAllDocSub = new Label("Danh sách toàn bộ các văn bản, chứng nhận, phả ký gốc và sắc phong của dòng họ."); lblAllDocSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #78716C;");
        allDocTitleBox.getChildren().addAll(lblAllDocTitle, lblAllDocSub);

        Region allDocSpacer = new Region(); HBox.setHgrow(allDocSpacer, Priority.ALWAYS);
        Button btnAddNewDocFull = new Button("⬆   Tải lên Tài liệu mới");
        btnAddNewDocFull.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 22px; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnAddNewDocFull.setOnAction(e -> switchScreen(screenUpload));

        allDocHeader.getChildren().addAll(btnBackFromAllDoc, allDocTitleBox, allDocSpacer, btnAddNewDocFull);
        listAllDocsFullContainer = new VBox(14);
        screenAllDocs.getChildren().addAll(allDocHeader, listAllDocsFullContainer);

        // Khởi tạo trạng thái mặc định
        switchScreen(screenLibrary);

        rootSwitcher.getChildren().addAll(screenLibrary, screenUpload, screenAlbumDetail, screenAllAlbums, screenAllDocs);
        this.setContent(rootSwitcher);
    }

    // --- CƠ CHẾ SWITCH SWITCHER SIÊU AN TOÀN ---
    private void switchScreen(VBox target) {
        screenLibrary.setVisible(target == screenLibrary); screenLibrary.setManaged(target == screenLibrary);
        screenUpload.setVisible(target == screenUpload); screenUpload.setManaged(target == screenUpload);
        screenAlbumDetail.setVisible(target == screenAlbumDetail); screenAlbumDetail.setManaged(target == screenAlbumDetail);
        screenAllAlbums.setVisible(target == screenAllAlbums); screenAllAlbums.setManaged(target == screenAllAlbums);
        screenAllDocs.setVisible(target == screenAllDocs); screenAllDocs.setManaged(target == screenAllDocs);
        if (target == screenLibrary) napDuLieu(currentMaSoDo);
    }

    private void moManHinhDanhSachAlbumToanPhan() {
        switchScreen(screenAllAlbums);
        taiDanhSachAlbumsToanPhan();
    }

    private void moManHinhDanhSachTaiLieuToanPhan() {
        switchScreen(screenAllDocs);
        taiDanhSachTaiLieuToanPhan();
    }

    private void capNhatDanhSachComboBoxAlbum() {
        if (cbUploadAlbums == null) return;
        cbUploadAlbums.getItems().clear();
        cbUploadAlbums.getItems().add("Mặc định (Không cần Album)");
        for (ArchiveRepository.AlbumDTO alb : repo.layDanhSachAlbum(currentMaSoDo)) {
            cbUploadAlbums.getItems().add(alb.getTenAlbum());
        }
        cbUploadAlbums.setValue("Mặc định (Không cần Album)");
    }

    // --- NGHIỆP VỤ XEM CHI TIẾT ALBUM ---
    private void moManHinhChiTietAlbum(String tenAlbum) {
        this.currentViewingAlbumName = tenAlbum;
        switchScreen(screenAlbumDetail);

        lblDetailTitle.setText("Album: " + tenAlbum);
        fullGridMediaDetail.getChildren().clear();

        List<ArchiveRepository.MediaDTO> mediaList = repo.layMediaTrongAlbum(currentMaSoDo, tenAlbum);
        lblDetailSub.setText("Bộ sưu tập gồm " + mediaList.size() + " tệp tin phương tiện đã bảo tồn trên đám mây Cloudflare.");

        if (mediaList.isEmpty()) {
            Label empty = new Label("Album này chưa có tệp tin nào.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic; -fx-padding: 20px;");
            fullGridMediaDetail.getChildren().add(empty);
            return;
        }

        for (ArchiveRepository.MediaDTO item : mediaList) {
            VBox mediaCard = new VBox();
            mediaCard.setPrefSize(210, 240);
            mediaCard.setStyle("-fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 3);");

            StackPane thumbBox = new StackPane();
            thumbBox.setPrefSize(208, 190);
            thumbBox.setStyle("-fx-background-color: #F5F4F0; -fx-background-radius: 10px 10px 0 0; -fx-cursor: hand;");

            boolean isVideo = item.getUrl() != null && (item.getUrl().endsWith(".mp4") || item.getUrl().endsWith(".mov"));
            ImageView iv = new ImageView(); iv.setFitWidth(208); iv.setFitHeight(190);
            Rectangle clip = new Rectangle(208, 190); clip.setArcWidth(10); clip.setArcHeight(10); iv.setClip(clip);

            if (!isVideo && item.getUrl() != null) {
                try { iv.setImage(new Image(item.getUrl(), true)); } catch (Exception ignored) {}
                thumbBox.getChildren().add(iv);
            } else {
                Label vidLbl = new Label("▶   Video MP4");
                vidLbl.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 14px; -fx-background-radius: 20px;");
                thumbBox.getChildren().add(vidLbl);
            }

            VBox capBox = new VBox(4); capBox.setPadding(new Insets(8, 10, 10, 10));
            String cap = (item.getChuThich() != null && !item.getChuThich().isEmpty()) ? item.getChuThich() : "Tệp tin gia đình";
            if (cap.length() > 22) cap = cap.substring(0, 20) + "...";
            Label lblCap = new Label(cap); lblCap.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #334155;");

            HBox btnRow = new HBox(8); btnRow.setAlignment(Pos.CENTER);

            Button btnDown = new Button("⬇ Tải về");
            btnDown.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #15803D; -fx-border-color: #BBF7D0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px 10px;");
            btnDown.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDown, Priority.ALWAYS);
            btnDown.setOnAction(e -> {
                FileChooser fc = new FileChooser(); fc.setTitle("Lưu tệp tin về máy");
                String ext = (item.getUrl() != null && item.getUrl().contains(".")) ? item.getUrl().substring(item.getUrl().lastIndexOf(".")) : ".jpg";
                fc.setInitialFileName("giapha_media_" + System.currentTimeMillis() + ext);
                File dest = fc.showSaveDialog((Stage) this.getScene().getWindow());
                if (dest != null) {
                    try {
                        try (java.io.InputStream in = new java.net.URL(item.getUrl()).openStream()) {
                            java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                        Alert ok = new Alert(Alert.AlertType.INFORMATION, "✔ Đã lưu tệp tin về máy thành công:\n" + dest.getAbsolutePath()); ok.show();
                    } catch (Exception ex) {
                        try { java.awt.Desktop.getDesktop().browse(new URI(item.getUrl())); } catch (Exception ignored) {}
                    }
                }
            });

            Button btnDel = new Button("🗑 Xóa");
            btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-border-color: #FECACA; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px 10px;");
            btnDel.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDel, Priority.ALWAYS);
            btnDel.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa tệp tin này khỏi gia phả không?", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Xác nhận xóa");
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        if (repo.xoaMedia(item.getId())) moManHinhChiTietAlbum(currentViewingAlbumName);
                    }
                });
            });

            btnRow.getChildren().addAll(btnDown, btnDel);
            capBox.getChildren().addAll(lblCap, btnRow);
            mediaCard.getChildren().addAll(thumbBox, capBox);

            thumbBox.setOnMouseClicked(e -> {
                if (item.getUrl() != null) {
                    try { java.awt.Desktop.getDesktop().browse(new URI(item.getUrl())); } catch (Exception ignored) {}
                }
            });

            fullGridMediaDetail.getChildren().add(mediaCard);
        }
    }

    // --- NGHIỆP VỤ UPLOAD PHẦN 1 & 2 ---
    private void chonNhiêuFileMedia() {
        FileChooser fc = new FileChooser(); fc.setTitle("Chọn Hình ảnh hoặc Video");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Ảnh & Video", "*.png", "*.jpg", "*.jpeg", "*.mp4", "*.mov"), new FileChooser.ExtensionFilter("Tất cả file", "*.*"));
        List<File> list = fc.showOpenMultipleDialog((Stage) this.getScene().getWindow());
        if (list != null && !list.isEmpty()) {
            pickedMediaFiles = list;
            long bytes = 0; for(File f : list) bytes += f.length();
            lblMediaSelectedCount.setText(String.format("✔ Sẵn sàng upload %d tệp tin (%.1f MB)", list.size(), bytes / (1024.0 * 1024.0)));
            lblMediaSelectedCount.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
        }
    }

    private void thucHienUploadMedia() {
        if (pickedMediaFiles == null || pickedMediaFiles.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Vui lòng bấm chọn ít nhất 1 file ảnh hoặc video trước!").show(); return; }
        String alb = cbUploadAlbums.getValue(); if (alb == null || alb.contains("Không cần Album")) alb = "Mặc định";
        int count = 0; CloudflareStorageService s3 = CloudflareStorageService.getInstance();
        for (File f : pickedMediaFiles) {
            String cdnUrl = s3.uploadFile(f, "albums");
            if (cdnUrl != null && repo.luuAnhVideoAlbum(currentMaSoDo, alb, cdnUrl, f.getName(), f.getName().endsWith(".mp4") ? "video/mp4" : "image/jpeg", f.length())) count++;
        }
        pickedMediaFiles.clear(); lblMediaSelectedCount.setText("✔ Đã tải xong!");
        new Alert(Alert.AlertType.INFORMATION, "Đã lưu thành công " + count + " tệp tin vào Album '" + alb + "'!").show();
        taiDanhSachAlbums();
    }

    private void chonFileTaiLieu() {
        FileChooser fc = new FileChooser(); fc.setTitle("Chọn tệp tài liệu gia phả");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tài liệu (PDF, Word)", "*.pdf", "*.docx", "*.doc"));
        File f = fc.showOpenDialog((Stage) this.getScene().getWindow());
        if (f != null) {
            pickedDocFile = f; lblDocSelectedName.setText("✔ Đã chọn: " + f.getName()); lblDocSelectedName.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
            if (txtUploadDocName.getText().trim().isEmpty()) txtUploadDocName.setText(f.getName().contains(".") ? f.getName().substring(0, f.getName().lastIndexOf(".")) : f.getName());
        }
    }

    private void thucHienUploadTaiLieu() {
        if (pickedDocFile == null) { new Alert(Alert.AlertType.WARNING, "Vui lòng chọn 1 tệp tài liệu trước!").show(); return; }
        String docTitle = txtUploadDocName.getText().trim(); if (docTitle.isEmpty()) docTitle = pickedDocFile.getName();
        String cdnUrl = CloudflareStorageService.getInstance().uploadFile(pickedDocFile, "docs");
        if (cdnUrl != null && repo.luuTaiLieuQuanTrong(currentMaSoDo, docTitle, cdnUrl, pickedDocFile.getName(), pickedDocFile.getName().endsWith(".pdf") ? "application/pdf" : "application/msword", pickedDocFile.length())) {
            pickedDocFile = null; txtUploadDocName.clear(); lblDocSelectedName.setText("✔ Đã tải xong!");
            new Alert(Alert.AlertType.INFORMATION, "✔ Đã bảo tồn thành công tài liệu '" + docTitle + "'!").show();
            taiDanhSachTaiLieu();
        } else new Alert(Alert.AlertType.ERROR, "Lỗi upload tài liệu.").show();
    }

    // =========================================================
    // NGHIỆP VỤ THƯ VIỆN, ALBUMS TOÀN PHẦN & TÀI LIỆU TOÀN PHẦN
    // =========================================================
    public void taiDanhSachAlbums() {
        gridAlbums.getChildren().clear();
        for (ArchiveRepository.AlbumDTO alb : repo.layDanhSachAlbum(currentMaSoDo)) gridAlbums.getChildren().add(taoTheAlbumCard(alb));
    }

    public void taiDanhSachAlbumsToanPhan() {
        gridAllAlbumsFull.getChildren().clear();
        for (ArchiveRepository.AlbumDTO alb : repo.layDanhSachAlbum(currentMaSoDo)) gridAllAlbumsFull.getChildren().add(taoTheAlbumCard(alb));
    }

    private VBox taoTheAlbumCard(ArchiveRepository.AlbumDTO alb) {
        VBox card = new VBox(); card.setPrefSize(190, 222);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 3);");

        StackPane topImgBox = new StackPane(); topImgBox.setPrefSize(188, 180);
        topImgBox.setStyle("-fx-background-color: #F4F2EC; -fx-background-radius: 12px 12px 0 0; -fx-cursor: hand;");
        ImageView iv = new ImageView(); iv.setFitWidth(188); iv.setFitHeight(180);
        Rectangle clip = new Rectangle(188, 180); clip.setArcWidth(12); clip.setArcHeight(12); iv.setClip(clip);

        if (alb.getUrlAnhDaiDien() != null && !alb.getUrlAnhDaiDien().isEmpty()) { try { iv.setImage(new Image(alb.getUrlAnhDaiDien(), true)); } catch (Exception ignored) {} }
        else { Label lblDef = new Label("🖼"); lblDef.setStyle("-fx-font-size: 48px;"); topImgBox.getChildren().add(lblDef); }

        VBox bottomOverlay = new VBox(2); bottomOverlay.setAlignment(Pos.BOTTOM_LEFT); bottomOverlay.setPadding(new Insets(8, 10, 8, 10));
        bottomOverlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.45) 60%, transparent 100%);");
        Label lblName = new Label(alb.getTenAlbum()); lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13.5px;");
        Label lblCnt = new Label("• " + alb.getSoLuongAnh() + " ảnh/video"); lblCnt.setStyle("-fx-text-fill: #E7E5E4; -fx-font-size: 11px;");
        bottomOverlay.getChildren().addAll(lblName, lblCnt);
        topImgBox.getChildren().addAll(iv, bottomOverlay); StackPane.setAlignment(bottomOverlay, Pos.BOTTOM_CENTER);

        HBox delRow = new HBox(); delRow.setAlignment(Pos.CENTER); delRow.setPadding(new Insets(6, 8, 6, 8));
        Button btnDelAlb = new Button("🗑 Xóa Album"); btnDelAlb.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDelAlb, Priority.ALWAYS);
        btnDelAlb.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-border-color: #FECACA; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4px;");
        btnDelAlb.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa bộ sưu tập Album '" + alb.getTenAlbum() + "' không?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES && repo.xoaAlbum(currentMaSoDo, alb.getTenAlbum())) { taiDanhSachAlbums(); taiDanhSachAlbumsToanPhan(); }
            });
        });
        delRow.getChildren().add(btnDelAlb);
        card.getChildren().addAll(topImgBox, delRow);

        topImgBox.setOnMouseClicked(e -> moManHinhChiTietAlbum(alb.getTenAlbum()));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #0369A1; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(3,105,161,0.15), 12, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 3);"));
        return card;
    }

    public void taiDanhSachTaiLieu() {
        listDocsContainer.getChildren().clear();
        for (ArchiveRepository.DocumentDTO doc : repo.layDanhSachTaiLieu(currentMaSoDo)) listDocsContainer.getChildren().add(taoHangTaiLieu(doc, false));
    }

    public void taiDanhSachTaiLieuToanPhan() {
        listAllDocsFullContainer.getChildren().clear();
        List<ArchiveRepository.DocumentDTO> docs = repo.layDanhSachTaiLieu(currentMaSoDo);
        if (docs.isEmpty()) {
            Label empty = new Label("Chưa có tài liệu quan trọng nào."); empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic; -fx-padding: 20px; -fx-font-size: 15px;");
            listAllDocsFullContainer.getChildren().add(empty); return;
        }
        for (ArchiveRepository.DocumentDTO doc : docs) listAllDocsFullContainer.getChildren().add(taoHangTaiLieu(doc, true));
    }

    private HBox taoHangTaiLieu(ArchiveRepository.DocumentDTO doc, boolean isFullWidth) {
        HBox row = new HBox(16); row.setAlignment(Pos.CENTER_LEFT);
        String padStr = isFullWidth ? "-fx-padding: 14px; -fx-background-color: white; -fx-border-color: #E6E1D6; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 2);" 
                                    : "-fx-padding: 8px; -fx-background-radius: 8px;";
        row.setStyle(padStr);

        StackPane iconBox = new StackPane(); iconBox.setPrefSize(isFullWidth?48:42, isFullWidth?48:42); iconBox.setStyle("-fx-background-color: #E7E5E4; -fx-background-radius: 8px; -fx-cursor: hand;");
        Label lblIco = new Label(doc.getKieuMime().contains("pdf") ? "📕" : "📘"); lblIco.setStyle("-fx-font-size: " + (isFullWidth?24:20) + "px;"); iconBox.getChildren().add(lblIco);

        VBox info = new VBox(3);
        Label lblName = new Label(doc.getTenTaiLieu()); lblName.setStyle("-fx-font-weight: bold; -fx-font-size: " + (isFullWidth?15:13) + "px; -fx-text-fill: #292524; -fx-cursor: hand;");
        Label lblSub = new Label((doc.getKieuMime().contains("pdf") ? "PDF" : "DOCX") + " • " + String.format("%.1f MB", doc.getKichThuocByte() / (1024.0 * 1024.0)) + " • " + doc.getNgayTaoStr());
        lblSub.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #78716C;");
        info.getChildren().addAll(lblName, lblSub);

        Region docSpacer = new Region(); HBox.setHgrow(docSpacer, Priority.ALWAYS);

        Button btnDown = new Button(isFullWidth ? "⬇ Tải về máy" : "⬇");
        btnDown.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: " + (isFullWidth?"8px 16px":"4px 8px") + "; -fx-background-radius: 6px;");
        btnDown.setOnAction(e -> {
            FileChooser fc = new FileChooser(); fc.setTitle("Lưu tài liệu về máy");
            fc.setInitialFileName("giapha_doc_" + System.currentTimeMillis() + (doc.getKieuMime().contains("pdf")?".pdf":".docx"));
            File dest = fc.showSaveDialog((Stage) this.getScene().getWindow());
            if (dest != null) {
                try {
                    try (java.io.InputStream in = new java.net.URL(doc.getUrl()).openStream()) { java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
                    new Alert(Alert.AlertType.INFORMATION, "✔ Đã tải tài liệu về máy:\n" + dest.getAbsolutePath()).show();
                } catch (Exception ex) { try { java.awt.Desktop.getDesktop().browse(new URI(doc.getUrl())); } catch (Exception ignored) {} }
            }
        });

        Button btnDel = new Button(isFullWidth ? "🗑 Xóa tài liệu" : "🗑");
        btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: " + (isFullWidth?"8px 16px":"4px 8px") + "; -fx-background-radius: 6px;");
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa tài liệu này khỏi gia phả không?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES && repo.xoaTaiLieu(doc.getId())) { taiDanhSachTaiLieu(); taiDanhSachTaiLieuToanPhan(); }
            });
        });

        row.getChildren().addAll(iconBox, info, docSpacer, btnDown, btnDel);
        iconBox.setOnMouseClicked(e -> { if (doc.getUrl() != null) try { java.awt.Desktop.getDesktop().browse(new URI(doc.getUrl())); } catch (Exception ignored) {} });
        info.setOnMouseClicked(e -> { if (doc.getUrl() != null) try { java.awt.Desktop.getDesktop().browse(new URI(doc.getUrl())); } catch (Exception ignored) {} });

        return row;
    }

    @Override
    public boolean timKiem(String tuKhoa) { return true; }
}
