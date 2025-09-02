package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderEventMapper;
import pos.pos.DTO.Order.OrderEventResponseDTO;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderEvent;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Repository.OrderEventRepository;
import pos.pos.Repository.OrderRepository;
import pos.pos.Service.Interfecaes.OrderEventService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderEventServiceImpl implements OrderEventService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository eventRepository;
    private final OrderEventMapper eventMapper;

    @Override
    public void logEvent(Order order, OrderEventType type, String userEmail, String metadata) {
        OrderEvent event = OrderEvent.builder()
                .order(order)
                .type(type)
                .staffEmail(userEmail)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        eventRepository.save(event);
    }

    @Override
    public List<OrderEventResponseDTO> getEvents(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        return eventRepository.findByOrder_Id(orderId).stream()
                .map(eventMapper::toOrderEventResponse)
                .toList();
    }
}
