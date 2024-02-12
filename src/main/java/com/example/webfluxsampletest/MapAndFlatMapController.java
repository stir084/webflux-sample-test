package com.example.webfluxsampletest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RestController
public class MapAndFlatMapController {

    private final Logger log = LoggerFactory.getLogger(MapAndFlatMapController.class);

    public static List<Integer> inputList = new ArrayList<>();

    // input-number로 숫자 100만개 입력 후에
    // /map 요청 후 /test를 요청해보면 쓰레드가 1개여도 동시 처리된다.(io Thread가 방해받지 않는 비동기 연산이란 의미다)
    // 이전의 오해는 .map() 메소드가 synchrounous, non-blocking 방식이라 Netty Http 쓰레드가 대기 상태일줄 알았지만 non-blocking에 의해 다른 작업도 동시 처리할 수 있다.
    // synchronous는 순서를 보장한다는 측면에서 생각하는 것이 맞다.

    // 결과적으로 단순 시간을 비교해보면 .map()이 5초 .flatMap()이 8초로 .map이 더 빠르다.
    // 하지만 /map과 /flatMap을 동시에 요청하면 각각 15초, 15초가 나오는데, 이는 Flux 내부에 있는 연산을 수행하는 Event Loop의 Thread는 단일 스레드로 구성되어있고 당연히 자원이 모자라기 때문이다.
    // 즉 io Thread가 방해받지 않더라도 Event Loop Thread를 방해하는 경우도 충분히 생길 수 있다.
    // 이벤트 루프 쓰레드 갯수를 늘릴 수 있냐고? 없다 https://stir.tistory.com/459 를 보면 그냥 싱글 스레드 구조다.
    @GetMapping("/input-number")
    public void inputNumber() {
        for (int i = 1; i <= 1000000; i++) {
            inputList.add(i);
        }
    }
    @GetMapping("/map")
    public Flux<String> map() {
        return Flux.fromIterable(inputList)
            .map(i -> {
                String heavyObject = "Heavy Object " + i;
                return heavyObject;
            });
    }
    @GetMapping("/flatMap")
    public Flux<String> flatMap() {
        Flux<String> flatMapResult = Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    String heavyObject = "Heavy Object " + i;
                    return heavyObject;
                });
                return heavyObjectMono;
            });
        return flatMapResult;
    }

    @GetMapping("/test")
    public Mono<String> test(){
        return Mono.just("Hi");
    }

    // Webflux의 비동기를 살려 flatMap을 사용할 지라도 내부에 동기식 코드가 있으면 Thread는 Block 된다.
    @GetMapping("/use-incorrectly-flatMap")
    public Flux<String> useIncorrectlyFlatMap() {
        Flux<String> flatMapResult = Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    IntStream.range(0, 1_000_000_000).forEach(it -> {
                        if (it % 1_000_000 == 0) {
                            //log.info("Request : " + it);
                        }
                    });
                    String heavyObject = "Heavy Object " + i;
                    return heavyObject;
                });
                return heavyObjectMono;
            });
        return flatMapResult;
    }

    // 약 16초 소요된다.
    // 로그를 찍는 작업도 Thread가 하는 작업이기 때문에 속도 저하의 원인이 될 수 있다.
    @GetMapping("/use-incorrectly-flatMap2")
    public Flux<String> useIncorrectlyFlatMap2() {
        Flux<String> flatMapResult = Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    log.info("Request : " + i);
                    String heavyObject = "Heavy Object " + i;
                    return heavyObject;
                });
                return heavyObjectMono;
            });
        return flatMapResult;
    }
}

