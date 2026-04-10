package fun.cyhgraph.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockPageVO implements Serializable {
    private Long storeId;
    private String storeName;
    private Integer dishId;
    private String dishName;
    private Integer dishStatus;
    private Integer onShelfStatus;
    private Integer stock;
    private Integer warningStock;
    private Integer warning;
}
