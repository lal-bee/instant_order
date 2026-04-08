package fun.cyhgraph.constant;

public final class CouponConstant {

    private CouponConstant() {
    }

    public static final Integer COUPON_STATUS_DISABLED = 0;
    public static final Integer COUPON_STATUS_ENABLED = 1;

    public static final Integer COUPON_TYPE_FULL_REDUCTION = 1;
    public static final Integer COUPON_TYPE_DISCOUNT = 2;

    public static final Integer PUBLISH_TYPE_GLOBAL = 1;
    public static final Integer PUBLISH_TYPE_STORE = 2;

    public static final Integer RECEIVE_TYPE_ALL = 1;
    public static final Integer RECEIVE_TYPE_MEMBER = 2;

    public static final Integer USER_COUPON_STATUS_UNUSED = 1;
    public static final Integer USER_COUPON_STATUS_LOCKED = 2;
    public static final Integer USER_COUPON_STATUS_USED = 3;
    public static final Integer USER_COUPON_STATUS_EXPIRED = 4;

    public static final Integer RECORD_TYPE_RECEIVE = 1;
    public static final Integer RECORD_TYPE_USED = 2;
}
