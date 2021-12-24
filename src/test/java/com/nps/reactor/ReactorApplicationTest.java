package com.nps.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReactorApplicationTest {

    @Test
    public void createAFlux(){
        Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        fruitFlux.subscribe(f -> System.out.println("Here`s some fruit: " + f));
        StepVerifier.create(fruitFlux).expectNext("Apple").expectNext("Orange").expectNext("Grape")
                .expectNext("Banana").expectNext("Strawberry").verifyComplete();
    }

    @Test
    public void createAFlux_fromArray(){
        String[] fruits = new String[]{"Apple", "Orange", "Grape", "Banana", "Strawberry"};
        Flux<String> fruitFlux = Flux.fromArray(fruits);
        fruitFlux.subscribe(f -> System.out.println("Here`s some fruit: " + f));
        StepVerifier.create(fruitFlux).expectNext("Apple").expectNext("Orange").expectNext("Grape")
                .expectNext("Banana").expectNext("Strawberry").verifyComplete();
    }

    @Test
    public void createAFlux_fromIterable(){
        List<String> fruitList = new ArrayList<>();
        fruitList.add("Apple");
        fruitList.add("Orange");
        fruitList.add("Grape");
        fruitList.add("Banana");
        fruitList.add("Strawberry");
        Flux<String> fruitFlux = Flux.fromIterable(fruitList);
        fruitFlux.subscribe(f -> System.out.println("Here`s some fruit: " + f));
        StepVerifier.create(fruitFlux).expectNext("Apple").expectNext("Orange").expectNext("Grape")
                .expectNext("Banana").expectNext("Strawberry").verifyComplete();
    }

    @Test
    public void createAFlux_fromStream(){
        Stream<String> fruitStream = Stream.of("Apple", "Orange", "Grape", "Banana", "Strawberry");
        Flux<String> fruitFlux = Flux.fromStream(fruitStream);
        fruitFlux.subscribe(f -> System.out.println("Here`s some fruit: " + f));
        StepVerifier.create(fruitFlux).expectNext("Apple").expectNext("Orange").expectNext("Grape")
                .expectNext("Banana").expectNext("Strawberry").verifyComplete();
    }

    @Test
    public void createAFlux_range(){
        Flux<Integer> rangeFlux = Flux.range(1, 5);
        StepVerifier.create(rangeFlux).expectNext(1).expectNext(2).expectNext(3)
                .expectNext(4).expectNext(5).verifyComplete();
    }

    @Test
    public void createAFlux_interval(){
        Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(5);
        StepVerifier.create(intervalFlux).expectNext(1L).expectNext(2L).expectNext(3L)
                .expectNext(4L).expectNext(5L).verifyComplete();
    }

    /**
     * merge方法原本是不确定先后顺序的，但是因为代码中设置了delay失效，所以chara和food先后发布出来
     */
    @Test
    public void mergeFluxes(){
        Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa")
                .delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));
        Flux<String> mergeFlux = characterFlux.mergeWith(foodFlux);
        StepVerifier.create(mergeFlux)
                .expectNext("Garfield")
                .expectNext("Lasagna")
                .expectNext("Kojak")
                .expectNext("Lollipops")
                .expectNext("Barbossa")
                .expectNext("Apples").verifyComplete();
    }

    /**
     * zip方法的两种模式，可以打包后同时发布两个对象，也可以将两个对象组合起来一起发布
     */
    @Test
    public void zipFluxes(){
        Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
        Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");

//        Flux<Tuple2<String, String>> zippedFlux = Flux.zip(characterFlux, foodFlux);
//        StepVerifier.create(zippedFlux)
//                .expectNextMatches(p -> p.getT1().equals("Garfield") && p.getT2().equals("Lasagna"))
//                .expectNextMatches(p -> p.getT1().equals("Kojak") && p.getT2().equals("Lollipops"))
//                .expectNextMatches(p -> p.getT1().equals("Barbossa") && p.getT2().equals("Apples"))
//                .verifyComplete();

        Flux<String> zippedFlux = Flux.zip(characterFlux, foodFlux, (c, f) -> c + " eats " + f);
        StepVerifier.create(zippedFlux)
                .expectNext("Garfield eats Lasagna")
                .expectNext("Kojak eats Lollipops")
                .expectNext("Barbossa eats Apples")
                .verifyComplete();
    }

    /**
     * first方法会优先发布更快推送出来的流，所以直接把慢的流全部忽略了
     */
    @Test
    public void firstFlux(){
        Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(100));
        Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

        Flux<String> firstFlux = Flux.firstWithSignal(slowFlux, fastFlux);
        StepVerifier.create(firstFlux)
                .expectNext("hare")
                .expectNext("cheetah")
                .expectNext("squirrel")
                .verifyComplete();
    }

    /**
     * skip方法可以跳过指定个数的数据项
     */
    @Test
    public void skipAFew(){
        Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "one hundred").skip(1);

        StepVerifier.create(skipFlux).expectNext("two", "skip a few","ninety nine", "one hundred").verifyComplete();
    }

    /**
     * skip方法还可以指定跳过的时间
     */
    @Test
    public void skipAFewSeconds(){
        Flux<String> skipFlux = Flux.just("one", "two", "skip a few", "ninety nine", "one hundred")
                .delayElements(Duration.ofSeconds(1)).skip(Duration.ofSeconds(2));

        StepVerifier.create(skipFlux).expectNext("two", "skip a few","ninety nine", "one hundred").verifyComplete();
    }
}