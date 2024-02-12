package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class WebfluxFunctionTestController {

    // defer를 쓴 경우에 출력 시간이 다르다.
    // Mono.defer()에서 defer 단어의 뜻으로는 보류하다라는 의미를 가지고 있다.
    // Mono를 구독할 때까지 실행하지 않고 실제 구독될 때 함수를 실행하게 한다.
    // 보통 Mono를 생성하는 함수를 지연시키고자 할 때 사용한다.
    // 구독될 때마다 함수를 재평가하기 때문에 발생하는 현상
    // Cold 스트림은 구독할 때마다 독립적으로 데이터를 다시 생성한다.
    @GetMapping("/withoutDefer")
    public void withoutDefer() {
        Mono<Long> mono = Mono.just(System.currentTimeMillis());
        mono.subscribe(time -> System.out.println("Without defer: " + time));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mono.subscribe(time -> System.out.println("Without defer after delay: " + time));
    }

    // Mono.just는 Hot 스트림이다.
    // 이건 구독하기도 전에 실행해서 이미 값이 들어와있음.
    // Hot 스트림은 데이터 생성과 구독이 독립적으로 이루어진다.
    @GetMapping("/withDefer")
    public void withDefer() {
        Mono<Long> mono = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        mono.subscribe(time -> System.out.println("With defer: " + time)); // 구독할 때 실행

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mono.subscribe(time -> System.out.println("With defer after delay: " + time));
    }
}
