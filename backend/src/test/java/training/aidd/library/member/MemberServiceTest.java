package training.aidd.library.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    private Member sampleMember;

    @BeforeEach
    void setUp() {
        sampleMember = new Member();
        sampleMember.setId(1L);
        sampleMember.setMemberNumber("M-00001");
        sampleMember.setName("佐藤 花子");
        sampleMember.setEmail("sato@example.com");
        sampleMember.setPhone("090-1234-5678");
        sampleMember.setActive(true);
    }

    // ─── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsActiveMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(sampleMember));

        List<MemberResponse> result = memberService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("佐藤 花子");
    }

    // ─── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        MemberResponse result = memberService.findById(1L);

        assertThat(result.memberNumber()).isEqualTo("M-00001");
    }

    @Test
    void findById_unknownId_throwsMemberNotFoundException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    // ─── create ────────────────────────────────────────────────────────────────

    @Test
    void create_validRequest_savesAndReturnsResponse() {
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        MemberRequest request = new MemberRequest("M-00001", "佐藤 花子", "sato@example.com", "090-1234-5678");

        MemberResponse result = memberService.create(request);

        assertThat(result.name()).isEqualTo("佐藤 花子");
        verify(memberRepository).save(any(Member.class));
    }

    // ─── update ────────────────────────────────────────────────────────────────

    @Test
    void update_existingId_updatesAndReturnsResponse() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        MemberRequest request = new MemberRequest("M-00001", "佐藤 花子（更新）", "new@example.com", "090-0000-0000");

        MemberResponse result = memberService.update(1L, request);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void update_unknownId_throwsMemberNotFoundException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        MemberRequest request = new MemberRequest("M-00099", "氏名", "a@b.com", "000");

        assertThatThrownBy(() -> memberService.update(99L, request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    // ─── deactivate ────────────────────────────────────────────────────────────

    @Test
    void deactivate_noActiveLoans_deactivatesSuccessfully() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        memberService.deactivate(1L);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void deactivate_unknownId_throwsMemberNotFoundException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.deactivate(99L))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
