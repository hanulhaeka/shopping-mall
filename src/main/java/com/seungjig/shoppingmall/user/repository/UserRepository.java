package com.seungjig.shoppingmall.user.repository;

import com.seungjig.shoppingmall.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
