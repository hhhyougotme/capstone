package com.flashmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(max = 32)
    private String phone;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @Size(max = 64)
    private String nickname;
}
