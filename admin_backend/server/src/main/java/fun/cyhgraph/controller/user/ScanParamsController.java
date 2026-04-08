package fun.cyhgraph.controller.user;

import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 扫码点餐：将门店号、桌号存入 Redis，供后续下单使用
 */
@RestController("userScanParamsController")
@RequestMapping("/user/scan-params")
@Slf4j
public class ScanParamsController {

    private static final String KEY_PREFIX = "scan_params:user:";
    private static final long TTL_HOURS = 24;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 保存扫码参数到 Redis（需登录）
     * 正式发布后：用户扫码进入时前端调用此接口，将 storeId、tableId 写入 Redis
     */
    @PostMapping
    public Result<Void> save(@RequestParam Long storeId, @RequestParam Long tableId) {
        Integer userId = BaseContext.getCurrentId();
        String key = KEY_PREFIX + userId;
        String value = storeId + ":" + tableId;
        redisTemplate.opsForValue().set(key, value, TTL_HOURS, TimeUnit.HOURS);
        log.info("扫码参数已存入 Redis，userId={}, storeId={}, tableId={}", userId, storeId, tableId);
        return Result.success();
    }
}
