package com.nps.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
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

    /**
     * take方法可以指定只接收的数据项个数
     */
    @Test
    public void take(){
        Flux<String> nationalParkFlux = Flux.just("Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton").take(4);

        StepVerifier.create(nationalParkFlux)
                .expectNext("Yellowstone", "Yosemite", "Grand Canyon", "Zion")
                .verifyComplete();
    }

    /**
     * 于skip一样，take也可以指定订阅之后发布的时间，超过这个时间的数据项就不再接收
     */
    @Test
    public void take2(){
        Flux<String> nationalParkFlux = Flux.just("Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
                .delayElements(Duration.ofSeconds(1))
                .take(Duration.ofMillis(4500));

        StepVerifier.create(nationalParkFlux)
                .expectNext("Yellowstone", "Yosemite", "Grand Canyon", "Zion")
                .verifyComplete();
    }

    /**
     * filter能根据给定的条件过滤数据项
     */
    @Test
    public void filter(){
        Flux<String> nationalParkFlux = Flux.just("Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Grand Teton")
                .filter(np -> !np.contains("t"));

        StepVerifier.create(nationalParkFlux)
                .expectNext("Grand Canyon", "Zion")
                .verifyComplete();
    }

    /**
     * distinct方法可以过滤掉重复的数据项
     */
    @Test
    public void distinct(){
        Flux<String> animalFlux = Flux.just("dog", "cat", "pig", "dog", "pig", "bird").distinct();

        StepVerifier.create(animalFlux)
                .expectNext("dog", "cat", "pig", "bird").verifyComplete();
    }

    static class Player{
        private String firstName;
        private String lastName;

        Player(String firstName, String lastName){
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    /**
     * map方法可以将原本发布的数据变为想要的格式类型
     */
    @Test
    public void mapFlux(){
        Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
                .map(n -> {
                    String[] split = n.split("\\s");
                    return new Player(split[0], split[1]);
                });

        StepVerifier.create(playerFlux)
                .expectNextMatches(p -> p.getFirstName().equals("Michael") && p.getLastName().equals("Jordan"))
                .expectNextMatches(p -> p.getFirstName().equals("Scottie") && p.getLastName().equals("Pippen"))
                .expectNextMatches(p -> p.getFirstName().equals("Steve") && p.getLastName().equals("Kerr"))
                .verifyComplete();
    }

    /**
     * flatmap就是异步执行map的操作（使用Schedulers.parallel方法）
     */
    @Test
    public void flatMapFlux(){
        Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
                .flatMap(n -> Mono.just(n).map(p -> {
                    String[] split = n.split("\\s");
                    return new Player(split[0], split[1]);
                }).subscribeOn(Schedulers.single())//single()会使用单个线程来执行订阅操作，只是为了保证顺序来让测试通过而已
                );

        StepVerifier.create(playerFlux)
                .expectNextMatches(p -> p.getFirstName().equals("Michael") && p.getLastName().equals("Jordan"))
                .expectNextMatches(p -> p.getFirstName().equals("Scottie") && p.getLastName().equals("Pippen"))
                .expectNextMatches(p -> p.getFirstName().equals("Steve") && p.getLastName().equals("Kerr"))
                .verifyComplete();
    }

    /**
     * buffer操作可以产生一个新的flux列表
     */
    @Test
    public void buffer(){
        Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
        Flux<List<String>> bufferedFlux = fruitFlux.buffer(2);

        StepVerifier.create(bufferedFlux)
                .expectNext(Arrays.asList("apple", "orange"))
                .expectNext(Arrays.asList("banana", "kiwi"))
                .expectNext(Arrays.asList("strawberry"))
                .verifyComplete();
    }

    @Test
    public void bufferAndFlat(){
        Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
                .buffer(3)
                .flatMap(x ->
                        Flux.fromIterable(x)
                                .map(y -> y.toUpperCase(Locale.ROOT))
                                .subscribeOn(Schedulers.parallel()).log()
                ).subscribe();
    }

    /**
     * 将发布的数据项转为list
     */
    @Test
    public void collectList(){
        Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
        Mono<List<String>> fruitListMono = fruitFlux.collectList();

        StepVerifier.create(fruitListMono)
                .expectNext(Arrays.asList("apple", "orange", "banana", "kiwi", "strawberry"))
                .verifyComplete();
    }

    /**
     * 将发布的数据项转为map
     */
    @Test
    public void collectMap(){
        Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala");
        Mono<Map<Character, String>> animalMapMono = animalFlux.collectMap(a -> a.charAt(0));

        StepVerifier.create(animalMapMono)
                .expectNextMatches(map -> {
                    return map.size() == 3 && map.get('a').equals("aardvark")
                            && map.get('e').equals("elephant")
                            && map.get('k').equals("koala");
                }).verifyComplete();
    }

    /**
     * all是需要全部匹配才为true
     */
    @Test
    public void all(){
        Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
        Mono<Boolean> hasAMono = animalFlux.all(a -> a.contains("a"));
        StepVerifier.create(hasAMono)
                .expectNext(true)
                .verifyComplete();

        Mono<Boolean> hasKMono = animalFlux.all(a -> a.contains("k"));
        StepVerifier.create(hasKMono)
                .expectNext(false).verifyComplete();
    }

    /**
     * any只需要一个匹配则就是true
     */
    @Test
    public void any(){
        Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
        Mono<Boolean> hasTMono = animalFlux.any(a -> a.contains("t"));
        StepVerifier.create(hasTMono)
                .expectNext(true)
                .verifyComplete();

        Mono<Boolean> hasVMono = animalFlux.all(a -> a.contains("v"));
        StepVerifier.create(hasVMono)
                .expectNext(false).verifyComplete();
    }
}