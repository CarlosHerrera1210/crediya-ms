package co.com.crediya.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo_electronico"),
                @UniqueConstraint(name = "uk_usuarios_documento", columnNames = "documento_identidad")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String names;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String lastNames;

    @Column(name = "fecha_nacimiento")
    private LocalDate birthDate;

    @Column(name = "direccion", length = 255)
    private String address;

    @Column(name = "telefono", length = 30)
    private String phone;

    @Column(name = "correo_electronico", nullable = false, length = 150)
    private String email;

    @Column(name = "salario_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "documento_identidad", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "id_rol", nullable = false)
    private Integer roleId;
}
