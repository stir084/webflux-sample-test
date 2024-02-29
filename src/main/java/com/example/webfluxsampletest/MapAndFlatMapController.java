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
    /**
     * Map과 FlatMap의 핵심은
     * 동시성이 있느냐와 없느냐의 차이가 있다.
     * Map은 동시성이 없다.
     * FlatMap은 동시성이 있다.
     * Map은 동시성이 없기 때문에 순치적으로 처리해서 순차적으로 데이터를 쌓을 수 있다.
     * FlatMap은 동시성이 있기 때문에 병렬적으로 처리해서 순서에 상관없이 데이터가 쌓인다.
     * 당연히 FlatMap이 더 빨라보이겠지만 Map은 객체 생성 비용이 더 적기 때문에 메소드 그 자체로만 보면 시간이 덜 소요된다.
     * 하지만 FlatMap은 내용이 무겁고 여러개를 처리해야할 필요가 있는 작업이라면 FlatMap을 쓰는 것이 더 옳다.
     * FlatMap에도 순서를 보장해주는 FlatMapSequential이 존재하는데, 그럼 Map과 무슨 차이냐 할 수 있겠지만
     * FlatMapSequential이 당연히 객체 생성 비용이 더 많으니 그 자체로 느리다.
     * 다만 이것도 내부의 요소가 얼마나 비동기적으로 병렬처리가 필요하냐에 따라서 FlatMapSequential을 선택하면된다.
     */
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
    // 여기서 얻을 수 있는 정보는 순서에 상관이 없이 8초라서 각각 독립된 쓰레드가 일을 하고 독립된 시간을 유지한다는 뜻이다.
    // (하지만 로그는 순차적으로 찍히긴 한다.)
    // 6초보다 8초가 걸리는 이유는 각 Thread가 서로 다른 I/O를 처리하러 가끔 Context Switching을 하기 때문이다(Thread의 Park 개념 확인)
    // 작업 관리자로 보니까 단일 요청은 CPU가 55퍼까지 오르지만 이중 요청은 80퍼센트까지 오른다.

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

    // Webflux의 비동기를 살려 flatMap을 사용할 지라도 내부에 CPU 사용이 많은 작업을 하면 Thread는 Block 된다.(당신의 WebFlux는 왜 느린가 유튜브 참고)
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

