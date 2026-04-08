package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private String openid;
    private String phone;
    private Integer gender;
    private String idNumber;
    private String pic;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
