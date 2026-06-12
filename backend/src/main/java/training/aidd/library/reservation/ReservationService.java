package training.aidd.library.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.audit.Audit;
import training.aidd.library.book.Book;
import training.aidd.library.book.BookNotFoundException;
import training.aidd.library.book.BookRepository;
import training.aidd.library.member.Member;
import training.aidd.library.member.MemberNotFoundException;
import training.aidd.library.member.MemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Audit(action = "RESERVATION_CREATED", targetType = "Reservation")
    public ReservationResponse reserve(ReservationRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException(request.memberId()));

        if (reservationRepository.existsByBookIdAndMemberIdAndStatus(
                request.bookId(), request.memberId(), ReservationStatus.WAITING)) {
            throw new AlreadyReservedException(request.bookId(), request.memberId());
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setMember(member);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.WAITING);

        return toResponse(reservationRepository.save(reservation));
    }

    public void cancel(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new ReservationNotFoundException(reservationId);
        }
        if (reservation.getStatus() != ReservationStatus.WAITING
                && reservation.getStatus() != ReservationStatus.READY) {
            throw new ReservationNotFoundException(reservationId);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return reservationRepository.findByMemberIdOrderByReservedAtDesc(memberId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByBook(Long bookId) {
        return reservationRepository
                .findByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING)
                .stream().map(this::toResponse).toList();
    }

    /** 返却時に呼び出し、次の予約者を READY に昇格する */
    public Optional<ReservationResponse> notifyNextReservation(Long bookId) {
        return reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING)
                .map(r -> {
                    r.setStatus(ReservationStatus.READY);
                    return toResponse(reservationRepository.save(r));
                });
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getBook().getId(),
                r.getBook().getTitle(),
                r.getMember().getId(),
                r.getMember().getName(),
                r.getReservedAt(),
                r.getStatus()
        );
    }
}
