package com.seungjig.shoppingmall.user.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

     @Pattern(regexp = "^$|^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
     private String phoneNumber;
}
