package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO implements Serializable {

    private Integer waitingOrders; // 待制作数量（字段名兼容历史）
    private Integer deliveredOrders; // 制作中数量（字段名兼容历史）
    private Integer completedOrders; // 已完成数量
    private Integer cancelledOrders; // 已取消数量
    private Integer allOrders; // 全部订单

}
