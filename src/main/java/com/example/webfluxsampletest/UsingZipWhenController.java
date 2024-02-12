package com.example.webfluxsampletest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UsingZipWhenController {
    @GetMapping("zipWhen")
    public void test () {
        String shopId = "123";
        ShopRepository shopRepository = new ShopRepository();
        SellerRepository sellerRepository = new SellerRepository();
        Mapper mapper = new Mapper();

        // flatMap()을 사용한 depth가 깊어지는 케이스
        shopRepository.findById(shopId)
            .flatMap(shop -> sellerRepository.findById(shop.getSellerId())
                .flatMap(seller -> Mono.just(mapper.toDto(shop, seller))))
            .subscribe(dto -> System.out.println("FlatMap Result: " + dto));

        // zipWhen()을 사용해 depth 탈출하는 케이스
        shopRepository.findById(shopId)
            .zipWhen(shop -> sellerRepository.findById(shop.getSellerId()))
            .map(tuple -> mapper.toDto(tuple.getT1(), tuple.getT2()))
            .subscribe(dto -> System.out.println("ZipWhen Result: " + dto));

        // zipWhen은 combinator를 받는 오버로드가 있기 때문에 여기 있는 map() 연산자는 생략 가능
        shopRepository.findById(shopId)
            .zipWhen(shop -> sellerRepository.findById(shop.getSellerId()), mapper::toDto)
            .subscribe(dto -> System.out.println("ZipWhen Result: " + dto));
    }
}

class Shop {
    private String id;
    private String sellerId;

    public Shop(String id, String sellerId) {
        this.id = id;
        this.sellerId = sellerId;
    }

    public String getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }
}

class Seller {
    private String id;
    private String name;

    public Seller(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class ShopDto {
    private String shopId;
    private String sellerName;

    public ShopDto(String shopId, String sellerName) {
        this.shopId = shopId;
        this.sellerName = sellerName;
    }

    public String getShopId() {
        return shopId;
    }

    public String getSellerName() {
        return sellerName;
    }
}

class ShopRepository {
    public Mono<Shop> findById(String shopId) {
        return Mono.empty();
    }
}

class SellerRepository {
    public Mono<Seller> findById(String sellerId) {
        return Mono.empty();
    }
}
class Mapper {
    public ShopDto toDto(Shop shop, Seller seller) {
        return new ShopDto(shop.getId(), seller.getName());
    }
}
