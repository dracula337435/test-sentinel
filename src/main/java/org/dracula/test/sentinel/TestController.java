package org.dracula.test.sentinel;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
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
import java.util.LinkedList;
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

    private int count = 20;

    @GetMapping("/switchLoop")
    public boolean switchLoop(@RequestParam(name="count", defaultValue = "20") int count){
        loopSwitch = !loopSwitch;
        this.count = count;
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
                    for(int i=0; i<count; i++){
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
        //配置
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("HelloWorld");
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        flowRule.setCount(5);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setLimitApp("default");
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        //cluster
        flowRule.setClusterMode(true);
        ClusterFlowConfig clusterFlowConfig = new ClusterFlowConfig();
        clusterFlowConfig.setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL);
        flowRule.setClusterConfig(clusterFlowConfig);
        //list
        List<FlowRule> flowRuleList = new LinkedList<>();
        flowRuleList.add(flowRule);
        String rule = JSON.toJSONString(flowRuleList);
        //
        String remoteAddress = "localhost";
        String groupId = "DEFAULT_GROUP";
        String dataId = "test-sentinel";
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
        ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
    }

}
