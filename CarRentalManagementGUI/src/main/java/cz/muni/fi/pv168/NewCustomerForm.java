package cz.muni.fi.pv168;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.ResourceBundle;

public class NewCustomerForm {

    private CustomersFxModel model;
    private ResourceBundle localization;

    public NewCustomerForm(CustomersFxModel model, ResourceBundle localization) {
        this.model = model;
        this.localization = localization;
    }

    public void show() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_customer"));
        dialog.setHeaderText(localization.getString("new_customer"));

        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        TextField surnameField = new TextField();
        surnameField.setPromptText(localization.getString("surname"));
        surnameField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(localization.getString("first_name"));
        firstNameField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField licenseField = new TextField();
        licenseField.setPromptText(localization.getString("driver_license"));
        licenseField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField addressField = new TextField();
        addressField.setPromptText(localization.getString("address"));
        addressField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField phoneField = new TextField();
        phoneField.setPromptText(localization.getString("phone_number"));
        phoneField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        Label surnameLabel = new Label(localization.getString("surname") + ":");
        surnameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label firstNameLabel = new Label(localization.getString("first_name") + ":");
        firstNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label licenseLabel = new Label(localization.getString("driver_license") + ":");
        licenseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label addressLabel = new Label(localization.getString("address") + ":");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label phoneLabel = new Label(localization.getString("phone_number") + ":");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        content.getChildren().addAll(
            surnameLabel, surnameField,
            firstNameLabel, firstNameField,
            licenseLabel, licenseField,
            addressLabel, addressField,
            phoneLabel, phoneField
        );

        dialog.getDialogPane().setContent(content);

        String cssResource = NewCustomerForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        surnameField.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    return Customer.create(firstNameField.getText(), surnameField.getText(),
                            addressField.getText(), phoneField.getText(), licenseField.getText(), false);
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> model.addCustomer(customer));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}