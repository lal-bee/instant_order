package fun.cyhgraph.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminUserStatusDTO implements Serializable {

    private Integer id;
    /**
     * 1-启用 0-禁用
     */
    private Integer status;
}

