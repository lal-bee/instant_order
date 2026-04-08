package fun.cyhgraph.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTableVO implements Serializable {
    private Long tableId;
    private Long storeId;
    private String storeName;
    private String tableNo;
}
