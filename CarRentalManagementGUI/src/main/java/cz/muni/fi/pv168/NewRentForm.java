package cz.muni.fi.pv168;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class NewRentForm {

    private RentsFxModel model;
    private ResourceBundle localization;
    private CarManager carManager;
    private CustomerManager customerManager;

    public NewRentForm(RentsFxModel model, ResourceBundle localization,
                      CarManager carManager, CustomerManager customerManager) {
        this.model = model;
        this.localization = localization;
        this.carManager = carManager;
        this.customerManager = customerManager;
    }

    public void show() {
        Dialog<Rent> dialog = new Dialog<>();
        dialog.setTitle(localization.getString("new_rent"));
        dialog.setHeaderText(localization.getString("new_rent"));

        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");

        ButtonType addButtonType = new ButtonType(localization.getString("add"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        // ComboBox for Car selection
        ComboBox<Car> carComboBox = new ComboBox<>();
        carComboBox.setPromptText("Select a Car");
        carComboBox.setMaxWidth(Double.MAX_VALUE);
        carComboBox.setStyle("-fx-font-size: 12;");
        
        // Load cars
        try {
            List<Car> cars = carManager.getAllCars();
            carComboBox.setItems(FXCollections.observableArrayList(cars));
        } catch (Exception e) {
            e.printStackTrace();
        }

        carComboBox.setConverter(new StringConverter<Car>() {
            @Override
            public String toString(Car car) {
                if (car == null) return "";
                return car.model() + " (" + car.licensePlate() + ") - " + car.rentalPayment();
            }

            @Override
            public Car fromString(String string) {
                return null; // Not needed for selection only
            }
        });

        // ComboBox for Customer selection
        ComboBox<Customer> customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("Select a Customer");
        customerComboBox.setMaxWidth(Double.MAX_VALUE);
        customerComboBox.setStyle("-fx-font-size: 12;");

        // Load customers
        try {
            List<Customer> customers = customerManager.getAllCustomers();
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
        } catch (Exception e) {
            e.printStackTrace();
        }

        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) return "";
                return customer.firstName() + " " + customer.lastName() + " (" + customer.driversLicense() + ")";
            }

            @Override
            public Customer fromString(String string) {
                return null; // Not needed
            }
        });

        DatePicker rentDatePicker = new DatePicker(LocalDate.now());
        rentDatePicker.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        SpinnerValueFactory<Integer> durationSpinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 7);
        Spinner<Integer> durationSpinner = new Spinner<>(durationSpinnerFactory);
        durationSpinner.setEditable(true);
        durationSpinner.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-control-inner-background: #45475a;");

        Label carIdLabel = new Label("Car:");
        carIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label customerIdLabel = new Label("Customer:");
        customerIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label rentDateLabel = new Label(localization.getString("lease_date") + ":");
        rentDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label durationLabel = new Label(localization.getString("duration") + " (days):");
        durationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        content.getChildren().addAll(
            carIdLabel, carComboBox,
            customerIdLabel, customerComboBox,
            rentDateLabel, rentDatePicker,
            durationLabel, durationSpinner
        );

        dialog.getDialogPane().setContent(content);

        String cssResource = NewRentForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        carComboBox.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Car selectedCar = carComboBox.getValue();
                    Customer selectedCustomer = customerComboBox.getValue();

                    if (selectedCar == null || selectedCustomer == null) {
                        showError(localization.getString("error"), "Please select both a car and a customer.");
                        return null;
                    }

                    LocalDate localDate = rentDatePicker.getValue();
                    Date rentDate = Date.valueOf(localDate);
                    LocalDate dueLocalDate = localDate.plusDays(durationSpinner.getValue());
                    Date dueDate = Date.valueOf(dueLocalDate);

                    return Rent.create(rentDate, dueDate, selectedCar.uuid(), selectedCustomer.uuid());
                } catch (Exception e) {
                    showError(localization.getString("error"), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Rent> result = dialog.showAndWait();
        result.ifPresent(rent -> model.addRent(rent));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}