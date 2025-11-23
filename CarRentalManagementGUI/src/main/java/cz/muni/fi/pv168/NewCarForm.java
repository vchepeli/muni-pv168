package cz.muni.fi.pv168;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.ResourceBundle;

public class NewCarForm {

    private CarsTableModel tableModel;
    private TableView<Car> tableView;
    private ResourceBundle localization;

    public NewCarForm(CarsTableModel ctm, TableView<Car> tv, ResourceBundle localization) {
        this.tableModel = ctm;
        this.tableView = tv;
        this.localization = localization;
    }

    public void show() {
        Dialog<Car> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_car"));
        dialog.setHeaderText(localization.getString("new_car"));

        // Style the dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        // Create the button types
        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields with proper styling
        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        TextField colorField = new TextField();
        colorField.setPromptText(localization.getString("colour"));
        colorField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField modelField = new TextField();
        modelField.setPromptText(localization.getString("model"));
        modelField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField licensePlateField = new TextField();
        licensePlateField.setPromptText(localization.getString("license_plate"));
        licensePlateField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField priceField = new TextField("0.00");
        priceField.setPromptText(localization.getString("price"));
        priceField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        // Create labels with styling
        Label colorLabel = new Label(localization.getString("colour") + ":");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label modelLabel = new Label(localization.getString("model") + ":");
        modelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label licensePlateLabel = new Label(localization.getString("license_plate") + ":");
        licensePlateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label priceLabel = new Label(localization.getString("price") + ":");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        // Add fields to content
        content.getChildren().addAll(
            colorLabel, colorField,
            modelLabel, modelField,
            licensePlateLabel, licensePlateField,
            priceLabel, priceField
        );

        dialog.getDialogPane().setContent(content);

        // Apply CSS stylesheet to dialog
        String cssResource = NewCarForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        // Request focus on the model field by default
        modelField.requestFocus();

        // Convert the result to a Car object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double rentalPayment;
                    try {
                        rentalPayment = Double.parseDouble(priceField.getText());
                    } catch (NumberFormatException e) {
                        showError(localization.getString("invalid_input"),
                            localization.getString("price") + " " + localization.getString("must_be_number"));
                        return null;
                    }
                    Car car = Car.create(modelField.getText(), colorField.getText(), true, rentalPayment, licensePlateField.getText());
                    return car;
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Car> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            Car car = result.get();
            tableModel.add(car);
            tableView.getItems().add(car);
            // Ensure UI thread executes the refresh
            if (Platform.isFxApplicationThread()) {
                tableView.refresh();
            } else {
                Platform.runLater(() -> tableView.refresh());
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
