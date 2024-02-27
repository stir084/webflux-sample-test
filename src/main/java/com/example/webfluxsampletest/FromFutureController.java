package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class FromFutureController {
    /**
     * CompletableFuture(완료될 수 있는 미래의 결과)는 비동기 작업을 생성한다.
     * return 된 mono는 비동기 작업을 처리한다.
     * Mono<String> mono = Mono.just("Hello, world!");
     * 위의 mono를 리턴하는 것은 단순히 비동기적인 작업이 아니라 String을 내보내는 것이다.
     *
     * webflux에서는 대부분 flatMap으로 처리할 수 있다.
     * But, 특정 작업이 비동기적으로 수행되어야 하지만, Reactor의 기능만으로는 충분하지 않은 경우: 예를 들어, 자체 스레드 풀을 사용하거나 복잡한 작업 처리를 위해 직접 CompletableFuture를 만들어야 할 때가 있다.
     */
    @GetMapping("/fromFuture")
    public Mono<String> test (){
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello, world!");
        Mono<String> mono = Mono.fromFuture(future);
        return mono;
    }
}
