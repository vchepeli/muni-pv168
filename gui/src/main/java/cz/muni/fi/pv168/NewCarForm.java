package cz.muni.fi.pv168;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.ResourceBundle;

public class NewCarForm {

    private CarsFxModel model;
    private ResourceBundle localization;

    public NewCarForm(CarsFxModel model, ResourceBundle localization) {
        this.model = model;
        this.localization = localization;
    }

    public void show() {
        Dialog<Car> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_car"));
        dialog.setHeaderText(localization.getString("new_car"));

        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

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

        Label colorLabel = new Label(localization.getString("colour") + ":");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label modelLabel = new Label(localization.getString("model") + ":");
        modelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label licensePlateLabel = new Label(localization.getString("license_plate") + ":");
        licensePlateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label priceLabel = new Label(localization.getString("price") + ":");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        content.getChildren().addAll(
            colorLabel, colorField,
            modelLabel, modelField,
            licensePlateLabel, licensePlateField,
            priceLabel, priceField
        );

        dialog.getDialogPane().setContent(content);

        String cssResource = NewCarForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        modelField.requestFocus();

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
                    return Car.create(modelField.getText(), colorField.getText(), true, rentalPayment, licensePlateField.getText());
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Car> result = dialog.showAndWait();
        result.ifPresent(car -> model.addCar(car));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}