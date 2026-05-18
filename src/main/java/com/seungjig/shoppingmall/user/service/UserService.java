package com.seungjig.shoppingmall.user.service;

import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.entity.dto.SignupDto;
import com.seungjig.shoppingmall.user.exception.DuplicateLoginIdException;
import com.seungjig.shoppingmall.user.exception.NotCorrectConfirmPasswordException;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public void signup(SignupDto signupDto) {

        if (!signupDto.getPassword().equals(signupDto.getConfirmPassword())) {
            throw new NotCorrectConfirmPasswordException("검증을 위해 입력한 비밀번호가 서로 일치하지 않습니다.");
        }

        if (userRepository.existsByLoginId(signupDto.getLoginId())) {
            throw new DuplicateLoginIdException("중복된 아이디가 있습니다.");
        }

        String hashed = passwordEncoder.encode(signupDto.getPassword());

        User user = new User(signupDto.getLoginId(), hashed, signupDto.getName(), signupDto.getPhoneNumber(),
                Role.USER, UserStatus.ACTIVE);


        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateLoginIdException("중복된 아이디가 있습니다.");
        }
    }
}
