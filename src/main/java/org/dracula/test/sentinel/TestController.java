package org.dracula.test.sentinel;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.dracula.test.sentinel.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author dk
 */
@RestController
public class TestController {

    @GetMapping("/hello")
    public String getHello(@RequestParam(name = "name", defaultValue = "world") String name){
        return "hello "+name;
    }

    private boolean loopSwitch;

    @GetMapping("/switchLoop")
    public boolean switchLoop(){
        loopSwitch = !loopSwitch;
        synchronized (monitor) {
            monitor.notifyAll();
        }
        return loopSwitch;
    }

    @Autowired
    private TestService testService;

    private Object monitor = new Object();

    @PostConstruct
    public void loop(){
        startLoopThread();
        sendInitConfigToNacos();
    }

    public void startLoopThread(){
        new Thread(()->{
            while(true){
                synchronized (monitor) {
                    while(!loopSwitch){
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for(int i=0; i<20; i++){
                        try {
                            System.out.println(testService.test());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendInitConfigToNacos(){
        String remoteAddress = "localhost";
        String groupId = "DEFAULT_GROUP";
        String dataId = "test-sentinel";
        //放入配置
        final String rule = "[\n"
                + "  {\n"
                + "    \"resource\": \"HelloWorld\",\n"
                + "    \"controlBehavior\": 0,\n"   // 0. default(reject directly), 1. warm up, 2. rate limiter, 3. warm up + rate limiter
                + "    \"count\": 5.0,\n"
                + "    \"grade\": 1,\n" //0: thread count, 1: QPS
                + "    \"limitApp\": \"default\",\n"
                + "    \"strategy\": 0\n"
                + "  }\n"
                + "]";
        try {
            ConfigService configService = NacosFactory.createConfigService(remoteAddress);
            System.out.println(configService.publishConfig(dataId, groupId, rule));
        } catch (NacosException e) {
            e.printStackTrace();
        }
        //得到配置，并置流控规则生效
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

}
