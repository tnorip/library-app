package training.aidd.library.loan;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/overdue")
public class OverdueController {

    private final OverdueService overdueService;

    public OverdueController(OverdueService overdueService) {
        this.overdueService = overdueService;
    }

    @GetMapping
    public List<OverdueResponse> listOverdue() {
        return overdueService.findOverdue();
    }

    @PostMapping("/notify")
    public List<OverdueResponse> sendReminders(Authentication auth) {
        String operator = auth != null ? auth.getName() : "system";
        return overdueService.sendReminders(operator);
    }
}
