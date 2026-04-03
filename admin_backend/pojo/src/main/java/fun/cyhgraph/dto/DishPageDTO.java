package fun.cyhgraph.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishPageDTO implements Serializable {

    private int page;
    private int pageSize;
    private Long headquartersId;
    private String name;
    private Integer categoryId;
    private Integer status;

}
