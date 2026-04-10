package fun.cyhgraph.service.serviceImpl;

import fun.cyhgraph.constant.CouponConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.coupon.UserCouponAvailableQueryDTO;
import fun.cyhgraph.entity.Coupon;
import fun.cyhgraph.entity.Order;
import fun.cyhgraph.entity.UserCoupon;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.CouponMapper;
import fun.cyhgraph.mapper.UserCouponMapper;
import fun.cyhgraph.service.MemberService;
import fun.cyhgraph.service.UserCouponService;
import fun.cyhgraph.vo.coupon.CouponLockResultVO;
import fun.cyhgraph.vo.coupon.CouponVO;
import fun.cyhgraph.vo.coupon.UserCouponVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCouponServiceImpl implements UserCouponService {

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private MemberService memberService;

    @Override
    public List<CouponVO> listReceiveList(Long storeId) {
        if (storeId == null) {
            throw new BaseException("门店ID不能为空");
        }
        Integer userId = BaseContext.getCurrentId();
        Integer isMember = isUserMember(userId) ? 1 : 0;
        List<Coupon> list = couponMapper.listReceiveList(storeId, isMember, LocalDateTime.now());
        return list.stream().map(item -> {
            CouponVO vo = new CouponVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void receiveCoupon(Integer couponId) {
        Integer userId = BaseContext.getCurrentId();
        Coupon coupon = couponMapper.getById(couponId);
        if (coupon == null) {
            throw new BaseException("优惠券不存在");
        }
        validateCouponReceivable(coupon, userId);
        int userReceiveCount = userCouponMapper.countUserReceive(userId, couponId);
        if (userReceiveCount >= coupon.getPerUserLimit()) {
            throw new BaseException("已达到每人限领数量");
        }

        int changed = couponMapper.increaseReceiveCount(couponId);
        if (changed <= 0) {
            throw new BaseException("优惠券库存不足");
        }

        LocalDateTime now = LocalDateTime.now();
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setStatus(CouponConstant.USER_COUPON_STATUS_UNUSED);
        userCoupon.setThresholdAmount(coupon.getThresholdAmount());
        userCoupon.setDiscountAmount(coupon.getDiscountAmount());
        userCoupon.setDiscountRate(coupon.getDiscountRate());
        userCoupon.setStartTime(coupon.getStartTime());
        userCoupon.setEndTime(coupon.getEndTime());
        userCoupon.setReceiveTime(now);
        userCoupon.setCreateUser(userId);
        userCoupon.setUpdateUser(userId);
        userCoupon.setCreateTime(now);
        userCoupon.setUpdateTime(now);
        userCouponMapper.insert(userCoupon);
    }

    @Override
    public List<UserCouponVO> listMyCoupons(Integer statusType) {
        Integer userId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        userCouponMapper.batchMarkExpired(userId, now, now);

        Integer status = null;
        if (statusType != null) {
            if (statusType == 1) {
                status = CouponConstant.USER_COUPON_STATUS_UNUSED;
            } else if (statusType == 2) {
                status = CouponConstant.USER_COUPON_STATUS_USED;
            } else if (statusType == 3) {
                status = CouponConstant.USER_COUPON_STATUS_EXPIRED;
            }
        }
        List<UserCoupon> list = userCouponMapper.listMyCoupons(userId, status, now);
        return toUserCouponVOList(list);
    }

    @Override
    public List<UserCouponVO> listOrderAvailable(UserCouponAvailableQueryDTO queryDTO) {
        if (queryDTO.getStoreId() == null) {
            throw new BaseException("门店ID不能为空");
        }
        if (queryDTO.getAmount() == null) {
            throw new BaseException("订单金额不能为空");
        }
        Integer userId = BaseContext.getCurrentId();
        Integer isMember = isUserMember(userId) ? 1 : 0;
        List<UserCoupon> list = userCouponMapper.listOrderAvailable(userId, queryDTO.getStoreId(), queryDTO.getAmount(), isMember, LocalDateTime.now());
        return toUserCouponVOList(list);
    }

    @Override
    @Transactional
    public CouponLockResultVO lockCouponForOrder(Integer userCouponId, Integer userId, Long storeId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException("订单金额不合法");
        }
        if (userCouponId == null) {
            return new CouponLockResultVO(null, null, amount, BigDecimal.ZERO, amount);
        }
        LocalDateTime now = LocalDateTime.now();
        userCouponMapper.batchMarkExpired(userId, now, now);

        UserCoupon userCoupon = userCouponMapper.getById(userCouponId);
        if (userCoupon == null || !userId.equals(userCoupon.getUserId())) {
            throw new BaseException("优惠券不存在或不属于当前用户");
        }
        if (!CouponConstant.USER_COUPON_STATUS_UNUSED.equals(userCoupon.getStatus())) {
            throw new BaseException("优惠券状态不可用");
        }
        if (userCoupon.getStartTime().isAfter(now) || userCoupon.getEndTime().isBefore(now)) {
            throw new BaseException("优惠券不在有效期内");
        }
        Coupon coupon = couponMapper.getById(userCoupon.getCouponId());
        if (coupon == null || !CouponConstant.COUPON_STATUS_ENABLED.equals(coupon.getStatus())) {
            throw new BaseException("优惠券模板不可用");
        }
        if (!isCouponMatchStore(coupon, storeId)) {
            throw new BaseException("优惠券与当前门店不匹配");
        }
        if (coupon.getReceiveType() != null && coupon.getReceiveType().equals(CouponConstant.RECEIVE_TYPE_MEMBER) && !isUserMember(userId)) {
            throw new BaseException("该优惠券仅会员可用");
        }
        if (userCoupon.getThresholdAmount() != null && amount.compareTo(userCoupon.getThresholdAmount()) < 0) {
            throw new BaseException("订单金额未达到优惠券使用门槛");
        }

        BigDecimal couponAmount = calculateDiscountAmount(userCoupon, amount);
        BigDecimal payAmount = amount.subtract(couponAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        int changed = userCouponMapper.lockCouponForOrder(userCouponId, userId, now, null);
        if (changed <= 0) {
            throw new BaseException("优惠券已被使用，请重新选择");
        }
        return new CouponLockResultVO(coupon.getId(), userCouponId, amount, couponAmount, payAmount);
    }

    @Override
    @Transactional
    public void bindOrder(Integer userCouponId, Integer userId, Integer orderId) {
        if (userCouponId == null || userId == null || orderId == null) {
            return;
        }
        userCouponMapper.bindOrder(userCouponId, userId, orderId);
    }

    @Override
    @Transactional
    public void markCouponUsed(Order order) {
        if (order == null || order.getId() == null || order.getUserCouponId() == null) {
            return;
        }
        UserCoupon userCoupon = userCouponMapper.getById(order.getUserCouponId());
        if (userCoupon == null) {
            return;
        }
        int changed = userCouponMapper.markCouponUsedByUserCouponId(order.getUserCouponId(), LocalDateTime.now(), LocalDateTime.now());
        if (changed > 0 && userCoupon.getCouponId() != null) {
            couponMapper.increaseUsedCount(userCoupon.getCouponId());
        }
    }

    @Override
    @Transactional
    public void releaseLockedCoupon(Order order) {
        if (order == null) {
            return;
        }
        if (order.getUserCouponId() != null) {
            userCouponMapper.unlockCouponByUserCouponId(order.getUserCouponId(), LocalDateTime.now());
            return;
        }
        if (order.getId() != null) {
            userCouponMapper.unlockCouponByOrderId(order.getId(), LocalDateTime.now());
        }
    }

    @Override
    public boolean isUserMember(Integer userId) {
        return memberService.isValidMember(userId);
    }

    @Override
    public boolean isCouponMatchStore(Coupon coupon, Long storeId) {
        if (coupon == null || storeId == null) {
            return false;
        }
        if (CouponConstant.PUBLISH_TYPE_GLOBAL.equals(coupon.getPublishType())) {
            return true;
        }
        return CouponConstant.PUBLISH_TYPE_STORE.equals(coupon.getPublishType()) && storeId.equals(coupon.getStoreId());
    }

    private void validateCouponReceivable(Coupon coupon, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        if (!CouponConstant.COUPON_STATUS_ENABLED.equals(coupon.getStatus())) {
            throw new BaseException("优惠券已停用");
        }
        if (coupon.getStartTime().isAfter(now) || coupon.getEndTime().isBefore(now)) {
            throw new BaseException("优惠券不在领取时间内");
        }
        if (coupon.getReceiveCount() != null && coupon.getTotalCount() != null && coupon.getReceiveCount() >= coupon.getTotalCount()) {
            throw new BaseException("优惠券已领完");
        }
        if (CouponConstant.RECEIVE_TYPE_MEMBER.equals(coupon.getReceiveType()) && !isUserMember(userId)) {
            throw new BaseException("该优惠券仅会员可领取");
        }
    }

    private BigDecimal calculateDiscountAmount(UserCoupon userCoupon, BigDecimal amount) {
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (CouponConstant.COUPON_TYPE_FULL_REDUCTION.equals(userCoupon.getCouponType())) {
            couponAmount = userCoupon.getDiscountAmount() == null ? BigDecimal.ZERO : userCoupon.getDiscountAmount();
        } else if (CouponConstant.COUPON_TYPE_DISCOUNT.equals(userCoupon.getCouponType())) {
            BigDecimal rate = userCoupon.getDiscountRate() == null ? BigDecimal.TEN : userCoupon.getDiscountRate();
            couponAmount = amount.multiply(BigDecimal.TEN.subtract(rate)).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        }
        if (couponAmount.compareTo(amount) > 0) {
            couponAmount = amount;
        }
        if (couponAmount.compareTo(BigDecimal.ZERO) < 0) {
            couponAmount = BigDecimal.ZERO;
        }
        return couponAmount;
    }

    private List<UserCouponVO> toUserCouponVOList(List<UserCoupon> list) {
        List<UserCouponVO> result = new ArrayList<>();
        for (UserCoupon item : list) {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(item, vo);
            vo.setCouponId(item.getCouponId());
            result.add(vo);
        }
        return result;
    }
}
