package fun.cyhgraph.mapper;

import com.github.pagehelper.Page;
import fun.cyhgraph.dto.coupon.CouponRecordQueryDTO;
import fun.cyhgraph.entity.UserCoupon;
import fun.cyhgraph.vo.coupon.CouponRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserCouponMapper {

    void insert(UserCoupon userCoupon);

    UserCoupon getById(@Param("id") Integer id);

    int countUserReceive(@Param("userId") Integer userId, @Param("couponId") Integer couponId);

    int lockCouponForOrder(@Param("userCouponId") Integer userCouponId,
                           @Param("userId") Integer userId,
                           @Param("lockTime") LocalDateTime lockTime,
                           @Param("orderId") Integer orderId);

    int bindOrder(@Param("userCouponId") Integer userCouponId,
                  @Param("userId") Integer userId,
                  @Param("orderId") Integer orderId);

    int unlockCouponByOrderId(@Param("orderId") Integer orderId,
                              @Param("updateTime") LocalDateTime updateTime);

    int unlockCouponByUserCouponId(@Param("userCouponId") Integer userCouponId,
                                   @Param("updateTime") LocalDateTime updateTime);

    int markCouponUsedByUserCouponId(@Param("userCouponId") Integer userCouponId,
                                     @Param("useTime") LocalDateTime useTime,
                                     @Param("updateTime") LocalDateTime updateTime);

    int batchMarkExpired(@Param("userId") Integer userId,
                         @Param("now") LocalDateTime now,
                         @Param("updateTime") LocalDateTime updateTime);

    List<UserCoupon> listMyCoupons(@Param("userId") Integer userId,
                                   @Param("status") Integer status,
                                   @Param("now") LocalDateTime now);

    List<UserCoupon> listOrderAvailable(@Param("userId") Integer userId,
                                        @Param("storeId") Long storeId,
                                        @Param("amount") BigDecimal amount,
                                        @Param("isMember") Integer isMember,
                                        @Param("now") LocalDateTime now);

    Page<CouponRecordVO> pageCouponRecords(@Param("couponId") Integer couponId,
                                           @Param("query") CouponRecordQueryDTO query);
}
