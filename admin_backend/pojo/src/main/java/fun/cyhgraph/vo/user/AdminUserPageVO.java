package fun.cyhgraph.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminUserPageVO implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private String phone;
    private Integer gender;
    private Integer status;
    private Integer isMember;
    private Integer memberLevel;
    private LocalDateTime memberExpireTime;
    private LocalDateTime createTime;
    private String memberStatusText;
}

