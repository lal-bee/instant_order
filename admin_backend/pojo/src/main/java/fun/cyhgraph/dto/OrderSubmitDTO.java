package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderSubmitDTO implements Serializable {

    private Long storeId;    // 堂食：门店 id
    private Long tableId;  // 堂食：餐桌id
    private String tableNo; // 堂食：桌号
    private int payMethod; // 付款方式
    private String remark; // 备注
    private Integer tablewareNumber; // 餐具数量
    private Integer tablewareStatus; // 餐具数量状态  1按餐量提供  0选择具体数量
    private BigDecimal amount; // 总金额
    private Integer userCouponId; // 用户券id（可选）
}
