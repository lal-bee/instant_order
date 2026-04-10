package fun.cyhgraph.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminUserSaveDTO implements Serializable {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private Integer gender;
    private String pic;
}

