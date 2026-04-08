package fun.cyhgraph.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouponOperateLog implements Serializable {

    private Integer id;
    private Integer couponId;
    private Integer operateEmployeeId;
    private String operateRole;
    private String operateType;
    private String operateDesc;
    private LocalDateTime createTime;
}
