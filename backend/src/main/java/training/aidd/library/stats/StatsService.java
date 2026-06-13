package training.aidd.library.stats;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.book.BookRepository;
import training.aidd.library.loan.Loan;
import training.aidd.library.loan.LoanRepository;
import training.aidd.library.member.MemberRepository;
import training.aidd.library.stats.StatsResponse.GenreStat;
import training.aidd.library.stats.StatsResponse.MonthlyLoans;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Gatherer;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public StatsService(BookRepository bookRepository,
                        MemberRepository memberRepository,
                        LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    public StatsResponse getStats() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);

        List<Loan> recentLoans = loanRepository.findByLoanDateAfter(sixMonthsAgo);

        List<MonthlyLoans> loansByMonth = recentLoans.stream()
                .gather(groupByMonth())
                .toList();

        List<GenreStat> topGenres = bookRepository.findAllCallNumbers().stream()
                .map(cn -> cn == null || cn.isBlank()
                        ? "未分類"
                        : cn.substring(0, Math.min(3, cn.length())))
                .gather(countByKey())
                .gather(topN(5, Comparator.comparingLong(GenreStat::count)))
                .toList();

        return new StatsResponse(
                bookRepository.count(),
                memberRepository.countByActiveTrue(),
                loanRepository.countByReturnedDateIsNull(),
                loanRepository.countByReturnedDateIsNullAndDueDateBefore(LocalDate.now()),
                loansByMonth,
                topGenres
        );
    }

    // ─── Stream Gatherers ─────────────────────────────────────────────────────

    /** 貸出を月別にグループ化して MonthlyLoans のストリームに変換する */
    private static Gatherer<Loan, TreeMap<String, Long>, MonthlyLoans> groupByMonth() {
        return Gatherer.ofSequential(
                TreeMap::new,
                (map, loan, downstream) -> {
                    YearMonth ym = YearMonth.from(loan.getLoanDate());
                    map.merge(ym.toString(), 1L, Long::sum);
                    return true;
                },
                (map, downstream) -> map.forEach((month, count) ->
                        downstream.push(new MonthlyLoans(month, count)))
        );
    }

    /** 文字列ストリームを集計して GenreStat のストリームに変換する */
    private static Gatherer<String, TreeMap<String, Long>, GenreStat> countByKey() {
        return Gatherer.ofSequential(
                TreeMap::new,
                (map, key, downstream) -> {
                    map.merge(key, 1L, Long::sum);
                    return true;
                },
                (map, downstream) -> map.forEach((genre, count) ->
                        downstream.push(new GenreStat(genre, count)))
        );
    }

    /** ストリームから上位 N 件を取り出す汎用 Gatherer */
    private static <T> Gatherer<T, List<T>, T> topN(int n, Comparator<T> comparator) {
        return Gatherer.ofSequential(
                ArrayList::new,
                (list, item, downstream) -> { list.add(item); return true; },
                (list, downstream) -> list.stream()
                        .sorted(comparator.reversed())
                        .limit(n)
                        .forEach(downstream::push)
        );
    }
}
