package training.aidd.library.staff;

public record StaffResponse(
        Long id,
        String staffNumber,
        String name,
        String email,
        StaffRole role,
        Boolean active
) {}
