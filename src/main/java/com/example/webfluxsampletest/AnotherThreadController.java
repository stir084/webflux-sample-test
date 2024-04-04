package com.example.webfluxsampletest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@Slf4j
public class AnotherThreadController {

    /**
     * publishOn에서 boundedElastic을 쓰는 것은 다운 스트림으로 시그널을 전송할 떄 실행되는 스레드를 제어하는 역할을 하는 오퍼레이터다.
     * 그래서 filter를 포함해서 하단은 전부 메인스레드가 아닌 다른 스레드로 처리함.
     */

    /**
     * flatMap은 앞에서 전달받은 아이템을 다른 형태로 변환하여 방출하는 반면, doOnNext는 로깅, api 콜과 같은 부가적인 동작을 하되, 전달받은 아이템을 변환하여 넘기는 동작은 하지 않는다는 점이다. 즉, source에 대한 transforming은 하지 않는다.
     */
    @GetMapping("/another")
    public void another() throws InterruptedException {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    // 이렇게하면 두번째 publishOn은 다른 스레드가 처리한다.

    @GetMapping("/another2")
    public void another2() throws InterruptedException {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.parallel())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);

        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.parallel())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * subscribeOn을 하면 그 위에것부터 다른 스레드로 처리한다.
     */
    @GetMapping("/another3")
    public void another3() throws InterruptedException {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.parallel())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

}
