package ltd.mingcloud.vault.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wynn5a
 */
@RefreshScope
@RestController
@ConfigurationProperties
public class SecretController {

  @Value("${secret:n/a}")
  String secret;

  @GetMapping("secret")
  public String secret() {
    return secret;
  }
}
