package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResUserDTO;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvaliException;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postUser)
            throws IdInvaliException {
        boolean isEmailExist = this.userService.isEmailExist(postUser.getEmail());
        if (isEmailExist) {
            throw new IdInvaliException(
                    "Email: " + postUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác!");
        }
        String hashPassword = this.passwordEncoder.encode(postUser.getPassword());
        postUser.setPassword(hashPassword);
        User new_user = this.userService.handleCreateUser(postUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(new_user));

    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> delateUser(@PathVariable("id") long id)
            throws IdInvaliException {

        User user = this.userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvaliException("User với id: " + id + "không tồn tại");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws IdInvaliException {
        User user = this.userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvaliException("User với id: " + id + "không tồn tại");
        }

        user = this.userService.fetchUserById(id);

        return ResponseEntity.ok(this.userService.convertToResUserDTO(user));
    }

    @GetMapping("/users")
    @ApiMessage(value = "fecth all user")
    public ResponseEntity<ResultPaginationDTO> getAllUser(@Filter Specification<User> spect, Pageable pageable) {
        return ResponseEntity.ok(this.userService.fetchAllUser(spect, pageable));

    }

    @PutMapping("/users")
    @ApiMessage("update a user")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvaliException {
        User user_update = this.userService.fetchUserById(user.getId());
        if (user_update == null) {
            throw new IdInvaliException("User với id: " + user.getId() + "không tồn tại");
        }
        user_update = this.userService.updateUser(user);

        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(user_update));
    }

}
