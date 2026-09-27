# Pocketkeeper: Notes and Password Manager

A beginner-friendly Spring Boot app for private notes and an encrypted password vault. It uses Java 17+, Spring MVC, Thymeleaf, Spring Security, Hibernate/JPA, and MySQL.

## Requirements

- Java 17 or newer
- Maven 3.9+ (or add the Maven Wrapper)
- MySQL 8+

## Configure MySQL

Create a database and a least-privilege application user. For local development, MySQL can create the database automatically when the configured account has permission:

```sql
CREATE DATABASE notes_vault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'notes_app'@'localhost' IDENTIFIED BY 'choose-a-local-password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON notes_vault.* TO 'notes_app'@'localhost';
```

Set the connection variables in your terminal or IDE run configuration. Never put real credentials in `application.properties` or commit them.

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/notes_vault?useSSL=false&serverTimezone=UTC'
$env:DB_USERNAME = 'notes_app'
$env:DB_PASSWORD = 'choose-a-local-password'
```

The defaults are intended only for a local MySQL installation using `root` with an empty password. Override them for your setup.

## Generate the vault key

The vault key must be a stable, randomly generated 32-byte value encoded as Base64. Losing or changing this key makes existing vault secrets permanently unreadable. Back it up in a secret manager; never commit it.

```powershell
$keyBytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$env:VAULT_ENCRYPTION_KEY = [Convert]::ToBase64String($keyBytes)
```

Set `VAULT_ENCRYPTION_KEY` in the same environment that starts the app. The app intentionally fails at startup if the key is missing or malformed.

## Run

From this directory:

```powershell
mvn spring-boot:run
```

Open <http://localhost:8080>. Create an account, then add notes or vault entries. Build a runnable jar with `mvn clean package`, then run it with `java -jar target/notes-password-manager-0.0.1-SNAPSHOT.jar` after setting the same environment variables.

Run tests with:

```powershell
mvn test
```

## Security notes

- Spring Security protects all app routes, uses CSRF tokens for form actions, and stores account passwords as BCrypt hashes.
- Vault values use AES-256-GCM with a random 96-bit nonce per encryption. Only ciphertext is saved to MySQL; secrets are not returned in the vault list and are revealed only after an authenticated POST request.
- Notes are private to their owner at the repository query boundary. This starter stores note content as plaintext in MySQL; use encrypted storage and managed backups for sensitive notes in a production deployment.
- Use HTTPS, secure environment/secret management, database backups, rate limiting, account recovery, and security review before exposing the app to the internet. Configure secure session cookies behind HTTPS for production.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for learning. Replace it with a versioned migration tool such as Flyway or Liquibase before production use.


Sure. You generate the key in PowerShell, then copy it into IntelliJ. You do this once and keep the same key.

Open PowerShell (Windows Start menu → search “PowerShell”).
Copy and run this command:

$keyBytes = [byte[]]::new(32); [System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes); [Convert]::ToBase64String($keyBytes)

copy the out before ending =
