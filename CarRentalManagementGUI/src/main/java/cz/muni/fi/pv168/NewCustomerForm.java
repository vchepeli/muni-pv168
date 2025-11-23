package cz.muni.fi.pv168;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.ResourceBundle;

public class NewCustomerForm {

    private CustomersTableModel tableModel;
    private TableView<Customer> tableView;
    private ResourceBundle localization;

    public NewCustomerForm(CustomersTableModel ctm, TableView<Customer> tv, ResourceBundle localization) {
        this.tableModel = ctm;
        this.tableView = tv;
        this.localization = localization;
    }

    public void show() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_customer"));
        dialog.setHeaderText(localization.getString("new_customer"));

        // Style the dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        // Create the button types
        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields with proper styling
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

        // Create labels with styling
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

        // Add fields to content
        content.getChildren().addAll(
            surnameLabel, surnameField,
            firstNameLabel, firstNameField,
            licenseLabel, licenseField,
            addressLabel, addressField,
            phoneLabel, phoneField
        );

        dialog.getDialogPane().setContent(content);

        // Apply CSS stylesheet to dialog
        String cssResource = NewCustomerForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        // Request focus on the surname field by default
        surnameField.requestFocus();

        // Convert the result to a Customer object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Customer customer = Customer.create(firstNameField.getText(), surnameField.getText(),
                            addressField.getText(), phoneField.getText(), licenseField.getText(), false);
                    return customer;
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            Customer customer = result.get();
            tableModel.add(customer);
            tableView.getItems().add(customer);
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
