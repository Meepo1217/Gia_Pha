package view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import model.DatabaseConnection;
import model.User;
import model.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * FamilyTreeCanvas - Custom Component Panel (Tương tự UserControl trong C#
 * WinForms/WPF)
 * Kế thừa trực tiếp từ Pane của JavaFX, chuyên trách toàn bộ logic hiển thị cây
 * gia phả.
 * Đóng gói (Encapsulation) toán học tạo tọa độ, kết nối CSDL Supabase và xử lý
 * sự kiện thẻ thành viên.
 */
public class FamilyTreeCanvas extends Pane implements ISearchablePanel {

    private HBox toolbarNoi;
    private StackPane nodeDangDuocChon = null;
    private String nodeDangChonTen = "";
    private java.util.function.Consumer<String> onNodeSelectedListener;

    private List<ThanhVienNode> danhSachThanhVienTong = new ArrayList<>();

    private StackPane selfCardNode = null;
    private double lastMouseX, lastMouseY;
    private int maxGen = 1;

    private boolean hienThiAnh = true;
    private boolean hienThiVoChong = true;
    private boolean dungLoiConGai = false;
    private boolean dangXuatFile = false;

    public void setDangXuatFile(boolean dangXuatFile) {
        this.dangXuatFile = dangXuatFile;
    }

    public void thietLapCheDoInAn(int cheDo) {
        switch (cheDo) {
            case 1: hienThiAnh = false; hienThiVoChong = true;  dungLoiConGai = false; break;
            case 2: hienThiAnh = false; hienThiVoChong = false; dungLoiConGai = true;  break;
            case 3: hienThiAnh = true;  hienThiVoChong = true;  dungLoiConGai = false; break;
            case 4: hienThiAnh = true;  hienThiVoChong = false; dungLoiConGai = true;  break;
        }
    }

    public int getMaxGen() {
        return maxGen;
    }

    public static class ThanhVienNode {
        private String id, hoTen, namHienThi, gioiTinh, urlAnh;
        private String ngaySinhDuongLich = "";
        private boolean laBanThan, laConRuot;
        private ThanhVienNode voChongNode = null;

        public String getNgaySinhDuongLich() {
            return ngaySinhDuongLich;
        }

        public void setNgaySinhDuongLich(String ns) {
            this.ngaySinhDuongLich = ns;
        }

        public ThanhVienNode(String id, String hoTen, String namHienThi, String gioiTinh, boolean laBanThan,
                String urlAnh) {
            this.id = id;
            this.hoTen = hoTen;
            this.namHienThi = namHienThi;
            this.gioiTinh = gioiTinh;
            this.laBanThan = laBanThan;
            this.urlAnh = urlAnh;
        }

        public ThanhVienNode(String id, String hoTen, String namHienThi, String gioiTinh, boolean laBanThan) {
            this(id, hoTen, namHienThi, gioiTinh, laBanThan, "");
        }

        public String getId() {
            return id;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getNamHienThi() {
            return namHienThi;
        }

        public String getGioiTinh() {
            return gioiTinh;
        }

        public String getUrlAnh() {
            return urlAnh;
        }

        public boolean isLaBanThan() {
            return laBanThan;
        }

        public boolean isLaConRuot() {
            return laConRuot;
        }

        public void setLaConRuot(boolean laConRuot) {
            this.laConRuot = laConRuot;
        }

        public ThanhVienNode getVoChongNode() {
            return voChongNode;
        }

        public void setVoChongNode(ThanhVienNode voChongNode) {
            this.voChongNode = voChongNode;
        }
    }

    public FamilyTreeCanvas(HBox toolbarNoi) {
        this.toolbarNoi = toolbarNoi;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/component/FamilyTreeCanvas.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            this.setStyle("-fx-background-color: #FAFAF8;");
        }
        this.setOnMouseClicked(this::xuLyClickVungTrong);

        // Kéo chuột để di chuyển bản đồ vô tận (Pan)
        this.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });
        this.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - lastMouseX;
            double deltaY = event.getSceneY() - lastMouseY;
            this.setTranslateX(this.getTranslateX() + deltaX);
            this.setTranslateY(this.getTranslateY() + deltaY);
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        // Lăn chuột để phóng to thu nhỏ (Zoom)
        this.setOnScroll(event -> {
            double zoomFactor = 1.08;
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor;
            }
            double newScaleX = this.getScaleX() * zoomFactor;
            double newScaleY = this.getScaleY() * zoomFactor;
            if (newScaleX >= 0.2 && newScaleX <= 4.0) {
                this.setScaleX(newScaleX);
                this.setScaleY(newScaleY);
            }
            event.consume();
        });
    }

    public void setOnNodeSelectedListener(java.util.function.Consumer<String> listener) {
        this.onNodeSelectedListener = listener;
    }

    public String getNodeDangChonTen() {
        return nodeDangChonTen;
    }

    public StackPane getNodeDangDuocChon() {
        return nodeDangDuocChon;
    }

    /**
     * Tải dữ liệu từ CSDL Supabase và tự động vẽ cây phả hệ
     */
    public void taiVaVeCayGiaPha() {
        User currentUser = UserSession.getInstance().getNguoiDungHienTai();
        if (currentUser == null)
            return;

        String maSoDo = null;
        String maUser = currentUser.getMaNguoiDung();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement st = conn.prepareStatement(
                        "SELECT ma_so_do::TEXT FROM thanh_vien_so_do WHERE ma_nguoi_dung::TEXT = ? LIMIT 1")) {
            st.setString(1, maUser);
            ResultSet rs = st.executeQuery();
            if (rs.next())
                maSoDo = rs.getString(1);
        } catch (Exception ignored) {
        }

        if (maSoDo == null)
            return;

        danhSachThanhVienTong.clear();
        Map<String, ThanhVienNode> mapAllNodes = new HashMap<>();

        // BƯỚC 1: TRUY VẤN MỐI QUAN HỆ TRỰC TIẾP TỪ CSDL
        Map<String, String> childToParent = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ma_con::TEXT, ma_cha_me::TEXT FROM quan_he_cha_me_con WHERE ma_so_do::TEXT = ?")) {
            st.setString(1, maSoDo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                childToParent.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception ignored) {}

        Map<String, String> spouseMap = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ma_nguoi_1::TEXT, ma_nguoi_2::TEXT FROM quan_he_vo_chong WHERE ma_so_do::TEXT = ?")) {
            st.setString(1, maSoDo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                spouseMap.put(rs.getString(1), rs.getString(2));
                spouseMap.put(rs.getString(2), rs.getString(1));
            }
        } catch (Exception ignored) {}

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT ma_nguoi::TEXT, ho_ten, gioi_tinh, ngay_sinh_duong_lich, ma_nguoi_dung_lien_ket::TEXT, url_anh_dai_dien FROM nguoi_trong_gia_pha WHERE ma_so_do::TEXT = ? ORDER BY ngay_sinh_duong_lich ASC NULLS LAST, ho_ten ASC")) {
            st.setString(1, maSoDo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String id = rs.getString(1);
                String ten = rs.getString(2);
                String gt = rs.getString(3);
                String ns = rs.getString(4);
                String lienKet = rs.getString(5);
                String urlAnh = rs.getString(6);
                boolean isSelf = (maUser != null && maUser.equalsIgnoreCase(lienKet))
                        || (ten != null && ten.equalsIgnoreCase(currentUser.getHoTen()));

                String namHienThi = (ns != null && ns.length() >= 4) ? ns.substring(0, 4) + " - Hiện tại"
                        : "... - Hiện tại";

                ThanhVienNode node = new ThanhVienNode(id, ten, namHienThi, gt, isSelf, urlAnh != null ? urlAnh : "");
                node.setNgaySinhDuongLich(ns != null ? ns : "");
                mapAllNodes.put(id, node);
                danhSachThanhVienTong.add(node);
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (!hienThiVoChong) {
            spouseMap.clear();
        }

        if (dungLoiConGai) {
            Iterator<Map.Entry<String, String>> it = childToParent.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String chaMeId = entry.getValue();
                ThanhVienNode pNode = mapAllNodes.get(chaMeId);
                if (pNode != null && "nu".equalsIgnoreCase(pNode.getGioiTinh())) {
                    it.remove();
                }
            }
        }

        if (!hienThiVoChong) {
            Set<String> reachable = new HashSet<>();
            for (ThanhVienNode n : danhSachThanhVienTong) {
                if (n.namHienThi != null && n.namHienThi.compareTo("1940") < 0) {
                    reachable.add(n.id);
                }
            }
            boolean added = true;
            while (added) {
                added = false;
                for (Map.Entry<String, String> e : childToParent.entrySet()) {
                    if (reachable.contains(e.getValue()) && !reachable.contains(e.getKey())) {
                        reachable.add(e.getKey());
                        added = true;
                    }
                }
            }
            danhSachThanhVienTong.removeIf(n -> !reachable.contains(n.id));
        }

        // BƯỚC 2: THUẬT TOÁN LOGIC CHUNG PHÂN TẦNG VÔ HẠN ĐA HÌNH
        // Quy tắc gốc: Ông/bà gốc tầng 1. Con nối xuống L+1 vô hạn. Vợ/chồng ngang cùng tầng L.
        Map<String, Integer> levels = new HashMap<>();
        for (ThanhVienNode n : danhSachThanhVienTong) {
            if (n.namHienThi != null && n.namHienThi.compareTo("1940") < 0) {
                levels.put(n.id, 1);
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (ThanhVienNode n : danhSachThanhVienTong) {
                Integer curL = levels.get(n.id);

                // Quy tắc A: Vợ/Chồng cùng tầng L
                String spId = spouseMap.get(n.id);
                if (spId != null && levels.containsKey(spId)) {
                    int spL = levels.get(spId);
                    if (curL == null || curL != spL) {
                        levels.put(n.id, spL);
                        changed = true;
                        curL = spL;
                    }
                }

                // Quy tắc B: Con nối xuống tầng dưới L+1 (mở rộng vô hạn thế hệ)
                String pId = childToParent.get(n.id);
                if (pId != null && levels.containsKey(pId)) {
                    int nextL = levels.get(pId) + 1; // KHÔNG giới hạn tầng!
                    if (curL == null || curL != nextL) {
                        levels.put(n.id, nextL);
                        changed = true;
                        curL = nextL;
                    }
                }
            }
        }

        for (ThanhVienNode n : danhSachThanhVienTong) {
            if (!levels.containsKey(n.id)) levels.put(n.id, 2);
        }

        // BƯỚC 3: PHÂN BỔ VÀO DANH SÁCH CÁC THẾ HỆ VÔ HẠN
        Map<Integer, List<ThanhVienNode>> genMap = new TreeMap<>();
        for (ThanhVienNode n : danhSachThanhVienTong) {
            int lvl = levels.get(n.id);
            genMap.computeIfAbsent(lvl, k -> new ArrayList<>()).add(n);
        }

        // Ghép cặp vợ chồng trong từng thế hệ đảm bảo chuẩn mực phả hệ Việt Nam (Họ nội/Chồng làm gốc bên trái)
        for (Map.Entry<Integer, List<ThanhVienNode>> entry : genMap.entrySet()) {
            List<ThanhVienNode> oldList = entry.getValue();
            List<ThanhVienNode> newList = new ArrayList<>();
            Set<String> processed = new HashSet<>();

            for (ThanhVienNode n1 : oldList) {
                if (processed.contains(n1.id)) continue;
                String id2 = spouseMap.get(n1.id);
                if (id2 != null && !processed.contains(id2)) {
                    ThanhVienNode n2 = null;
                    for (ThanhVienNode cand : oldList) {
                        if (cand.id.equals(id2)) { n2 = cand; break; }
                    }
                    if (n2 != null) {
                        boolean n1LaRuot = childToParent.containsKey(n1.id);
                        boolean n2LaRuot = childToParent.containsKey(n2.id);
                        if (!n1LaRuot && n2LaRuot) {
                            n2.setVoChongNode(n1);
                            newList.add(n2);
                        } else if ("nu".equalsIgnoreCase(n1.getGioiTinh()) && !"nu".equalsIgnoreCase(n2.getGioiTinh()) && n1LaRuot == n2LaRuot) {
                            n2.setVoChongNode(n1);
                            newList.add(n2);
                        } else {
                            n1.setVoChongNode(n2);
                            newList.add(n1);
                        }
                        processed.add(n1.id);
                        processed.add(n2.id);
                        continue;
                    }
                }
                newList.add(n1);
                processed.add(n1.id);
            }
            genMap.put(entry.getKey(), newList);
        }

        for (ThanhVienNode n : danhSachThanhVienTong) {
            boolean isConRuot = childToParent.containsKey(n.id) || (levels.getOrDefault(n.id, 2) == 1 && !"nu".equalsIgnoreCase(n.gioiTinh));
            n.setLaConRuot(isConRuot);
        }

        veCayGiaPhaVoHan(genMap, childToParent);
    }

    private void veCayGiaPhaVoHan(Map<Integer, List<ThanhVienNode>> genMap, Map<String, String> childToParent) {
        this.getChildren().clear();
        if (genMap.isEmpty()) return;

        double cardWidth = 160;
        double cardHeight = hienThiAnh ? 140 : 55;
        double gapX = 80;
        double rowGapY = hienThiAnh ? 240 : 130;

        // BƯỚC 1: Hợp nhất ID vợ chồng về cùng 1 gốc gia đình
        Map<String, String> canonicalParentId = new HashMap<>();
        for (List<ThanhVienNode> list : genMap.values()) {
            for (ThanhVienNode node : list) {
                canonicalParentId.put(node.id, node.id);
                if (node.getVoChongNode() != null) canonicalParentId.put(node.getVoChongNode().id, node.id);
            }
        }

        // BƯỚC 2: Ánh xạ Cha mẹ -> Danh sách các con trực tiếp
        Map<String, List<ThanhVienNode>> parentToKids = new HashMap<>();
        for (List<ThanhVienNode> list : genMap.values()) {
            for (ThanhVienNode node : list) {
                String pId = childToParent.get(node.id);
                if (pId == null && node.getVoChongNode() != null) pId = childToParent.get(node.getVoChongNode().id);
                if (pId != null && canonicalParentId.containsKey(pId)) pId = canonicalParentId.get(pId);
                if (pId != null) {
                    parentToKids.computeIfAbsent(pId, k -> new ArrayList<>()).add(node);
                }
            }
        }

        this.maxGen = genMap.isEmpty() ? 0 : Collections.max(genMap.keySet());
        int maxGen = this.maxGen;

        // BƯỚC 3: Tính toán không gian cần thiết từ dưới lên (Bottom-up Subtree Sizing)
        Map<String, Double> subtreeW = new HashMap<>();
        for (int g = maxGen; g >= 1; g--) {
            List<ThanhVienNode> list = genMap.getOrDefault(g, Collections.emptyList());
            for (ThanhVienNode u : list) {
                double uSelfW = (u.getVoChongNode() != null) ? (cardWidth * 2 + 20) : cardWidth;
                List<ThanhVienNode> kids = parentToKids.get(u.id);

                if (kids == null || kids.isEmpty()) {
                    subtreeW.put(u.id, uSelfW);
                } else {
                    double kidsTotalW = 0;
                    for (int i = 0; i < kids.size(); i++) {
                        kidsTotalW += subtreeW.getOrDefault(kids.get(i).id, cardWidth);
                        if (i > 0) kidsTotalW += gapX;
                    }
                    subtreeW.put(u.id, Math.max(uSelfW, kidsTotalW));
                }
            }
        }

        // BƯỚC 4: Phân bổ tọa độ X từ trên xuống dưới đảm bảo đối xứng tuyệt đối (Top-down Positioning)
        Map<String, Double> unitLeftX = new HashMap<>();

        double curRootX = 200;
        for (ThanhVienNode root : genMap.getOrDefault(1, Collections.emptyList())) {
            unitLeftX.put(root.id, curRootX);
            curRootX += subtreeW.getOrDefault(root.id, cardWidth) + 180;
        }

        for (int g = 1; g < maxGen; g++) {
            List<ThanhVienNode> list = genMap.getOrDefault(g, Collections.emptyList());
            for (ThanhVienNode u : list) {
                if (!unitLeftX.containsKey(u.id)) continue;
                double uLeft = unitLeftX.get(u.id);
                double uSubW = subtreeW.getOrDefault(u.id, cardWidth);
                double uSubCenter = uLeft + uSubW / 2;

                List<ThanhVienNode> kids = parentToKids.get(u.id);
                if (kids != null && !kids.isEmpty()) {
                    double kidsTotalW = 0;
                    for (int i = 0; i < kids.size(); i++) {
                        kidsTotalW += subtreeW.getOrDefault(kids.get(i).id, cardWidth);
                        if (i > 0) kidsTotalW += gapX;
                    }
                    double kLeft = uSubCenter - kidsTotalW / 2;
                    for (ThanhVienNode kid : kids) {
                        unitLeftX.put(kid.id, kLeft);
                        kLeft += subtreeW.getOrDefault(kid.id, cardWidth) + gapX;
                    }
                }
            }

            // Xử lý các con mồ côi ở tầng g+1 không khớp cha mẹ tầng g
            double maxRight = 200;
            for (ThanhVienNode cand : genMap.getOrDefault(g + 1, Collections.emptyList())) {
                if (unitLeftX.containsKey(cand.id)) {
                    maxRight = Math.max(maxRight, unitLeftX.get(cand.id) + subtreeW.getOrDefault(cand.id, cardWidth) + 80);
                }
            }
            double orphX = maxRight;
            for (ThanhVienNode cand : genMap.getOrDefault(g + 1, Collections.emptyList())) {
                if (!unitLeftX.containsKey(cand.id)) {
                    unitLeftX.put(cand.id, orphX);
                    orphX += subtreeW.getOrDefault(cand.id, cardWidth) + gapX;
                }
            }
        }

        // BƯỚC 5: Vẽ các thẻ giao diện và các đường nhánh lên
        Map<String, Double> unitMidX = new HashMap<>();
        Map<String, Double> stemDownY = new HashMap<>();
        double maxRightCanvas = 1500;

        for (Map.Entry<Integer, List<ThanhVienNode>> entry : genMap.entrySet()) {
            int g = entry.getKey();
            List<ThanhVienNode> list = entry.getValue();
            double yRow = 50 + (g - 1) * rowGapY;

            for (ThanhVienNode node : list) {
                if (!unitLeftX.containsKey(node.id)) continue;
                double uLeft = unitLeftX.get(node.id);
                double uSubW = subtreeW.getOrDefault(node.id, cardWidth);
                double uSelfW = (node.getVoChongNode() != null) ? (cardWidth * 2 + 20) : cardWidth;
                double cardX = uLeft + (uSubW - uSelfW) / 2; // Đặt thẻ ngay giữa không gian nhánh của chính nó
                double midX = uLeft + uSubW / 2;

                if (midX + 400 > maxRightCanvas) maxRightCanvas = midX + 400;

                if (node.getVoChongNode() == null) {
                    this.getChildren().add(taoTheUI(node, cardX, yRow));
                    unitMidX.put(node.id, midX);
                    stemDownY.put(node.id, yRow + cardHeight);
                } else {
                    double hX = cardX;
                    double wX = cardX + cardWidth + 20;
                    double mY = yRow + cardHeight / 2;
                    Line mLine = new Line(hX + cardWidth, mY, wX, mY);
                    thietLapDuongKe(mLine);
                    this.getChildren().add(mLine);

                    this.getChildren().add(taoTheUI(node, hX, yRow));
                    this.getChildren().add(taoTheUI(node.getVoChongNode(), wX, yRow));

                    unitMidX.put(node.id, midX);
                    unitMidX.put(node.getVoChongNode().id, midX);
                    stemDownY.put(node.id, mY);
                    stemDownY.put(node.getVoChongNode().id, mY);
                }

                if (g > 1) {
                    double pBusY = 50 + (g - 2) * rowGapY + cardHeight + 40;
                    double branchTopY = (node.getVoChongNode() == null) ? yRow : (yRow + cardHeight / 2);
                    Line branchUp = new Line(midX, pBusY, midX, branchTopY);
                    thietLapDuongKe(branchUp);
                    this.getChildren().add(branchUp);
                }
            }
        }

        // BƯỚC 6: Vẽ thanh ngang Bus và nhánh trục cha mẹ thả thẳng đứng xuống
        for (int g = 1; g < maxGen; g++) {
            double curBusY = 50 + (g - 1) * rowGapY + cardHeight + 40;
            List<ThanhVienNode> list = genMap.getOrDefault(g, Collections.emptyList());

            for (ThanhVienNode node : list) {
                List<ThanhVienNode> kids = parentToKids.get(node.id);
                if (kids != null && !kids.isEmpty()) {
                    double firstKidX = unitMidX.getOrDefault(kids.get(0).id, -1.0);
                    double lastKidX = unitMidX.getOrDefault(kids.get(kids.size() - 1).id, -1.0);

                    if (firstKidX != -1 && lastKidX != -1) {
                        double pX = unitMidX.getOrDefault(node.id, firstKidX);
                        double pY = stemDownY.getOrDefault(node.id, curBusY - 40);

                        Line stemDown = new Line(pX, pY, pX, curBusY);
                        thietLapDuongKe(stemDown);
                        this.getChildren().add(stemDown);

                        double minBusX = Math.min(pX, firstKidX);
                        double maxBusX = Math.max(pX, lastKidX);
                        if (minBusX < maxBusX) {
                            Line hBusConnect = new Line(minBusX, curBusY, maxBusX, curBusY);
                            thietLapDuongKe(hBusConnect);
                            this.getChildren().add(hBusConnect);
                        }
                    }
                }
            }

            // Vẽ bus nối ngang cho các thẻ mồ côi tầng g+1
            List<ThanhVienNode> orphs = new ArrayList<>();
            for (ThanhVienNode cand : genMap.getOrDefault(g + 1, Collections.emptyList())) {
                String pId = childToParent.get(cand.id);
                if (pId == null && cand.getVoChongNode() != null) pId = childToParent.get(cand.getVoChongNode().id);
                if (pId != null && canonicalParentId.containsKey(pId)) pId = canonicalParentId.get(pId);
                if (pId == null || !unitMidX.containsKey(pId)) orphs.add(cand);
            }
            if (orphs.size() > 1) {
                double fX = unitMidX.getOrDefault(orphs.get(0).id, -1.0);
                double lX = unitMidX.getOrDefault(orphs.get(orphs.size() - 1).id, -1.0);
                if (fX != -1 && lX != -1 && fX < lX) {
                    Line oBus = new Line(fX, curBusY, lX, curBusY);
                    thietLapDuongKe(oBus);
                    this.getChildren().add(oBus);
                }
            }
        }

        this.setPrefSize(maxRightCanvas, Math.max(800, genMap.size() * rowGapY + 300));

        if (toolbarNoi != null) {
            this.getChildren().add(toolbarNoi);
            toolbarNoi.setVisible(false);
        }

        javafx.application.Platform.runLater(this::canChinhVeBanThan);
    }

    public void canChinhVeBanThan() {
        this.setScaleX(1.0);
        this.setScaleY(1.0);

        if (selfCardNode != null && getParent() instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region parentRegion = (javafx.scene.layout.Region) getParent();
            double targetX = (parentRegion.getWidth() / 2) - (selfCardNode.getLayoutX() + 80);
            double targetY = (parentRegion.getHeight() / 2) - (selfCardNode.getLayoutY() + 70);

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(350), this);
            tt.setToX(targetX);
            tt.setToY(targetY);
            tt.play();
        } else {
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(350), this);
            tt.setToX(0);
            tt.setToY(0);
            tt.play();
        }
    }

    private void thietLapDuongKe(Line line) {
        line.setStroke(javafx.scene.paint.Color.web("#C8BFA8"));
        line.setStrokeWidth(1.5);
    }

    private StackPane taoTheUI(ThanhVienNode data, double layoutX, double layoutY) {
        double cardH = hienThiAnh ? 140 : 55;
        StackPane container = new StackPane();
        container.setPrefSize(160, cardH);
        container.setLayoutX(layoutX);
        container.setLayoutY(layoutY);

        VBox card = new VBox(7);
        card.setPrefSize(160, hienThiAnh ? 155 : 60);
        card.setAlignment(Pos.CENTER);

        boolean laBanThanHienThi = data.isLaBanThan() && !dangXuatFile;
        String borderColor = laBanThanHienThi ? "#2E7D32" : "#E6E1D6";
        String borderWidth = laBanThanHienThi ? "2px" : "1px";
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-radius: 12px; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: " + borderWidth + "; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 3);");
        card.setPadding(new Insets(12, 8, 12, 8));

        StackPane avtFrame = new StackPane();
        avtFrame.setPrefSize(66, 88);
        avtFrame.setMaxSize(66, 88);
        avtFrame.setStyle("-fx-background-color: " + (laBanThanHienThi ? "#E8F5E9" : "#F4F2EC")
                + "; -fx-background-radius: 10px;");

        boolean loadedImage = false;
        if (data.getUrlAnh() != null && !data.getUrlAnh().isEmpty() && (data.getUrlAnh().startsWith("http") || data.getUrlAnh().startsWith("file:"))) {
            try {
                ImageView iv = new ImageView(new javafx.scene.image.Image(data.getUrlAnh(), true));
                iv.setFitWidth(66);
                iv.setFitHeight(88);
                iv.setPreserveRatio(false);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(66, 88);
                clip.setArcWidth(18);
                clip.setArcHeight(18);
                iv.setClip(clip);
                avtFrame.getChildren().add(iv);
                loadedImage = true;
            } catch (Exception ignored) {
            }
        }

        if (!loadedImage) {
            String defaultFileName = "nu".equalsIgnoreCase(data.getGioiTinh()) ? "src/view/default_nu.png"
                    : "src/view/default_nam.png";
            java.io.File defFile = new java.io.File(defaultFileName);
            if (defFile.exists()) {
                try {
                    ImageView iv = new ImageView(new javafx.scene.image.Image(defFile.toURI().toString()));
                    iv.setFitWidth(66);
                    iv.setFitHeight(88);
                    iv.setPreserveRatio(false);
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(66, 88);
                    clip.setArcWidth(18);
                    clip.setArcHeight(18);
                    iv.setClip(clip);
                    avtFrame.getChildren().add(iv);
                } catch (Exception ignored) {
                }
            } else {
                Label lblIcon = new Label("nu".equalsIgnoreCase(data.getGioiTinh()) ? "👩" : "👨");
                lblIcon.setStyle("-fx-font-size: 26px;");
                avtFrame.getChildren().add(lblIcon);
            }
        }

        Label lblTen = new Label(data.getHoTen());
        lblTen.setStyle(
                "-fx-font-family: 'Segoe UI Semibold', 'Georgia', serif; -fx-font-size: 14px; -fx-text-fill: #3D2E1E; -fx-font-weight: bold;");
        lblTen.setWrapText(true);
        lblTen.setAlignment(Pos.CENTER);

        Label lblNam = new Label(data.getNamHienThi());
        lblNam.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #8C827A;");

        if (hienThiAnh) {
            card.getChildren().addAll(avtFrame, lblTen, lblNam);
        } else {
            card.getChildren().addAll(lblTen, lblNam);
        }
        container.getChildren().add(card);

        if (data.isLaBanThan()) {
            this.selfCardNode = container;
            if (!dangXuatFile) {
                Label badge = new Label("Bản thân");
                badge.setStyle(
                        "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-size: 10.5px; -fx-font-weight: bold; "
                                +
                                "-fx-padding: 2px 10px; -fx-background-radius: 10px;");
                StackPane.setAlignment(badge, Pos.BOTTOM_CENTER);
                StackPane.setMargin(badge, new Insets(0, 0, -8, 0));
                container.getChildren().add(badge);
            }
        }

        if (data.isLaConRuot()) {
            Label starBadge = new Label("⭐");
            starBadge.setStyle("-fx-font-size: 15px; -fx-effect: dropshadow(three-pass-box, rgba(217,119,6,0.35), 4, 0, 0, 1);");
            StackPane.setAlignment(starBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(starBadge, new Insets(6, 8, 0, 0));
            container.getChildren().add(starBadge);
        }

        container.setOnMouseClicked(event -> xuLyChonNodeDong(container, data, event));
        container.setStyle("-fx-cursor: hand;");

        return container;
    }

    private void xuLyChonNodeDong(StackPane node, ThanhVienNode data, MouseEvent event) {
        User currentUser = UserSession.getInstance().getNguoiDungHienTai();
        boolean laChuHo = (currentUser != null && "Chủ_họ".equalsIgnoreCase(currentUser.getVaiTro()));

        if (!laChuHo && !data.isLaBanThan()) {
            if (toolbarNoi != null)
                toolbarNoi.setVisible(false);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Từ chối quyền hạn");
            alert.setHeaderText("Vai trò Chủ nhà");
            alert.setContentText(
                    "Bạn có vai trò 'Chủ nhà' nên chỉ được phép chỉnh sửa thông tin của chính mình (thẻ Bản thân)!");
            alert.showAndWait();
            event.consume();
            return;
        }

        nodeDangDuocChon = node;
        nodeDangChonTen = data.getHoTen();
        if (onNodeSelectedListener != null) {
            onNodeSelectedListener.accept(data.getHoTen());
        }

        khoiPhucMauViencacThe();

        if (node.getChildren().get(0) instanceof VBox box) {
            box.setStyle(box.getStyle() + "-fx-border-color: #D97706; -fx-border-width: 2.5px;");
        }

        double toaDoX = node.getLayoutX() + (node.getPrefWidth() / 2) - 80;
        double toaDoY = node.getLayoutY() + node.getPrefHeight() + 14;

        if (toolbarNoi != null) {
            toolbarNoi.setLayoutX(toaDoX);
            toolbarNoi.setLayoutY(toaDoY);
            toolbarNoi.setVisible(true);
            toolbarNoi.toFront();
        }

        event.consume();
    }

    private void xuLyClickVungTrong(MouseEvent event) {
        if (toolbarNoi != null)
            toolbarNoi.setVisible(false);
        khoiPhucMauViencacThe();
        nodeDangDuocChon = null;
    }

    private void khoiPhucMauViencacThe() {
        for (javafx.scene.Node n : this.getChildren()) {
            if (n instanceof StackPane sp && !sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof VBox box) {
                boolean isSelf = sp.getChildren().size() > 1;
                box.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-radius: 12px; " +
                        "-fx-border-color: " + (isSelf ? "#2E7D32" : "#E6E1D6") + "; " +
                        "-fx-border-width: " + (isSelf ? "2px" : "1px") + "; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 3);");
            }
        }
    }

    /**
     * Phương thức đóng gói (Encapsulation) chuẩn OOP: Tìm kiếm và làm nổi bật thẻ thành viên.
     * @param tuKhoa từ khóa tên cần tìm
     * @return true nếu tìm thấy, false nếu không tìm thấy
     */
    @Override
    public boolean timKiem(String tuKhoa) {
        khoiPhucMauViencacThe();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return false;
        String query = tuKhoa.trim().toLowerCase();

        boolean timThay = false;
        for (javafx.scene.Node n : this.getChildren()) {
            if (n instanceof StackPane sp && !sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof VBox box) {
                if (box.getChildren().size() > 1 && box.getChildren().get(1) instanceof Label lblTen) {
                    String hoTen = lblTen.getText();
                    if (hoTen != null && hoTen.toLowerCase().contains(query)) {
                        // Viền đỏ rực rỡ phát sáng và tạo bóng đổ nổi bật
                        box.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 12px; -fx-border-radius: 12px; " +
                                "-fx-border-color: #EF4444; -fx-border-width: 3px; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(239,68,68,0.45), 15, 0, 0, 0);");
                        timThay = true;
                    }
                }
            }
        }
        return timThay;
    }

    public List<ThanhVienNode> getDanhSachThanhVienTong() {
        return danhSachThanhVienTong;
    }
}
