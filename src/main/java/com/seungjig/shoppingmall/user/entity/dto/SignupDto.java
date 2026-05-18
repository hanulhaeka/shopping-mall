package com.seungjig.shoppingmall.user.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

     @NotBlank(message = "아이디를 입력해야 합니다.")
     private String loginId;

     @NotBlank(message = "비밀번호를 입력해야 합니다.")
     private String password;

     @NotBlank(message = "검증용 비밀번호를 입력해야 합니다.")
     private String confirmPassword;

     @NotBlank(message = "이름을 입력해야 합니다.")
     private String name;

     private String phoneNumber;
}
