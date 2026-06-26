package model;

import java.sql.SQLException;

/**
 * IUserRepository - Interface cho tầng truy vấn người dùng
 *
 * Nguyên tắc OOP: TÍNH TRỪU TƯỢNG + TÍNH ĐA HÌNH
 * - Trừu tượng: Ẩn chi tiết SQL, chỉ lộ ra phương thức cần dùng
 * - Đa hình: Có thể có nhiều cách triển khai khác nhau:
 *     + UserRepository        → kết nối Supabase thật
 *     + MockUserRepository    → dữ liệu giả để test
 *     + CachedUserRepository  → có cache tăng hiệu năng
 */
public interface IUserRepository {

    /**
     * Xác thực đăng nhập bằng số điện thoại và mật khẩu
     *
     * @param soDienThoai Số điện thoại người dùng nhập
     * @param matKhau     Mật khẩu người dùng nhập
     * @return User nếu đúng, null nếu sai
     * @throws SQLException nếu lỗi kết nối database
     */
    User xacThucDangNhap(String soDienThoai, String matKhau) throws SQLException;

    /**
     * Kiểm tra số điện thoại đã tồn tại trong hệ thống chưa
     *
     * @param soDienThoai Số điện thoại cần kiểm tra
     * @return true nếu đã tồn tại
     * @throws SQLException nếu lỗi kết nối database
     */
    boolean tonTaiSoDienThoai(String soDienThoai) throws SQLException;

    /**
     * Đăng ký tài khoản mới vào cơ sở dữ liệu
     *
     * @param user    Thông tin cơ bản của người dùng mới
     * @param matKhau Mật khẩu plain text (SQL sẽ tự hash)
     * @return true nếu lưu thành công
     * @throws SQLException nếu lỗi truy vấn
     */
    boolean dangKyTaiKhoan(User user, String matKhau) throws SQLException;

    /**
     * Cập nhật thông tin chi tiết (Giới tính, Ngày sinh, Địa chỉ) vào cơ sở dữ liệu
     */
    boolean capNhatThongTinHoanThien(User user, String gioiTinh, String ngaySinh, String diaChi) throws SQLException;

    /**
     * Tìm kiếm sơ đồ theo mã và tham gia sơ đồ
     */
    String timKiemVaThamGiaSoDo(String maThamGia, User user) throws SQLException;
}
