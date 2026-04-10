package fun.cyhgraph.vo.member;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MemberPageVO implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private String phone;
    private Integer isMember;
    private Integer memberLevel;
    private LocalDateTime memberExpireTime;
    /**
     * 会员状态：1-有效会员 0-非会员 2-已过期
     */
    private Integer memberStatus;
    private String memberStatusText;
}

