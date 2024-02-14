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
    // /map 요청 후 /test를 요청해보면 쓰레드가 1개여도 동시 처리된다.
    // 쓰기 작업에서 I/O Thread 1개가 동시 처리는 하는 것이다.
    // 하지만 당연히 /map을 2번 요청하면 두번째 요청은 동시 처리여도 시간이 2배다.
    // 이전의 오해는 .map() 메소드가 synchrounous, non-blocking 방식이라 Netty Http 쓰레드가 대기 상태일줄 알았지만 non-blocking에 의해 다른 작업도 동시 처리할 수 있다.
    // 단순히 synchronous는 순서를 보장한다는 측면에서 생각하는 것이 맞다.

    // 결과적으로 단순 시간을 비교해보면 .map()이 5초 .flatMap()이 8초로 .map이 더 빠르다.
    // 하지만 /map과 /flatMap을 동시에 요청하면 각각 15초, 15초가 나오는데
    // 이것도 i/o Thread가 1개 여서 그렇고 2개로 늘리면 속도가 짧아지는 것을 볼 수 있다.
    // 근데 궁금한 점은 2개로 늘리더라도 속도가 단일 요청(6초)보다 아주 약간 느리다(8초)는 점이었다.
    // log.info로 찍어본 결과 flatMap, Map을 순서에 상관 없이 동시에 요청을 하면 map은 8초가 된다.
    // 여기서 얻을 수 있는 정보는 순서에 상관이 없이 8초라서 각각 독립된 쓰레드가 일을 하고 독립된 시간을 유지한다는 뜻이다.
    // (하지만 로그는 순차적으로 찍히긴 한다.)
    // 그냥 CPU 하나를 통째로 써먹어서 그런가? 아닐텐데.. 쓰레드 하나가 그렇게 많이 처리한다고?
    // 작업 관리자로 보니까 단일 요청은 CPU가 55퍼까지 오르지만 이중 요청은 80퍼센트까지 오른다.
    // 결국 그냥 스레드만 나눈 것 뿐이지 CPU가 연산하는 속도의 한계때문에 8초가 걸리는 것이라고 결론 내릴 수 있겠다.

    // 이벤트 루프는 그냥 이벤트 순서를 지켜주는 쓰레드고 그냥 싱글 스레드 구조다.
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
                if(i%100==0) {
                    log.info("짠map" + Thread.currentThread().getName());
                }
                return "Heavy Object " + i;
            });
    }
    @GetMapping("/flatMap")
    public Flux<String> flatMap() {
        return Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    if(i%10000==0) {
                        log.info("짠flat" + Thread.currentThread().getName());
                    }
                    return "Heavy Object " + i;
                });
                return heavyObjectMono;
            });
    }

    @GetMapping("/test")
    public Mono<String> test(){
        return Mono.just("Hi");
    }

    // Webflux의 비동기를 살려 flatMap을 사용할 지라도 내부에 동기식 코드가 있으면 Thread는 Block 된다.
    @GetMapping("/use-incorrectly-flatMap")
    public Flux<String> useIncorrectlyFlatMap() {
        return Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    IntStream.range(0, 1_000_000_000).forEach(it -> {
                        if (it % 1_000_000 == 0) {
                            //log.info("Request : " + it);
                        }
                    });
                    return "Heavy Object " + i;
                });
                return heavyObjectMono;
            });
    }

    // 약 16초 소요된다.
    // 로그를 찍는 작업도 Thread가 하는 작업이기 때문에 속도 저하의 원인이 될 수 있다.
    @GetMapping("/use-incorrectly-flatMap2")
    public Flux<String> useIncorrectlyFlatMap2() {
        Flux<String> flatMapResult = Flux.fromIterable(inputList)
            .flatMap(i -> {
                Mono<String> heavyObjectMono = Mono.fromCallable(() -> {
                    log.info("Request : " + i);
                    return "Heavy Object " + i;
                });
                return heavyObjectMono;
            });
        return flatMapResult;
    }
}

