package fun.cyhgraph.dto.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponSaveDTO implements Serializable {

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
    private Integer perUserLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String remark;
}
