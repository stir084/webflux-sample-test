package com.example.webfluxsampletest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class BackPressureController {

    /**
     * 일반적으로 데이터 스트림에서 생산자는 데이터를 생성하고, 소비자는 데이터를 처리합니다.
     * 생산자는 소비자가 처리할 수 있는 속도보다 빠르게 데이터를 생성할 수 있습니다.
     * 이 경우에 생산자가 소비자로부터 데이터를 처리할 수 있는 속도로 데이터를 전송하는 것이 중요합니다.
     * 그렇지 않으면 소비자가 데이터를 처리할 수 없고 생산자가 대기열에 계속 데이러를 쌓기 때문에 메모리 부족이나 다른 문제가 발생할 수 있습니다.
     *
     * delayElements는 BackPressure를 구현하기 위한 방법 중 하나다.
     *
     * 백프레셔는 일종의 저항이 있는 시스템에서 유체의 흐름이 더 이상 증가하지 못하고 역압(BackPressure)이 발생할 때, 이 역압을 처리하고 시스템의 안정성을 유지하기 위한 메커니즘을 의미합니다.
     * BackPressure Ignore, Error, Latest, Buffer 등의 전략을 사용한다.
     */
    @GetMapping(value = "/data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getData() {
        return Flux.range(1, 1000)
                .delayElements(Duration.ofMillis(100)) // 각 요소를 100ms마다 방출
                .log(); // 로그 출력 (BackPressure 확인용)
    }

    @GetMapping("/data-mono")
    public Mono<String> getSingleData() {
        return Mono.just("Hello, World!");
    }
}