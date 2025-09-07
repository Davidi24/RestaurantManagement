package pos.pos.Util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class OrderNumberFormatter {
    public String format(Long tableId, LocalDate date, long seq) {
        String d = date.toString().replace("-", ""); // yyyyMMdd
        return "T" + tableId + "-" + d + "-" + String.format("%03d", seq);
    }
}
