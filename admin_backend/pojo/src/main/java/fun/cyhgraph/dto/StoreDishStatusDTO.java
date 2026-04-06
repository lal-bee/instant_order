package fun.cyhgraph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreDishStatusDTO implements Serializable {
    private Long storeId;
    private Integer dishId;
    private Integer status;
}
