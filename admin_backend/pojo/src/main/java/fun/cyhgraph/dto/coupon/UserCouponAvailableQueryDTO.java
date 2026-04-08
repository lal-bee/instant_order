package fun.cyhgraph.dto.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserCouponAvailableQueryDTO implements Serializable {

    private Long storeId;
    private BigDecimal amount;
}
