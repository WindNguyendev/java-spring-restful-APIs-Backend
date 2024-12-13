package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.request.reqLoginDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvaliException;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                        UserService userService) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
        }

        @PostMapping("/auth/login")
        public ResponseEntity<Object> login(@Valid @RequestBody reqLoginDTO loginDTO) {

                // Nạp input gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng => cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO res = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        res.setUserLogin(userLogin);
                }
                // Tạo ra token
                String access_token = this.securityUtil.createAccessToken(authentication.getName(), res.getUserLogin());
                res.setAccsseToken(access_token);

                String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);

                // update user
                this.userService.updateUserToken(refreshToken, loginDTO.getUsername());

                // set cookies
                ResponseCookie responseCookie = ResponseCookie
                                .from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(60) // set thời gian tồn tại cho cookies
                                .build();

                return ResponseEntity.ok()
                                // .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(res);
        }

        @GetMapping("/auth/account")
        public ResponseEntity<Object> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDB != null) {
                        userLogin.setId(currentUserDB.getId());
                        userLogin.setEmail(currentUserDB.getEmail());
                        userLogin.setName(currentUserDB.getName());
                        userGetAccount.setUser(userLogin);

                }
                return ResponseEntity.ok(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @ApiMessage("Get user by refresh token")
        public ResponseEntity<Object> getRefreshToken(
                        @CookieValue(name = "refreshToken", defaultValue = "") String re) throws IdInvaliException {

                if (re.equals("")) {
                        throw new IdInvaliException("Bạn không có refresh Token ở cookies");
                }
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(re);
                String email = decodedToken.getSubject();

                // check new token/set refresh token as cookies
                User currentUser = this.userService.getRefreshAndEmail(re, email);
                if (currentUser == null) {
                        throw new IdInvaliException("Refresh token không hợp lệ!");
                }

                ResLoginDTO res = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());

                        res.setUserLogin(userLogin);
                        userGetAccount.setUser(userLogin);
                }
                String access_token = this.securityUtil.createAccessToken(email, res.getUserLogin());
                res.setAccsseToken(access_token);

                String new_refreshToken = this.securityUtil.createRefreshToken(email, res);

                // update user
                this.userService.updateUserToken(new_refreshToken, email);

                // set cookies
                ResponseCookie responseCookie = ResponseCookie
                                .from("refreshToken", new_refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(60) // set thời gian tồn tại cho cookies
                                .build();

                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(userGetAccount);
        }

        @PostMapping("/auth/logout")
        @ApiMessage("Logout user")
        public ResponseEntity<Void> logout() throws IdInvaliException {
                String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                if (email.equals("")) {
                        throw new IdInvaliException("Access Token không hợp lệ!");
                }

                // update refresh token == null
                this.userService.updateUserToken(null, email);

                // remove refreshToken cookies
                ResponseCookie deleteSpringCookie = ResponseCookie
                                .from("refreshToken", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0) // set thời gian tồn tại cho cookies
                                .build();

                return ResponseEntity.ok()
                                // .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                                .body(null);

        }

}
