package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserRepository - Triển khai cụ thể của IUserRepository
 * Kết nối Supabase thật thông qua JDBC
 *
 * Nguyên tắc OOP: TÍNH ĐA HÌNH (Polymorphism)
 * - implements IUserRepository → có thể thay thế bằng MockUserRepository
 *   mà không cần sửa AuthService hay LoginController
 */
public class UserRepository implements IUserRepository {

    /**
     * Xác thực đăng nhập bằng số điện thoại + mật khẩu
     * Dùng hàm crypt() của pgcrypto để kiểm tra mật khẩu hash
     *
     * @param soDienThoai  Số điện thoại người dùng nhập
     * @param matKhau      Mật khẩu plain text (DB sẽ tự hash để so sánh)
     * @return User nếu đúng, null nếu sai
     * @throws SQLException nếu lỗi kết nối
     */
    public User xacThucDangNhap(String soDienThoai, String matKhau) throws SQLException {
        String sql = "SELECT ma_nguoi_dung, ho_ten, email, so_dien_thoai, " +
                     "ngon_ngu_mac_dinh, trang_thai, vai_tro " +
                     "FROM nguoi_dung " +
                     "WHERE so_dien_thoai = ? " +
                     "AND mat_khau_hash = crypt(?, mat_khau_hash) " +
                     "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, soDienThoai);
            stmt.setString(2, matKhau);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setMaNguoiDung(rs.getString("ma_nguoi_dung"));
                user.setHoTen(rs.getString("ho_ten"));
                user.setEmail(rs.getString("email"));
                user.setSoDienThoai(rs.getString("so_dien_thoai"));
                user.setNgonNguMacDinh(rs.getString("ngon_ngu_mac_dinh"));
                user.setTrangThai(rs.getString("trang_thai"));
                user.setVaiTro(rs.getString("vai_tro"));
                return user;
            }
        }
        return null;
    }

    /**
     * Tìm người dùng theo số điện thoại
     * Dùng để kiểm tra tài khoản có tồn tại không
     *
     * @param soDienThoai Số điện thoại cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     * @throws SQLException nếu lỗi kết nối
     */
    public boolean tonTaiSoDienThoai(String soDienThoai) throws SQLException {
        String sql = "SELECT 1 FROM nguoi_dung WHERE so_dien_thoai = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, soDienThoai);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @Override
    public boolean dangKyTaiKhoan(User user, String matKhau) throws SQLException {
        String sql = "INSERT INTO nguoi_dung (ho_ten, so_dien_thoai, email, mat_khau_hash, trang_thai, vai_tro) " +
                     "VALUES (?, ?, ?, crypt(?, gen_salt('bf')), 'hoat_dong', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getHoTen());
            stmt.setString(2, user.getSoDienThoai());
            stmt.setString(3, (user.getEmail() == null || user.getEmail().isEmpty()) ? null : user.getEmail());
            stmt.setString(4, matKhau);
            stmt.setString(5, user.getVaiTro());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Lấy ma_nguoi_dung vừa được Supabase tự tạo
                try (PreparedStatement stGet = conn.prepareStatement("SELECT ma_nguoi_dung::TEXT FROM nguoi_dung WHERE so_dien_thoai = ?")) {
                    stGet.setString(1, user.getSoDienThoai());
                    ResultSet rsGet = stGet.executeQuery();
                    if (rsGet.next()) user.setMaNguoiDung(rsGet.getString(1));
                } catch (Exception e) {
                    try (PreparedStatement stGet = conn.prepareStatement("SELECT ma_nguoi_dung FROM nguoi_dung WHERE so_dien_thoai = ?")) {
                        stGet.setString(1, user.getSoDienThoai());
                        ResultSet rsGet = stGet.executeQuery();
                        if (rsGet.next()) user.setMaNguoiDung(rsGet.getString("ma_nguoi_dung"));
                    } catch (Exception ignored) {}
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean capNhatThongTinHoanThien(User user, String gioiTinh, String ngaySinh, String diaChi) throws SQLException {
        return capNhatThongTinHoanThien(user, gioiTinh, ngaySinh, diaChi, "");
    }

    public boolean capNhatThongTinHoanThien(User user, String gioiTinh, String ngaySinh, String diaChi, String urlAnh) throws SQLException {
        String sqlUpdateUser = "UPDATE nguoi_dung SET ho_ten = ?, gioi_tinh = ?, ngay_sinh = ?, dia_chi = ? WHERE so_dien_thoai = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUpdateUser)) {
            stmt.setString(1, user.getHoTen());
            stmt.setString(2, gioiTinh);
            stmt.setString(3, ngaySinh);
            stmt.setString(4, diaChi);
            stmt.setString(5, user.getSoDienThoai());
            stmt.executeUpdate();
        }

        // Nếu user.getMaNguoiDung() chưa có (do session cũ), tra cứu lại theo sđt
        String maUserStr = user.getMaNguoiDung();
        if (maUserStr == null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stGet = conn.prepareStatement("SELECT ma_nguoi_dung::TEXT FROM nguoi_dung WHERE so_dien_thoai = ?")) {
                stGet.setString(1, user.getSoDienThoai());
                ResultSet rsGet = stGet.executeQuery();
                if (rsGet.next()) { maUserStr = rsGet.getString(1); user.setMaNguoiDung(maUserStr); }
            } catch (Exception ignored) {}
        }

        // Đảm bảo các bảng liên quan tồn tại (phòng trường hợp test local chưa chạy full script SQL)
        // Tìm ma_so_do hiện tại của tài khoản
        String maSoDoStr = null;
        if (maUserStr != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stFind = conn.prepareStatement("SELECT ma_so_do::TEXT FROM thanh_vien_so_do WHERE ma_nguoi_dung::TEXT = ? LIMIT 1")) {
                stFind.setString(1, maUserStr);
                ResultSet rs = stFind.executeQuery();
                if (rs.next()) maSoDoStr = rs.getString(1);
            } catch (Exception e) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stFind = conn.prepareStatement("SELECT ma_so_do FROM thanh_vien_so_do WHERE ma_nguoi_dung = ? LIMIT 1")) {
                    stFind.setString(1, maUserStr);
                    ResultSet rs = stFind.executeQuery();
                    if (rs.next()) maSoDoStr = rs.getString(1);
                } catch (Exception ignored) {}
            }
        }

        // Nếu chưa tham gia/tạo sơ đồ nào -> Tạo mới sơ đồ gia phả cho dòng họ này
        if (maSoDoStr == null && maUserStr != null) {
            String validUser = maUserStr.matches("^[0-9a-fA-F\\-]{36}$") ? maUserStr : java.util.UUID.randomUUID().toString();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stTree = conn.prepareStatement("INSERT INTO so_do_gia_pha (ma_chu_so_do, ten_so_do, mo_ta) VALUES (?::UUID, ?, ?) RETURNING ma_so_do::TEXT")) {
                stTree.setString(1, validUser);
                stTree.setString(2, "Gia phả của " + user.getHoTen());
                stTree.setString(3, "Được tạo tự động khi hoàn thiện thông tin");
                ResultSet rsTree = stTree.executeQuery();
                if (rsTree.next()) maSoDoStr = rsTree.getString(1);
            } catch (Exception ex) {
                String fallbackId = java.util.UUID.randomUUID().toString();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stTree = conn.prepareStatement("INSERT INTO so_do_gia_pha (ma_so_do, ma_chu_so_do, ten_so_do, mo_ta) VALUES (?::UUID, ?::UUID, ?, ?)")) {
                    stTree.setString(1, fallbackId);
                    stTree.setString(2, validUser);
                    stTree.setString(3, "Gia phả của " + user.getHoTen());
                    stTree.setString(4, "Được tạo tự động");
                    stTree.executeUpdate();
                    maSoDoStr = fallbackId;
                } catch (Exception ignored) {}
            }

            if (maSoDoStr != null) {
                String validSoDo = maSoDoStr.matches("^[0-9a-fA-F\\-]{36}$") ? maSoDoStr : java.util.UUID.randomUUID().toString();
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stMem = conn.prepareStatement("INSERT INTO thanh_vien_so_do (ma_so_do, ma_nguoi_dung, vai_tro) VALUES (?::UUID, ?::UUID, ?) ON CONFLICT DO NOTHING")) {
                    stMem.setString(1, validSoDo);
                    stMem.setString(2, validUser);
                    stMem.setString(3, user.getVaiTro());
                    stMem.executeUpdate();
                } catch (Exception ex) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stMem = conn.prepareStatement("INSERT INTO thanh_vien_so_do (ma_so_do, ma_nguoi_dung, vai_tro) VALUES (?, ?, ?)")) {
                        stMem.setString(1, validSoDo);
                        stMem.setString(2, validUser);
                        stMem.setString(3, user.getVaiTro());
                        stMem.executeUpdate();
                    } catch (Exception ignored) {}
                }
            }
        }

        // Thực thi lưu xuống bảng nguoi_trong_gia_pha
        if (maSoDoStr != null) {
            String ns = (ngaySinh != null && ngaySinh.matches("^\\d{4}-\\d{2}-\\d{2}$")) ? ngaySinh : "1990-01-01";
            String validSoDo = maSoDoStr.matches("^[0-9a-fA-F\\-]{36}$") ? maSoDoStr : java.util.UUID.randomUUID().toString();
            String validUser = (maUserStr != null && maUserStr.matches("^[0-9a-fA-F\\-]{36}$")) ? maUserStr : java.util.UUID.randomUUID().toString();

            boolean exists = false;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stCheck = conn.prepareStatement("SELECT 1 FROM nguoi_trong_gia_pha WHERE ma_so_do::TEXT = ? AND ma_nguoi_dung_lien_ket::TEXT = ?")) {
                stCheck.setString(1, validSoDo);
                stCheck.setString(2, validUser);
                ResultSet rsCheck = stCheck.executeQuery();
                exists = rsCheck.next();
            } catch (Exception ignored) {}

            if (exists) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stUp = conn.prepareStatement("UPDATE nguoi_trong_gia_pha SET ho_ten=?, gioi_tinh=?, dia_chi=?, url_anh_dai_dien=? WHERE ma_so_do::TEXT=? AND ma_nguoi_dung_lien_ket::TEXT=?")) {
                    stUp.setString(1, user.getHoTen());
                    stUp.setString(2, gioiTinh);
                    stUp.setString(3, diaChi);
                    stUp.setString(4, urlAnh != null ? urlAnh : "");
                    stUp.setString(5, validSoDo);
                    stUp.setString(6, validUser);
                    stUp.executeUpdate();
                    System.out.println("[UserRepository] Đã cập nhật thông tin thành viên kiêm ảnh đại diện trong nguoi_trong_gia_pha!");
                } catch (Exception ignored) {}
            } else {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stPer = conn.prepareStatement(
                        "INSERT INTO nguoi_trong_gia_pha (ma_so_do, ma_nguoi_dung_lien_ket, ho_ten, gioi_tinh, ngay_sinh_duong_lich, so_dien_thoai, email_ca_nhan, dia_chi, con_song, url_anh_dai_dien) " +
                        "VALUES (?::UUID, ?::UUID, ?, ?, ?::DATE, ?, ?, ?, TRUE, ?)")) {
                    stPer.setString(1, validSoDo);
                    stPer.setString(2, validUser);
                    stPer.setString(3, user.getHoTen());
                    stPer.setString(4, gioiTinh);
                    stPer.setString(5, ns);
                    stPer.setString(6, user.getSoDienThoai());
                    stPer.setString(7, user.getEmail());
                    stPer.setString(8, diaChi);
                    stPer.setString(9, urlAnh != null ? urlAnh : "");
                    stPer.executeUpdate();
                    System.out.println("[UserRepository] Đã thêm mới thành viên kiêm ảnh đại diện vào bảng nguoi_trong_gia_pha!");
                } catch (Exception ex) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stPer = conn.prepareStatement(
                            "INSERT INTO nguoi_trong_gia_pha (ma_nguoi, ma_so_do, ma_nguoi_dung_lien_ket, ho_ten, gioi_tinh, ngay_sinh_duong_lich, so_dien_thoai, email_ca_nhan, dia_chi, con_song, url_anh_dai_dien) " +
                            "VALUES (?::UUID, ?::UUID, ?::UUID, ?, ?, ?::DATE, ?, ?, ?, TRUE, ?)")) {
                        stPer.setString(1, java.util.UUID.randomUUID().toString());
                        stPer.setString(2, validSoDo);
                        stPer.setString(3, validUser);
                        stPer.setString(4, user.getHoTen());
                        stPer.setString(5, gioiTinh);
                        stPer.setString(6, ns);
                        stPer.setString(7, user.getSoDienThoai());
                        stPer.setString(8, user.getEmail());
                        stPer.setString(9, diaChi);
                        stPer.setString(10, urlAnh != null ? urlAnh : "");
                        stPer.executeUpdate();
                        System.out.println("[UserRepository] Đã lưu thành viên kiêm ảnh đại diện (Fallback Local)!");
                    } catch (Exception ignored) {}
                }
            }
        }

        return true;
    }

    @Override
    public String timKiemVaThamGiaSoDo(String maThamGia, User user) throws SQLException {
        String sqlFind = "SELECT ma_so_do, ten_so_do, mo_ta FROM so_do_gia_pha WHERE UPPER(ma_tham_gia) = UPPER(?) LIMIT 1";
        String maSoDo = null, tenSoDo = null, moTa = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlFind)) {
            stmt.setString(1, maThamGia.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                maSoDo = rs.getString("ma_so_do");
                tenSoDo = rs.getString("ten_so_do");
                moTa = rs.getString("mo_ta");
            }
        }

        if (maSoDo == null) return null;

        if (user != null && user.getMaNguoiDung() != null) {
            String sqlJoin = "INSERT INTO thanh_vien_so_do (ma_so_do, ma_nguoi_dung, vai_tro, tham_gia_bang_ma) VALUES (?, ?, 'Chủ_nhà', TRUE) ON CONFLICT DO NOTHING";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlJoin)) {
                stmt.setString(1, maSoDo);
                stmt.setString(2, user.getMaNguoiDung());
                stmt.executeUpdate();
            }
        }

        return "Tên dòng họ: " + tenSoDo + "\nMô tả: " + (moTa != null ? moTa : "");
    }
}
