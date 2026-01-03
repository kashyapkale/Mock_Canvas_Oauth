package org.oauth.fake_oauth_canvas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.oauth.fake_oauth_canvas")
public class FakeOauthCanvasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakeOauthCanvasApplication.class, args);
    }

}
