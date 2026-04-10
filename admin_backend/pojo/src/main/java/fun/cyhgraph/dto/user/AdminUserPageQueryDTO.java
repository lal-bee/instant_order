package fun.cyhgraph.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminUserPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    /**
     * 昵称或用户名
     */
    private String keyword;
    private String phone;
    private Integer status;
    /**
     * 是否会员筛选：1-有效会员 0-非会员
     */
    private Integer isMember;
}

