package com.antocecere77.learnreactivespring.itemclient.controller;

import com.antocecere77.learnreactivespring.itemclient.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ItemClientController {

    WebClient webClient = WebClient.create("http://localhost:8080");

    @GetMapping("/client/retrieve")
    public Flux<Item> getAllItemsUsingRetrieve() {
        return webClient.get()
                .uri("v1/items")
                .retrieve()
                .bodyToFlux(Item.class)
                .log("Items in client project retrieve");
    }

    @GetMapping("/client/exchange")
    public Flux<Item> getAllItemsUsingExchange() {
        return webClient.get()
                .uri("v1/items")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
                .log("Items in client project exchange");
    }

    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> getSingleItemUsingRetrieve() {

        String id = "ABC";
        return webClient.get()
                .uri("v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Items in client project retrieve single item");
    }

    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> getSingleItemUsingExchange() {

        String id = "ABC";
        return webClient.get()
                .uri("v1/items/{id}", id)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
                .log("Items in client project exchange single item");
    }

    @PostMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item) {

        Mono<Item> itemMono = Mono.just(item);
        return webClient.post()
                .uri("v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body(itemMono, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Create item is: ");
    }

    @PutMapping("/client/updateItem/{id}")
    public Mono<Item> upadteItem(@PathVariable String id, @RequestBody Item item) {

        Mono<Item> itemBody = Mono.just(item);
        return webClient.put()
                .uri("/v1/items/{id}", id)
                .body(itemBody, Item.class)
                .retrieve()
                .bodyToMono(Item.class)
                .log("Updated item is: ");
    }

    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id) {
        return webClient.delete()
                .uri("/v1/items/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .log("Delete item is: ");
    }

    @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve() {
        return webClient.get()
                .uri("/v1/items/returnException")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                    return errorMono.flatMap(errorMessage -> {
                        log.error("The error message is " + errorMessage);
                        throw new RuntimeException(errorMessage);
                    });
                })
                .bodyToFlux(Item.class);
    }

    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange() {
        return webClient.get()
                .uri("/v1/items/returnException")
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().is5xxServerError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    log.error("Error message in exchange: " + errorMessage);
                                    throw new RuntimeException(errorMessage);
                                });
                    } else {
                        return clientResponse.bodyToFlux(Item.class);
                    }
                });
    }

}
