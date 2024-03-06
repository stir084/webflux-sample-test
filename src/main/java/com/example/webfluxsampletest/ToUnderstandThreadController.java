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

    /**
     * https://techblog.woowahan.com/15398/#toc-9
     * Spring의 Reactive 프로그래밍 모델인 WebFlux는 Netty의 event loop 기반으로 동작합니다.
     * Event loop가 중심에서 모든 요청을 처리하고, 요청 처리 구간을 callback으로 등록해놓고 worker 스레드 풀이 작업들을 처리하는 형태입니다.
     * Worker 스레드가 작업을 처리하는 과정에서 I/O를 마주치게 되면 작업이 park 되면서 컨텍스트 스위칭이 발생합니다.
     * 현재 실행 중인 스레드가 I/O 작업을 수행하기 위해 일시적으로 중단되고, 다른 스레드가 CPU 자원을 할당받아 실행될 수 있도록 컨텍스트가 변경된다는 것을 의미합니다.
     * I/O 작업을 시작한 후 즉시 다른 작업을 수행할 수 있습니다. 컨텍스트 스위칭 비용 발생
     * 마지막으로 컨텍스트 스위칭시 실제 스레드를 switch 하기 때문에, 경량스레드 switch에 비해 성능 낭비가 존재하고, 스레드의 컨텍스트를 상실하기 때문에 스택 트레이스가 유실된다는 단점도 존재합니다. 이는 디버그를 어렵게 만들 수 있습니다.

     ioWorkerCount를 1로 유지하고
    요청을 하나만 날리면 7초가 걸린다.
    하지만 동시에 요청을 2개 날리면 두번째 요청은 14초가 걸린다.
    그렇다고해서 두번째 요청이 첫번째 요청이 끝날때까지 대기하는 것은 아니고 동시처리는 하지만 14초가 걸린다.
    ioWorkerCount를 2로 유지하면 두번째 요청도 7초가 걸린다.
    확실한건 그럼 네티 쓰레드가 일한다는 뜻이다.
    네티 쓰레드가 하나여도 동시 처리는 한다.
    */
    @GetMapping("/correct2/{id}")
    public Flux<Integer> useIteratorCorrectly2() {
        return Flux.range(0, 1_000_000);
    }

    /**
     * Flux.range는 For문 형태로 작동하기 때문에 CPU 작업이 거기서 많아지는 것이지.
     * String 자체를 Client로 내보내는 것이 느린 것은 아니다.
     * 결과적으로 I/O를 내뱉는 과정은 느리지 않다.
     * 다만 아래를 실행하면서 /test를 요청해도 결과가 보인다.
     * 즉 쓰기 I/O작업 도중에도
     */
    @GetMapping("/correct3/{id}")
    public Mono<String> useIteratorCorrectly3() {
        return Mono.just(WebfluxSampleTestApplication.number);
    }
}
