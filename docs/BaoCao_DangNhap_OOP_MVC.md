# BAO CAO CHUC NANG: DANG NHAP TAI KHOAN
## Ung dung Quan Ly So Do Gia Pha
### Thiet ke theo MVC + 4 Nguyen Tac OOP

---

## I. CHUC NANG TUNG FILE

### Nhom VIEW (Giao dien)

#### 1. view/Login.fxml
Chuc nang: Dinh nghia giao dien man hinh dang nhap bang FXML (XML).
Khong chua bat ky logic nao - chi mo ta bo cuc va ket noi su kien voi Controller.

| Thanh phan    | fx:id               | Vai tro                      |
|---------------|---------------------|------------------------------|
| TextField     | truongSoDienThoai   | O nhap so dien thoai         |
| PasswordField | truongMatKhau       | O nhap mat khau (an ky tu)   |
| CheckBox      | hopGhiNho           | Tuy chon ghi nho dang nhap   |
| Button        | nutDangNhap         | Kich hoat dang nhap          |
| Button        | nutHienMatKhau      | Hien/an mat khau             |
| ImageView     | anhCuTo             | Hien thi anh cu to ben trai  |

Su kien:
- onAction="#xuLyDangNhap"    -> Bam nut Dang nhap
- onAction="#xuLyQuenMatKhau" -> Bam Quen mat khau
- onAction="#xuLyTaoTaiKhoan" -> Bam Tao so do ngay

#### 2. view/style.css
Chuc nang: Dinh dang giao dien - mau sac, font chu, bo goc, do bong.
Dung font Segoe UI thay Georgia vi JavaFX khong ho tro Google Fonts qua @import.

---

### Nhom CONTROLLER (Dieu khien)

#### 3. controller/BaseController.java [MOI]
Chuc nang: Lop cha truu tuong chua cac phuong thuc dung chung cho moi man hinh.

Cac phuong thuc ke thua:
- hienThongBaoLoi(tieuDe, noiDung)    - Hien hop thoai loi
- hienThongBaoThanhCong(noiDung)      - Hien hop thoai thanh cong
- hienThongBaoThongTin(tieuDe, noi)   - Hien hop thoai thong tin
- khoiTao()                           - Phuong thuc truu tuong, con phai ghi de

#### 4. controller/LoginController.java
Chuc nang: Nhan su kien tu View -> goi Service xu ly -> cap nhat lai View.
KHONG chua SQL hay logic nghiep vu.

Phuong thuc chinh:
- initialize()           - Gan su kien Enter, thiet lap ban dau
- khoiTao()              - Ghi de phuong thuc truu tuong tu BaseController
- xuLyDangNhap()         - Lay input -> chay Thread -> goi AuthService -> cap nhat UI
- xuLyDangNhapThanhCong()- Chuyen sang man hinh chinh sau khi OK

---

### Nhom MODEL (Nghiep vu & Du lieu)

#### 5. model/IAuthService.java [MOI]
Chuc nang: Interface dinh nghia hop dong cho tang Service xac thuc.

Phuong thuc:
- dangNhap(soDienThoai, matKhau) -> Tra ve KetQuaDangNhap
- dangXuat()                     -> Ket thuc phien lam viec
- daDangNhap()                   -> Kiem tra trang thai

#### 6. model/AuthService.java
Chuc nang: Trien khai IAuthService - xu ly toan bo logic dang nhap.

Luong xu ly trong dangNhap():
1. Validate so dien thoai (rong, dinh dang 0xxxxxxxxx)
2. Validate mat khau (rong, toi thieu 6 ky tu)
3. Goi IUserRepository xac thuc
4. Kiem tra trang_thai tai khoan (bi khoa?)
5. Luu session vao UserSession
6. Tra ve KetQuaDangNhap

Lop noi bo KetQuaDangNhap:
  private final boolean thanhCong;
  private final String thongBao;
  private final User nguoiDung;

#### 7. model/IUserRepository.java [MOI]
Chuc nang: Interface dinh nghia hop dong cho tang truy van database.

Phuong thuc:
- xacThucDangNhap(soDienThoai, matKhau) -> Tra ve User hoac null
- tonTaiSoDienThoai(soDienThoai)        -> Kiem tra SDT da dang ky chua

#### 8. model/UserRepository.java
Chuc nang: Trien khai IUserRepository - viet va thuc thi SQL len Supabase.

Cau SQL xac thuc:
  SELECT ma_nguoi_dung, ho_ten, email, so_dien_thoai,
         ngon_ngu_mac_dinh, trang_thai
  FROM nguoi_dung
  WHERE so_dien_thoai = ?
  AND mat_khau_hash = crypt(?, mat_khau_hash)
  LIMIT 1

Ham crypt() cua pgcrypto dam bao mat khau so sanh qua bcrypt hash.

#### 9. model/User.java
Chuc nang: Doi tuong Java anh xa mot hang trong bang nguoi_dung.

| Thuoc tinh     | Cot database       | Kieu        |
|----------------|--------------------|-------------|
| maNguoiDung    | ma_nguoi_dung      | UUID        |
| hoTen          | ho_ten             | TEXT        |
| soDienThoai    | so_dien_thoai      | VARCHAR(20) |
| trangThai      | trang_thai         | TEXT        |
| ngonNguMacDinh | ngon_ngu_mac_dinh  | VARCHAR(10) |

#### 10. model/UserSession.java
Chuc nang: Luu thong tin nguoi dung dang dang nhap, truy cap duoc tu moi man hinh.

Phuong thuc:
- getInstance()          - Lay instance Singleton duy nhat
- dangNhap(User)         - Luu thong tin sau khi xac thuc thanh cong
- dangXuat()             - Xoa session khi dang xuat
- getNguoiDungHienTai()  - Lay doi tuong User
- getIdNguoiDung()       - Lay UUID cho cac cau query sau

#### 11. model/DatabaseConnection.java
Chuc nang: Quan ly ket noi JDBC den Supabase (PostgreSQL cloud).
Doc cau hinh tu config.properties.

---

## II. LUONG CHAY MVC

[1] VIEW: Nguoi dung nhap so dien thoai + mat khau -> bam "Dang nhap"
         |
         | onAction -> xuLyDangNhap()
         v
[2] CONTROLLER: LoginController (extends BaseController)
    - Lay input tu truongSoDienThoai, truongMatKhau
    - Disable nut, hien "Dang dang nhap..."
    - Tao Thread moi - tranh dong bang giao dien
    - Goi: authService.dangNhap(soDienThoai, matKhau)
    - Platform.runLater() -> cap nhat UI tu ket qua
         |
         | dangNhap() [qua IAuthService]
         v
[3] SERVICE: AuthService implements IAuthService
    - Validate so dien thoai (rong? sai dinh dang?)
    - Validate mat khau (rong? < 6 ky tu?)
    - Goi: userRepository.xacThucDangNhap()
    - Kiem tra trang_thai: hoat_dong hay bi_khoa?
    - Goi UserSession.getInstance().dangNhap(user)
    - Tra ve KetQuaDangNhap
         |
         | xacThucDangNhap() [qua IUserRepository]
         v
[4] REPOSITORY: UserRepository implements IUserRepository
    - Tao PreparedStatement voi tham so ?
    - Thuc thi SQL: WHERE so_dien_thoai=? AND crypt(...)
    - Map ResultSet -> doi tuong User
    - Tra ve User (neu dung) hoac null (neu sai)
         |
         | JDBC
         v
[5] DATABASE: Supabase PostgreSQL
    - Bang: nguoi_dung
    - Ham: crypt() tu extension pgcrypto
    - Tim hang khop -> tra ve ket qua

   <--- Ket qua tra nguoc len tung tang --->

[6] SESSION: UserSession (Singleton)
    - Luu thong tin User vao bo nho
    - Moi man hinh khac doc qua getInstance()

---

## III. GIAI THICH 4 NGUYEN TAC OOP

### 1. TINH DONG GOI (Encapsulation)

Dinh nghia: An du lieu ben trong doi tuong, chi cho phep truy cap
qua cac phuong thuc duoc kiem soat.

Ap dung trong User.java:
  // Thuoc tinh PRIVATE - khong ai truy cap truc tiep
  private String maNguoiDung;
  private String soDienThoai;

  // Chi truy cap qua GETTER/SETTER
  public String getMaNguoiDung() { return maNguoiDung; }
  public void setSoDienThoai(String sdt) { this.soDienThoai = sdt; }

  // Phuong thuc dong goi logic kiem tra
  public boolean isHoatDong() { return "hoat_dong".equals(trangThai); }

Ap dung trong KetQuaDangNhap:
  // Thuoc tinh FINAL - khong the thay doi sau khi tao
  private final boolean thanhCong;
  private final String thongBao;
  private final User nguoiDung;

Loi ich: Du lieu nguoi dung khong bi thay doi tuy tien tu ben ngoai.

---

### 2. TINH TRU TUONG (Abstraction)

Dinh nghia: An chi tiet cai dat phuc tap, chi lo ra nhung gi
can thiet thong qua Interface.

Ap dung qua IAuthService va IUserRepository:

  // LoginController chi biet Interface
  private final IAuthService authService = new AuthService();
  // Goi mot dong - khong biet co SQL, JDBC, Thread, hash mat khau...
  KetQuaDangNhap kq = authService.dangNhap(soDienThoai, matKhau);

  // AuthService chi biet Interface cua Repository
  private final IUserRepository userRepository;
  // Goi - khong biet ben duoi la SQL hay API hay file
  User user = userRepository.xacThucDangNhap(sdt, matKhau);

Loi ich: Moi tang chi can biet ket qua, khong can biet cach thuc hien.

---

### 3. TINH KE THUA (Inheritance)

Dinh nghia: Lop con nhan va dung lai thuoc tinh, phuong thuc tu lop cha.

Ap dung: LoginController extends BaseController

  // BaseController - lop CHA
  public abstract class BaseController {
      protected void hienThongBaoLoi(String td, String nd) { ... }
      protected void hienThongBaoThanhCong(String nd) { ... }
      public abstract void khoiTao(); // Bat buoc con phai cai dat
  }

  // LoginController - lop CON
  public class LoginController extends BaseController implements Initializable {
      @Override
      public void khoiTao() { // Ghi de bat buoc
          truongMatKhau.setOnAction(e -> xuLyDangNhap(null));
      }
      // Dung thang tu lop CHA - khong can viet lai!
      hienThongBaoThanhCong("Chao mung " + user.getHoTen());
  }

Loi ich: ManHinhDangKy, ManHinhQuenMatKhau deu extends BaseController
la co ngay thong bao loi/thanh cong - khong viet lai.

---

### 4. TINH DA HINH (Polymorphism)

Dinh nghia: Cung mot giao dien nhung nhieu cach thuc hien khac nhau.

Dang 1 - Qua Interface (Runtime Polymorphism):
  IAuthService a1 = new AuthService();    // Ket noi Supabase that
  IAuthService a2 = new MockAuthService();// Du lieu gia de test UI
  // Goi CUNG MOT CACH - khong biet cai dat nao
  a1.dangNhap(sdt, mk); // -> Supabase
  a2.dangNhap(sdt, mk); // -> Du lieu gia

Dang 2 - Ghi de phuong thuc (Override):
  // JavaFX goi initialize() - moi Controller co phien ban rieng
  LoginController.initialize()    -> Thiet lap man hinh Dang nhap
  RegisterController.initialize() -> Thiet lap man hinh Dang ky

Dang 3 - Constructor Injection:
  new AuthService(new UserRepository());      // Supabase that
  new AuthService(new MockUserRepository());  // Test khong can mang

Loi ich: De thay the, de test, de mo rong ma khong pha vo code hien co.

---

## IV. BANG TONG HOP 4 NGUYEN TAC

| Nguyen tac  | File ap dung                         | Cach the hien                         |
|-------------|--------------------------------------|---------------------------------------|
| Dong goi    | User.java, KetQuaDangNhap            | Field private + getter/setter + final |
| Tru tuong   | IAuthService.java, IUserRepository   | Interface an chi tiet cai dat         |
| Ke thua     | LoginController extends BaseCtrl     | Dung lai phuong thuc thong bao        |
| Da hinh     | implements Initializable, IAuthSvc   | Nhieu cai dat, cung giao dien         |

---

## V. KET QUA HOAT DONG

| Tinh huong                  | Thong bao                                     |
|-----------------------------|-----------------------------------------------|
| Bo trong so dien thoai      | Vui long nhap so dien thoai!                  |
| Sai dinh dang SDT           | So dien thoai khong hop le!                   |
| Bo trong mat khau           | Vui long nhap mat khau!                       |
| Mat khau < 6 ky tu          | Mat khau phai co it nhat 6 ky tu!             |
| Sai thong tin dang nhap     | So dien thoai hoac mat khau khong dung!       |
| Tai khoan bi khoa           | Tai khoan da bi khoa!                         |
| Mat ket noi mang            | Khong the ket noi den server!                 |
| Dung thong tin              | Luu UserSession -> chuyen man hinh chinh      |

---
Ngay: 22/06/2026 | Cong nghe: JavaFX 21 + MVC + JDBC + Supabase PostgreSQL
