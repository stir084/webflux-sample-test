package com.example.webfluxsampletest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.stream.IntStream;

@RestController
public class ToUnderstandThreadController {

    private final Logger log = LoggerFactory.getLogger(ToUnderstandThreadController.class);

    @GetMapping("/impoverish/{id}")
    public Mono<String> impoverishThread(@PathVariable String id) {
        long start = System.currentTimeMillis();


        IntStream.range(0, 1_000_000_000).forEach(it -> {
            if (it % 100_000_000 == 0) {
                // log.info("Request [" + id + "] for: " + it);
            }
        });

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);
        return Mono.just( format + "초 소요되었습니다.");
    }
    /*
    하지만 실제로 쓰레드가 일을 해야하는 무거운 연산을 수행하는 경우에는 응답이 지연되는 결과를 얻었습니다."
    스트리밍 서비스 처럼 Webflux랑 안 어울리는 프로젝트도 당연히 있지만 단순히 무거운 연산과 웹플럭스가 안어울리진 않습니다."
    테스트 방식의 문제점은 Intstream.range에 있습니다.
    해당 코드는 동기식으로 돌아가기 때문에 당연히 두번째 요청은 첫번째가 끝날 때까지 대기합니다.
    웹플럭스는 비동기적으로 처리하는게 핵심이기 때문에 Intstream.range가 아닌 Flux.range로 처리해보시면 올바른 결과를 얻을 수 있습니다.
     */



    // IO도 역시 느리다.
    @GetMapping("/correct")
    public Flux<Integer> useIteratorCorrectly() {
        Flux<Integer> flux = Flux.range(0, 1_000_00);

       /* flux.doOnNext(item -> System.out.println("On next: " + item + "--" + Thread.currentThread().getName())).flatMap(it -> {
            if (it % 100 == 0) {
                log.info("Request ["  + "] for: " + it);
            }
            //return Mono.empty(); // 비동기 작업 처리
            //flatMap 내에서 Mono.empty()를 사용하면 비동기 작업을 처리하도록 지정하지만 실제로는 데이터를 전달하지 않습니다.
            return Mono.just(it);
        }); // 대기

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);*/

        /*Mono<String> flux2 = Mono.just(format);
        return flux2;*/
        return flux;
    }

/*
    @GetMapping("/hello2/{id}")
    public Mono<String> sayHello2(@PathVariable String id) {
        long start = System.currentTimeMillis();


        IntStream.range(0, 1_000_000_000).forEach(it -> {
            if (it % 100_000_000 == 0) {
               // log.info("Request [" + id + "] for: " + it);
            }
        });

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);
        return Mono.just("Hello, WebFlux!" + id + "---" + format + " seconds");
    }*/

    @GetMapping("/test")
    public void test() {
        Flux<Integer> flux = Flux.just(1);
//Observer 1
        flux.subscribe(i -> System.out.println("Observer-1 : " + i));
//Observer 2
        flux.subscribe(i -> System.out.println("Observer-2 : " + i));

//Output
        //Observer-1 : 1
        //Observer-2 : 1
              //  -----------------------------------



                Flux.just('a', 'b', 'c')
                        .subscribe(i -> System.out.println("Received : " + i));

//Output
       // Received : a
       // Received : b
      //  Received : c

    }

    @GetMapping("/hello3/{id}")
    public Mono<String> sayHello22(@PathVariable String id) {
        long start = System.currentTimeMillis();

        Flux<Integer> flux = Flux.range(0, 1_000_0);

        flux.flatMap(it -> {
            if (it % 100_000 == 0) {
                // log.info("Request [" + id + "] for: " + it);
            }
            return Mono.empty(); // 비동기 작업 처리
        }); // 대기

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);
        Mono<String> flux2 = Mono.just(format);
        //Mono<String> flux2 = Mono.just(format);
        return flux2;
    }

    @GetMapping("/hello2/{id}")
    public Mono<String> sayHello2(@PathVariable String id) {
        long start = System.currentTimeMillis();

        Flux<Integer> flux = Flux.range(0, 1_000_000);

        flux.doOnNext(it -> {
            if (it % 100_000 == 0) {
                //System.out.println(it + "--" + Thread.currentThread().getName()+"--"+id);
                // log.info("Request [" + id + "] for: " + it);
            }
        }).flatMap(it -> {
            if (it % 100_000 == 0) {
                //System.out.println(it + "--" + Thread.currentThread().getName());
                // log.info("Request [" + id + "] for: " + it);
            }
            return Mono.empty(); // 비동기 작업 처리
        }).subscribe(); // 대기

        long end = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        String format = formatter.format((end - start) / 1000d);

        Mono<String> flux2 = Mono.just(format);
        return flux2;
    }
}
