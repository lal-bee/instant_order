package fun.cyhgraph.controller.admin;

import fun.cyhgraph.dto.coupon.CouponPageQueryDTO;
import fun.cyhgraph.dto.coupon.CouponRecordQueryDTO;
import fun.cyhgraph.dto.coupon.CouponSaveDTO;
import fun.cyhgraph.dto.coupon.CouponStatusDTO;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.CouponService;
import fun.cyhgraph.vo.coupon.CouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    public Result<Void> save(@RequestBody CouponSaveDTO dto) {
        couponService.save(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CouponSaveDTO dto) {
        couponService.update(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(CouponPageQueryDTO queryDTO) {
        return Result.success(couponService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<CouponVO> getById(@PathVariable Integer id) {
        return Result.success(couponService.getById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id, @RequestBody CouponStatusDTO dto) {
        couponService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @GetMapping("/{id}/records")
    public Result<PageResult> records(@PathVariable Integer id, CouponRecordQueryDTO queryDTO) {
        return Result.success(couponService.pageRecords(id, queryDTO));
    }
}
