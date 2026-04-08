package fun.cyhgraph.vo;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class OrderStatisticsVO implements Serializable {

    private Integer toBeConfirmed; // 待制作数量（字段名兼容历史）
    private Integer confirmed; // 制作中数量（字段名兼容历史）
    private Integer deliveryInProgress; // 待取餐数量（字段名兼容历史）

}
