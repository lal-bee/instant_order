package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private Integer isMember;
    private Integer memberLevel;
    private String token;
}
