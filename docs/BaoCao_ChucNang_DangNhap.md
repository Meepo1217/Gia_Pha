# BÁO CÁO CHỨC NĂNG: ĐĂNG NHẬP TÀI KHOẢN
## Ứng dụng Quản Lý Sơ Đồ Gia Phả

---

## I. TỔNG QUAN

Chức năng **Đăng nhập** cho phép xác thực bằng **số điện thoại + mật khẩu** để truy cập hệ thống quản lý sơ đồ gia phả.

---

## II. CHỨC NĂNG TỪNG FILE

### 1. `view/Login.fxml` — Giao diện
| Thành phần | fx:id | Chức năng |
|---|---|---|
| TextField | `truongSoDienThoai` | Ô nhập số điện thoại |
| PasswordField | `truongMatKhau` | Ô nhập mật khẩu |
| CheckBox | `hopGhiNho` | Ghi nhớ đăng nhập |
| Button | `nutDangNhap` | Nút đăng nhập |

### 2. `view/style.css` — Định dạng
Dùng `Segoe UI` để hiển thị tiếng Việt đúng trong JavaFX. Các class: `.khung-chinh`, `.cot-trai`, `.cot-phai`, `.nut-dang-nhap`, `.o-nhap-lieu`.

### 3. `controller/LoginController.java` — Controller
Nhận sự kiện UI → gọi Service → cập nhật View. **KHÔNG** chứa SQL hay business logic.

Phương thức chính:
- `xuLyDangNhap()` → lấy input, chạy Thread mới, gọi AuthService
- `datTrangThaiDangXuLy()` → disable/enable nút khi đang xử lý

### 4. `model/AuthService.java` — Service (Business Logic)
Validate input → gọi Repository → kiểm tra trang thái tài khoản → lưu session → trả `KetQuaDangNhap`.

### 5. `model/UserRepository.java` — Repository (Database)
Thực thi SQL:
```sql
SELECT ... FROM nguoi_dung
WHERE so_dien_thoai = ?
AND mat_khau_hash = crypt(?, mat_khau_hash)
```
`crypt()` của pgcrypto đảm bảo mật khẩu được so sánh an toàn (không plain text).

### 6. `model/User.java` — Model
Ánh xạ bảng `nguoi_dung`: `maNguoiDung`, `hoTen`, `soDienThoai`, `trangThai`, `ngonNguMacDinh`.

### 7. `model/UserSession.java` — Session (Singleton)
Lưu thông tin người dùng đang đăng nhập. Mọi màn hình truy cập qua `UserSession.getInstance()`.

### 8. `model/DatabaseConnection.java` — Kết nối DB
Singleton JDBC kết nối Supabase, đọc cấu hình từ `config.properties`.

---

## III. LUỒNG CHẠY MVC

```
[1] VIEW: Người dùng nhập SĐT + mật khẩu → bấm Đăng nhập
          ↓ onAction
[2] CONTROLLER: LoginController.xuLyDangNhap()
    - Lấy input từ truongSoDienThoai, truongMatKhau
    - Chạy Thread mới (tránh freeze UI)
          ↓ authService.dangNhap()
[3] SERVICE: AuthService.dangNhap()
    - Validate SĐT (rỗng, định dạng)
    - Validate mật khẩu (rỗng, độ dài)
    - Kiểm tra trang_thai (bị khóa?)
    - Gọi UserSession.dangNhap(user)
          ↓ xacThucDangNhap()
[4] REPOSITORY: UserRepository.xacThucDangNhap()
    - Thực thi SQL PreparedStatement
    - Map ResultSet → User object
          ↓ JDBC
[5] DATABASE: Supabase/PostgreSQL
    - Bảng nguoi_dung
    - Hàm crypt() từ pgcrypto
          ↑ kết quả trả ngược về
[6] CONTROLLER: Platform.runLater()
    - Hiện thông báo thành công/lỗi
    - Chuyển màn hình chính (nếu OK)
```

---

## IV. GIẢI THÍCH OOP

### 1. Encapsulation (Đóng gói)
```java
// User.java: thuộc tính private, truy cập qua getter/setter
private String maNguoiDung;
public String getMaNguoiDung() { return maNguoiDung; }

// KetQuaDangNhap: final fields — bất biến sau khi tạo
private final boolean thanhCong;
private final String thongBao;
```

### 2. Abstraction (Trừu tượng hóa)
```java
// Controller không biết SQL:
authService.dangNhap(soDienThoai, matKhau);

// Service không biết JDBC:
userRepository.xacThucDangNhap(sdt, matKhau);
```
Mỗi tầng chỉ biết **giao diện** của tầng bên dưới.

### 3. Single Responsibility Principle
| Class | Trách nhiệm DUY NHẤT |
|---|---|
| `LoginController` | Xử lý sự kiện UI |
| `AuthService` | Logic nghiệp vụ đăng nhập |
| `UserRepository` | Truy vấn bảng nguoi_dung |
| `User` | Lưu dữ liệu người dùng |
| `UserSession` | Quản lý phiên đăng nhập |

### 4. Singleton Pattern
```java
// Chỉ 1 instance — dùng cho UserSession và DatabaseConnection
public static synchronized UserSession getInstance() {
    if (instance == null) instance = new UserSession();
    return instance;
}
```

### 5. Dependency Injection (thủ công)
Chuỗi phụ thuộc một chiều: `Controller → Service → Repository → DB`
```java
public class AuthService {
    private final UserRepository userRepository = new UserRepository();
}
```

---

## V. KẾT QUẢ

| Tình huống | Kết quả |
|---|---|
| Bỏ trống SĐT | ❌ "Vui lòng nhập số điện thoại!" |
| SĐT sai định dạng | ❌ "Số điện thoại không hợp lệ!" |
| Sai mật khẩu | ❌ "Số điện thoại hoặc mật khẩu không đúng!" |
| Tài khoản bị khóa | ❌ "Tài khoản đã bị khóa." |
| Mất mạng | ❌ "Không thể kết nối đến server!" |
| Đúng thông tin | ✅ Lưu session → chuyển màn hình chính |

---
*21/06/2026 | JavaFX + MVC + JDBC + Supabase*
