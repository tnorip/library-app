package training.aidd.library.reservation;

public enum ReservationStatus {
    WAITING,    // 予約待ち
    READY,      // 貸出可能（返却通知済み）
    CANCELLED,  // キャンセル済み
    FULFILLED   // 貸出済み（完了）
}
