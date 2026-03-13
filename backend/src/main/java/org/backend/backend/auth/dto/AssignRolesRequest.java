package org.backend.backend.auth.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {

    private Set<String> roles;
}
