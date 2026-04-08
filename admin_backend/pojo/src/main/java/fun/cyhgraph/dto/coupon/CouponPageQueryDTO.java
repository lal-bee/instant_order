package fun.cyhgraph.dto.coupon;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String name;
    private Integer couponType;
    private Integer status;
    private Integer publishType;
    private Long storeId;
}
