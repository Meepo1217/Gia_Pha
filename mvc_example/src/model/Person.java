package model;

/**
 * Person class - Represents a member in the family tree
 * Maps to the 'thanh_vien' table in Supabase
 */
public class Person {
    private String id;
    private String giaPhaId;
    private String hoTen;
    private String gioiTinh;       // "nam" hoặc "nu"
    private String ngaySinh;       // Định dạng: dd/MM/yyyy
    private String ngayMat;        // null nếu còn sống
    private boolean conSong;
    private String noiSinh;
    private String ngheNghiep;
    private String tieuSu;
    private String anhDaiDienUrl;   // URL ảnh trên Cloudflare R2

    // Constructors
    public Person() {}

    public Person(String hoTen, String gioiTinh, String ngaySinh, boolean conSong) {
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.conSong = conSong;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGiaPhaId() { return giaPhaId; }
    public void setGiaPhaId(String giaPhaId) { this.giaPhaId = giaPhaId; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getNgayMat() { return ngayMat; }
    public void setNgayMat(String ngayMat) { this.ngayMat = ngayMat; }

    public boolean isConSong() { return conSong; }
    public void setConSong(boolean conSong) { this.conSong = conSong; }

    public String getNoiSinh() { return noiSinh; }
    public void setNoiSinh(String noiSinh) { this.noiSinh = noiSinh; }

    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }

    public String getTieuSu() { return tieuSu; }
    public void setTieuSu(String tieuSu) { this.tieuSu = tieuSu; }

    public String getAnhDaiDienUrl() { return anhDaiDienUrl; }
    public void setAnhDaiDienUrl(String anhDaiDienUrl) { this.anhDaiDienUrl = anhDaiDienUrl; }

    /**
     * Trả về khoảng năm hiển thị trên thẻ cây gia phả
     * VD: "1920 - 1995" hoặc "1950 - Hiện tại"
     */
    public String getKhoangNam() {
        String namSinh = (ngaySinh != null && ngaySinh.length() >= 4)
                ? ngaySinh.substring(ngaySinh.length() - 4) : "?";
        if (conSong) {
            return namSinh + " - Hiện tại";
        } else {
            String namMat = (ngayMat != null && ngayMat.length() >= 4)
                    ? ngayMat.substring(ngayMat.length() - 4) : "?";
            return namSinh + " - " + namMat;
        }
    }

    @Override
    public String toString() {
        return "Person{hoTen='" + hoTen + "', ngaySinh='" + ngaySinh + "'}";
    }
}
