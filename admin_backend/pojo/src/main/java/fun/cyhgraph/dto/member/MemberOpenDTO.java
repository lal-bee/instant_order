package fun.cyhgraph.dto.member;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MemberOpenDTO implements Serializable {

    private Integer userId;
    private Integer memberLevel;
    private LocalDateTime memberExpireTime;
}

