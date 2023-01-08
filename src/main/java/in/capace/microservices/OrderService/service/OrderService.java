package in.capace.microservices.OrderService.service;

import in.capace.microservices.OrderService.dto.InventoryResponse;
import in.capace.microservices.OrderService.dto.OrderRequest;
import in.capace.microservices.OrderService.model.Order;
import in.capace.microservices.OrderService.model.OrderLineItems;
import in.capace.microservices.OrderService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        ModelMapper modelMapper = new ModelMapper();
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(orderLineItemsDto -> modelMapper.map(orderLineItemsDto, OrderLineItems.class))
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = orderLineItems
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        Boolean allInStock = Arrays.stream(inventoryResponses)
                .allMatch(InventoryResponse::getIsInStock);
        if (allInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Some products are out of stock");
        }
    }

}
