package training.aidd.library.loan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.book.BookCopy;
import training.aidd.library.book.BookCopyRepository;
import training.aidd.library.book.BookNotFoundException;
import training.aidd.library.book.CopyStatus;
import training.aidd.library.member.Member;
import training.aidd.library.member.MemberNotFoundException;
import training.aidd.library.member.MemberRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanRuleRepository loanRuleRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository,
                       LoanRuleRepository loanRuleRepository,
                       BookCopyRepository bookCopyRepository,
                       MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.loanRuleRepository = loanRuleRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.memberRepository = memberRepository;
    }

    public LoanResponse checkout(LoanRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));
        BookCopy copy = bookCopyRepository.findById(request.bookCopyId())
                .orElseThrow(() -> new BookNotFoundException(request.bookCopyId()));

        validateMemberActive(member);
        validateCopyAvailable(copy);
        validateLoanLimit(member);

        LoanRule rule = loanRuleRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> { LoanRule r = new LoanRule(); return r; });

        LocalDate today = LocalDate.now();
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBookCopy(copy);
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(rule.getLoanPeriodDays()));

        copy.setStatus(CopyStatus.CHECKED_OUT);
        bookCopyRepository.save(copy);

        Loan savedLoan = loanRepository.save(loan);
        return toResponse(savedLoan);
    }

    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        if (loan.getReturnedDate() != null) {
            throw new AlreadyReturnedException(loanId);
        }

        loan.setReturnedDate(LocalDate.now());
        BookCopy copy = loan.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);

        bookCopyRepository.save(copy);
        Loan savedLoan = loanRepository.save(loan);
        return toResponse(savedLoan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findByMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return loanRepository.findByMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Business Logic ────────────────────────────────────────────────────────

    private void validateMemberActive(Member member) {
        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new MemberNotActiveException(member.getId());
        }
    }

    private void validateCopyAvailable(BookCopy copy) {
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new CopyNotAvailableException(copy.getId());
        }
    }

    private void validateLoanLimit(Member member) {
        LoanRule rule = loanRuleRepository.findAll().stream()
                .findFirst()
                .orElseGet(LoanRule::new);
        long currentCount = loanRepository.countByMemberIdAndReturnedDateIsNull(member.getId());
        if (currentCount >= rule.getMaxLoanCount()) {
            throw new LoanLimitExceededException(member.getId(), rule.getMaxLoanCount());
        }
    }

    // ─── Mapping ───────────────────────────────────────────────────────────────

    private LoanResponse toResponse(Loan loan) {
        BookCopy copy = loan.getBookCopy();
        Member member = loan.getMember();
        String bookTitle = (copy.getBook() != null) ? copy.getBook().getTitle() : "";
        return new LoanResponse(
                loan.getId(),
                member.getId(),
                member.getName(),
                copy.getId(),
                copy.getCopyCode(),
                bookTitle,
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnedDate()
        );
    }
}
