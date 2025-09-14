package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderTotals;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.Order.TotalsService;

@Service
@RequiredArgsConstructor
@Transactional
public class TotalsServiceImpl implements TotalsService {

    private final OrderRepository orderRepository;

    @Override
    public void recalculateTotals(Order order) {
        double itemsSubtotal = order.getLineItems().stream()
                .mapToDouble(li -> li.getLineSubtotal() != null ? li.getLineSubtotal() : 0.0)
                .sum();

        double lineDiscounts = order.getLineItems().stream()
                .mapToDouble(li -> li.getLineDiscount() != null ? li.getLineDiscount() : 0.0)
                .sum();

        double orderLevelDiscounts = order.getDiscounts().stream()
                .mapToDouble(d -> d.getAmount() != null ? d.getAmount()
                        : (d.getPercentage() != null ? (itemsSubtotal * d.getPercentage() / 100) : 0.0))
                .sum();

        double discountTotal = lineDiscounts + orderLevelDiscounts;
        double serviceChargeTotal = 0.0;
        double grandTotal = itemsSubtotal - discountTotal + serviceChargeTotal;
        double paidTotal = 0.0;
        double balanceDue = grandTotal - paidTotal;

        OrderTotals totals = order.getTotals();
        if (totals == null) {
            totals = new OrderTotals();
            totals.setOrder(order);
            order.setTotals(totals);
        }

        totals.setItemsSubtotal(itemsSubtotal);
        totals.setDiscountTotal(discountTotal);
        totals.setServiceChargeTotal(serviceChargeTotal);
        totals.setGrandTotal(grandTotal);
        totals.setPaidTotal(paidTotal);
        totals.setBalanceDue(balanceDue);

        orderRepository.save(order);
    }
}
