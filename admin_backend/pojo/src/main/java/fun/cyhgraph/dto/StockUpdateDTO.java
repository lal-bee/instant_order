package fun.cyhgraph.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockUpdateDTO implements Serializable {
    private Long storeId;
    private Integer dishId;
    private Integer stock;
    private Integer warningStock;
}
