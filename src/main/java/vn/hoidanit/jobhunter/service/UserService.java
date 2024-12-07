package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        User user = userOptional.get();
        return user;

    }

    public List<User> fetchAllUser() {

        return this.userRepository.findAll();

    }

    public User updateUser(User user) {
        User user_new = new User();
        try {
            user_new = this.fetchUserById(user.getId());
            if (user_new != null) {
                user_new.setName(user.getName());
                user_new.setEmail(user.getEmail());
                user_new.setPassword(user.getPassword());
                this.userRepository.save(user_new);

            } else {
                return null;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return user;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

}
