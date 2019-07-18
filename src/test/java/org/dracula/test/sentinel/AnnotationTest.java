package org.dracula.test.sentinel;

import org.dracula.test.sentinel.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author dk
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApp.class)
public class AnnotationTest {

    static{
        try {
            // 执行其静态代码块
            Class.forName("org.dracula.test.sentinel.BasicTest");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private TestService testService;

    @Test
    public void test(){
        while(true){
            for(int i=0; i<100; i++){
                System.out.println(testService.test());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
