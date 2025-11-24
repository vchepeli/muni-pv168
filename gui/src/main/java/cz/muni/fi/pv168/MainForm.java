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
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    // Models
    private final CarsFxModel carsModel = new CarsFxModel();
    private final CustomersFxModel customersModel = new CustomersFxModel();
    private final RentsFxModel rentsModel = new RentsFxModel();

    // Filtered Lists for Search
    private javafx.collections.transformation.FilteredList<Car> filteredCars;
    private javafx.collections.transformation.FilteredList<Customer> filteredCustomers;
    private javafx.collections.transformation.FilteredList<Rent> filteredRents;

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
        commitButton.getStyleClass().add("accent");
        commitButton.setOnAction(e -> handleCommit());
        bottomBox.getChildren().add(commitButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root);
        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource);
        }
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
        newCar.setOnAction(e -> {
            tabPane.getSelectionModel().select(0); // Select Cars tab
            openNewCarDialog();
        });
        MenuItem newCustomer = new MenuItem(localization.getString("customer"));
        newCustomer.setOnAction(e -> {
            tabPane.getSelectionModel().select(1); // Select Customers tab
            openNewCustomerDialog();
        });
        MenuItem newRent = new MenuItem(localization.getString("rent"));
        newRent.setOnAction(e -> {
            tabPane.getSelectionModel().select(2); // Select Rents tab
            openNewRentDialog();
        });
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
        newCarBtn.getStyleClass().add("accent");
        newCarBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(0);
            openNewCarDialog();
        });

        Button newCustomerBtn = new Button(localization.getString("new_customer"));
        newCustomerBtn.getStyleClass().add("accent");
        newCustomerBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(1);
            openNewCustomerDialog();
        });

        Button newRentBtn = new Button(localization.getString("new_rent"));
        newRentBtn.getStyleClass().add("accent");
        newRentBtn.setOnAction(e -> {
            tabPane.getSelectionModel().select(2);
            openNewRentDialog();
        });

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
        carTable = createCarTableView();
        filteredCars = new javafx.collections.transformation.FilteredList<>(carsModel.getCars(), p -> true);
        carTable.setItems(filteredCars);
        vbox.getChildren().add(carTable);
        VBox.setVgrow(carTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private VBox createCustomersTab() {
        VBox vbox = new VBox();
        customerTable = createCustomerTableView();
        filteredCustomers = new javafx.collections.transformation.FilteredList<>(customersModel.getCustomers(), p -> true);
        customerTable.setItems(filteredCustomers);
        vbox.getChildren().add(customerTable);
        VBox.setVgrow(customerTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private VBox createRentsTab() {
        VBox vbox = new VBox();
        rentTable = createRentTableView();
        filteredRents = new javafx.collections.transformation.FilteredList<>(rentsModel.getRents(), p -> true);
        rentTable.setItems(filteredRents);
        vbox.getChildren().add(rentTable);
        VBox.setVgrow(rentTable, javafx.scene.layout.Priority.ALWAYS);
        return vbox;
    }

    private TableView<Car> createCarTableView() {
        TableView<Car> table = new TableView<>();

        TableColumn<Car, Number> rowNumCol = new TableColumn<>("#");
        rowNumCol.setSortable(false);
        rowNumCol.setPrefWidth(40);
        rowNumCol.setCellFactory(col -> new javafx.scene.control.TableCell<Car, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

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
                editBtn.getStyleClass().add("accent");
                editBtn.setOnAction(e -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        openEditCarDialog(car);
                    }
                });
                deleteBtn.getStyleClass().add("danger");
                deleteBtn.setOnAction(e -> {
                    Car car = getTableView().getItems().get(getIndex());
                    if (car != null) {
                        if (carsModel.getAddedCars().contains(car)) {
                            // New car not yet in database - remove immediately from list
                            carsModel.removeCarFromList(car);
                            showInfo(localization.getString("info"), "Car removed from table.");
                        } else {
                            // Existing car in database - toggle deletion marking
                            boolean isMarkedForDeletion = carsModel.getDeletedCars().contains(car);
                            if (!isMarkedForDeletion) {
                                carsModel.markForDeletion(car);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Car marked for deletion. Click Commit to confirm.");
                            } else {
                                carsModel.unmarkFromDeletion(car);
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
                    if (car != null && car.uuid() != null) {
                        boolean isMarkedForDeletion = carsModel.getDeletedCars().contains(car);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(rowNumCol, modelCol, colorCol, licensePlateCol, priceCol, availableCol, actionCol);
        return table;
    }

    private TableView<Customer> createCustomerTableView() {
        TableView<Customer> table = new TableView<>();

        TableColumn<Customer, Number> rowNumCol = new TableColumn<>("#");
        rowNumCol.setSortable(false);
        rowNumCol.setPrefWidth(40);
        rowNumCol.setCellFactory(col -> new javafx.scene.control.TableCell<Customer, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

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
                editBtn.getStyleClass().add("accent");
                editBtn.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        openEditCustomerDialog(customer);
                    }
                });
                deleteBtn.getStyleClass().add("danger");
                deleteBtn.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        if (customersModel.getAddedCustomers().contains(customer)) {
                            customersModel.removeCustomerFromList(customer);
                            showInfo(localization.getString("info"), "Customer removed from table.");
                        } else {
                            boolean isMarkedForDeletion = customersModel.getDeletedCustomers().contains(customer);
                            if (!isMarkedForDeletion) {
                                customersModel.markForDeletion(customer);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Customer marked for deletion. Click Commit to confirm.");
                            } else {
                                customersModel.unmarkFromDeletion(customer);
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
                    if (customer != null && customer.uuid() != null) {
                        boolean isMarkedForDeletion = customersModel.getDeletedCustomers().contains(customer);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(rowNumCol, firstNameCol, lastNameCol, addressCol, phoneCol, licenseCol, actionCol);
        return table;
    }

    private TableView<Rent> createRentTableView() {
        TableView<Rent> table = new TableView<>();

        TableColumn<Rent, Number> rowNumCol = new TableColumn<>("#");
        rowNumCol.setSortable(false);
        rowNumCol.setPrefWidth(40);
        rowNumCol.setCellFactory(col -> new javafx.scene.control.TableCell<Rent, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        TableColumn<Rent, String> carIdCol = new TableColumn<>(localization.getString("car_id"));
        carIdCol.setCellValueFactory(cellData ->
            javafx.beans.binding.Bindings.createObjectBinding(() -> cellData.getValue().carID()));

        TableColumn<Rent, String> customerIdCol = new TableColumn<>(localization.getString("customer_id"));
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
                editBtn.getStyleClass().add("accent");
                editBtn.setOnAction(e -> {
                    Rent rent = getTableView().getItems().get(getIndex());
                    if (rent != null) {
                        openEditRentDialog(rent);
                    }
                });
                deleteBtn.getStyleClass().add("danger");
                deleteBtn.setOnAction(e -> {
                    Rent rent = getTableView().getItems().get(getIndex());
                    if (rent != null) {
                        if (rentsModel.getAddedRents().contains(rent)) {
                            rentsModel.removeRentFromList(rent);
                            showInfo(localization.getString("info"), "Rent removed from table.");
                        } else {
                            boolean isMarkedForDeletion = rentsModel.getDeletedRents().contains(rent);
                            if (!isMarkedForDeletion) {
                                rentsModel.markForDeletion(rent);
                                deleteBtn.setText("Undelete");
                                showInfo(localization.getString("info"), "Rent marked for deletion. Click Commit to confirm.");
                            } else {
                                rentsModel.unmarkFromDeletion(rent);
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
                    if (rent != null && rent.uuid() != null) {
                        boolean isMarkedForDeletion = rentsModel.getDeletedRents().contains(rent);
                        deleteBtn.setText(isMarkedForDeletion ? "Undelete" : "Delete");
                    }
                }
                setGraphic(empty ? null : hbox);
            }
        });

        table.getColumns().addAll(rowNumCol, carIdCol, customerIdCol, rentDateCol, dueDateCol, actionCol);
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
            carsModel.getCars().clear();
            customersModel.getCustomers().clear();
            rentsModel.getRents().clear();
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
        for (Car car : new HashSet<>(carsModel.getAddedCars())) {
            if (car != null) {
                try {
                    carManager.addCar(car);
                    addedCount++;
                    carsModel.carResolved(car); // Remove from added set
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_car") + ": " + ex.getMessage());
                }
            }
        }

        // Update modified cars
        for (Car car : new HashSet<>(carsModel.getUpdatedCars())) {
            if (car != null) {
                try {
                    carManager.updateCarInfo(car);
                    updatedCount++;
                    carsModel.carResolved(car);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_update_car") + ": " + ex.getMessage());
                }
            }
        }

        // Delete removed cars
        for (Car car : new HashSet<>(carsModel.getDeletedCars())) {
            if (car != null) {
                try {
                    carManager.removeCar(car);
                    deletedCount++;
                    carsModel.carResolved(car); // remove from tracking
                    carsModel.removeCarFromList(car); // now actually remove from UI
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_remove_car") + ": " + ex.getMessage());
                }
            }
        }

        String message = addedCount + " " + localization.getString("cars") + " added, " +
                        updatedCount + " updated, " + deletedCount + " removed";
        showInfo(localization.getString("commit"), message);

        // Reload data from database to update added cars (get IDs)
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

        for (Customer customer : new HashSet<>(customersModel.getAddedCustomers())) {
            if (customer != null) {
                try {
                    customerManager.addCustomer(customer);
                    addedCount++;
                    customersModel.customerResolved(customer);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_customer") + ": " + ex.getMessage());
                }
            }
        }

        for (Customer customer : new HashSet<>(customersModel.getUpdatedCustomers())) {
            if (customer != null) {
                try {
                    customerManager.updateCustomerInfo(customer);
                    updatedCount++;
                    customersModel.customerResolved(customer);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_update_customer") + ": " + ex.getMessage());
                }
            }
        }

        for (Customer customer : new HashSet<>(customersModel.getDeletedCustomers())) {
            if (customer != null) {
                try {
                    customerManager.removeCustomer(customer);
                    deletedCount++;
                    customersModel.customerResolved(customer);
                    customersModel.removeCustomerFromList(customer);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_remove_customer") + ": " + ex.getMessage());
                }
            }
        }

        String message = addedCount + " " + localization.getString("customers") + " added, " +
                        updatedCount + " updated, " + deletedCount + " removed";
        showInfo(localization.getString("commit"), message);

        loadAllData();
    }

    private void commitRents() {
        if (dataSource == null) {
            showAlert(localization.getString("error"), localization.getString("no_db_loaded_message"));
            return;
        }

        int addedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        for (Rent rent : new HashSet<>(rentsModel.getAddedRents())) {
            if (rent != null) {
                try {
                    rentManager.addRent(rent); // Use the new method that accepts the Rent object (preserving UUID)
                    addedCount++;
                    rentsModel.rentResolved(rent);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_add_rent") + ": " + ex.getMessage());
                }
            }
        }

        // Update modified rents
        for (Rent rent : new HashSet<>(rentsModel.getUpdatedRents())) {
            if (rent != null) {
                try {
                    rentManager.updateRent(rent);
                    updatedCount++;
                    rentsModel.rentResolved(rent);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        "Cannot update rent: " + ex.getMessage());
                }
            }
        }

        for (Rent rent : new HashSet<>(rentsModel.getDeletedRents())) {
            if (rent != null && rent.uuid() != null) {
                try {
                    rentManager.getCarFromCustomer(
                        carManager.findCarByID(rent.carID()),
                        customerManager.findCustomerByID(rent.customerID())
                    );
                    deletedCount++;
                    rentsModel.rentResolved(rent);
                    rentsModel.removeRentFromList(rent);
                } catch (Exception ex) {
                    showAlert(localization.getString("error"),
                        localization.getString("cannot_remove_rent") + ": " + ex.getMessage());
                }
            }
        }

        String message = addedCount + " " + localization.getString("rents") + " added, " +
                        updatedCount + " updated, " + deletedCount + " removed";
        showInfo(localization.getString("commit"), message);
        
        loadAllData(); // Ensure UI is synced with DB
    }

    private void openNewCarDialog() {
        NewCarForm carForm = new NewCarForm(carsModel, localization);
        carForm.show();
    }

    private void openNewCustomerDialog() {
        NewCustomerForm customerForm = new NewCustomerForm(customersModel, localization);
        customerForm.show();
    }

    private void openNewRentDialog() {
        NewRentForm rentForm = new NewRentForm(rentsModel, localization, carManager, customerManager);
        rentForm.show();
    }

    private void removeCar() {
        String idStr = showInputDialog("ID");
        if (idStr == null || idStr.trim().isEmpty()) return;

        Car carToRemove = null;
        for (Car car : carsModel.getCars()) {
            if (car != null && car.uuid() != null && car.uuid().equals(idStr)) {
                carToRemove = car;
                break;
            }
        }
        if (carToRemove != null) {
            carsModel.markForDeletion(carToRemove);
            showInfo(localization.getString("info"), "Car ID " + idStr + " marked for removal");
            // Trigger table update to show Undelete button
            carTable.refresh();
        } else {
            showAlert(localization.getString("error"), "Car with ID " + idStr + " not found");
        }
    }

    private void removeCustomer() {
        String idStr = showInputDialog("ID");
        if (idStr == null || idStr.trim().isEmpty()) return;

        Customer customerToRemove = null;
        for (Customer customer : customersModel.getCustomers()) {
            if (customer != null && customer.uuid() != null && customer.uuid().equals(idStr)) {
                customerToRemove = customer;
                break;
            }
        }
        if (customerToRemove != null) {
            customersModel.markForDeletion(customerToRemove);
            showInfo(localization.getString("info"), "Customer ID " + idStr + " marked for removal");
            customerTable.refresh();
        } else {
            showAlert(localization.getString("error"), "Customer with ID " + idStr + " not found");
        }
    }

    private void removeRent() {
        String idStr = showInputDialog("ID");
        if (idStr == null || idStr.trim().isEmpty()) return;

        Rent rentToRemove = null;
        for (Rent rent : rentsModel.getRents()) {
            if (rent != null && rent.uuid() != null && rent.uuid().equals(idStr)) {
                rentToRemove = rent;
                break;
            }
        }
        if (rentToRemove != null) {
            rentsModel.markForDeletion(rentToRemove);
            showInfo(localization.getString("info"), "Rent ID " + idStr + " marked for removal");
            rentTable.refresh();
        } else {
            showAlert(localization.getString("error"), "Rent with ID " + idStr + " not found");
        }
    }

    private void performSearch() {
        String query = searchField.getText().toLowerCase().trim();
        
        // Update predicates for all lists to allow searching across tabs without switching logic
        filteredCars.setPredicate(car -> {
            if (query.isEmpty()) return true;
            return car.model().toLowerCase().contains(query) ||
                   car.licensePlate().toLowerCase().contains(query) ||
                   car.color().toLowerCase().contains(query);
        });

        filteredCustomers.setPredicate(customer -> {
            if (query.isEmpty()) return true;
            return customer.firstName().toLowerCase().contains(query) ||
                   customer.lastName().toLowerCase().contains(query) ||
                   customer.driversLicense().toLowerCase().contains(query);
        });

        filteredRents.setPredicate(rent -> {
            if (query.isEmpty()) return true;
            // Simple search for rents by ID or related IDs
            return (rent.uuid() != null && rent.uuid().toLowerCase().contains(query)) ||
                   (rent.carID() != null && rent.carID().toLowerCase().contains(query)) ||
                   (rent.customerID() != null && rent.customerID().toLowerCase().contains(query));
        });
    }

    private void loadAllData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load cars
                List<Car> cars = carManager.getAllCars();
                Platform.runLater(() -> carsModel.mergeCars(cars));

                // Load customers
                List<Customer> customers = customerManager.getAllCustomers();
                Platform.runLater(() -> customersModel.mergeCustomers(customers));

                // Load rents
                List<Rent> rents = rentManager.getAllRents();
                Platform.runLater(() -> rentsModel.mergeRents(rents));

                return null;
            }
        };

        new Thread(loadTask).start();
    }

    private DataSource prepareDataSource() {
        try {
            carManager.tryCreateTables();
            customerManager.tryCreateTables();
            rentManager.tryCreateTables();
            return new BasicDataSource();
        } catch (Exception ex) {
            showAlert(localization.getString("error"), localization.getString("db_connection_failure"));
            return null;
        }
    }

    private boolean discardChanges() {
        // TODO: Check for unsaved changes in models
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

        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Car updatedCar = new Car(car.uuid(), modelField.getText(), colorField.getText(), car.available(), Double.parseDouble(priceField.getText()), licensePlateField.getText());
                carsModel.updateCar(car, updatedCar);
                // Table automatically updates via ObservableList
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

        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Customer updatedCustomer = new Customer(customer.uuid(), firstNameField.getText(), lastNameField.getText(), addressField.getText(), phoneField.getText(), licenseField.getText(), customer.active());
            customersModel.updateCustomer(customer, updatedCustomer);
        }
    }

    private void openEditRentDialog(Rent rent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Rent");
        dialog.setHeaderText("Edit rent details");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-spacing: 10; -fx-background-color: #252526;");

        // Fetch related Car to get daily price
        Car car = null;
        try {
            car = carManager.findCarByID(rent.carID());
        } catch (Exception e) {
            // If offline or not found, try to find in local list if needed or fail gracefully
            // For simplicity, we assume car exists if rent exists or we might need to lookup in model
            for(Car c : carsModel.getCars()) {
                if (c.uuid() != null && c.uuid().equals(rent.carID())) {
                    car = c;
                    break;
                }
            }
        }
        final double dailyPrice = (car != null && car.rentalPayment() != null) ? car.rentalPayment() : 0.0;

        DatePicker rentDatePicker = new DatePicker(rent.rentDate().toLocalDate());
        rentDatePicker.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");
        
        // Disable rent date if it has already started
        if (rent.rentDate().toLocalDate().isBefore(LocalDate.now()) || rent.rentDate().toLocalDate().isEqual(LocalDate.now())) {
            rentDatePicker.setDisable(true);
        }

        DatePicker dueDatePicker = new DatePicker(rent.dueDate().toLocalDate());
        dueDatePicker.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-control-inner-background: #45475a;");

        Label rentDateLabel = new Label("Lease Date:");
        rentDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");
        
        Label dueDateLabel = new Label("Due Date:");
        dueDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #cdd6f4;");

        Label priceLabel = new Label("Estimated Total Price: ");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #a6da95;");

        // Logic to update price
        Runnable updatePrice = () -> {
            if (rentDatePicker.getValue() != null && dueDatePicker.getValue() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(rentDatePicker.getValue(), dueDatePicker.getValue());
                if (days < 0) days = 0;
                double total = days * dailyPrice;
                priceLabel.setText(String.format("Estimated Total Price: %.2f", total));
            }
        };

        rentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice.run());
        dueDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePrice.run());
        updatePrice.run(); // Initial calculation

        content.getChildren().addAll(
            rentDateLabel, rentDatePicker,
            dueDateLabel, dueDatePicker,
            priceLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("-fx-background-color: #252526;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        String cssResource = MainForm.class.getResource("modern.css").toExternalForm();
        if (cssResource != null) {
            dialog.getDialogPane().getStylesheets().add(cssResource);
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            java.sql.Date newRentDate = java.sql.Date.valueOf(rentDatePicker.getValue());
            java.sql.Date newDueDate = java.sql.Date.valueOf(dueDatePicker.getValue());
            
            Rent updatedRent = rent.withRentDate(newRentDate).withDueDate(newDueDate);
            rentsModel.updateRent(rent, updatedRent);
        }
    }
}