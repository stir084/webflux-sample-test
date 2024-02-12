package com.example.webfluxsampletest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RestController
public class ToUnderstandThreadController {

    private final Logger log = LoggerFactory.getLogger(ToUnderstandThreadController.class);

    // 동기식 코드는 쓰레드를 빈곤하게 만든다.
    // 반복문 자체는 동기식 작업이며 반복문 자체가 비동기 처리에는 어울리지 않는다.
    @GetMapping("/impoverish/{id}")
    public Mono<String> impoverishThread(@PathVariable String id) {
        long start = System.currentTimeMillis();


        IntStream.range(0, 1_000_000_000).forEach(it -> {
            if (it % 10000 == 0) {
                log.info("Request [" + id + "] for: " + it);
            }
        });

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);
        return Mono.just( format + "초 소요되었습니다.");
    }
    // 동기식 코드가 필요하다면 새로운 스레드를 생성하여 수행한다.
    @GetMapping("/correct/{id}")
    public Mono<String> useIteratorCorrectly(@PathVariable String id) {
        return Mono.fromCallable(() -> {
            long start = System.currentTimeMillis();

            IntStream.range(0, 1_000_000_000).forEach(it -> {
                if (it % 10000 == 0) {
                    log.info("Request [" + id + "] for: " + it);
                }
            });

            long end = System.currentTimeMillis();

            NumberFormat formatter = new DecimalFormat("#0.00000");
            String format = formatter.format((end - start) / 1000d);
            return format + "초 소요되었습니다.";
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // I/O 작업도 Thread가 하는 일이지만 요청을 2개 보내는 경우 동시처리가 되는데, 이 이유는 Flux의 Subscribe는 Event Loop의 Thread가 처리하기 때문이다.
    // 이 코드에서 flux를 리턴하는 것은 netty thread지만 사용자에게 0~1000000까지의 숫자를 보여주게 작업하는 것은 Event Loop의 Thread다.
    @GetMapping("/correct2/{id}")
    public Flux<Integer> useIteratorCorrectly2() {
        return Flux.range(0, 1_000_000);
    }
}
