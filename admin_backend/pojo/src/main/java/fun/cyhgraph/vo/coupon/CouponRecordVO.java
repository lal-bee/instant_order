package fun.cyhgraph.vo.coupon;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CouponRecordVO implements Serializable {

    private Integer userCouponId;
    private Integer couponId;
    private Integer userId;
    private String userName;
    private String username;
    private Integer status;
    private Integer orderId;
    private LocalDateTime receiveTime;
    private LocalDateTime lockTime;
    private LocalDateTime useTime;
    private LocalDateTime endTime;
}
