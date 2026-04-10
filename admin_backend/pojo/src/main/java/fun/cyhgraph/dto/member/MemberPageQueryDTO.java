package fun.cyhgraph.dto.member;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MemberPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;
    private String phone;
    /**
     * 会员状态：1-有效会员 0-非会员 2-已过期
     */
    private Integer memberStatus;

    private LocalDateTime now;
}

