package fun.cyhgraph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSpecialDishDTO implements Serializable {
    private Long storeId;
    private String name;
    private String pic;
    private String detail;
    private BigDecimal price;
    private Integer categoryId;
}
