package training.aidd.library.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.aidd.library.book.BookCopy;
import training.aidd.library.book.BookCopyRepository;
import training.aidd.library.book.CopyStatus;
import training.aidd.library.member.Member;
import training.aidd.library.member.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock LoanRuleRepository loanRuleRepository;
    @Mock BookCopyRepository bookCopyRepository;
    @Mock MemberRepository memberRepository;

    @InjectMocks
    LoanService loanService;

    private Member activeMember;
    private BookCopy availableCopy;
    private LoanRule defaultRule;
    private Loan activeLoan;

    @BeforeEach
    void setUp() {
        activeMember = new Member();
        activeMember.setId(1L);
        activeMember.setName("佐藤 花子");
        activeMember.setActive(true);

        availableCopy = new BookCopy();
        availableCopy.setId(1L);
        availableCopy.setCopyCode("ISBN-001");
        availableCopy.setStatus(CopyStatus.AVAILABLE);

        defaultRule = new LoanRule();
        defaultRule.setId(1L);
        defaultRule.setMaxLoanCount(10);
        defaultRule.setLoanPeriodDays(14);

        activeLoan = new Loan();
        activeLoan.setId(1L);
        activeLoan.setMember(activeMember);
        activeLoan.setBookCopy(availableCopy);
        activeLoan.setLoanDate(LocalDate.now());
        activeLoan.setDueDate(LocalDate.now().plusDays(14));
    }

    // ─── checkout ──────────────────────────────────────────────────────────────

    @Test
    void checkout_validRequest_createsLoanAndSetsCheckedOut() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(availableCopy));
        when(loanRuleRepository.findAll()).thenReturn(List.of(defaultRule));
        when(loanRepository.countByMemberIdAndReturnedDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenReturn(activeLoan);

        LoanResponse result = loanService.checkout(new LoanRequest(1L, 1L));

        assertThat(result).isNotNull();
        verify(bookCopyRepository).save(any(BookCopy.class));
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void checkout_copyNotAvailable_throwsCopyNotAvailableException() {
        availableCopy.setStatus(CopyStatus.CHECKED_OUT);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(availableCopy));

        assertThatThrownBy(() -> loanService.checkout(new LoanRequest(1L, 1L)))
                .isInstanceOf(CopyNotAvailableException.class);
    }

    @Test
    void checkout_memberAtLoanLimit_throwsLoanLimitExceededException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(availableCopy));
        when(loanRuleRepository.findAll()).thenReturn(List.of(defaultRule));
        when(loanRepository.countByMemberIdAndReturnedDateIsNull(1L)).thenReturn(10L);

        assertThatThrownBy(() -> loanService.checkout(new LoanRequest(1L, 1L)))
                .isInstanceOf(LoanLimitExceededException.class);
    }

    @Test
    void checkout_inactiveMember_throwsMemberNotActiveException() {
        activeMember.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(availableCopy));

        assertThatThrownBy(() -> loanService.checkout(new LoanRequest(1L, 1L)))
                .isInstanceOf(MemberNotActiveException.class);
    }

    // ─── returnBook ────────────────────────────────────────────────────────────

    @Test
    void returnBook_activeLoan_setsReturnedDateAndAvailable() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(activeLoan);

        LoanResponse result = loanService.returnBook(1L);

        assertThat(result).isNotNull();
        verify(bookCopyRepository).save(any(BookCopy.class));
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void returnBook_alreadyReturned_throwsAlreadyReturnedException() {
        activeLoan.setReturnedDate(LocalDate.now().minusDays(1));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(activeLoan));

        assertThatThrownBy(() -> loanService.returnBook(1L))
                .isInstanceOf(AlreadyReturnedException.class);
    }

    @Test
    void returnBook_unknownLoan_throwsLoanNotFoundException() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(99L))
                .isInstanceOf(LoanNotFoundException.class);
    }

    // ─── findByMember ──────────────────────────────────────────────────────────

    @Test
    void findByMember_returnsLoansForMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(loanRepository.findByMemberId(1L)).thenReturn(List.of(activeLoan));

        List<LoanResponse> result = loanService.findByMember(1L);

        assertThat(result).hasSize(1);
    }
}
