package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
public class ChangeMonoAndFluxEachOtherController {

    /**
     * fromIterable - 리스트로 된 데이터를 Flux로 변환해준다.
     */
    @GetMapping("/fromIterable")
    public void fromIterable() {
        final List<String> baskets = Arrays.asList("test1", "test2", "test3");
        final Flux<String> basketFlux = Flux.fromIterable(baskets);
    }

    /**
     * flatMapMany - Mono로 감싸진 리스트를 Flux로 바꿔준다.
     */

    @GetMapping("/flatMapMany")
    public Flux<Integer> flatMapMany() {
        Mono<List<Integer>> listMono1 = Mono.just(List.of(1, 2, 3, 4, 5));
        Flux<Integer> intFlux1 = listMono1.flatMapMany(Flux::fromIterable);
        return intFlux1;
    }

    /**
     * Mono List를 Flux로 바꿀 수 있다.
     */
    @GetMapping("/mono-flux")
    public Flux<Integer> monoFlux() {
        Mono<List<Integer>> listMono1 = Mono.just(List.of(1, 2, 3, 4, 5));
        Flux<Integer> intFlux1 = listMono1.flatMapMany(Flux::fromIterable);
        return intFlux1;
    }

    /**
     * Flux를 Mono List로 바꿀 수 있다.
     */
    @GetMapping("/flux-mono")
    public Mono<List<Integer>> fluxMono(){
        Mono<List<Integer>> listMono = Flux.just(1, 2, 3, 4, 5).collectList();
        return listMono;
    }

    @GetMapping("/mono-flux-mono")
    public Mono<List<Integer>> monoFluxMono() {
        Mono<List<Integer>> listMono1 = Mono.just(List.of(1, 2, 3, 4, 5));
        Mono<List<Integer>> listMono2 = Mono.just(List.of(6, 7, 8, 9, 10));
        Flux<Integer> intFlux1 = listMono1.flatMapMany(Flux::fromIterable);
        Flux<Integer> intFlux2 = listMono2.flatMapMany(Flux::fromIterable);
        Mono<List<Integer>> mergedListMono = Flux.merge(intFlux1, intFlux2).collectList();
        return mergedListMono;
    }


}
