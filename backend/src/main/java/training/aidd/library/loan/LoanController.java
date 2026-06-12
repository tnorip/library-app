package training.aidd.library.loan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse checkout(@RequestBody LoanRequest request) {
        return loanService.checkout(request);
    }

    @PostMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Long id) {
        return loanService.returnBook(id);
    }

    @GetMapping
    public List<LoanResponse> findByMember(@RequestParam Long memberId) {
        return loanService.findByMember(memberId);
    }
}
