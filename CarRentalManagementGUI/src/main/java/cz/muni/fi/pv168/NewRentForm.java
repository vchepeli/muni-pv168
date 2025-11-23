package cz.muni.fi.pv168;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.ResourceBundle;

public class NewRentForm {

    private RentsTableModel tableModel;
    private TableView<Rent> tableView;
    private ResourceBundle localization;

    public NewRentForm(RentsTableModel rtm, TableView<Rent> tv, ResourceBundle localization) {
        this.tableModel = rtm;
        this.tableView = tv;
        this.localization = localization;
    }

    public void show() {
        Dialog<Rent> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_rent"));
        dialog.setHeaderText(localization.getString("new_rent"));

        // Style the dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        // Create the button types
        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields with proper styling
        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        // Spinners for car and customer IDs
        SpinnerValueFactory<Integer> carSpinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        Spinner<Integer> carIdSpinner = new Spinner<>(carSpinnerFactory);
        carIdSpinner.setEditable(true);
        carIdSpinner.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-control-inner-background: #45475a;");

        SpinnerValueFactory<Integer> customerSpinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        Spinner<Integer> customerIdSpinner = new Spinner<>(customerSpinnerFactory);
        customerIdSpinner.setEditable(true);
        customerIdSpinner.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-control-inner-background: #45475a;");

        // Date picker for rent date
        DatePicker rentDatePicker = new DatePicker(LocalDate.now());
        rentDatePicker.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        // Spinner for duration in days
        SpinnerValueFactory<Integer> durationSpinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 7);
        Spinner<Integer> durationSpinner = new Spinner<>(durationSpinnerFactory);
        durationSpinner.setEditable(true);
        durationSpinner.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-control-inner-background: #45475a;");

        // Create labels with styling
        Label carIdLabel = new Label("Car ID:");
        carIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label customerIdLabel = new Label("Customer ID:");
        customerIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label rentDateLabel = new Label(localization.getString("lease_date") + ":");
        rentDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label durationLabel = new Label(localization.getString("duration") + " (days):");
        durationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        // Add fields to content
        content.getChildren().addAll(
            carIdLabel, carIdSpinner,
            customerIdLabel, customerIdSpinner,
            rentDateLabel, rentDatePicker,
            durationLabel, durationSpinner
        );

        dialog.getDialogPane().setContent(content);

        // Apply CSS stylesheet to dialog
        String cssResource = NewRentForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        // Request focus on the car ID field by default
        carIdSpinner.requestFocus();

        // Convert the result to a Rent object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    LocalDate localDate = rentDatePicker.getValue();
                    Date rentDate = Date.valueOf(localDate);

                    // Calculate due date
                    LocalDate dueLocalDate = localDate.plusDays(durationSpinner.getValue());
                    Date dueDate = Date.valueOf(dueLocalDate);

                    Rent rent = new Rent(null, rentDate, dueDate, (long) carIdSpinner.getValue(), (long) customerIdSpinner.getValue());
                    return rent;
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Rent> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            Rent rent = result.get();
            tableModel.add(rent);
            tableView.getItems().add(rent);
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
