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

    /*
    ioWorkerCount를 1로 유지하고
    요청을 하나만 날리면 7초가 걸린다.
    하지만 동시에 요청을 2개 날리면 두번째 요청은 14초가 걸린다.
    그렇다고해서 두번째 요청이 첫번째 요청이 끝날때까지 대기하는 것은 아니고 동시처리는 하지만 14초가 걸린다.
    ioWorkerCount를 2로 유지하면 두번째 요청도 7초가 걸린다.
    확실한건 그럼 네티 쓰레드가 일한다는 뜻이다.
    네티 쓰레드가 하나여도 동시 처리는 한다.
    */
    // 이로서 알 수 있는건 I/O 작업이 많은 서비스는 Webflux와 어울리지 않는다.
    @GetMapping("/correct2/{id}")
    public Flux<Integer> useIteratorCorrectly2() {
        return Flux.range(0, 1_000_000);
    }
}
