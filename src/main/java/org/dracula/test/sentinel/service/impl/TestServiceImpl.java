package org.dracula.test.sentinel.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.dracula.test.sentinel.service.TestService;
import org.springframework.stereotype.Component;

/**
 * @author dk
 */
@Component
public class TestServiceImpl implements TestService {

    @SentinelResource(value = "HelloWorld", blockHandler = "handleBlockException")
    @Override
    public String test() {
        return "hello world";
    }

    public void handleBlockException(Throwable throwable){
        throwable.printStackTrace();
    }
}
