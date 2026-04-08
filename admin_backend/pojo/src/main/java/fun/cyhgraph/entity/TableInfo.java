package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo implements Serializable {

    private Long id;
    private Long storeId;
    private String tableNo;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
