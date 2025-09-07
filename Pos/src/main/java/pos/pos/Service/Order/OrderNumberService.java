package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.Entity.Order.OrderNumberCounter;
import pos.pos.Repository.Order.OrderNumberCounterRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class OrderNumberService {

    private final OrderNumberCounterRepository repo;

    @Transactional
    public long nextFor(LocalDate date, Long tableId) {
        var existing = repo.findForUpdate(date, tableId)
                .orElseGet(() -> repo.save(
                        OrderNumberCounter.builder()
                                .date(date)
                                .tableId(tableId)
                                .value(0L)
                                .build()
                ));
        long next = existing.getValue() + 1;
        existing.setValue(next);
        // save not strictly necessary; dirty check will flush on commit
        return next;
    }
}
