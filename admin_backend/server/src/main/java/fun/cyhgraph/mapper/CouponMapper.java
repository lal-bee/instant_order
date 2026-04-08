package fun.cyhgraph.mapper;

import com.github.pagehelper.Page;
import fun.cyhgraph.dto.coupon.CouponPageQueryDTO;
import fun.cyhgraph.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CouponMapper {

    void insert(Coupon coupon);

    void update(Coupon coupon);

    Coupon getById(@Param("id") Integer id);

    Page<Coupon> pageQuery(CouponPageQueryDTO queryDTO);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateUser") Integer updateUser);

    int increaseReceiveCount(@Param("couponId") Integer couponId);

    int increaseUsedCount(@Param("couponId") Integer couponId);

    List<Coupon> listReceiveList(@Param("storeId") Long storeId,
                                 @Param("isMember") Integer isMember,
                                 @Param("now") LocalDateTime now);
}
