package fun.cyhgraph.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Coupon implements Serializable {

    private Integer id;
    private String name;
    private Integer couponType;
    private Integer publishType;
    private Long storeId;
    private Integer receiveType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer totalCount;
    private Integer receiveCount;
    private Integer usedCount;
    private Integer perUserLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String remark;
    private Integer createUser;
    private Integer updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String storeName;
}
