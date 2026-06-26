package model;

import java.sql.SQLException;

/**
 * AuthService - Triển khai cụ thể của IAuthService
 * Xử lý logic đăng nhập qua Supabase
 *
 * Nguyên tắc OOP: TÍNH ĐA HÌNH (Polymorphism)
 * - implements IAuthService → LoginController chỉ biết IAuthService
 *   có thể hoán đổi AuthService bằng MockAuthService khi test
 *
 * Nguyên tắc OOP: TÍNH TRỪ TƯỢNG (Abstraction)
 * - Sử dụng IUserRepository thay vì UserRepository cụ thể
 * - Không cần biết dưới là SQL hay API hay file
 */
public class AuthService implements IAuthService {

    // Dùng INTERFACE, không dùng class cụ thể → Tính Đa Hình
    private final IUserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository(); // Mặc định dùng kết nối thật
    }

    // Constructor cho phép Inject tùy chọn → Tính Đa Hình
    public AuthService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Kết quả đăng nhập - trả về cho Controller
     */
    public static class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongBao;
        private final User nguoiDung;

        public KetQuaDangNhap(boolean thanhCong, String thongBao, User nguoiDung) {
            this.thanhCong = thanhCong;
            this.thongBao = thongBao;
            this.nguoiDung = nguoiDung;
        }

        public boolean isThanhCong()    { return thanhCong; }
        public String getThongBao()     { return thongBao; }
        public User getNguoiDung()      { return nguoiDung; }
    }

    /**
     * Thực hiện đăng nhập bằng SỐ ĐIỆN THOẠI + MẬT KHẨU
     *
     * @param soDienThoai  Số điện thoại người dùng nhập
     * @param matKhau      Mật khẩu người dùng nhập
     * @return KetQuaDangNhap
     */
    public KetQuaDangNhap dangNhap(String soDienThoai, String matKhau) {

        // Bước 1: Validate input
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập số điện thoại!", null);
        }

        // Số điện thoại chỉ chứa số, có thể bắt đầu bằng 0 hoặc +84
        String sdt = soDienThoai.trim().replaceAll("\\s+", "");
        if (!sdt.matches("^(0|\\+84)[0-9]{8,10}$")) {
            return new KetQuaDangNhap(false,
                    "Số điện thoại không hợp lệ!\nVD: 0912345678 hoặc +84912345678", null);
        }

        if (matKhau == null || matKhau.isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập mật khẩu!", null);
        }

        if (matKhau.length() < 6) {
            return new KetQuaDangNhap(false, "Mật khẩu phải có ít nhất 6 ký tự!", null);
        }

        // Bước 2: Gọi Repository xác thực
        try {
            User nguoiDung = userRepository.xacThucDangNhap(sdt, matKhau);

            if (nguoiDung != null) {
                // Kiểm tra tài khoản có bị khóa không
                if (!nguoiDung.isHoatDong()) {
                    return new KetQuaDangNhap(false,
                            "Tài khoản của bạn đã bị khóa.\nVui lòng liên hệ quản trị viên!", null);
                }

                // Bước 3: Lưu session
                UserSession.getInstance().dangNhap(nguoiDung);
                System.out.println("[AuthService] Đăng nhập thành công: " + nguoiDung.getHoTen());

                return new KetQuaDangNhap(true,
                        "Chào mừng " + nguoiDung.getHoTen() + "!", nguoiDung);

            } else {
                return new KetQuaDangNhap(false,
                        "Số điện thoại hoặc mật khẩu không đúng!\nVui lòng thử lại.", null);
            }

        } catch (SQLException e) {
            System.err.println("[AuthService] Lỗi database: " + e.getMessage());
            return new KetQuaDangNhap(false,
                    "Không thể kết nối đến server!\nKiểm tra kết nối internet và thử lại.", null);
        }
    }

    /** Đăng xuất */
    public void dangXuat() {
        UserSession.getInstance().dangXuat();
    }

    /** Kiểm tra đã đăng nhập chưa */
    public boolean daDangNhap() {
        return UserSession.getInstance().daDangNhap();
    }

    public KetQuaDangNhap dangKy(String hoTen, String soDienThoai, String email, String matKhau) {
        return dangKy(hoTen, soDienThoai, email, matKhau, "Chủ_nhà");
    }

    @Override
    public KetQuaDangNhap dangKy(String hoTen, String soDienThoai, String email, String matKhau, String vaiTro) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập họ và tên!", null);
        }
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập số điện thoại!", null);
        }
        String sdt = soDienThoai.trim().replaceAll("\\s+", "");
        if (!sdt.matches("^(0|\\+84)[0-9]{8,10}$")) {
            return new KetQuaDangNhap(false, "Số điện thoại không hợp lệ!", null);
        }
        if (matKhau == null || matKhau.length() < 6) {
            return new KetQuaDangNhap(false, "Mật khẩu phải từ 6 ký tự trở lên!", null);
        }

        try {
            if (userRepository.tonTaiSoDienThoai(sdt)) {
                return new KetQuaDangNhap(false, "Số điện thoại này đã được đăng ký!", null);
            }

            User user = new User();
            user.setHoTen(hoTen.trim());
            user.setSoDienThoai(sdt);
            user.setEmail(email != null ? email.trim() : "");
            user.setVaiTro(vaiTro);

            boolean thanhCong = userRepository.dangKyTaiKhoan(user, matKhau);
            if (thanhCong) {
                UserSession.getInstance().dangNhap(user);
                return new KetQuaDangNhap(true, "Đăng ký thành công! Hãy bổ sung thông tin gia phả.", user);
            } else {
                return new KetQuaDangNhap(false, "Không thể lưu vào cơ sở dữ liệu.", null);
            }
        } catch (SQLException e) {
            System.err.println("[AuthService] Lỗi DB khi đăng ký: " + e.getMessage());
            return new KetQuaDangNhap(false, "Lỗi kết nối máy chủ Supabase: " + e.getMessage(), null);
        }
    }
}
