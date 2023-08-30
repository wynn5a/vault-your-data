package ltd.mingcloud.vault.data;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.domain.RequestedSecret.Mode;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent;

@Configuration
@ConditionalOnBean(SecretLeaseContainer.class)
public class DatabaseLeaseEventHandler {

  private final Logger log = LoggerFactory.getLogger(DatabaseLeaseEventHandler.class);

  private final ConfigurableApplicationContext applicationContext;
  private final HikariDataSource hikariDataSource;
  private final SecretLeaseContainer secretLeaseContainer;

  @Value("${spring.cloud.vault.database.role}")
  private String datasourceRole;

  private final String vaultPath;

  public DatabaseLeaseEventHandler(ConfigurableApplicationContext applicationContext, HikariDataSource hikariDataSource,
      SecretLeaseContainer secretLeaseContainer) {
    this.applicationContext = applicationContext;
    this.hikariDataSource = hikariDataSource;
    this.secretLeaseContainer = secretLeaseContainer;
    vaultPath = "database/creds/%s".formatted(datasourceRole);
  }


  @PostConstruct
  public void afterInit() {

    secretLeaseContainer.addLeaseListener(leaseEvent -> {
      RequestedSecret source = leaseEvent.getSource();
      if (vaultPath.equals(source.getPath())) {
        Mode mode = source.getMode();
        if (leaseEvent instanceof SecretLeaseExpiredEvent && Mode.RENEW.equals(mode)) {
          log.info("Database lease is expired, request new database credentials");
          secretLeaseContainer.requestRotatingSecret(vaultPath);
        } else if (leaseEvent instanceof SecretLeaseCreatedEvent secretLeaseCreatedEvent && Mode.ROTATE.equals(mode)) {
          log.info("Database lease is created, update to new database credentials");
          Map<String, Object> secrets = secretLeaseCreatedEvent.getSecrets();
          var username = secrets.get("username");
          var password = secrets.get("password");
          Credential credential = new Credential(username, password);
          if (!credential.valid()) {
            log.error("Cannot get updated DB credentials. Shutting down.");
            applicationContext.close();
            return;
          }

          refreshDatabaseCredentials(credential.stringify());
        }
      }
    });
  }

  private void refreshDatabaseCredentials(StringCredential credential) {
    updateProperties(credential);
    updateDataSource(credential);
  }

  private void updateProperties(StringCredential credential) {
    System.setProperty("spring.datasource.username", credential.username());
    System.setProperty("spring.datasource.password", credential.password());
  }

  private void updateDataSource(StringCredential credential) {
    hikariDataSource.getHikariConfigMXBean().setUsername(credential.username());
    hikariDataSource.getHikariConfigMXBean().setPassword(credential.password());
    hikariDataSource.getHikariPoolMXBean().softEvictConnections();
    log.info("Database credentials are updated");
  }
}

record Credential(Object username, Object password) {

  public boolean valid() {
    return username != null && password != null;
  }


  public StringCredential stringify() {
    return new StringCredential(username.toString(), password.toString());
  }
}

record StringCredential(String username, String password) {

}