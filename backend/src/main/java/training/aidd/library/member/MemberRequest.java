package training.aidd.library.member;

public record MemberRequest(
        String memberNumber,
        String name,
        String email,
        String phone
) {}
