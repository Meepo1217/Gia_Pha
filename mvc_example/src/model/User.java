package model;

/**
 * User - Model người dùng hệ thống
 * Ánh xạ tới bảng 'nguoi_dung' trong Supabase
 *
 * Schema thực tế:
 * - ma_nguoi_dung UUID PRIMARY KEY
 * - ho_ten TEXT NOT NULL
 * - email VARCHAR(150) UNIQUE (có thể null)
 * - so_dien_thoai VARCHAR(20) UNIQUE NOT NULL  ← Dùng để đăng nhập
 * - mat_khau_hash TEXT NOT NULL
 * - ngon_ngu_mac_dinh VARCHAR(10) DEFAULT 'vi'
 * - trang_thai TEXT DEFAULT 'hoat_dong'
 */
public class User {
    private String maNguoiDung;
    private String hoTen;
    private String email;           // Có thể null
    private String soDienThoai;     // Dùng để đăng nhập
    private String ngonNguMacDinh;  // 'vi' hoặc 'en'
    private String trangThai;       // 'hoat_dong' hoặc 'bi_khoa'
    private String vaiTro;          // 'Chủ_họ' hoặc 'Chủ_nhà'

    // Constructor rỗng
    public User() {}

    // Constructor cơ bản
    public User(String maNguoiDung, String hoTen, String soDienThoai, String trangThai) {
        this.maNguoiDung = maNguoiDung;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.trangThai = trangThai;
    }

    // ===== GETTERS & SETTERS =====

    public String getMaNguoiDung()                      { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung)      { this.maNguoiDung = maNguoiDung; }

    public String getHoTen()                            { return hoTen; }
    public void setHoTen(String hoTen)                  { this.hoTen = hoTen; }

    public String getEmail()                            { return email; }
    public void setEmail(String email)                  { this.email = email; }

    public String getSoDienThoai()                      { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai)      { this.soDienThoai = soDienThoai; }

    public String getNgonNguMacDinh()                   { return ngonNguMacDinh; }
    public void setNgonNguMacDinh(String ngonNgu)       { this.ngonNguMacDinh = ngonNgu; }

    public String getTrangThai()                        { return trangThai; }
    public void setTrangThai(String trangThai)          { this.trangThai = trangThai; }

    public String getVaiTro()                           { return vaiTro != null ? vaiTro : "Chủ_nhà"; }
    public void setVaiTro(String vaiTro)                { this.vaiTro = vaiTro; }

    // ===== PHƯƠNG THỨC TIỆN ÍCH =====

    public boolean isHoatDong() {
        return "hoat_dong".equals(trangThai);
    }

    public boolean isDungTiengViet() {
        return "vi".equals(ngonNguMacDinh);
    }

    @Override
    public String toString() {
        return "User{maNguoiDung='" + maNguoiDung
                + "', hoTen='" + hoTen
                + "', soDienThoai='" + soDienThoai + "'}";
    }
}
