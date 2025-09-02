package pos.pos.Service.Interfecaes;

import pos.pos.Entity.Order.Order;

public interface TotalsService {
    void recalculateTotals(Order order);
}
