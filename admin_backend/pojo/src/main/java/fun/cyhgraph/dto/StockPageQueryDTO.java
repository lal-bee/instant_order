package fun.cyhgraph.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockPageQueryDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Long storeId;
    private String dishName;
    /**
     * 1 仅预警，0/空 全部
     */
    private Integer warningOnly;
}
