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

import javax.persistence.Entity;
import javax.persistence.Tuple;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
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
                .filter(order -> order.getCustomer().getTier() == 2)
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


    /**
     * Get the 3 cheapest products of "Books" category
     */
    @Test
    public void exercise5(){
        String category = "Books";
        List<Product> result = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equals(category))
                .sorted(Comparator.comparing(Product::getPrice))
                .limit(3)
                .collect(Collectors.toList());

        result.forEach(product -> System.out.println(product.toString()));

        List<Long> expectedIdsInOrder = Arrays.asList(17L, 16L, 7L);

        assertEquals(expectedIdsInOrder.size(), result.size());
        for(int i=0; i<expectedIdsInOrder.size(); i++){
            assertEquals(expectedIdsInOrder.get(i), result.get(i).getId());
        }
    }

    /**
     * Get the 3 most recent placed order
     */
    @Test
    public void exercise6(){
        List<Order> orderList = orderRepo.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate))
                .limit(3)
                .collect(Collectors.toList());

        List<Long> expectedOrderIds = Arrays.asList(12L, 41L, 22L);
        assertEquals(expectedOrderIds.size(), orderList.size());
        for(int i=0; i<expectedOrderIds.size(); i++){
            assertEquals(expectedOrderIds.get(i), orderList.get(i).getId());
        }
    }

    /**
     * Get a list of products which was ordered on 15-Mar-2021
     */
    @Test
    public void exercise7(){

        LocalDate expectedDate = LocalDate.of(2021, 3, 15);

        List<Product> products = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().equals(expectedDate))
                .flatMap(order -> order.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());

        for (Product product: products){

            boolean hasCorrectOrderDate = false;
            for (Order order: product.getOrders()){
                if(expectedDate.equals(order.getOrderDate())){
                    hasCorrectOrderDate = true;
                }
            }
            assertTrue(hasCorrectOrderDate);
        }

    }

    /**
     * Calculate the total lump sum of all orders placed in Feb 2021
     */
    @Test
    public void exercise8(){

        Double expectedTotalPrice = Double.valueOf("11995.36");

        Double totalPrice = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(LocalDate.of(2021, 1, 31)))
                .filter(order -> order.getOrderDate().isBefore(LocalDate.of(2021, 3, 1)))
                .flatMap(order -> order.getProducts().stream())
                //.mapToDouble(order -> order.getPrice())
                .mapToDouble(Product::getPrice)// method reference improves readability
                .sum();

        assertEquals(expectedTotalPrice, totalPrice);

    }

    /**
     * Calculate the total lump of all orders placed in Feb 2021 (using reduce with BiFunction)")
     */
    @Test
    public void exercise8a(){

        Double expectedTotalPrice = Double.valueOf("11995.36");

        Double totalPrice = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(LocalDate.of(2021, 1, 31)))
                .filter(order -> order.getOrderDate().isBefore(LocalDate.of(2021, 3, 1)))
                .flatMap(order -> order.getProducts().stream())
                .mapToDouble(Product::getPrice)
                .reduce(0, (x, y) -> x+y);

        assertEquals(expectedTotalPrice, totalPrice);
    }

    /**
     * Calculate the average price of all orders placed on 15-Mar-2021
     */
    @Test
    public void exercise9(){

        Double expectedAveragePrice = Double.valueOf("352.89");

        Double averagePrice = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().equals( LocalDate.of(2021, 3, 15) ) )
                .flatMap(order -> order.getProducts().stream())
                .mapToDouble(Product::getPrice)
                .average()
                .getAsDouble();

        assertEquals(expectedAveragePrice, averagePrice);
    }

    /**
     * Obtain statistics summary of all products belong to "Books" category
     */
    @Test
    public void exercise10(){

        DoubleSummaryStatistics booksStatistics = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equals("Books"))
                .mapToDouble(Product::getPrice)
                .peek(System.out::println)
                .summaryStatistics();

        assertEquals(607.88, booksStatistics.getAverage());
        assertEquals(5, booksStatistics.getCount());
        assertEquals(893.44, booksStatistics.getMax());
        assertEquals(240.58, booksStatistics.getMin());
        assertEquals(3039.4, booksStatistics.getSum());
    }

    /**
     * Obtain a data map with order id and order’s product count
     */
    @Test
    public void exercise11(){

        Map<Long, Integer> orderIdToProductCountMap = orderRepo.findAll().stream()
                .collect(Collectors.toMap(Order::getId, order -> order.getProducts().size()));

        for(Order order: orderRepo.findAll()){
            assertEquals(order.getProducts().size(), orderIdToProductCountMap.get(order.getId()));
        }
    }

    /**
     * Obtain a data map of customer and list of orders
     * Produce a data map with order records grouped by customer
     */
    @Test
    public void exercise12(){

        Map<Customer, List<Order>> customerToOrderMap = orderRepo.findAll().stream()
                .collect(Collectors.groupingBy(Order::getCustomer));

        assertEquals(10, customerToOrderMap.size());
    }

    /**
     * Obtain a data map of customer_id and list of order_id(s)
     */
    @Test
    public void exercise12a(){

        Map<Long, List<Long>> customersToOrderListMap = orderRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCustomer().getId(),
                        mapping(Order::getId, toList())
                ));

        // create list of orderId to Order to check the results of the other map
        Map<Long, Order> idToOrderMap = orderRepo.findAll().stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        // get number of customers to check size of map
        int numberOfCustomers = customerRepo.findAll().size();
        assertEquals(numberOfCustomers, customersToOrderListMap.size());

        for(Map.Entry<Long, List<Long>> customerOrders: customersToOrderListMap.entrySet()){
            System.out.println("Customer: "+customerOrders.getKey());
            System.out.println("Orders: "+customerOrders.getValue());
            System.out.println("-------------");
            for(Long orderId: customerOrders.getValue()){
                assertEquals(customerOrders.getKey(), idToOrderMap.get(orderId).getCustomer().getId());
            }
        }
    }

    /**
     * Obtain a data map with order and its total price
     */
    @Test
    public void exercise13(){


        Map<Order, Double> orderToPriceMap = orderRepo.findAll().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        order -> order.getProducts().stream()
                                .mapToDouble(Product::getPrice).sum())
                );


        // check answer
        for(Map.Entry<Order, Double> orderEntry: orderToPriceMap.entrySet()){
            Double orderTotal = getOrderTotal(orderEntry.getKey().getProducts());
            System.out.println(orderTotal + " : " + orderEntry.getValue());
            assertEquals(orderTotal, orderEntry.getValue());
        }
    }

    private Double getOrderTotal(Set<Product> products) {
        return products.stream().mapToDouble(Product::getPrice).sum();
    }

    /**
     * Obtain a data map with order and its total price (using reduce)
     */
    @Test
    public void exercise13a(){Map<Order, Double> orderToPriceMap = orderRepo.findAll().stream()
            .collect(Collectors.toMap(
                    Function.identity(),
                    order -> order.getProducts().stream()
                            .mapToDouble(Product::getPrice)
                            .reduce(0, (x, y) -> x+y))
            );


        // check answer
        for(Map.Entry<Order, Double> orderEntry: orderToPriceMap.entrySet()){
            Double orderTotal = getOrderTotalAdd(orderEntry.getKey().getProducts());
            System.out.println(orderTotal + " : " + orderEntry.getValue());
            assertEquals(orderTotal, orderEntry.getValue());
        }
    }

    private Double getOrderTotalAdd(Set<Product> products) {
        Double sum = Double.valueOf(0);
        for(Product product: products){
            sum += product.getPrice();
        }
        return sum;
    }

    /**
     * Obtain a data map of product name by category
     */
    @Test
    public void exercise14(){

    }

}
