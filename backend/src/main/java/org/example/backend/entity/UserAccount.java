package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_account")
public class UserAccount extends BaseEntity {
    private String username;
    private String passwordHash;
    private String email;
}
