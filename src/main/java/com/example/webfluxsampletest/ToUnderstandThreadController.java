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
import java.util.stream.IntStream;

@RestController
public class ToUnderstandThreadController {

    private final Logger log = LoggerFactory.getLogger(ToUnderstandThreadController.class);

    // 동기식 코드는 I/O Thread를 빈곤하게 만든다.
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

    // 아래의 코드는 요청 시 7초가 소요된다.
    // I/O 작업은 I/O Thread(ioWorkerCount)가 하는 일이다.(우리가 흔히 아는 Netty Thread를 의미한다.)
    // 사용자가 요청을 동시에 2개 보내는 경우 동시처리는 된다.
    // Flux 안에 있는 행위는 단일 이벤트 루프 쓰레드가 처리하지만 결과가 끝나고나면 I/O 작업은 I/O Thread가 담당한다.
    // 그래서 Netty Thread를 1개만 유지하고 동시요청을 하면 두번째 요청이 단일 요청보다 2배인 15초가 걸린다.
    // 그래서 Thread 제한을 풀고 동시 요청을 해보면 두번째 요청도 7~8초로 유지되는 것을 볼 수 있다.
    // 이로서 알 수 있는건 I/O 작업이 많은 서비스는 Webflux와 어울리지 않는다.
    @GetMapping("/correct2/{id}")
    public Flux<Integer> useIteratorCorrectly2() {
        return Flux.range(0, 1_000_000);
    }
}
