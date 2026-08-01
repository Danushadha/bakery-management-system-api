package com.bakery.bakery_management_system_api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverDetailsResponseDTO {
    private Long id;
    private String name;
    private String nic;
    private String contactNo;
}
