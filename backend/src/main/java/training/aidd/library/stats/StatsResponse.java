package training.aidd.library.stats;

import java.util.List;

public record StatsResponse(
        long totalBooks,
        long totalMembers,
        long activeLoans,
        long overdueLoans,
        List<MonthlyLoans> loansByMonth,
        List<GenreStat> topGenres
) {
    public record MonthlyLoans(String month, long count) {}
    public record GenreStat(String genre, long count) {}
}
