package pos.pos.Service.Interfecaes.Order;

import pos.pos.Entity.Order.Order;

public interface TotalsService {
    void recalculateTotals(Order order);
}
