package fun.cyhgraph.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMemberInfoVO implements Serializable {

    /**
     * 是否为有效会员：1-是 0-否
     */
    private Integer isMember;
    private Integer memberLevel;
    private LocalDateTime memberExpireTime;
    private String memberStatusText;
}

