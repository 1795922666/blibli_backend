package com.zspt.blibli;

import com.zspt.blibli.account.mapper.UserMapper;
import org.junit.jupiter.api.Test;

class BliBliApplicationTests {
    UserMapper userMapper;
    @Test
    void contextLoads() {
        String str=String.format("Hello %s", "CSDN");
        System.out.println(str);
    }

}
