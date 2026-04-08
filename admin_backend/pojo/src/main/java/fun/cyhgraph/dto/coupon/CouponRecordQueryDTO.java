package fun.cyhgraph.dto.coupon;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponRecordQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer recordType;
    private Integer userCouponStatus;
    private String userName;
}
