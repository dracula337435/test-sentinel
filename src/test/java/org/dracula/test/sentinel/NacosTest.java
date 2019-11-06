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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author dk
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApp.class)
public class NacosTest {

    @Autowired
    private TestService testService;

    static {
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

    @Test
    public void test(){
        while(true){
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

    /**
     * 这个写法会出问题
     * 一来如果sleep(...)使用200以下的，失败的会有极多
     */
//    @Test
    public void testAccident(){
        while(true){
            try {
                System.out.println(testService.test());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
