package model;

/**
 * UserSession - Lưu thông tin người dùng đang đăng nhập
 * Dùng Singleton Pattern: chỉ có 1 session duy nhất trong toàn app
 *
 * Nguyên tắc OOP: Singleton Pattern + Encapsulation
 * - Đảm bảo chỉ 1 instance tồn tại
 * - Tất cả màn hình có thể truy cập thông tin user hiện tại
 */
public class UserSession {

    // Instance duy nhất (Singleton)
    private static UserSession instance;

    // Thông tin người dùng hiện tại
    private User nguoiDungHienTai;
    private boolean daDangNhap;

    // Constructor private - không cho tạo từ bên ngoài
    private UserSession() {
        this.daDangNhap = false;
        this.nguoiDungHienTai = null;
    }

    /**
     * Lấy instance duy nhất (Thread-safe Singleton)
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Ghi nhận đăng nhập thành công
     * Gọi sau khi AuthService xác thực OK
     */
    public void dangNhap(User nguoiDung) {
        this.nguoiDungHienTai = nguoiDung;
        this.daDangNhap = true;
        System.out.println("[UserSession] Đã lưu session: " + nguoiDung.getHoTen());
    }

    /**
     * Xóa session khi đăng xuất
     */
    public void dangXuat() {
        this.nguoiDungHienTai = null;
        this.daDangNhap = false;
        System.out.println("[UserSession] Đã xóa session.");
    }

    // ===== GETTERS =====

    public User getNguoiDungHienTai()   { return nguoiDungHienTai; }
    public boolean daDangNhap()         { return daDangNhap; }

    /**
     * Lấy tên người dùng hiện tại (tiện ích)
     */
    public String getTenNguoiDung() {
        if (nguoiDungHienTai != null) {
            return nguoiDungHienTai.getHoTen();
        }
        return "Khách";
    }

    /**
     * Lấy ID người dùng hiện tại (dùng khi truy vấn dữ liệu)
     */
    public String getIdNguoiDung() {
        if (nguoiDungHienTai != null) {
            return nguoiDungHienTai.getMaNguoiDung();
        }
        return null;
    }
}
