package com.mk.www.smsmonitor.user.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String loginId;
    private String password;
}
