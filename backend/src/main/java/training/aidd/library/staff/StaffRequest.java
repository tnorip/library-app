package training.aidd.library.staff;

public record StaffRequest(
        String staffNumber,
        String name,
        String email,
        String password,
        StaffRole role
) {}
