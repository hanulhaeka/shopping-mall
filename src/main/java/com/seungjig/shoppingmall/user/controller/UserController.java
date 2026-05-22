package com.seungjig.shoppingmall.user.controller;

import com.seungjig.shoppingmall.user.entity.dto.SignupDto;
import com.seungjig.shoppingmall.user.exception.DuplicateLoginIdException;
import com.seungjig.shoppingmall.user.exception.NotCorrectConfirmPasswordException;
import com.seungjig.shoppingmall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입 페이지 전송
    @GetMapping("/signup")
    public String signup(@ModelAttribute("signupDto") SignupDto signupDto) {
        return "user/signup";
    }

    // 회원 가입 요청 처리
    @PostMapping("/signup")
    public String signup(@Validated @ModelAttribute("signupDto") SignupDto signupDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        try {
            userService.signup(signupDto);
        } catch (NotCorrectConfirmPasswordException e) {
            bindingResult.rejectValue("confirmPassword", "NotCorrectPassword", e.getMessage());
            return "user/signup";
        } catch (DuplicateLoginIdException e) {
            bindingResult.rejectValue("loginId", "duplicateLoginId", e.getMessage());
            return "user/signup";
        }

        return "redirect:/";
    }
}
