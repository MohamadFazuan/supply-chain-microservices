package com.supply.chain.microservice.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private String subject;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> authorities;
    private String serviceId;
}
