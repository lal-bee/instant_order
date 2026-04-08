package fun.cyhgraph.vo.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponVO implements Serializable {

    private Integer id;
    private Integer couponId;
    private String couponName;
    private Integer couponType;
    private Integer publishType;
    private Long storeId;
    private String storeName;
    private Integer receiveType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime receiveTime;
    private LocalDateTime lockTime;
    private LocalDateTime useTime;
    private Integer orderId;
    private Integer status;
}
