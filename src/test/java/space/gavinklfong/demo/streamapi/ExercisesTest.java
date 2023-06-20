package space.gavinklfong.demo.streamapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import space.gavinklfong.demo.streamapi.models.Customer;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@DataJpaTest
public class ExercisesTest {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    /**
     * Obtain a list of products belongs to category “Books” with price > 100
     */
    @Test
    public void exercise1(){
        String category = "Books";
        List<Product> result = productRepo.findAll().stream()
                .filter(x -> x.getCategory().equals(category))
                .filter(x -> x.getPrice() > 100)
                .collect(Collectors.toList());

        for (Product product: result){
            assertEquals(category, product.getCategory());
            assertTrue(product.getPrice() > 100);
        }

        Random random = new Random();
        IntStream randNumberStream = random.ints(10, 0, 10);
        OptionalInt next = randNumberStream.findFirst();
        next.getAsInt();
    }

    /**
     * Obtain a list of order with products belong to category “Baby”
     */
    @Test
    public void exercise2(){

        String category = "Baby";

        List<Order> orderList = orderRepo.findAll().stream()
                .filter(order -> order.getProducts().stream()
                        .anyMatch(product -> product.getCategory().equals(category))
                ).collect(Collectors.toList());

        // check if each Order has a Product with the baby category
        boolean containsBaby = false;
        for (Order order: orderList){
            for (Product product: order.getProducts()){
                if(product.getCategory().equals(category)){
                    containsBaby = true;
                }
            }
            assertTrue(containsBaby);
        }
    }

    /**
     * Obtain a list of product with category = “Toys” and then apply 10% discount
     */
    @Test
    public void exercise3(){

        String category = "Toys";
        List<Product> productList = productRepo.findAll();

        // create map of id to Product to check results
        Map<Long, Product> idToProductMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // get products with category toy and reduce price by 10%
        List<Product> result = productList.stream()
                .filter(product -> product.getCategory().equals(category) )
                //.map(product -> getDiscountedProduct(product))
                .map(p -> p.withPrice(p.getPrice() * 0.9)) // improved answer from solution
                .collect(Collectors.toList());

        for(Product product: result){
            assertEquals(category, product.getCategory());
            // check that all prices are reduced by 10%
            assertEquals(
                    idToProductMap.get(product.getId()).getPrice()*.9,
                    product.getPrice());
        }

    }

    /**
     * Obtain a list of products ordered by customer of tier 2 between 01-Feb-2021 and 01-Apr-2021
     */
    @Test
    public void exercise4(){

        LocalDate minDate = LocalDate.of(2021, 2, 1);
        LocalDate maxDate = LocalDate.of(2021, 4, 1);
        List<Order> orderList = orderRepo.findAll();

        List<Order> orders = orderList.stream()
                .filter(x -> x.getCustomer().getTier() == 2)
                .filter(order -> order.getOrderDate().isAfter(minDate))
                .filter(order -> order.getOrderDate().isBefore(maxDate))
                .sorted(Comparator.comparing(order -> order.getCustomer().getName()))
                .collect(Collectors.toList());

        orders.forEach(x -> System.out.println(x.getCustomer().getName()));

        for(Order order: orders){
            assertEquals(2, order.getCustomer().getTier());
            assertTrue(order.getOrderDate().isAfter(minDate));
            assertTrue(order.getOrderDate().isBefore(maxDate));
        }
    }
}
