package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Store implements Serializable {

    private Long id;
    private Long headquartersId;
    private String headquartersName;
    private String name;
    private Integer managerEmployeeId;
    private String managerName;
    private Integer status;
    private Integer createUser;
    private Integer updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
