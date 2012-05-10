package cz.muni.fi.pv168;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Jooji
 */
public class CarRentalManagementGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainForm mainForm = new MainForm();
        mainForm.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
