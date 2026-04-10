package fun.cyhgraph.dto.member;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMemberOpenDTO implements Serializable {

    /**
     * 套餐类型：1-9.9一个月普通会员；2-99终身高级会员（演示版）
     */
    private Integer planType;
}

