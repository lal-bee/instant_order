package fun.cyhgraph.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderConfirmDTO implements Serializable {

    private Integer id;
    private Integer status; // 订单状态 1待付款 2已支付(待制作) 3制作中 4待取餐 5已完成 6已取消 7退款

}
