package training.aidd.library.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(MemberController.class)
@WithMockUser
class MemberControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    MemberService memberService;

    // ─── GET /api/members → 200 ───────────────────────────────────────────────

    @Test
    void listMembers_returns200() {
        when(memberService.findAll()).thenReturn(List.of());

        assertThat(mockMvc.get().uri("/api/members"))
                .hasStatus(200);
    }

    // ─── GET /api/members/{id} → 200 / 404 ───────────────────────────────────

    @Test
    void getMember_existingId_returns200() {
        MemberResponse response = new MemberResponse(1L, "M-00001", "佐藤 花子", "sato@example.com", "090-1234-5678", true);
        when(memberService.findById(1L)).thenReturn(response);

        assertThat(mockMvc.get().uri("/api/members/1"))
                .hasStatus(200);
    }

    @Test
    void getMember_unknownId_returns404() {
        when(memberService.findById(99L)).thenThrow(new MemberNotFoundException(99L));

        assertThat(mockMvc.get().uri("/api/members/99"))
                .hasStatus(404);
    }

    // ─── POST /api/members → 201 ──────────────────────────────────────────────

    @Test
    void createMember_validRequest_returns201() {
        MemberResponse response = new MemberResponse(1L, "M-00001", "佐藤 花子", "sato@example.com", "090-1234-5678", true);
        when(memberService.create(any(MemberRequest.class))).thenReturn(response);

        assertThat(mockMvc.post().uri("/api/members")
                .contentType("application/json")
                .content("""
                        {
                          "memberNumber": "M-00001",
                          "name": "佐藤 花子",
                          "email": "sato@example.com",
                          "phone": "090-1234-5678"
                        }
                        """))
                .hasStatus(201);
    }

    // ─── PUT /api/members/{id} → 200 / 404 ───────────────────────────────────

    @Test
    void updateMember_existingId_returns200() {
        MemberResponse response = new MemberResponse(1L, "M-00001", "更新名", "new@example.com", "000", true);
        when(memberService.update(eq(1L), any(MemberRequest.class))).thenReturn(response);

        assertThat(mockMvc.put().uri("/api/members/1")
                .contentType("application/json")
                .content("""
                        {
                          "memberNumber": "M-00001",
                          "name": "更新名",
                          "email": "new@example.com",
                          "phone": "000"
                        }
                        """))
                .hasStatus(200);
    }

    @Test
    void updateMember_unknownId_returns404() {
        when(memberService.update(eq(99L), any(MemberRequest.class)))
                .thenThrow(new MemberNotFoundException(99L));

        assertThat(mockMvc.put().uri("/api/members/99")
                .contentType("application/json")
                .content("""
                        {
                          "memberNumber": "M-00099",
                          "name": "氏名",
                          "email": "a@b.com",
                          "phone": "000"
                        }
                        """))
                .hasStatus(404);
    }

    // ─── DELETE /api/members/{id} → 204 ──────────────────────────────────────

    @Test
    void deactivateMember_returns204() {
        assertThat(mockMvc.delete().uri("/api/members/1"))
                .hasStatus(204);
    }

    @Test
    void deactivateMember_unknownId_returns404() {
        doThrow(new MemberNotFoundException(99L)).when(memberService).deactivate(99L);

        assertThat(mockMvc.delete().uri("/api/members/99"))
                .hasStatus(404);
    }
}
