package controller;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import model.DatabaseConnection;
import view.component.SuaThanhVienModal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SuaThanhVienController - Controller điều khiển Modal chỉnh sửa thông tin thành viên
 * Đóng gói logic đọc/ghi CSDL Supabase theo chuẩn kiến trúc OOP MVC.
 */
public class SuaThanhVienController extends BaseController {

    private final SuaThanhVienModal view;
    private final String hoTenCu;
    private final Runnable callbackVeLaiCay;

    private String urlAnhDaiDien = "";

    public SuaThanhVienController(SuaThanhVienModal view, String hoTenCu, Runnable callbackVeLaiCay) {
        this.view = view;
        this.hoTenCu = hoTenCu;
        this.callbackVeLaiCay = callbackVeLaiCay;
    }

    @Override
    public void khoiTao() {
        // Thực thi trong constructor hoặc taiDuLieuCu
    }

    /**
     * Nạp dữ liệu hiện tại của thành viên từ bảng nguoi_trong_gia_pha lên form
     */
    public void taiDuLieuCu() {
        view.getTxtHoTen().setText(hoTenCu);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT gioi_tinh, url_anh_dai_dien, ngay_sinh_duong_lich, so_dien_thoai, email_ca_nhan, dia_chi FROM nguoi_trong_gia_pha WHERE ho_ten = ? LIMIT 1")) {
            st.setString(1, hoTenCu);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String gt = rs.getString(1);
                urlAnhDaiDien = rs.getString(2);
                java.sql.Date ns = rs.getDate(3);
                String sdt = rs.getString(4);
                String email = rs.getString(5);
                String diaChi = rs.getString(6);

                if ("nam".equalsIgnoreCase(gt)) view.getRdoNam().setSelected(true);
                else view.getRdoNu().setSelected(true);

                if (ns != null) view.getDpNgaySinh().setValue(ns.toLocalDate());
                if (sdt != null) view.getTxtSoDienThoai().setText(sdt);
                if (email != null) view.getTxtEmail().setText(email);
                if (diaChi != null) view.getTxtDiaChi().setText(diaChi);

                // Nạp Cha/Mẹ
                try (PreparedStatement stCM = conn.prepareStatement(
                        "SELECT p.ho_ten FROM quan_he_cha_me_con c JOIN nguoi_trong_gia_pha p ON c.ma_cha_me = p.ma_nguoi WHERE c.ma_con IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ? LIMIT 1)")) {
                    stCM.setString(1, hoTenCu);
                    ResultSet rsCM = stCM.executeQuery();
                    List<String> listCM = new ArrayList<>();
                    while (rsCM.next()) listCM.add(rsCM.getString(1));
                    if (!listCM.isEmpty()) view.getTxtChaMe().setText(String.join(", ", listCM));
                } catch (Exception ignored) {}

                // Nạp Vợ/Chồng
                try (PreparedStatement stVC = conn.prepareStatement(
                        "SELECT p.ho_ten FROM quan_he_vo_chong v JOIN nguoi_trong_gia_pha p ON (p.ma_nguoi = v.ma_nguoi_1 OR p.ma_nguoi = v.ma_nguoi_2) WHERE p.ho_ten != ? AND (v.ma_nguoi_1 IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ? LIMIT 1) OR v.ma_nguoi_2 IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ? LIMIT 1))")) {
                    stVC.setString(1, hoTenCu); stVC.setString(2, hoTenCu); stVC.setString(3, hoTenCu);
                    ResultSet rsVC = stVC.executeQuery();
                    List<String> listVC = new ArrayList<>();
                    while (rsVC.next()) listVC.add(rsVC.getString(1));
                    if (!listVC.isEmpty()) view.getTxtVoChong().setText(String.join(", ", listVC));
                } catch (Exception ignored) {}

                boolean loadedAvt = false;
                if (urlAnhDaiDien != null && !urlAnhDaiDien.isEmpty() && (urlAnhDaiDien.startsWith("http") || urlAnhDaiDien.startsWith("file:"))) {
                    try {
                        view.getAnhAvatar().setImage(new Image(urlAnhDaiDien, true));
                        loadedAvt = true;
                    } catch (Exception ignored) {}
                }
                if (!loadedAvt) {
                    view.hienThiAnhMacDinh(gt);
                }
            }
        } catch (Exception ex) {
            System.err.println("[SuaThanhVienController] Lỗi nạp dữ liệu cũ: " + ex.getMessage());
            view.hienThiAnhMacDinh("nam");
        }
    }

    public void chonVaTaiAnhLen() {
        String urlMoi = ImagePickerHelper.chonVaUploadAnh(view.getAnhAvatar(), view);
        if (urlMoi != null) {
            this.urlAnhDaiDien = urlMoi;
        }
    }

    public void xuLyLuuThongTin() {
        String hoTenMoi = view.getTxtHoTen().getText().trim();
        if (hoTenMoi.isEmpty()) {
            hienThongBaoLoi("Thiếu thông tin", "Vui lòng nhập Họ và tên thành viên!");
            return;
        }

        LocalDate ngaySinhMoi = view.getDpNgaySinh().getValue();
        if (ngaySinhMoi == null) {
            ngaySinhMoi = LocalDate.of(1990, 1, 1); // Mặc định nếu để trống
        }

        String gioiTinhMoi = view.getRdoNam().isSelected() ? "nam" : "nu";
        String sdtMoi = view.getTxtSoDienThoai().getText().trim();
        String emailMoi = view.getTxtEmail().getText().trim();
        String diaChiMoi = view.getTxtDiaChi().getText().trim();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE nguoi_trong_gia_pha SET ho_ten = ?, gioi_tinh = ?, ngay_sinh_duong_lich = ?, so_dien_thoai = ?, email_ca_nhan = ?, dia_chi = ?, url_anh_dai_dien = ?, ngay_cap_nhat = NOW() WHERE ho_ten = ?")) {

            stmt.setString(1, hoTenMoi);
            stmt.setString(2, gioiTinhMoi);
            stmt.setDate(3, java.sql.Date.valueOf(ngaySinhMoi));
            stmt.setString(4, sdtMoi.isEmpty() ? null : sdtMoi);
            stmt.setString(5, emailMoi.isEmpty() ? null : emailMoi);
            stmt.setString(6, diaChiMoi.isEmpty() ? null : diaChiMoi);
            stmt.setString(7, urlAnhDaiDien == null || urlAnhDaiDien.isEmpty() ? null : urlAnhDaiDien);
            stmt.setString(8, hoTenCu);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                // Đồng bộ Họ tên mới sang bảng tài khoản đăng nhập (nguoi_dung) và bộ nhớ phiên
                model.User cur = model.UserSession.getInstance().getNguoiDungHienTai();
                if (cur != null && hoTenCu.equalsIgnoreCase(cur.getHoTen())) {
                    cur.setHoTen(hoTenMoi);
                }
                try (PreparedStatement stSync = conn.prepareStatement(
                        "UPDATE nguoi_dung SET ho_ten = ? WHERE ho_ten = ? OR ma_nguoi_dung IN (SELECT ma_nguoi_dung_lien_ket FROM nguoi_trong_gia_pha WHERE ho_ten = ?)")) {
                    stSync.setString(1, hoTenMoi);
                    stSync.setString(2, hoTenCu);
                    stSync.setString(3, hoTenMoi);
                    stSync.executeUpdate();
                } catch (Exception ignored) {}

                // Cập nhật quan hệ Cha/Mẹ
                String chaMeMoi = view.getTxtChaMe().getText().trim();
                if (!chaMeMoi.isEmpty() && !chaMeMoi.contains("Tìm tên")) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM quan_he_cha_me_con WHERE ma_con IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ?)")) {
                        del.setString(1, hoTenMoi); del.executeUpdate();
                    } catch (Exception ignored) {}
                    for (String pName : chaMeMoi.split(",")) {
                        pName = pName.trim();
                        if (!pName.isEmpty()) {
                            try (PreparedStatement ins = conn.prepareStatement(
                                    "INSERT INTO quan_he_cha_me_con (ma_so_do, ma_cha_me, ma_con, loai_quan_he, vai_tro_cha_me) SELECT p1.ma_so_do, p1.ma_nguoi, p2.ma_nguoi, 'ruot', CASE WHEN LOWER(p1.gioi_tinh) = 'nu' THEN 'me' ELSE 'cha' END FROM nguoi_trong_gia_pha p1, nguoi_trong_gia_pha p2 WHERE p1.ho_ten ILIKE ? AND p2.ho_ten = ? LIMIT 1")) {
                                ins.setString(1, pName); ins.setString(2, hoTenMoi); ins.executeUpdate();
                            } catch (Exception ignored) {}
                        }
                    }
                }

                // Cập nhật quan hệ Vợ/Chồng
                String voChongMoi = view.getTxtVoChong().getText().trim();
                if (!voChongMoi.isEmpty() && !voChongMoi.contains("Tìm tên")) {
                    try (PreparedStatement delVC = conn.prepareStatement("DELETE FROM quan_he_vo_chong WHERE ma_nguoi_1 IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ?) OR ma_nguoi_2 IN (SELECT ma_nguoi FROM nguoi_trong_gia_pha WHERE ho_ten = ?)")) {
                        delVC.setString(1, hoTenMoi); delVC.setString(2, hoTenMoi); delVC.executeUpdate();
                    } catch (Exception ignored) {}
                    for (String vcName : voChongMoi.split(",")) {
                        vcName = vcName.trim();
                        if (!vcName.isEmpty()) {
                            try (PreparedStatement insVC = conn.prepareStatement(
                                    "INSERT INTO quan_he_vo_chong (ma_so_do, ma_nguoi_1, ma_nguoi_2, loai_quan_he) SELECT p1.ma_so_do, p1.ma_nguoi, p2.ma_nguoi, 'hon_nhan' FROM nguoi_trong_gia_pha p1, nguoi_trong_gia_pha p2 WHERE p1.ho_ten ILIKE ? AND p2.ho_ten = ? LIMIT 1")) {
                                insVC.setString(1, vcName); insVC.setString(2, hoTenMoi); insVC.executeUpdate();
                            } catch (Exception ignored) {}
                        }
                    }
                }

                hienThongBaoThanhCong("Đã cập nhật tiểu sử thành viên '" + hoTenMoi + "' thành công!");
                view.close();
                if (callbackVeLaiCay != null) {
                    callbackVeLaiCay.run();
                }
            } else {
                hienThongBaoLoi("Lỗi CSDL", "Không tìm thấy thành viên '" + hoTenCu + "' để cập nhật!");
            }
        } catch (Exception ex) {
            hienThongBaoLoi("Lỗi lưu CSDL", "Không thể cập nhật thông tin: " + ex.getMessage());
        }
    }
}
