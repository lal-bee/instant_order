package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreDishConfigVO implements Serializable {
    private Integer dishId;
    private String name;
    private String pic;
    private BigDecimal price;
    private String detail;
    private Integer categoryId;
    private String categoryName;
    private Integer onShelf;
}
