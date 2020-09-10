package org.tkit.quarkus.rs.interceptors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class AccessToken {
    private String token;
    private long expiresIn;
    private String refreshToken;
    private long refreshExpiresIn;
}
