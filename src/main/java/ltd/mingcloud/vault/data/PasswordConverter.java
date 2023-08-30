package ltd.mingcloud.vault.data;

import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Plaintext;

/**
 * @author wynn5a
 */
@Service
public class PasswordConverter implements AttributeConverter<String, String> {

  public static final String KEY_NAME = "password";
  private final VaultOperations vaultOperations;

  public PasswordConverter(VaultOperations vaultOperations) {
    this.vaultOperations = vaultOperations;
  }

  @Override
  public String convertToDatabaseColumn(String password) {
    Plaintext plaintext = Plaintext.of(password);
    return vaultOperations.opsForTransit().encrypt(KEY_NAME, plaintext).getCiphertext();
  }

  @Override
  public String convertToEntityAttribute(String password) {
    return vaultOperations.opsForTransit().decrypt(KEY_NAME, password);
  }
}
