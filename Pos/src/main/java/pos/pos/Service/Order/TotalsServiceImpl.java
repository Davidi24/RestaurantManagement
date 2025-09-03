package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.Entity.Order.*;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.TotalsService;

@Service
@RequiredArgsConstructor
@Transactional
public class TotalsServiceImpl implements TotalsService {

    private final OrderRepository orderRepository;

    @Override
    public void recalculateTotals(Order order) {
        double itemsSubtotal = order.getLineItems().stream()
                .mapToDouble(li -> li.getUnitPrice() * li.getQuantity())
                .sum();

        double discountTotal = order.getDiscounts().stream()
                .mapToDouble(d -> d.getAmount() != null ? d.getAmount()
                        : (d.getPercentage() != null ? (itemsSubtotal * d.getPercentage() / 100) : 0))
                .sum();

        double serviceChargeTotal = 0.0; // extend later if you add service charges

        double grandTotal = itemsSubtotal - discountTotal + serviceChargeTotal;

        double paidTotal = order.getTotals() != null ? order.getTotals().getPaidTotal() : 0.0;
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
