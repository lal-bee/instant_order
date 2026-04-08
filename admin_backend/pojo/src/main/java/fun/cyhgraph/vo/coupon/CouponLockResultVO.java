package fun.cyhgraph.vo.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponLockResultVO {

    private Integer couponId;
    private Integer userCouponId;
    private BigDecimal originAmount;
    private BigDecimal couponAmount;
    private BigDecimal payAmount;
}
