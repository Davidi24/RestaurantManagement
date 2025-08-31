package pos.pos.DTO.Order.OrderCollectorDTO;


import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderUpdateDTO {
    private Long id;
    private String notes;
}