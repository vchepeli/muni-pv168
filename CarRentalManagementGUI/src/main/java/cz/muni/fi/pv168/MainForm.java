package cz.muni.fi.pv168;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainForm {

    private Stage primaryStage;
    private ResourceBundle localization = ResourceBundle.getBundle("cz.muni.fi.pv168.localization");
    private DataSource dataSource;
    private CarManager carManager = new CarManagerImplementation();
    private CustomerManager customerManager = new CustomerManagerImplementation();
    private RentManager rentManager = new RentManagerImplementation();

    // UI Components
    private TableView<Car> carTable;
    private TableView<Customer> customerTable;
    private TableView<Rent> rentTable;
    private TabPane tabPane;
    private TextField searchField;
    private CarsTableModel carsTableModel;
    private CustomersTableModel customersTableModel;
    private RentsTableModel rentsTableModel;

    // Edit state tracking
    private Map<Car, Boolean> carEditStates = new HashMap<>();
    private Map<Customer, Boolean> customerEditStates = new HashMap<>();
    private Map<Rent, Boolean> rentEditStates = new HashMap<>();

    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        initializeDataSource();
        createUI();
        primaryStage.setTitle("Car Rental Manager");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setOnCloseRequest(e -> {
            if (!discardChanges()) {
                e.consume();
            }
        });
        primaryStage.show();
    }

    private void initializeDataSource() {
        try {
            FileOutputStream fs = new FileOutputStream("main.log", true);
            carManager.setLogger(fs);
            customerManager.setLogger(fs);
            rentManager.setLogger(fs);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 0;");

        // Create menu bar
        MenuBar menuBar = createMenuBar();

        // Create toolbar
        HBox toolbar = createToolbar();
        toolbar.setPadding(new Insets(12));
        toolbar.setStyle("-fx-spacing: 10; -fx-background-color: #363a52; -fx-border-color: #585b70; -fx-border-width: 0 0 1 0;");

        // Create top container with menu and toolbar
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, toolbar, createSearchBar());
        root.setTop(topContainer);

        // Create tabbed pane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPadding(new Insets(0));

        // Cars tab
        Tab carsTab = new Tab(localization.getString("cars"), createCarsTab());
        carsTab.setClosable(false);
        tabPane.getTabs().add(carsTab);

        // Customers tab
        Tab customersTab = new Tab(localization.getString("customers"), createCustomersTab());
        customersTab.setClosable(false);
        tabPane.getTabs().add(customersTab);

        // Rents tab
        Tab rentsTab = new Tab(localization.getString("rents"), createRentsTab());
        rentsTab.setClosable(false);
        tabPane.getTabs().add(rentsTab);

        root.setCenter(tabPane);

        // Create bottom buttons
        HBox bottomBox = new HBox(12);
        bottomBox.setPadding(new Insets(16));
        bottomBox.setStyle("-fx-alignment: CENTER_RIGHT; -fx-background-color: #363a52; -fx-border-color: #585b70; -fx-border-width: 1 0 0 0;");
        Button commitButton = new Button(localization.getString("commit"));
        commitButton.setStyle("-fx-padding: 10 24 10 24; -fx-font-size: 12; -fx-font-weight: 600;");
        commitButton.setOnAction(e -> handleCommit());
        bottomBox.getChildren().add(commitButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root);
        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        scene.getStylesheets().add(cssResource);
        primaryStage.setScene(scene);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu(localization.getString("file"));
        MenuItem connectDb = new MenuItem(localization.getString("db_connect"));
        connectDb.setOnAction(e -> handleConnectDatabase());
        MenuItem disconnectDb = new MenuItem(localization.getString("db_disconnect"));
        disconnectDb.setOnAction(e -> handleDisconnectDatabase());
        MenuItem exitItem = new MenuItem(localization.getString("exit"));
        exitItem.setOnAction(e -> handleExit());
        fileMenu.getItems().addAll(connectDb, disconnectDb, new SeparatorMenuItem(), exitItem);

        // Data menu
        Menu dataMenu = new Menu(localization.getString("data"));
        MenuItem commitItem = new MenuItem(localization.getString("commit"));
        commitItem.setOnAction(e -> handleCommit());

        Menu newMenu = new Menu(localization.getString("new"));
        MenuItem newCar = new MenuItem(localization.getString("car"));
        newCar.setOnAction(e -> openNewCarDialog());
        MenuItem newCustomer = new MenuItem(localization.getString("customer"));
        newCustomer.setOnAction(e -> openNewCustomerDialog());
        MenuItem newRent = new MenuItem(localization.getString("rent"));
        newRent.setOnAction(e -> openNewRentDialog());
        newMenu.getItems().addAll(newCar, newCustomer, newRent);

        Menu removeMenu = new Menu(localization.getString("remove"));
        MenuItem removeCar = new MenuItem(localization.getString("car"));
        removeCar.setOnAction(e -> removeCar());
        MenuItem removeCustomer = new MenuItem(localization.getString("customer"));
        removeCustomer.setOnAction(e -> removeCustomer());
        MenuItem removeRent = new MenuItem(localization.getString("rent"));
        removeRent.setOnAction(e -> removeRent());
        removeMenu.getItems().addAll(removeCar, removeCustomer, removeRent);

        dataMenu.getItems().addAll(commitItem, new SeparatorMenuItem(), newMenu, removeMenu);

        // Help menu
        Menu helpMenu = new Menu(localization.getString("help"));
        MenuItem helpItem = new MenuItem(localization.getString("help"));
        MenuItem creditsItem = new MenuItem(localization.getString("credits"));
        helpMenu.getItems().addAll(helpItem, creditsItem);

        menuBar.getMenus().addAll(fileMenu, dataMenu, helpMenu);
        return menuBar;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(0));
        toolbar.setStyle("-fx-spacing: 8;");

        Button newCarBtn = new Button(localization.getString("new_car"));
        newCarBtn.setStyle("-fx-padding: 10 16 10 16; -fx-font-size: 12; -fx-font-weight: 600;");
        newCarBtn.setOnAction(e -> openNewCarDialog());

        Button newCustomerBtn = new Button(localization.getString("new_customer"));
        newCustomerBtn.setStyle("-fx-padding: 10 16 10 16; -fx-font-size: 12; -fx-font-weight: 600;");
        newCustomerBtn.setOnAction(e -> openNewCustomerDialog());

        Button newRentBtn = new Button(localization.getString("new_rent"));
        newRentBtn.setStyle("-fx-padding: 10 16 10 16; -fx-font-size: 12; -fx-font-weight: 600;");
        newRentBtn.setOnAction(e -> openNewRentDialog());

        toolbar.getChildren().addAll(newCarBtn, newCustomerBtn, newRentBtn);
        return toolbar;
    }

    private HBox createSearchBar() {
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(12));
        searchBox.setStyle("-fx-background-color: #363a52; -fx-border-color: #585b70; -fx-border-width: 0 0 1 0; -fx-spacing: 8;");

        Label searchLabel = new Label(localization.getString("search"));
        searchLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #eed49f;");

        searchField = new TextField();
        searchField.setPromptText(localization.getString("search"));
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 8 12 8 12; -fx-border-color: #585b70; -fx-border-radius: 6; -fx-border-width: 1; -fx-background-color: #45475a; -fx-text-fill: #a6da95;");

        Button searchBtn = new Button(localization.getString("search"));
        searchBtn.setStyle("-fx-padding: 8 16 8 16; -fx-font-size: 12; -fx-font-weight: 600;");
        searchBtn.setOnAction(e -> performSearch());

        searchBox.getChildren().addAll(searchLabel, searchField, searchBtn);
        return searchBox;
    }

    private VBox createCarsTab() {
        VBox vbox = new VBox();
        carsTableModel = new CarsTableModel(localization);
        carTable = createCarTableView();
        carTable.setItems(javafx.collections.FXCollections.observableArrayList());
        vbox.getChildren().add(carTable);
        VBox.setVgrow(carTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private VBox createCustomersTab() {
        VBox vbox = new VBox();
        customersTableModel = new CustomersTableModel(localization);
        customerTable = createCustomerTableView();
        customerTable.setItems(javafx.collections.FXCollections.observableArrayList());
        vbox.getChildren().add(customerTable);
        VBox.setVgrow(customerTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private VBox createRentsTab() {
        VBox vbox = new VBox();
        rentsTableModel = new RentsTableModel(localization);
        rentTable = createRentTableView();
        rentTable.setItems(javafx.collections.FXCollections.observableArrayList());
        vbox.getChildren().add(rentTable);
        VBox.setVgrow(rentTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private TableView<Car> createCarTableView() {
        TableView<Car> table = new TableView<>();

        TableColumn<Car, Long> idCol = new TableColumn<>(localization.getString("id"));
        idCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().ID()));

        TableColumn<Car, String> modelCol = new TableColumn<>(localization.getString("model"));
        modelCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().model()));

        TableColumn<Car, String> colorCol = new TableColumn<>(localization.getString("colour"));
        colorCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().color()));

        TableColumn<Car, String> licensePlateCol = new TableColumn<>(localization.getString("license_plate"));
        licensePlateCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().licensePlate()));

        TableColumn<Car, Double> priceCol = new TableColumn<>(localization.getString("price"));
        priceCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().rentalPayment()));

        TableColumn<Car, Boolean> availableCol = new TableColumn<>(localization.getString("available"));
        availableCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().available()));

        // Action column with Edit and Delete/Undelete buttons
        TableColumn<Car, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setMinWidth(120);
        actionCol.setCellFactory(col -> new javafx.scene.control.TableCell<Car, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
            {
                editBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                editBtn.setOnAction(e -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        openEditCarDialog(car);
                    }
                });
                deleteBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                deleteBtn.setOnAction(e -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        if (car.ID() == null) {
                            // New car not yet in database - remove immediately
                            getTableView().getItems().remove(car);
                            showInfo(localization.getString("info"), "Car removed from table.");
                        } else {
                            // Existing car in database - toggle deletion marking
                            boolean isMarkedForDeletion = carsTableModel.getDeletedCars().contains(car);
                            if (!isMarkedForDeletion) {
                                carsTableModel.remove(car);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Car marked for deletion. Click Commit to confirm.");
                            } else {
                                carsTableModel.carResolved(car);
                                deleteBtn.setText("Delete");
                                showInfo(localization.getString("info"), "Car unmarked for deletion.");
                            }
                        }
                    }
                });
                hbox.getChildren().addAll(editBtn, deleteBtn);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item == null) {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null && car.ID() != null) {
                        boolean isMarkedForDeletion = carsTableModel.getDeletedCars().contains(car);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(idCol, modelCol, colorCol, licensePlateCol, priceCol, availableCol, actionCol);
        return table;
    }

    private TableView<Customer> createCustomerTableView() {
        TableView<Customer> table = new TableView<>();

        TableColumn<Customer, Long> idCol = new TableColumn<>(localization.getString("id"));
        idCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().ID()));

        TableColumn<Customer, String> firstNameCol = new TableColumn<>(localization.getString("first_name"));
        firstNameCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().firstName()));

        TableColumn<Customer, String> lastNameCol = new TableColumn<>(localization.getString("last_name"));
        lastNameCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().lastName()));

        TableColumn<Customer, String> addressCol = new TableColumn<>(localization.getString("address"));
        addressCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().address()));

        TableColumn<Customer, String> phoneCol = new TableColumn<>(localization.getString("telephone"));
        phoneCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().phoneNumber()));

        TableColumn<Customer, String> licenseCol = new TableColumn<>(localization.getString("license"));
        licenseCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().driversLicense()));

        // Action column with Edit and Delete/Undelete buttons
        TableColumn<Customer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setMinWidth(120);
        actionCol.setCellFactory(col -> new javafx.scene.control.TableCell<Customer, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
            {
                editBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                editBtn.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        openEditCustomerDialog(customer);
                    }
                });
                deleteBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                deleteBtn.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        if (customer.ID() == null) {
                            // New customer not yet in database - remove immediately
                            getTableView().getItems().remove(customer);
                            showInfo(localization.getString("info"), "Customer removed from table.");
                        } else {
                            // Existing customer in database - toggle deletion marking
                            boolean isMarkedForDeletion = customersTableModel.getDeletedCustomers().contains(customer);
                            if (!isMarkedForDeletion) {
                                customersTableModel.remove(customer);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Customer marked for deletion. Click Commit to confirm.");
                            } else {
                                customersTableModel.customerResolved(customer);
                                deleteBtn.setText("Delete");
                                showInfo(localization.getString("info"), "Customer unmarked for deletion.");
                            }
                        }
                    }
                });
                hbox.getChildren().addAll(editBtn, deleteBtn);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item == null) {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null && customer.ID() != null) {
                        boolean isMarkedForDeletion = customersTableModel.getDeletedCustomers().contains(customer);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(idCol, firstNameCol, lastNameCol, addressCol, phoneCol, licenseCol, actionCol);
        return table;
    }

    private TableView<Rent> createRentTableView() {
        TableView<Rent> table = new TableView<>();

        TableColumn<Rent, Long> idCol = new TableColumn<>(localization.getString("id"));
        idCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().ID()));

        TableColumn<Rent, Long> carIdCol = new TableColumn<>(localization.getString("car_id"));
        carIdCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().carID()));

        TableColumn<Rent, Long> customerIdCol = new TableColumn<>(localization.getString("customer_id"));
        customerIdCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().customerID()));

        TableColumn<Rent, String> rentDateCol = new TableColumn<>(localization.getString("lease_date"));
        rentDateCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().rentDate().toString()));

        TableColumn<Rent, String> dueDateCol = new TableColumn<>(localization.getString("due_date"));
        dueDateCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().dueDate().toString()));

        // Action column with Edit and Delete/Undelete buttons
        TableColumn<Rent, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setMinWidth(120);
        actionCol.setCellFactory(col -> new javafx.scene.control.TableCell<Rent, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
            {
                editBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                editBtn.setOnAction(e -> {
                    Rent rent = getTableView().getItems().get(getIndex());
                    if (rent != null) {
                        openEditRentDialog(rent);
                    }
                });
                deleteBtn.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11;");
                deleteBtn.setOnAction(e -> {
                    Rent rent = getTableView().getItems().get(getIndex());
                    if (rent != null) {
                        if (rent.ID() == null) {
                            // New rent not yet in database - remove immediately
                            getTableView().getItems().remove(rent);
                            showInfo(localization.getString("info"), "Rent removed from table.");
                        } else {
                            // Existing rent in database - toggle deletion marking
                            boolean isMarkedForDeletion = rentsTableModel.getDeletedRents().contains(rent);
                            if (!isMarkedForDeletion) {
                                rentsTableModel.remove(rent);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Rent marked for deletion. Click Commit to confirm.");
                            } else {
                                // Note: RentsTableModel doesn't have rentResolved method, so we manually remove
                                rentsTableModel.removeFromDeletedRents(rent);
                                deleteBtn.setText("Delete");
                                showInfo(localization.getString("info"), "Rent unmarked for deletion.");
                            }
                        }
                    }
                });
                hbox.getChildren().addAll(editBtn, deleteBtn);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item == null) {
                    Rent rent = getTableView().getItems().get(getIndex());
                    if (rent != null && rent.ID() != null) {
                        boolean isMarkedForDeletion = rentsTableModel.getDeletedRents().contains(rent);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(idCol, carIdCol, customerIdCol, rentDateCol, dueDateCol, actionCol);
        return table;
    }

    private void handleConnectDatabase() {
        try {
            dataSource = prepareDataSource();
            loadAllData();
            showInfo(localization.getString("info"), localization.getString("db_connection_success"));
        } catch (Exception ex) {
            showAlert(localization.getString("error"),
                localization.getString("db_connection_failure") + ": " + ex.getMessage());
            dataSource = null;
        }
    }

    private void handleDisconnectDatabase() {
        if (discardChanges()) {
            carTable.getItems().clear();
            customerTable.getItems().clear();
            rentTable.getItems().clear();
            dataSource = null;
        }
    }

    private void handleExit() {
        if (discardChanges()) {
            System.exit(0);
        }
    }

    private void handleCommit() {
        if (dataSource == null) {
            showAlert(localization.getString("db_missing"), localization.getString("no_db_loaded_message"));
            return;
        }

        int selectedTab = tabPane.getSelectionModel().getSelectedIndex();
        switch (selectedTab) {
            case 0:
                commitCars();
                break;
            case 1:
                commitCustomers();
                break;
            case 2:
                commitRents();
                break;
        }
    }

    private void commitCars() {
        if (dataSource == null) {
            showAlert(localization.getString("error"), localization.getString("no_db_loaded_message"));
            return;
        }

        int addedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        // Add new cars
        for (Car car : carsTableModel.getCars()) {
            if (car != null && car.ID() == null) {
                try {
                    carManager.addCar(car);
                    addedCount++;
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_car") + ": " + ex.getMessage());
                }
            }
        }

        // Update modified cars - create a copy to avoid ConcurrentModificationException
        Set<Car> updatedCarsCopy = new HashSet<>(carsTableModel.getUpdatedCars());
        for (Car car : updatedCarsCopy) {
            if (car != null) {
                try {
                    carManager.updateCarInfo(car);
                    updatedCount++;
                    carsTableModel.carResolved(car);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_update_car") + ": " + ex.getMessage());
                }
            }
        }

        // Delete removed cars - create a copy to avoid ConcurrentModificationException
        Set<Car> deletedCarsCopy = new HashSet<>(carsTableModel.getDeletedCars());
        for (Car car : deletedCarsCopy) {
            if (car != null) {
                // Only delete from database if the car was actually saved (has an ID)
                if (car.ID() != null) {
                    try {
                        carManager.removeCar(car);
                        deletedCount++;
                    } catch (Exception ex) {
                        showAlert(localization.getString("error"),
                            localization.getString("cannot_remove_car") + ": " + ex.getMessage());
                    }
                }
                // Remove from tracking regardless of whether it was in DB
                carsTableModel.carResolved(car);
                carTable.getItems().remove(car);
            }
        }

        String message = addedCount + " " + localization.getString("cars") + " added, " +
                        updatedCount + " updated, " + deletedCount + " removed";
        showInfo(localization.getString("commit"), message);

        // Reload data from database to update added cars
        loadAllData();
    }

    private void commitCustomers() {
        if (dataSource == null) {
            showAlert(localization.getString("error"), localization.getString("no_db_loaded_message"));
            return;
        }

        int addedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        // Add new customers
        for (Customer customer : customersTableModel.getCustomers()) {
            if (customer != null && customer.ID() == null) {
                try {
                    customerManager.addCustomer(customer);
                    addedCount++;
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_customer") + ": " + ex.getMessage());
                }
            }
        }

        // Update modified customers - create a copy to avoid ConcurrentModificationException
        Set<Customer> updatedCustomersCopy = new HashSet<>(customersTableModel.getUpdatedCustomers());
        for (Customer customer : updatedCustomersCopy) {
            if (customer != null) {
                try {
                    customerManager.updateCustomerInfo(customer);
                    updatedCount++;
                    customersTableModel.customerResolved(customer);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_update_customer") + ": " + ex.getMessage());
                }
            }
        }

        // Delete removed customers - create a copy to avoid ConcurrentModificationException
        Set<Customer> deletedCustomersCopy = new HashSet<>(customersTableModel.getDeletedCustomers());
        for (Customer customer : deletedCustomersCopy) {
            if (customer != null) {
                // Only delete from database if the customer was actually saved (has an ID)
                if (customer.ID() != null) {
                    try {
                        customerManager.removeCustomer(customer);
                        deletedCount++;
                    } catch (Exception ex) {
                        showAlert(localization.getString("error"),
                            localization.getString("cannot_remove_customer") + ": " + ex.getMessage());
                    }
                }
                // Remove from tracking regardless of whether it was in DB
                customersTableModel.customerResolved(customer);
                customerTable.getItems().remove(customer);
            }
        }

        String message = addedCount + " " + localization.getString("customers") + " added, " +
                        updatedCount + " updated, " + deletedCount + " removed";
        showInfo(localization.getString("commit"), message);

        // Reload data from database to update added customers
        loadAllData();
    }

    private void commitRents() {
        if (dataSource == null) {
            showAlert(localization.getString("error"), localization.getString("no_db_loaded_message"));
            return;
        }

        int addedCount = 0;
        int deletedCount = 0;

        // Add new rents
        for (Rent rent : rentsTableModel.getRents()) {
            if (rent != null && rent.ID() == null) {
                try {
                    // Load Car and Customer objects from database
                    Car car = carManager.findCarByID(rent.carID());
                    Customer customer = customerManager.findCustomerByID(rent.customerID());

                    if (car == null || customer == null) {
                        showAlert(localization.getString("error"),
                            localization.getString("cannot_add_rent") + ": Invalid car or customer ID");
                        continue;
                    }

                    // Call rentCarToCustomer with loaded objects
                    rentManager.rentCarToCustomer(car, customer, rent.rentDate(), rent.dueDate());
                    addedCount++;
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_rent") + ": " + ex.getMessage());
                }
            }
        }

        // Delete removed rents - create a copy to avoid ConcurrentModificationException
        Set<Rent> deletedRentsCopy = new HashSet<>(rentsTableModel.getDeletedRents());
        for (Rent rent : deletedRentsCopy) {
            if (rent != null) {
                // Only delete from database if the rent was actually saved (has an ID)
                if (rent.ID() != null) {
                    try {
                        rentManager.getCarFromCustomer(
                            carManager.findCarByID(rent.carID()),
                            customerManager.findCustomerByID(rent.customerID())
                        );
                        deletedCount++;
                    } catch (Exception ex) {
                        showAlert(localization.getString("error"),
                            localization.getString("cannot_remove_rent") + ": " + ex.getMessage());
                    }
                }
                // Remove from tracking regardless of whether it was in DB
                rentsTableModel.removeFromDeletedRents(rent);
                rentTable.getItems().remove(rent);
            }
        }

        String message = addedCount + " " + localization.getString("rents") + " added, " +
                        deletedCount + " removed";
        showInfo(localization.getString("commit"), message);

        // Reload data from database to update added rents
        loadAllData();
    }

    private void openNewCarDialog() {
        NewCarForm carForm = new NewCarForm(carsTableModel, carTable, localization);
        carForm.show();
    }

    private void openNewCustomerDialog() {
        NewCustomerForm customerForm = new NewCustomerForm(customersTableModel, customerTable, localization);
        customerForm.show();
    }

    private void openNewRentDialog() {
        NewRentForm rentForm = new NewRentForm(rentsTableModel, rentTable, localization);
        rentForm.show();
    }

    private void removeCar() {
        String idStr = showInputDialog("ID");
        if (idStr == null) return;

        try {
            long id = Long.parseLong(idStr);
            Car carToRemove = null;
            for (Car car : carsTableModel.getCars()) {
                if (car != null && car.ID() != null && car.ID().longValue() == id) {
                    carToRemove = car;
                    break;
                }
            }
            if (carToRemove != null) {
                carsTableModel.remove(carToRemove);
                showInfo(localization.getString("info"), "Car ID " + id + " marked for removal");
            } else {
                showAlert(localization.getString("error"), "Car with ID " + id + " not found");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", localization.getString("must_be_number"));
        }
    }

    private void removeCustomer() {
        String idStr = showInputDialog("ID");
        if (idStr == null) return;

        try {
            long id = Long.parseLong(idStr);
            Customer customerToRemove = null;
            for (Customer customer : customersTableModel.getCustomers()) {
                if (customer != null && customer.ID() != null && customer.ID().longValue() == id) {
                    customerToRemove = customer;
                    break;
                }
            }
            if (customerToRemove != null) {
                customersTableModel.remove(customerToRemove);
                showInfo(localization.getString("info"), "Customer ID " + id + " marked for removal");
            } else {
                showAlert(localization.getString("error"), "Customer with ID " + id + " not found");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", localization.getString("must_be_number"));
        }
    }

    private void removeRent() {
        String idStr = showInputDialog("ID");
        if (idStr == null) return;

        try {
            long id = Long.parseLong(idStr);
            Rent rentToRemove = null;
            for (Rent rent : rentsTableModel.getRents()) {
                if (rent != null && rent.ID() != null && rent.ID().longValue() == id) {
                    rentToRemove = rent;
                    break;
                }
            }
            if (rentToRemove != null) {
                rentsTableModel.remove(rentToRemove);
                showInfo(localization.getString("info"), "Rent ID " + id + " marked for removal");
            } else {
                showAlert(localization.getString("error"), "Rent with ID " + id + " not found");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", localization.getString("must_be_number"));
        }
    }

    private void performSearch() {
        String query = searchField.getText();
        int selectedTab = tabPane.getSelectionModel().getSelectedIndex();

        Task<Void> searchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // TODO: Implement search based on selected tab
                return null;
            }
        };

        new Thread(searchTask).start();
    }

    private void loadAllData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load cars
                List<Car> cars = carManager.getAllCars();
                Platform.runLater(() -> carTable.getItems().setAll(cars));

                // Load customers
                List<Customer> customers = customerManager.getAllCustomers();
                Platform.runLater(() -> customerTable.getItems().setAll(customers));

                // Load rents
                List<Rent> rents = rentManager.getAllRents();
                Platform.runLater(() -> rentTable.getItems().setAll(rents));

                return null;
            }
        };

        new Thread(loadTask).start();
    }

    private DataSource prepareDataSource() {
        ResourceBundle databaseProperties = ResourceBundle.getBundle("cz.muni.fi.pv168.database");

        String driver = databaseProperties.getString("jdbc.drivers");
        String url = databaseProperties.getString("jdbc.url");
        String username = databaseProperties.getString("jdbc.username");
        String password = databaseProperties.getString("jdbc.password");

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);

        try {
            DBUtils.tryCreateTables(ds);
        } catch (SQLException ex) {
            showAlert(localization.getString("error"), localization.getString("db_connection_failure"));
        }

        carManager.setDataSource(ds);
        customerManager.setDataSource(ds);
        rentManager.setDataSource(ds);

        return ds;
    }

    private boolean discardChanges() {
        // TODO: Check for unsaved changes
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String showInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setHeaderText(null);
        dialog.setContentText(prompt + ":");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void openEditCarDialog(Car car) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Car");
        dialog.setHeaderText("Edit car details");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        TextField modelField = new TextField(car.model() != null ? car.model() : "");
        modelField.setPromptText("Model");
        modelField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField colorField = new TextField(car.color() != null ? car.color() : "");
        colorField.setPromptText("Color");
        colorField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField licensePlateField = new TextField(car.licensePlate() != null ? car.licensePlate() : "");
        licensePlateField.setPromptText("License Plate");
        licensePlateField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField priceField = new TextField(car.rentalPayment() != null ? car.rentalPayment().toString() : "");
        priceField.setPromptText("Rental Price");
        priceField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        Label modelLabel = new Label("Model:");
        modelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label licensePlateLabel = new Label("License Plate:");
        licensePlateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label priceLabel = new Label("Rental Price:");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        content.getChildren().addAll(
            modelLabel, modelField,
            colorLabel, colorField,
            licensePlateLabel, licensePlateField,
            priceLabel, priceField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Apply CSS stylesheet to dialog
        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Car updatedCar = new Car(car.ID(), modelField.getText(), colorField.getText(), car.available(), Double.parseDouble(priceField.getText()), licensePlateField.getText());

                carsTableModel.carResolved(car);
                carsTableModel.carResolved(car); // Remove from both sets first
                if (updatedCar.ID() != null) {
                    carsTableModel.markCarForUpdate(updatedCar);
                }

                carTable.refresh();
                showInfo(localization.getString("info"), "Car marked for update. Click Commit to save changes.");
            } catch (NumberFormatException ex) {
                showAlert(localization.getString("error"), "Invalid price format. Please enter a number.");
            }
        }
    }

    private void openEditCustomerDialog(Customer customer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");
        dialog.setHeaderText("Edit customer details");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        TextField firstNameField = new TextField(customer.firstName() != null ? customer.firstName() : "");
        firstNameField.setPromptText("First Name");
        firstNameField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField lastNameField = new TextField(customer.lastName() != null ? customer.lastName() : "");
        lastNameField.setPromptText("Last Name");
        lastNameField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField addressField = new TextField(customer.address() != null ? customer.address() : "");
        addressField.setPromptText("Address");
        addressField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField phoneField = new TextField(customer.phoneNumber() != null ? customer.phoneNumber() : "");
        phoneField.setPromptText("Phone Number");
        phoneField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        TextField licenseField = new TextField(customer.driversLicense() != null ? customer.driversLicense() : "");
        licenseField.setPromptText("Driver's License");
        licenseField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label addressLabel = new Label("Address:");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label licenseLabel = new Label("Driver's License:");
        licenseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        content.getChildren().addAll(
            firstNameLabel, firstNameField,
            lastNameLabel, lastNameField,
            addressLabel, addressField,
            phoneLabel, phoneField,
            licenseLabel, licenseField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Apply CSS stylesheet to dialog
        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Customer updatedCustomer = new Customer(customer.ID(), firstNameField.getText(), lastNameField.getText(), addressField.getText(), phoneField.getText(), licenseField.getText(), customer.active());

            customersTableModel.customerResolved(customer);
            if (updatedCustomer.ID() != null) {
                customersTableModel.markCustomerForUpdate(updatedCustomer);
            }

            customerTable.refresh();
            showInfo(localization.getString("info"), "Customer marked for update. Click Commit to save changes.");
        }
    }

    private void openEditRentDialog(Rent rent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Rent");
        dialog.setHeaderText("Edit rent details");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        TextField carIdField = new TextField(rent.carID() != null ? rent.carID().toString() : "");
        carIdField.setPromptText("Car ID");
        carIdField.setDisable(true);
        carIdField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a; -fx-opacity: 0.7;");

        TextField customerIdField = new TextField(rent.customerID() != null ? rent.customerID().toString() : "");
        customerIdField.setPromptText("Customer ID");
        customerIdField.setDisable(true);
        customerIdField.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a; -fx-opacity: 0.7;");

        Label carIdLabel = new Label("Car ID:");
        carIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label customerIdLabel = new Label("Customer ID:");
        customerIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        Label noteLabel = new Label("(Note: Rent details cannot be modified. Please delete and create a new rent if changes are needed.)");
        noteLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #a6adc8; -fx-wrap-text: true;");

        content.getChildren().addAll(
            carIdLabel, carIdField,
            customerIdLabel, customerIdField,
            noteLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Apply CSS stylesheet to dialog
        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(cssResource);

        dialog.showAndWait();
    }
}
