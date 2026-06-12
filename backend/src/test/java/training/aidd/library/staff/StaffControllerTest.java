package training.aidd.library.staff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(StaffController.class)
@WithMockUser
class StaffControllerTest {

    @Autowired MockMvcTester mockMvc;
    @MockitoBean StaffService staffService;

    private StaffResponse sample() {
        return new StaffResponse(1L, "S-001", "田中 主任", "tanaka@library.jp", StaffRole.CHIEF, true);
    }

    @Test
    void listStaff_returns200() {
        when(staffService.findAll()).thenReturn(List.of(sample()));
        assertThat(mockMvc.get().uri("/api/staff")).hasStatus(200);
    }

    @Test
    void getStaff_existingId_returns200() {
        when(staffService.findById(1L)).thenReturn(sample());
        assertThat(mockMvc.get().uri("/api/staff/1")).hasStatus(200);
    }

    @Test
    void getStaff_unknownId_returns404() {
        when(staffService.findById(99L)).thenThrow(new StaffNotFoundException(99L));
        assertThat(mockMvc.get().uri("/api/staff/99")).hasStatus(404);
    }

    @Test
    void createStaff_returns201() {
        when(staffService.create(any(StaffRequest.class))).thenReturn(sample());
        assertThat(mockMvc.post().uri("/api/staff")
                .contentType("application/json")
                .content("""
                        {
                          "staffNumber": "S-002",
                          "name": "新職員",
                          "email": "new@lib.jp",
                          "password": "pass",
                          "role": "GENERAL"
                        }
                        """))
                .hasStatus(201);
    }

    @Test
    void deactivateStaff_returns204() {
        assertThat(mockMvc.delete().uri("/api/staff/1")).hasStatus(204);
    }

    @Test
    void deactivateStaff_unknownId_returns404() {
        doThrow(new StaffNotFoundException(99L)).when(staffService).deactivate(99L);
        assertThat(mockMvc.delete().uri("/api/staff/99")).hasStatus(404);
    }
}
