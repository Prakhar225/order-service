package in.capace.microservices.OrderService.repository;

import in.capace.microservices.OrderService.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
