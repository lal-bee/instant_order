package fun.cyhgraph.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fun.cyhgraph.constant.CouponConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.coupon.CouponPageQueryDTO;
import fun.cyhgraph.dto.coupon.CouponRecordQueryDTO;
import fun.cyhgraph.dto.coupon.CouponSaveDTO;
import fun.cyhgraph.entity.Coupon;
import fun.cyhgraph.entity.CouponOperateLog;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.mapper.CouponMapper;
import fun.cyhgraph.mapper.CouponOperateLogMapper;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.mapper.UserCouponMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.CouponService;
import fun.cyhgraph.utils.RoleUtil;
import fun.cyhgraph.vo.coupon.CouponVO;
import fun.cyhgraph.vo.coupon.CouponRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private CouponOperateLogMapper couponOperateLogMapper;

    @Override
    public void save(CouponSaveDTO dto) {
        Employee current = getCurrentEmployee();
        validateSaveDTO(dto, true, current);
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        LocalDateTime now = LocalDateTime.now();
        coupon.setReceiveCount(0);
        coupon.setUsedCount(0);
        coupon.setStatus(dto.getStatus() == null ? CouponConstant.COUPON_STATUS_ENABLED : dto.getStatus());
        coupon.setCreateUser(current.getId());
        coupon.setUpdateUser(current.getId());
        coupon.setCreateTime(now);
        coupon.setUpdateTime(now);
        couponMapper.insert(coupon);
        recordLog(coupon.getId(), current, "create", "创建优惠券");
    }

    @Override
    public void update(CouponSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BaseException("优惠券ID不能为空");
        }
        Employee current = getCurrentEmployee();
        Coupon old = couponMapper.getById(dto.getId());
        if (old == null) {
            throw new BaseException("优惠券不存在");
        }
        checkViewPermission(current, old);
        validateSaveDTO(dto, false, current);
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        coupon.setUpdateUser(current.getId());
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.update(coupon);
        recordLog(coupon.getId(), current, "update", "更新优惠券");
    }

    @Override
    public CouponVO getById(Integer id) {
        Coupon coupon = couponMapper.getById(id);
        if (coupon == null) {
            throw new BaseException("优惠券不存在");
        }
        checkViewPermission(getCurrentEmployee(), coupon);
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(coupon, vo);
        return vo;
    }

    @Override
    public PageResult pageQuery(CouponPageQueryDTO queryDTO) {
        Employee current = getCurrentEmployee();
        if (!RoleUtil.isChairman(current.getRole())) {
            queryDTO.setPublishType(CouponConstant.PUBLISH_TYPE_STORE);
            queryDTO.setStoreId(current.getStoreId());
        }
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Coupon> page = couponMapper.pageQuery(queryDTO);
        List<CouponVO> rows = page.getResult().stream().map(item -> {
            CouponVO vo = new CouponVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return new PageResult(page.getTotal(), rows);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null) {
            throw new BaseException("状态不能为空");
        }
        Coupon coupon = couponMapper.getById(id);
        if (coupon == null) {
            throw new BaseException("优惠券不存在");
        }
        Employee current = getCurrentEmployee();
        checkWritePermission(current, coupon);
        couponMapper.updateStatus(id, status, current.getId());
        recordLog(id, current, "status", "修改优惠券状态");
    }

    @Override
    public PageResult pageRecords(Integer couponId, CouponRecordQueryDTO queryDTO) {
        Coupon coupon = couponMapper.getById(couponId);
        if (coupon == null) {
            throw new BaseException("优惠券不存在");
        }
        checkViewPermission(getCurrentEmployee(), coupon);
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<CouponRecordVO> page = userCouponMapper.pageCouponRecords(couponId, queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    private void validateSaveDTO(CouponSaveDTO dto, boolean isCreate, Employee current) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BaseException("优惠券名称不能为空");
        }
        if (dto.getCouponType() == null) {
            throw new BaseException("优惠券类型不能为空");
        }
        if (dto.getPublishType() == null) {
            throw new BaseException("发布类型不能为空");
        }
        if (dto.getReceiveType() == null) {
            throw new BaseException("领取类型不能为空");
        }
        if (dto.getTotalCount() == null || dto.getTotalCount() <= 0) {
            throw new BaseException("总发行量必须大于0");
        }
        if (dto.getPerUserLimit() == null || dto.getPerUserLimit() <= 0) {
            throw new BaseException("每人限领数量必须大于0");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null || !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BaseException("生效时间范围不合法");
        }
        if (CouponConstant.COUPON_TYPE_FULL_REDUCTION.equals(dto.getCouponType())) {
            if (dto.getDiscountAmount() == null || dto.getDiscountAmount().doubleValue() <= 0) {
                throw new BaseException("满减券优惠金额必须大于0");
            }
            dto.setDiscountRate(null);
        }
        if (CouponConstant.COUPON_TYPE_DISCOUNT.equals(dto.getCouponType())) {
            if (dto.getDiscountRate() == null || dto.getDiscountRate().doubleValue() <= 0 || dto.getDiscountRate().doubleValue() >= 10) {
                throw new BaseException("折扣券折扣率必须在0到10之间");
            }
            dto.setDiscountAmount(null);
        }
        if (CouponConstant.PUBLISH_TYPE_STORE.equals(dto.getPublishType()) && dto.getStoreId() == null) {
            throw new BaseException("门店券必须指定所属门店");
        }

        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (RoleUtil.isManager(current.getRole())) {
            if (!CouponConstant.PUBLISH_TYPE_STORE.equals(dto.getPublishType())) {
                throw new BaseException("店长只能创建或维护门店券");
            }
            if (dto.getStoreId() == null || !dto.getStoreId().equals(current.getStoreId())) {
                throw new BaseException("店长只能操作自己门店优惠券");
            }
            return;
        }
        throw new BaseException("普通员工无权限操作优惠券");
    }

    private void checkViewPermission(Employee current, Coupon coupon) {
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (current.getStoreId() == null) {
            throw new BaseException("当前员工未绑定门店");
        }
        if (!CouponConstant.PUBLISH_TYPE_STORE.equals(coupon.getPublishType()) || !current.getStoreId().equals(coupon.getStoreId())) {
            throw new BaseException("无权限查看该优惠券");
        }
    }

    private void checkWritePermission(Employee current, Coupon coupon) {
        if (RoleUtil.isChairman(current.getRole())) {
            return;
        }
        if (RoleUtil.isManager(current.getRole())
                && CouponConstant.PUBLISH_TYPE_STORE.equals(coupon.getPublishType())
                && current.getStoreId() != null
                && current.getStoreId().equals(coupon.getStoreId())) {
            return;
        }
        throw new BaseException("无权限修改该优惠券");
    }

    private Employee getCurrentEmployee() {
        Integer id = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getById(id);
        if (employee == null) {
            throw new BaseException("当前登录员工不存在");
        }
        return employee;
    }

    private void recordLog(Integer couponId, Employee current, String type, String desc) {
        CouponOperateLog log = new CouponOperateLog();
        log.setCouponId(couponId);
        log.setOperateEmployeeId(current.getId());
        log.setOperateRole(current.getRole());
        log.setOperateType(type);
        log.setOperateDesc(desc);
        log.setCreateTime(LocalDateTime.now());
        couponOperateLogMapper.insert(log);
    }
}
