package training.aidd.library.loan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(LoanController.class)
@WithMockUser
class LoanControllerTest {

    @Autowired MockMvcTester mockMvc;
    @MockitoBean LoanService loanService;

    private LoanResponse sampleResponse() {
        return new LoanResponse(1L, 1L, "佐藤 花子", 1L, "ISBN-001", "吾輩は猫である",
                LocalDate.now(), LocalDate.now().plusDays(14), null);
    }

    // ─── POST /api/loans → 201 ────────────────────────────────────────────────

    @Test
    void checkout_validRequest_returns201() {
        when(loanService.checkout(any(LoanRequest.class))).thenReturn(sampleResponse());

        assertThat(mockMvc.post().uri("/api/loans")
                .contentType("application/json")
                .content("""
                        { "memberId": 1, "bookCopyId": 1 }
                        """))
                .hasStatus(201);
    }

    @Test
    void checkout_copyNotAvailable_returns409() {
        when(loanService.checkout(any(LoanRequest.class)))
                .thenThrow(new CopyNotAvailableException(1L));

        assertThat(mockMvc.post().uri("/api/loans")
                .contentType("application/json")
                .content("""
                        { "memberId": 1, "bookCopyId": 1 }
                        """))
                .hasStatus(409);
    }

    @Test
    void checkout_loanLimitExceeded_returns400() {
        when(loanService.checkout(any(LoanRequest.class)))
                .thenThrow(new LoanLimitExceededException(1L, 10));

        assertThat(mockMvc.post().uri("/api/loans")
                .contentType("application/json")
                .content("""
                        { "memberId": 1, "bookCopyId": 1 }
                        """))
                .hasStatus(400);
    }

    // ─── POST /api/loans/{id}/return → 200 ───────────────────────────────────

    @Test
    void returnBook_activeLoan_returns200() {
        LoanResponse returned = new LoanResponse(1L, 1L, "佐藤 花子", 1L, "ISBN-001",
                "吾輩は猫である", LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now());
        when(loanService.returnBook(1L)).thenReturn(returned);

        assertThat(mockMvc.post().uri("/api/loans/1/return"))
                .hasStatus(200);
    }

    @Test
    void returnBook_alreadyReturned_returns409() {
        when(loanService.returnBook(1L)).thenThrow(new AlreadyReturnedException(1L));

        assertThat(mockMvc.post().uri("/api/loans/1/return"))
                .hasStatus(409);
    }

    @Test
    void returnBook_unknownLoan_returns404() {
        when(loanService.returnBook(99L)).thenThrow(new LoanNotFoundException(99L));

        assertThat(mockMvc.post().uri("/api/loans/99/return"))
                .hasStatus(404);
    }

    // ─── GET /api/loans?memberId=X → 200 ─────────────────────────────────────

    @Test
    void findByMember_returns200() {
        when(loanService.findByMember(1L)).thenReturn(List.of(sampleResponse()));

        assertThat(mockMvc.get().uri("/api/loans").param("memberId", "1"))
                .hasStatus(200);
    }
}
