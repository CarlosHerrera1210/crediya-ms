package co.com.crediya.model.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Role {

    /** Role assigned to users registered without an explicit role. */
    public static final String DEFAULT_ROLE_NAME = "CLIENTE";

    private Integer id;
    private String name;
    private String description;
}
