package ltd.mingcloud.vault.data;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.RefreshEndpoint;

/**
 * @author wynn5a
 */
//@Service
public class RefreshService {

  Logger log = LoggerFactory.getLogger(RefreshService.class);

  private final RefreshEndpoint refreshEndpoint;

  public RefreshService(RefreshEndpoint refreshEndpoint) {
    this.refreshEndpoint = refreshEndpoint;
  }


  //  @Scheduled(fixedRate = 300000)
  public void refresh() {
    Collection<String> keys = refreshEndpoint.refresh();
    String refreshed = keys.stream().collect(Collectors.joining(","));
    log.info("Refreshed: {}", refreshed);
  }
}
