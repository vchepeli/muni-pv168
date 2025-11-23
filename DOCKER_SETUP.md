# Docker Setup for Car Rental Management System

This guide explains how to use Docker Compose to run PostgreSQL database for the Car Rental Management application.

## Prerequisites

- Docker installed ([Download Docker](https://www.docker.com/products/docker-desktop))
- Docker Compose installed (included with Docker Desktop)
- Java 21 for running the application

## Quick Start

### 1. Start the PostgreSQL Database

Navigate to the project root directory and run:

```bash
docker-compose up -d
```

This will:
- Start PostgreSQL 16 (Alpine Linux lightweight image)
- Create a container named `car-rental-postgres`
- Expose PostgreSQL on `localhost:5432`
- Create the `car_rental_db` database with admin user
- Create persistent storage for the database in `postgres-data` volume
- Initialize the database with sample tables and data

### 2. Verify PostgreSQL is Running

Check the container status:

```bash
docker-compose ps
```

Wait for the status to be "Up" and healthy. You can also check logs:

```bash
docker-compose logs postgres
```

### 3. Connect the Application

The application is already configured to connect to PostgreSQL at `jdbc:postgresql://localhost:5432/car_rental_db` with credentials:
- Username: `admin`
- Password: `admin`

Just build and run the JavaFX application:

```bash
mvn clean install
java -jar CarRentalManagementGUI/target/car-rental-management-gui-1.0.0.jar
```

## Database Details

**Database Name:** `car_rental_db`
**Host:** `localhost`
**Port:** `5432`
**Username:** `admin`
**Password:** `admin`

### Tables Created

1. **customer** - Customer information
   - id (auto-increment), first_name, last_name, phone_number, email

2. **car** - Vehicle inventory
   - id (auto-increment), spz (license plate - unique), brand, model, color, seats, price_per_day

3. **rent** - Rental records
   - id (auto-increment), car_id (FK), customer_id (FK), from_date, to_date

### Sample Data Included

The `postgres-init.sql` script automatically creates and populates:
- 5 sample cars (Toyota, Honda, VW, BMW, Mercedes)
- 5 sample customers

## Common Commands

### Stop the Database

```bash
docker-compose down
```

### Stop but Keep Data

```bash
docker-compose stop
```

### Resume the Database

```bash
docker-compose start
```

### Remove Everything (Including Data)

```bash
docker-compose down -v
```

### View PostgreSQL Server Logs

```bash
docker-compose logs postgres -f
```

## Connecting to PostgreSQL

### From Command Line (using psql)

```bash
docker-compose exec postgres psql -U admin -d car_rental_db
```

Then you can run SQL queries:

```sql
SELECT * FROM customer;
SELECT * FROM car;
SELECT * FROM rent;
```

### From PostgreSQL Management Tools

Use any PostgreSQL client (pgAdmin, DBeaver, etc.) with connection details:
- **Host:** localhost
- **Port:** 5432
- **Database:** car_rental_db
- **Username:** admin
- **Password:** admin

### From Java Application Code

Use the JDBC connection string:

```
jdbc:postgresql://localhost:5432/car_rental_db
```

## Troubleshooting

### PostgreSQL Port Already in Use

If port 5432 is already in use, modify `docker-compose.yml`:

```yaml
ports:
  - "15432:5432"  # Maps 15432 on host to 5432 in container
```

Then update `database.properties`:

```properties
jdbc.url=jdbc:postgresql://localhost:15432/car_rental_db
```

### Database Connection Failed

1. Wait 10-15 seconds for PostgreSQL to fully start and initialize
2. Check logs: `docker-compose logs postgres`
3. Ensure the container is healthy: `docker-compose ps`
4. Verify no firewall blocking port 5432

### Container Won't Start

Check detailed logs:

```bash
docker-compose logs postgres --tail=100
```

Rebuild and restart:

```bash
docker-compose down -v
docker-compose up -d
```

### Permission Denied Error

Ensure Docker daemon is running and your user has permission to use Docker:

```bash
sudo docker-compose up -d
```

Or add your user to docker group:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

## Managing Data

### View Database Files

The PostgreSQL data is stored in the `postgres-data` volume:

```bash
docker volume inspect car-rental-management_postgres-data
```

### Backup Database

Backup the volume (on Linux/Mac):

```bash
docker run --rm -v car-rental-management_postgres-data:/data \
  -v $(pwd)/backups:/backup alpine tar czf /backup/postgres-backup.tar.gz -C /data .
```

### Restore Database

Stop the service, remove the volume, and recreate:

```bash
docker-compose down -v
docker-compose up -d
```

This will reinitialize the database with sample data.

## Performance Notes

PostgreSQL performance optimizations are minimal for this development setup. For production:

1. Tune PostgreSQL configuration (`postgresql.conf`)
2. Adjust work_mem, shared_buffers, and other parameters
3. Monitor with `docker stats`
4. Consider using a dedicated PostgreSQL server

## Security Considerations

This setup is suitable for **development only**. For production:

1. Change default credentials (admin/admin)
2. Enable SSL/TLS connections
3. Restrict network access (use firewall rules)
4. Set up proper backups and recovery procedures
5. Enable PostgreSQL audit logging
6. Use environment variables for sensitive credentials
7. Consider using managed database services (AWS RDS, Azure Database, etc.)

To change credentials, modify `docker-compose.yml` and `database.properties`:

```yaml
environment:
  POSTGRES_USER: your_user
  POSTGRES_PASSWORD: your_secure_password
```

```properties
jdbc.url=jdbc:postgresql://localhost:5432/car_rental_db
jdbc.username=your_user
jdbc.password=your_secure_password
```

## Advanced Configuration

### Custom Database Name

Modify `docker-compose.yml`:

```yaml
environment:
  POSTGRES_DB: your_database_name
```

And update `database.properties`:

```properties
jdbc.url=jdbc:postgresql://localhost:5432/your_database_name
```

### Custom Initialization Scripts

Place additional SQL files in the root directory and add them to `docker-compose.yml`:

```yaml
volumes:
  - ./postgres-init.sql:/docker-entrypoint-initdb.d/01-init.sql
  - ./custom-script.sql:/docker-entrypoint-initdb.d/02-custom.sql
```

PostgreSQL executes scripts in alphanumeric order.

## Docker Compose vs Traditional Installation

| Aspect | Docker Compose | Traditional |
|--------|---|---|
| **Setup Time** | <30 seconds | 10+ minutes |
| **Portability** | Works everywhere | OS-specific |
| **Isolation** | Complete | Shared system resources |
| **Cleanup** | One command | Manual uninstall |
| **Data Persistence** | Volume management | Filesystem dependent |

## Useful Docker Commands

```bash
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# View container logs with timestamps
docker-compose logs postgres --timestamps

# Execute command in container
docker-compose exec postgres psql -U admin -d car_rental_db

# Get container IP address
docker inspect car-rental-postgres | grep IPAddress

# Monitor container resource usage
docker stats car-rental-postgres
```

## Switching Between Databases

If you previously used Apache Derby and want to switch to PostgreSQL:

1. Stop the Derby container (if running)
2. Update `pom.xml` - PostgreSQL driver is included
3. Update `database.properties` - Already configured for PostgreSQL
4. Run `docker-compose up -d` to start PostgreSQL
5. Rebuild and run the application

Data will not migrate automatically - you'll start with sample data from `postgres-init.sql`.
