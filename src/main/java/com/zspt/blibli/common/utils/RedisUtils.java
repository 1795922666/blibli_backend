package com.zspt.blibli.common.utils;
import cn.hutool.core.util.BooleanUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
@Component
public class RedisUtils {
    @Resource
   private  StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key){
        Boolean is = stringRedisTemplate.opsForValue().setIfAbsent(key, "", 10L, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(is);
    }

    public void unlock(Long id){
        stringRedisTemplate.delete("lock:shop:" + id);
    }

}
