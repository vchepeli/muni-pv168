# Java Swing to JavaFX Migration Guide

## Overview
This project has been successfully migrated from Java Swing to JavaFX with Java 21 as the target platform.

## What Was Changed

### Build System
- **Replaced**: Apache Ant (NetBeans-based) with Maven
- **Added**: `pom.xml` files for both projects with Java 21 configuration
- **Dependencies**: Now managed through Maven with proper version constraints

### Java Version
- **Old**: Java 7 (1.7)
- **New**: Java 21 (21.0.0)

### GUI Framework
- **Replaced**: All Swing components (JFrame, JPanel, JButton, etc.) with JavaFX equivalents
- **Main Window**: Converted from JFrame to Stage + Scene architecture
- **Tables**: Converted from JTable to TableView<T> with ObservableList
- **Dialogs**: Converted from custom JFrame dialogs to Dialog<T> components
- **Event Handling**: Updated to use JavaFX EventHandler and lambda expressions
- **Background Tasks**: Replaced SwingWorker with JavaFX Task

## File Changes

### Modified Files
1. **CarRentalManagementGUI.java**
   - Now extends `javafx.application.Application`
   - Implements `start(Stage)` method
   - Main entry point launches JavaFX application

2. **MainForm.java**
   - Completely rewritten for JavaFX
   - Uses BorderPane layout with TabPane for tabs
   - TableView<Car>, TableView<Customer>, TableView<Rent> for data display
   - Menu bar with File, Data, and Help menus
   - Toolbar with action buttons
   - Uses Task for background database operations

3. **NewCarForm.java**
   - Converted to Dialog<Car>
   - Uses GridPane for form layout
   - Result converter for data handling

4. **NewCustomerForm.java**
   - Converted to Dialog<Customer>
   - Uses GridPane with 5 input fields
   - Result converter for data handling

5. **NewRentForm.java**
   - Converted to Dialog<Rent>
   - Uses DatePicker instead of spinners/combobox for dates
   - Spinners for car ID, customer ID, and duration
   - Automatic due date calculation

### New Files
- `CarRentalManagement/pom.xml` - Maven configuration for business logic layer
- `CarRentalManagementGUI/pom.xml` - Maven configuration for JavaFX GUI layer
- `build-and-run.sh` - Build script for systems without Maven

## Building the Project

### Prerequisites
- Java 21 or later
- Maven 3.6 or later (for internet-connected builds)
- Internet connection (for first build to download dependencies)

### Build Instructions

#### Option 1: Using Maven (Recommended)
```bash
cd /home/user/muni-pv168

# Build both projects
mvn clean install

# Run the application
cd CarRentalManagementGUI
mvn javafx:run
```

#### Option 2: Using Maven with Single Command
```bash
cd /home/user/muni-pv168/CarRentalManagementGUI
mvn clean javafx:run
```

#### Option 3: Build JAR and Run
```bash
cd /home/user/muni-pv168

# Build JAR
mvn clean package

# Run JAR
java -jar CarRentalManagementGUI/target/car-rental-management-gui-1.0.0.jar
```

### Build Script (Local Compilation)
For systems without Maven or internet access:
```bash
cd /home/user/muni-pv168
chmod +x build-and-run.sh
./build-and-run.sh
```

Note: This script requires JavaFX libraries to be available on the system.

## Project Structure

```
/home/user/muni-pv168/
├── CarRentalManagement/          (Business Logic Layer)
│   ├── src/
│   │   └── cz/muni/fi/pv168/
│   │       ├── Car.java
│   │       ├── CarManager.java
│   │       ├── CarManagerImplementation.java
│   │       ├── Customer.java
│   │       ├── CustomerManager.java
│   │       ├── CustomerManagerImplementation.java
│   │       ├── Rent.java
│   │       ├── RentManager.java
│   │       ├── RentManagerImplementation.java
│   │       ├── DBUtils.java
│   │       └── TransactionException.java
│   └── pom.xml
│
├── CarRentalManagementGUI/       (JavaFX GUI Layer)
│   ├── src/
│   │   └── cz/muni/fi/pv168/
│   │       ├── CarRentalManagementGUI.java
│   │       ├── MainForm.java
│   │       ├── NewCarForm.java
│   │       ├── NewCustomerForm.java
│   │       ├── NewRentForm.java
│   │       ├── CarsTableModel.java
│   │       ├── CustomersTableModel.java
│   │       ├── RentsTableModel.java
│   │       └── SortFilterModel.java
│   └── pom.xml
│
├── drivers/                       (External Dependencies)
│   ├── commons-dbcp-1.4.jar
│   ├── commons-pool-1.6.jar
│   ├── derbyclient.jar
│   └── derbynet.jar
│
├── build-and-run.sh              (Build script for local compilation)
└── MIGRATION.md                  (This file)
```

## Key Improvements with JavaFX

### Modern UI Framework
- JavaFX provides better visual appearance and more controls
- Improved theming and styling capabilities
- Better cross-platform compatibility

### Java 21 Features
- Modern language features and improvements
- Better performance and memory management
- Enhanced module system

### Code Quality
- Cleaner separation of concerns
- Better event handling with lambda expressions
- Type-safe generic collections (TableView<T>)

## Database Configuration

The database configuration remains the same:
- Apache Derby database
- Connection pooling via Apache Commons DBCP
- Configuration file: `cz/muni/fi/pv168/database.properties`

## Troubleshooting

### Issue: "Package javafx.* does not exist"
**Solution**: Ensure Maven downloads JavaFX dependencies or set JAVAFX_PATH environment variable.

### Issue: "Output directory is empty" (Maven error)
**Solution**: Run `mvn compile` before `mvn javafx:run`.

### Issue: Database connection fails
**Solution**:
1. Ensure `database.properties` is in the classpath
2. Check Derby database is running or accessible
3. Verify database credentials in properties file

### Issue: Table data not displaying
**Solution**:
1. Ensure database connection is established first (via File menu)
2. Check that database contains data
3. Verify network connectivity for database access

## Future Considerations

1. **FXML Templates**: Could convert UI layouts to FXML (XML-based UI markup)
2. **CSS Styling**: Leverage JavaFX CSS for better UI customization
3. **Scene Builder**: Use JavaFX Scene Builder for visual UI design
4. **Testing**: Add JavaFX testing framework (TestFX) for UI tests
5. **Modules**: Take advantage of Java 21 module system

## Commit Information

- **Branch**: `claude/swing-to-javafx-migration-01SpccfN6hQeAyTcHFK1GZdY`
- **Commit Message**: "Migrate from Java Swing to JavaFX and Java 21"
- **Date**: 2025-11-23

## Support

For issues or questions about the migration:
1. Check the Maven build output for detailed error messages
2. Ensure all dependencies are properly downloaded
3. Verify Java 21 is being used: `java -version`
4. Check that JavaFX modules are available on your system
