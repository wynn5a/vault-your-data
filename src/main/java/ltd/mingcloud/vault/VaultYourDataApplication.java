package ltd.mingcloud.vault;

import jakarta.annotation.PostConstruct;
import java.net.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Configuration
@EnableScheduling
public class VaultYourDataApplication {

  final SessionManager sessionManager;
  final Environment environment;

  private static final Logger logger = LoggerFactory.getLogger(VaultYourDataApplication.class);

  public VaultYourDataApplication(SessionManager sessionManager, Environment environment) {
    this.sessionManager = sessionManager;
    this.environment = environment;
  }

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setProxy(Proxy.NO_PROXY);
    return new RestTemplate(requestFactory);
  }

  public static void main(String[] args) {
    SpringApplication.run(VaultYourDataApplication.class, args);
  }


  @PostConstruct
  public void initIt() {
    logger.info("Got Vault Token: " + sessionManager.getSessionToken().getToken());
    String[] activeProfiles = environment.getActiveProfiles();
    for (String profile : activeProfiles) {
      logger.info(profile);
    }
  }
}
