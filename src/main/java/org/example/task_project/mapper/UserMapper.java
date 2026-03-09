package org.example.task_project.mapper;

import org.example.task_project.dto.UserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class UserMapper {

    public UserDto toDto(UserRepresentation representation) {
        if (representation == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setKeycloakId(representation.getId());
        dto.setEmail(representation.getEmail());
        dto.setNom(representation.getLastName());
        dto.setPrenom(representation.getFirstName());

        Map<String, List<String>> attributes = representation.getAttributes();
        if (attributes != null) {
            if (attributes.containsKey("telephone") && !attributes.get("telephone").isEmpty()) {
                dto.setTelephone(attributes.get("telephone").get(0));
            }
            if (attributes.containsKey("dateNaissance") && !attributes.get("dateNaissance").isEmpty()) {
                // On parse la date
                try {
                    dto.setDateNaissance(LocalDate.parse(attributes.get("dateNaissance").get(0)));
                } catch (Exception e) {
                    // ignored
                }
            }
            if (attributes.containsKey("identityDocUrl") && !attributes.get("identityDocUrl").isEmpty()) {
                dto.setIdentityDocUrl(attributes.get("identityDocUrl").get(0));
            }
            if (attributes.containsKey("role") && !attributes.get("role").isEmpty()) {
                dto.setRole(attributes.get("role").get(0));
            }
        }
        return dto;
    }
}
