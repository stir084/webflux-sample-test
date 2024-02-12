package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MapAndFlatMapController {
    public static List<Integer> inputList = new ArrayList<>();

    // input-number로 숫자 100만개 입력 후에
    // /map 요청 후 /test를 요청해보면 쓰레드가 1개여도 동시 처리된다.
    // .map() 메소드가 synchrounous, non-blocking 방식이라 Netty Http 쓰레드가 대기 상태일줄 알았는데 .map() 메소드는 이벤트 루프가 처리한다.
    // 단순 시간을 비교해보면 .map()이 5초 .flatMap()이 8초로 .map이 더 빠르다.
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
}
