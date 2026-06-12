package training.aidd.library.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.audit.Audit;

import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        return toResponse(member);
    }

    @Audit(action = "MEMBER_CREATED", targetType = "Member")
    public MemberResponse create(MemberRequest request) {
        Member member = new Member();
        applyRequest(member, request);
        Member savedMember = memberRepository.save(member);
        return toResponse(savedMember);
    }

    @Audit(action = "MEMBER_UPDATED", targetType = "Member")
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        applyRequest(member, request);
        Member savedMember = memberRepository.save(member);
        return toResponse(savedMember);
    }

    // 退会処理（論理削除）
    // 貸出中チェックは US-005（Loan）実装後に追加予定
    public void deactivate(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        member.setActive(false);
        memberRepository.save(member);
    }

    private void applyRequest(Member member, MemberRequest request) {
        member.setMemberNumber(request.memberNumber());
        member.setName(request.name());
        member.setEmail(request.email());
        member.setPhone(request.phone());
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getMemberNumber(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getActive()
        );
    }
}
