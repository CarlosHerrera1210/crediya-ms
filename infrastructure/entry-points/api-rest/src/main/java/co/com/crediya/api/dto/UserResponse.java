package co.com.crediya.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("nombres") String names,
        @JsonProperty("apellidos") String lastNames,
        @JsonProperty("fecha_nacimiento") LocalDate birthDate,
        @JsonProperty("direccion") String address,
        @JsonProperty("telefono") String phone,
        @JsonProperty("correo_electronico") String email,
        @JsonProperty("salario_base") BigDecimal baseSalary,
        @JsonProperty("documento_identidad") String documentNumber,
        @JsonProperty("id_rol") Integer roleId
) {
}
