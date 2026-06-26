package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthLayoutController extends BaseController implements Initializable {

    @FXML
    private StackPane vungNoiDungAuth;

    private static AuthLayoutController instance;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        khoiTao();
    }

    @Override
    public void khoiTao() {
        instance = this;
        taiFormDangNhap();
    }

    public static AuthLayoutController getInstance() {
        return instance;
    }

    public void taiFormDangNhap() {
        taiForm("/view/LoginForm.fxml");
    }

    public void taiFormDangKy() {
        taiForm("/view/RegisterForm.fxml");
    }

    private void taiForm(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node formNode = loader.load();
            vungNoiDungAuth.getChildren().clear();
            vungNoiDungAuth.getChildren().add(formNode);
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi nạp giao diện", "Không thể tải " + fxmlPath);
            e.printStackTrace();
        }
    }
}
