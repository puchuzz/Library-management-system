# Deployment

## Environments
| Environment | Notes |
|---|---|
| Development | Run from IDE or `mvn compile exec:java` against a local MySQL |
| Production | Run the fat jar on the library's PC / server against a dedicated MySQL schema |

## Steps
1. Install JDK 17+ and MySQL 8+.
2. Create the schema: `mysql -u root -p < sql/schema.sql`.
3. Create a restricted MySQL user instead of using `root`:
   ```sql
   CREATE USER 'library_app'@'localhost' IDENTIFIED BY 'a-strong-password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON library_db.* TO 'library_app'@'localhost';
   ```
4. Build: `mvn clean package` (runs the unit tests first).
5. Configure credentials with `DB_URL`, `DB_USER`, `DB_PASSWORD` environment variables.
6. Start: `java -jar target/library-management-system-1.0.0.jar`.
7. Log in with the default admin account, change its password and create librarian accounts.

## Continuous integration
`.github/workflows/ci.yml` builds the project and runs all unit tests on every push and pull request to `main`.

## Backup
Schedule `mysqldump library_db > backup.sql` (cron or Task Scheduler) and keep copies off the machine.
