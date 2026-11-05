package open.judge.Services;

import lombok.RequiredArgsConstructor;
import open.judge.Entity.Role;
import open.judge.Entity.User;
import open.judge.GlobalExceptionHandler.BusinessException;
import open.judge.GlobalExceptionHandler.ResourceNotFoundException;
import open.judge.Repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(String email, String displayName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_EXISTS", "A user with this email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setRole(role);
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
