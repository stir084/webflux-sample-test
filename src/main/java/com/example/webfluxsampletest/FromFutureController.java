package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class FromFutureController {
    /**
     * CompletableFuture는 비동기 작업을 생성한다.
     * return 된 mono는 비동기 작업을 처리한다.
     * Mono<String> mono = Mono.just("Hello, world!");
     * 위의 mono를 리턴하는 것은 단순히 비동기적인 작업이 아니라 String을 내보내는 것이다.
     */
    @GetMapping("/fromFuture")
    public Mono<String> test (){
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello, world!");
        Mono<String> mono = Mono.fromFuture(future);
        return mono;
    }
}
