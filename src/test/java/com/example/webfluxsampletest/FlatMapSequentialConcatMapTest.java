package com.example.webfluxsampletest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
@Slf4j
public class FlatMapSequentialConcatMapTest {
    /**
     * The flatMap and flatMapSequential operators subscribe eagerly,
     * the concatMap waits for each inner completion before generating the next sub-stream and subscribing to it.
     *
     * 쉽게 말해 flatMap이랑 flatMapSequential은 Hot이고(Eager)
     * concatMap은 Cold다(Lazy)
     */
    @Test
    void test_flatMap() {
        Flux.just(1, 2, 3)
                //.flatMap(this::doSomethingAsync)
               // .flatMapSequential(this::doSomethingAsync)
                .concatMap(this::doSomethingAsync)
                .doOnNext(n -> log.info("Done {}", n))
                .blockLast();
    }

    private Mono<Integer> doSomethingAsync(Integer number) {
        //add some delay for the second item...
        return number == 2 ? Mono.just(number).doOnNext(n -> log.info("Executing {}", n)).delayElement(Duration.ofSeconds(1))
                : Mono.just(number).doOnNext(n -> log.info("Executing {}", n));
    }

    /**
     * 22:54:46.194 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 1
     * 22:54:46.197 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 1
     * 22:54:46.204 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 2
     * 22:54:46.205 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 3
     * 22:54:46.205 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 3
     * 22:54:47.216 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 2
     *
     * flatMap은 죄다 구독하고 한방에 처리한다.
     * 순서를 유지 하지 않는다. 3이 2보다 먼저 찍힘
     *
     *
     * 00:23:38.449 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 1
     * 00:23:38.453 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 1
     * 00:23:38.460 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 2
     * 00:23:38.462 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 3
     * 00:23:39.477 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 2
     * 00:23:39.477 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 3
     *
     * flatMapSequential도 죄다 구독하고 한방에 처리하지만 순서지킴.
     * flatMap순서가 맞지 않게 수신된 요소를 대기열에 추가하여 순서를 유지.
     *
     *
     * 00:25:50.555 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 1
     * 00:25:50.558 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 1
     * 00:25:50.565 [Test worker] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 2
     * 00:25:51.577 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 2
     * 00:25:51.577 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Executing 3
     * 00:25:51.577 [parallel-1] INFO com.example.webfluxsampletest.FlatMapSequentialConcatMapTest -- Done 3
     *
     * concatMap은 무조건 하나가 끝나야 다음을 실행한다.
     */

}
