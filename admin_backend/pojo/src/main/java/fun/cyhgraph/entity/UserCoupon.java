package fun.cyhgraph.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCoupon implements Serializable {

    private Integer id;
    private Integer userId;
    private Integer couponId;
    private Integer status;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime receiveTime;
    private LocalDateTime lockTime;
    private LocalDateTime useTime;
    private Integer orderId;
    private Integer createUser;
    private Integer updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String couponName;
    private Integer couponType;
    private Integer publishType;
    private Long storeId;
    private Integer receiveType;
    private Integer couponStatus;
    private String storeName;
}
