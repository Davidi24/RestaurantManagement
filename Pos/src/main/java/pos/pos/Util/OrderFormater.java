package pos.pos.Util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class OrderFormater {

    public String formatOrderNumber(Long tableId, LocalDate date, long seq) {
        String d = date.toString().replace("-", ""); // yyyyMMdd
        return "T" + tableId + "-" + d + "-" + String.format("%03d", seq);
    }

    public String sanitizeNotesOrderLine(String notes) {
        if (notes == null) return null;
        String n = notes.trim();
        return n.isEmpty() ? null : n.length() > 512 ? n.substring(0,512) : n;
    }

}
