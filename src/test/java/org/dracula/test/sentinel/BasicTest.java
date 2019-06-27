package org.dracula.test.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dk
 */
public class BasicTest {

    static{
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld");
        // set limit qps to 20
        rule.setCount(20);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    @Test
    public void test(){
        while(true){
            for(int i=0; i<100; i++){
                core();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void core(){
        try (Entry entry = SphU.entry("HelloWorld")) {
            // Your business logic here.
            System.out.println("hello world");
        } catch (BlockException e) {
            // Handle rejected request.
            e.printStackTrace();
        }
    }

}
