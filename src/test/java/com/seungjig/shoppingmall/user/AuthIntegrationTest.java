package com.seungjig.shoppingmall.user;

import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 가입 성공")
    void signUpSuccess() throws Exception {

        //given
        String loginId = "loginId";
        String password = "password";

        //when
        mockMvc.perform(post("/users/signup")
                        .with(csrf())
                .param("loginId", loginId)
                .param("password", password)
                .param("confirmPassword", password)
                .param("name", "name")
                .param("phoneNumber", "010-1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        //then
        User user = userRepository.findByLoginId("loginId")
                        .orElseThrow(() -> new AssertionError("회원이 저장되지 않음"));


        assertThat(user.getId()).isNotNull();
        assertThat(user.getLoginId()).isEqualTo("loginId");
        assertThat(passwordEncoder.matches(
                password,
                user.getHashedPassword()
        )).isTrue();
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 가입 실패 - 이미 존재하는 아이디로 회원 가입 시도")
    void signUpFailure() throws Exception {

        //given
        String loginId = "loginId";
        String password = "password";

        User user = new User(loginId, "hashedPassword", "name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        //when&then
        mockMvc.perform(post("/users/signup")
                        .with(csrf())
                        .param("loginId", loginId)
                        .param("password", password)
                        .param("confirmPassword", password)
                        .param("name", "name")
                        .param("phoneNumber", "010-1234-5678"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"))
                .andExpect(model().attributeHasErrors("signupDto"))
                .andExpect(model().attributeHasFieldErrors("signupDto", "loginId"));

    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() throws Exception {

        //given
        String loginId = "loginId";
        String hashedPassword = passwordEncoder.encode("password");

        User user = new User(loginId, hashedPassword,"name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        //when&then
        mockMvc.perform(post("/users/signin")
                .with(csrf())
                .param("username", "loginId")
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername(loginId))
                .andExpect(authenticated().withRoles(Role.USER.name()));

    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 입력")
    void loginFailure() throws Exception {

        //given
        String loginId = "loginId";
        String hashedPassword = passwordEncoder.encode("password");

        User user = new User(loginId, hashedPassword,"name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        //when&then
        mockMvc.perform(post("/users/signin")
                        .with(csrf())
                        .param("username", "loginId")
                        .param("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/signin?error"));
    }

}
