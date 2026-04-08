package fun.cyhgraph.service;

import fun.cyhgraph.dto.coupon.CouponPageQueryDTO;
import fun.cyhgraph.dto.coupon.CouponRecordQueryDTO;
import fun.cyhgraph.dto.coupon.CouponSaveDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.vo.coupon.CouponVO;

public interface CouponService {

    void save(CouponSaveDTO dto);

    void update(CouponSaveDTO dto);

    CouponVO getById(Integer id);

    PageResult pageQuery(CouponPageQueryDTO queryDTO);

    void updateStatus(Integer id, Integer status);

    PageResult pageRecords(Integer couponId, CouponRecordQueryDTO queryDTO);
}
