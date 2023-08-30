package ltd.mingcloud.vault.data;

import jakarta.annotation.Nonnull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wynn5a
 */
@RestController
public class UserController {

  final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostMapping("users")
  public String create(@RequestBody User user) {
    userRepository.save(user);
    return user.getId().toString();
  }


  @GetMapping("users/{id}")
  public User get(@PathVariable("id") @Nonnull Long id) {
    return userRepository.findById(id).orElseThrow();
  }
}
