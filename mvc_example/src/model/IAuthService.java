package model;

/**
 * IAuthService - Interface cho tầng nghiệp vụ xác thực
 *
 * Nguyên tắc OOP: TÍNH TRỪU TƯỢNG + TÍNH ĐA HÌNH
 * - Trừu tượng: Controller chỉ biết "gọi dangNhap()", không biết cách thực hiện
 * - Đa hình: Nhiều cách xác thực khác nhau cùng implement interface này:
 *     + AuthService       → xác thực qua Supabase (thật)
 *     + MockAuthService   → xác thực giả để test UI
 *     + OTPAuthService    → xác thực qua mã OTP (mở rộng)
 */
public interface IAuthService {

    /**
     * Thực hiện đăng nhập
     *
     * @param soDienThoai Số điện thoại người dùng nhập
     * @param matKhau     Mật khẩu người dùng nhập
     * @return KetQuaDangNhap chứa trạng thái + thông báo + thông tin user
     */
    AuthService.KetQuaDangNhap dangNhap(String soDienThoai, String matKhau);

    /**
     * Đăng xuất người dùng hiện tại
     */
    void dangXuat();

    /**
     * Kiểm tra người dùng đã đăng nhập chưa
     *
     * @return true nếu đã đăng nhập
     */
    boolean daDangNhap();

    /**
     * Đăng ký tài khoản mới kèm vai trò
     */
    AuthService.KetQuaDangNhap dangKy(String hoTen, String soDienThoai, String email, String matKhau, String vaiTro);
}
