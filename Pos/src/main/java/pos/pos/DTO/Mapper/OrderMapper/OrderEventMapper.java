package pos.pos.DTO.Mapper.OrderMapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderEventResponseDTO;
import pos.pos.Entity.Order.OrderEvent;

@Component
public class OrderEventMapper {

    public OrderEventResponseDTO toOrderEventResponse(OrderEvent entity) {
        return OrderEventResponseDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .staffEmail(entity.getStaffEmail())
                .timestamp(entity.getTimestamp())
                .metadata(entity.getMetadata())
                .build();
    }
}
