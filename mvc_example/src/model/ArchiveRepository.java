package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * ArchiveRepository - Quản lý truy vấn kho lưu trữ đa phương tiện (Ảnh, Video, Tài liệu)
 * Triển khai chuẩn OOP DAO Pattern + Encapsulation
 */
public class ArchiveRepository {

    public static class AlbumDTO {
        private String tenAlbum;
        private int soLuongAnh;
        private String urlAnhDaiDien;

        public AlbumDTO(String tenAlbum, int soLuongAnh, String urlAnhDaiDien) {
            this.tenAlbum = tenAlbum;
            this.soLuongAnh = soLuongAnh;
            this.urlAnhDaiDien = urlAnhDaiDien;
        }

        public String getTenAlbum() { return tenAlbum; }
        public int getSoLuongAnh() { return soLuongAnh; }
        public String getUrlAnhDaiDien() { return urlAnhDaiDien; }
    }

    public static class MediaDTO {
        private String id;
        private String url;
        private String chuThich;
        private String tenAlbum;

        public MediaDTO(String id, String url, String chuThich, String tenAlbum) {
            this.id = id;
            this.url = url;
            this.chuThich = chuThich;
            this.tenAlbum = tenAlbum;
        }

        public String getId() { return id; }
        public String getUrl() { return url; }
        public String getChuThich() { return chuThich; }
        public String getTenAlbum() { return tenAlbum; }
    }

    public static class DocumentDTO {
        private String id;
        private String tenTaiLieu;
        private String url;
        private String kieuMime;
        private long kichThuocByte;
        private String ngayTaoStr;

        public DocumentDTO(String id, String tenTaiLieu, String url, String kieuMime, long kichThuocByte, String ngayTaoStr) {
            this.id = id;
            this.tenTaiLieu = tenTaiLieu;
            this.url = url;
            this.kieuMime = kieuMime;
            this.kichThuocByte = kichThuocByte;
            this.ngayTaoStr = ngayTaoStr;
        }

        public String getId() { return id; }
        public String getTenTaiLieu() { return tenTaiLieu; }
        public String getUrl() { return url; }
        public String getKieuMime() { return kieuMime; }
        public long getKichThuocByte() { return kichThuocByte; }
        public String getNgayTaoStr() { return ngayTaoStr; }
    }

    public void chuanHoaLinkCdnCu() {
        String sql = "UPDATE tep_tin SET duong_dan_tep = REPLACE(duong_dan_tep, 'https://pub-DIEN_SUBDOMAIN_R2_DEV_CUA_BAN.r2.dev', 'https://pub-7a40043ee2ed4d8e8e87e5c0d54c65d5.r2.dev') WHERE duong_dan_tep LIKE '%pub-DIEN_SUBDOMAIN%'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.executeUpdate();
        } catch (Exception ignored) {}
    }

    /**
     * Lấy danh sách các album cùng ảnh đại diện mới nhất của bộ sưu tập
     */
    public List<AlbumDTO> layDanhSachAlbum(String maSoDo) {
        List<AlbumDTO> list = new ArrayList<>();
        if (maSoDo == null || maSoDo.isEmpty()) return list;

        String sql = "SELECT COALESCE(a.ten_album, 'Mặc định') AS album_name, COUNT(*) AS cnt, MAX(t.duong_dan_tep) AS thumb " +
                     "FROM anh_album_gia_dinh a JOIN tep_tin t ON a.ma_tep = t.ma_tep " +
                     "WHERE a.ma_so_do::text = ? " +
                     "GROUP BY COALESCE(a.ten_album, 'Mặc định')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maSoDo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new AlbumDTO(rs.getString("album_name"), rs.getInt("cnt"), rs.getString("thumb")));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi layDanhSachAlbum: " + ex.getMessage());
        }
        return list;
    }

    /**
     * Lấy toàn bộ ảnh/video thuộc một album cụ thể
     */
    public List<MediaDTO> layMediaTrongAlbum(String maSoDo, String tenAlbum) {
        List<MediaDTO> list = new ArrayList<>();
        if (maSoDo == null) return list;

        String sql = "SELECT a.ma_anh_album, t.duong_dan_tep, a.chu_thich, COALESCE(a.ten_album, 'Mặc định') " +
                     "FROM anh_album_gia_dinh a JOIN tep_tin t ON a.ma_tep = t.ma_tep " +
                     "WHERE a.ma_so_do::text = ? AND COALESCE(a.ten_album, 'Mặc định') = ? " +
                     "ORDER BY a.ngay_tao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maSoDo);
            st.setString(2, tenAlbum != null ? tenAlbum : "Mặc định");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new MediaDTO(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi layMediaTrongAlbum: " + ex.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách tài liệu quan trọng của phả hệ
     */
    public List<DocumentDTO> layDanhSachTaiLieu(String maSoDo) {
        List<DocumentDTO> list = new ArrayList<>();
        if (maSoDo == null) return list;

        String sql = "SELECT ma_tep, COALESCE(ten_tep_goc, 'Tài liệu không tên'), duong_dan_tep, COALESCE(kieu_mime, 'application/pdf'), COALESCE(kich_thuoc_byte, 0), TO_CHAR(ngay_tao, 'DD/MM/YYYY') " +
                     "FROM tep_tin WHERE ma_so_do::text = ? AND loai_tep = 'tai_lieu_mo_ta' ORDER BY ngay_tao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maSoDo);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new DocumentDTO(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getString(6)));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi layDanhSachTaiLieu: " + ex.getMessage());
        }
        return list;
    }

    /**
     * Lưu ảnh/video gia đình vào CSDL sau khi upload lên Cloudflare
     */
    public boolean luuAnhVideoAlbum(String maSoDo, String tenAlbum, String url, String tenFileGoc, String mimeType, long sizeBytes) {
        String sqlTep = "INSERT INTO tep_tin (ma_so_do, ten_bucket, duong_dan_tep, ten_tep_goc, kieu_mime, kich_thuoc_byte, loai_tep) " +
                        "VALUES (?::uuid, 'giapha', ?, ?, ?, ?, 'album_gia_dinh') RETURNING ma_tep";
        String sqlAlbum = "INSERT INTO anh_album_gia_dinh (ma_so_do, ma_tep, ten_album, chu_thich, ngay_tao) VALUES (?::uuid, ?::uuid, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            String maTep = null;
            try (PreparedStatement stTep = conn.prepareStatement(sqlTep)) {
                stTep.setString(1, maSoDo);
                stTep.setString(2, url);
                stTep.setString(3, tenFileGoc);
                stTep.setString(4, mimeType);
                stTep.setLong(5, sizeBytes);
                ResultSet rs = stTep.executeQuery();
                if (rs.next()) maTep = rs.getString(1);
            }

            if (maTep != null) {
                try (PreparedStatement stAlb = conn.prepareStatement(sqlAlbum)) {
                    stAlb.setString(1, maSoDo);
                    stAlb.setString(2, maTep);
                    stAlb.setString(3, (tenAlbum == null || tenAlbum.trim().isEmpty()) ? "Mặc định" : tenAlbum.trim());
                    stAlb.setString(4, tenFileGoc);
                    stAlb.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi luuAnhVideoAlbum: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Lưu thông tin tài liệu quan trọng vào CSDL sau khi upload lên Cloudflare
     */
    public boolean luuTaiLieuQuanTrong(String maSoDo, String tenTaiLieu, String url, String tenFileGoc, String mimeType, long sizeBytes) {
        String sql = "INSERT INTO tep_tin (ma_so_do, ten_bucket, duong_dan_tep, ten_tep_goc, kieu_mime, kich_thuoc_byte, loai_tep) " +
                     "VALUES (?::uuid, 'giapha', ?, ?, ?, ?, 'tai_lieu_mo_ta')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maSoDo);
            st.setString(2, url);
            st.setString(3, (tenTaiLieu != null && !tenTaiLieu.trim().isEmpty()) ? tenTaiLieu.trim() : tenFileGoc);
            st.setString(4, mimeType);
            st.setLong(5, sizeBytes);
            st.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi luuTaiLieuQuanTrong: " + ex.getMessage());
            return false;
        }
    }

    public boolean xoaMedia(String maAnhAlbum) {
        if (maAnhAlbum == null || maAnhAlbum.isEmpty()) return false;
        String queryUrl = "SELECT t.duong_dan_tep FROM anh_album_gia_dinh a JOIN tep_tin t ON a.ma_tep = t.ma_tep WHERE a.ma_anh_album::text = ?";
        String cdnUrl = null;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement st = conn.prepareStatement(queryUrl)) {
            st.setString(1, maAnhAlbum);
            ResultSet rs = st.executeQuery();
            if (rs.next()) cdnUrl = rs.getString(1);
        } catch (Exception ignored) {}

        String sql = "DELETE FROM tep_tin WHERE ma_tep = (SELECT ma_tep FROM anh_album_gia_dinh WHERE ma_anh_album::text = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maAnhAlbum);
            int cnt = st.executeUpdate();
            if (cnt == 0) {
                try (PreparedStatement st2 = conn.prepareStatement("DELETE FROM anh_album_gia_dinh WHERE ma_anh_album::text = ?")) {
                    st2.setString(1, maAnhAlbum);
                    st2.executeUpdate();
                }
            }
            if (cdnUrl != null) CloudflareStorageService.getInstance().deleteFile(cdnUrl);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi xoaMedia: " + ex.getMessage());
        }
        return false;
    }

    public boolean xoaTaiLieu(String maTep) {
        if (maTep == null || maTep.isEmpty()) return false;
        String queryUrl = "SELECT duong_dan_tep FROM tep_tin WHERE ma_tep::text = ?";
        String cdnUrl = null;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement st = conn.prepareStatement(queryUrl)) {
            st.setString(1, maTep);
            ResultSet rs = st.executeQuery();
            if (rs.next()) cdnUrl = rs.getString(1);
        } catch (Exception ignored) {}

        String sql = "DELETE FROM tep_tin WHERE ma_tep::text = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, maTep);
            st.executeUpdate();
            if (cdnUrl != null) CloudflareStorageService.getInstance().deleteFile(cdnUrl);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi xoaTaiLieu: " + ex.getMessage());
        }
        return false;
    }

    public boolean xoaAlbum(String maSoDo, String tenAlbum) {
        if (maSoDo == null || tenAlbum == null) return false;
        String query = "SELECT t.ma_tep, t.duong_dan_tep FROM anh_album_gia_dinh a JOIN tep_tin t ON a.ma_tep = t.ma_tep WHERE a.ma_so_do::text = ? AND COALESCE(a.ten_album, 'Mặc định') = ?";
        List<String> maTeps = new ArrayList<>();
        List<String> cdnUrls = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, maSoDo);
            st.setString(2, tenAlbum);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                maTeps.add(rs.getString(1));
                cdnUrls.add(rs.getString(2));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi query xoaAlbum: " + ex.getMessage());
        }

        if (maTeps.isEmpty()) return false;

        String delSql = "DELETE FROM tep_tin WHERE ma_tep::text = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement st = conn.prepareStatement(delSql)) {
            for (String mt : maTeps) {
                st.setString(1, mt);
                st.addBatch();
            }
            st.executeBatch();
            for (String url : cdnUrls) {
                if (url != null) CloudflareStorageService.getInstance().deleteFile(url);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi delSql xoaAlbum: " + ex.getMessage());
        }
        return false;
    }
}
