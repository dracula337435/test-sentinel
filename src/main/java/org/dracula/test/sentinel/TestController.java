package org.dracula.test.sentinel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dk
 */
@RestController
public class TestController {

    @GetMapping("/hello")
    public String getHello(@RequestParam(name = "name", defaultValue = "world") String name){
        return "hello "+name;
    }

}
