package fun.cyhgraph.service;

import fun.cyhgraph.dto.coupon.UserCouponAvailableQueryDTO;
import fun.cyhgraph.entity.Coupon;
import fun.cyhgraph.entity.Order;
import fun.cyhgraph.vo.coupon.CouponLockResultVO;
import fun.cyhgraph.vo.coupon.CouponVO;
import fun.cyhgraph.vo.coupon.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

public interface UserCouponService {

    List<CouponVO> listReceiveList(Long storeId);

    void receiveCoupon(Integer couponId);

    List<UserCouponVO> listMyCoupons(Integer statusType);

    List<UserCouponVO> listOrderAvailable(UserCouponAvailableQueryDTO queryDTO);

    CouponLockResultVO lockCouponForOrder(Integer userCouponId, Integer userId, Long storeId, BigDecimal amount);

    void bindOrder(Integer userCouponId, Integer userId, Integer orderId);

    void markCouponUsed(Order order);

    void releaseLockedCoupon(Order order);

    boolean isUserMember(Integer userId);

    boolean isCouponMatchStore(Coupon coupon, Long storeId);
}
