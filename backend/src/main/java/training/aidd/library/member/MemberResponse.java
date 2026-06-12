package training.aidd.library.member;

public record MemberResponse(
        Long id,
        String memberNumber,
        String name,
        String email,
        String phone,
        Boolean active
) {}
