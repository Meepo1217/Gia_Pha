package controller;

import javafx.scene.control.Alert;
import model.DatabaseConnection;
import view.component.ThemThanhVienModal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

/**
 * ThemThanhVienController - Controller điều khiển Modal hoàn thiện thêm thông tin thành viên mới
 * Đóng gói logic thêm con ruột hoặc vợ/chồng liên kết chuẩn xác vào CSDL Supabase theo kiến trúc OOP MVC.
 */
public class ThemThanhVienController extends BaseController {

    private final ThemThanhVienModal view;
    private final String nguoiGoc;
    private final String loaiQuanHe;
    private final Runnable onReload;
    private String urlAnhDaiDien = "";

    public ThemThanhVienController(ThemThanhVienModal view, String nguoiGoc, String loaiQuanHe, Runnable onReload) {
        this.view = view;
        this.nguoiGoc = nguoiGoc;
        this.loaiQuanHe = loaiQuanHe;
        this.onReload = onReload;
    }

    @Override
    public void khoiTao() {
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
            hienThongBaoLoi("Thiếu thông tin", "Vui lòng nhập Họ và tên thành viên mới!");
            return;
        }

        LocalDate ngaySinhMoi = view.getDpNgaySinh().getValue();
        if (ngaySinhMoi == null) {
            ngaySinhMoi = LocalDate.of(2000, 1, 1);
        }

        String gioiTinhMoi = view.getRdoNam().isSelected() ? "nam" : "nu";
        String sdtMoi = view.getTxtSoDienThoai().getText().trim();
        String emailMoi = view.getTxtEmail().getText().trim();
        String diaChiMoi = view.getTxtDiaChi().getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String newMaNguoiStr = null;
            String maSoDoStr = null;

            // 1. Thêm thành viên mới vào bảng nguoi_trong_gia_pha (sao chép ma_so_do từ người gốc)
            String sqlIns = "INSERT INTO nguoi_trong_gia_pha (ma_so_do, ho_ten, gioi_tinh, ngay_sinh_duong_lich, so_dien_thoai, email_ca_nhan, dia_chi, url_anh_dai_dien) " +
                            "SELECT ma_so_do, ?, ?, ?, ?, ?, ?, ? FROM nguoi_trong_gia_pha WHERE ho_ten = ? LIMIT 1 RETURNING ma_nguoi::text, ma_so_do::text";
            try (PreparedStatement stmt = conn.prepareStatement(sqlIns)) {
                stmt.setString(1, hoTenMoi);
                stmt.setString(2, gioiTinhMoi);
                stmt.setDate(3, java.sql.Date.valueOf(ngaySinhMoi));
                stmt.setString(4, sdtMoi.isEmpty() ? null : sdtMoi);
                stmt.setString(5, emailMoi.isEmpty() ? null : emailMoi);
                stmt.setString(6, diaChiMoi.isEmpty() ? null : diaChiMoi);
                stmt.setString(7, urlAnhDaiDien.isEmpty() ? null : urlAnhDaiDien);
                stmt.setString(8, nguoiGoc);

                ResultSet rsGen = stmt.executeQuery();
                if (rsGen.next()) {
                    newMaNguoiStr = rsGen.getString(1);
                    maSoDoStr = rsGen.getString(2);
                }
            }

            if (newMaNguoiStr == null || maSoDoStr == null) {
                hienThongBaoLoi("Lỗi phả hệ", "Không tìm thấy thông tin gốc của thành viên '" + nguoiGoc + "'!");
                return;
            }

            // 2. Tạo liên kết quan hệ dựa vào lựa chọn ban đầu
            if ("con".equalsIgnoreCase(loaiQuanHe)) {
                String sqlCM = "INSERT INTO quan_he_cha_me_con (ma_so_do, ma_cha_me, ma_con, loai_quan_he, vai_tro_cha_me) " +
                               "SELECT ?::uuid, ma_nguoi, ?::uuid, 'ruot', CASE WHEN LOWER(gioi_tinh) = 'nu' THEN 'me' ELSE 'cha' END " +
                               "FROM nguoi_trong_gia_pha WHERE ho_ten = ? AND ma_so_do::text = ? LIMIT 1";
                try (PreparedStatement stCM = conn.prepareStatement(sqlCM)) {
                    stCM.setString(1, maSoDoStr);
                    stCM.setString(2, newMaNguoiStr);
                    stCM.setString(3, nguoiGoc);
                    stCM.setString(4, maSoDoStr);
                    stCM.executeUpdate();
                }
            } else {
                String sqlVC = "INSERT INTO quan_he_vo_chong (ma_so_do, ma_nguoi_1, ma_nguoi_2, loai_quan_he) " +
                               "SELECT ?::uuid, ma_nguoi, ?::uuid, 'hon_nhan' FROM nguoi_trong_gia_pha WHERE ho_ten = ? AND ma_so_do::text = ? LIMIT 1";
                try (PreparedStatement stVC = conn.prepareStatement(sqlVC)) {
                    stVC.setString(1, maSoDoStr);
                    stVC.setString(2, newMaNguoiStr);
                    stVC.setString(3, nguoiGoc);
                    stVC.setString(4, maSoDoStr);
                    stVC.executeUpdate();
                }
            }

            hienThongBaoThanhCong("Đã hoàn thiện thêm thông tin thành viên mới '" + hoTenMoi + "' thành công!");
            view.close();
            if (onReload != null) {
                onReload.run();
            }
        } catch (Exception ex) {
            hienThongBaoLoi("Lỗi lưu CSDL", "Không thể thêm thành viên mới: " + ex.getMessage());
        }
    }
}
