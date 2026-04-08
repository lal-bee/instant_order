package fun.cyhgraph.controller.user;

import fun.cyhgraph.dto.coupon.UserCouponAvailableQueryDTO;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.UserCouponService;
import fun.cyhgraph.vo.coupon.CouponVO;
import fun.cyhgraph.vo.coupon.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCouponController")
@RequestMapping("/user/coupon")
public class CouponController {

    @Autowired
    private UserCouponService userCouponService;

    @GetMapping("/receive-list")
    public Result<List<CouponVO>> receiveList(@RequestParam Long storeId) {
        return Result.success(userCouponService.listReceiveList(storeId));
    }

    @PostMapping("/{couponId}/receive")
    public Result<Void> receive(@PathVariable Integer couponId) {
        userCouponService.receiveCoupon(couponId);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<UserCouponVO>> myCoupons(@RequestParam(required = false) Integer statusType) {
        return Result.success(userCouponService.listMyCoupons(statusType));
    }

    @GetMapping("/order-available")
    public Result<List<UserCouponVO>> orderAvailable(UserCouponAvailableQueryDTO queryDTO) {
        return Result.success(userCouponService.listOrderAvailable(queryDTO));
    }
}
