package fun.cyhgraph.mapper;

import fun.cyhgraph.entity.CouponOperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponOperateLogMapper {

    @Insert("insert into coupon_operate_log(coupon_id, operate_employee_id, operate_role, operate_type, operate_desc, create_time) " +
            "values(#{couponId}, #{operateEmployeeId}, #{operateRole}, #{operateType}, #{operateDesc}, #{createTime})")
    void insert(CouponOperateLog log);
}
