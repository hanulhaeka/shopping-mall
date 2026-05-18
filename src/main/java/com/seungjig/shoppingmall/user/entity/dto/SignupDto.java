package com.seungjig.shoppingmall.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

     private String loginId;

     private String password;

     private String confirmPassword;

     private String name;

     private String phoneNumber;
}
