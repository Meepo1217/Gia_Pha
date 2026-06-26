import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AuthLayout.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Sơ Đồ Gia Phả - Xác thực");
        stage.setScene(scene);
        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
