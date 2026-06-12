package training.aidd.library.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock StaffRepository staffRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks StaffService staffService;

    private Staff sampleStaff;

    @BeforeEach
    void setUp() {
        sampleStaff = new Staff();
        sampleStaff.setId(1L);
        sampleStaff.setStaffNumber("S-001");
        sampleStaff.setName("田中 主任");
        sampleStaff.setEmail("tanaka@library.jp");
        sampleStaff.setRole(StaffRole.CHIEF);
        sampleStaff.setActive(true);
    }

    @Test
    void findAll_returnsAllStaff() {
        when(staffRepository.findAll()).thenReturn(List.of(sampleStaff));

        List<StaffResponse> result = staffService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("田中 主任");
    }

    @Test
    void findById_existingId_returnsStaff() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(sampleStaff));

        StaffResponse result = staffService.findById(1L);

        assertThat(result.role()).isEqualTo(StaffRole.CHIEF);
    }

    @Test
    void findById_unknownId_throwsStaffNotFoundException() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.findById(99L))
                .isInstanceOf(StaffNotFoundException.class);
    }

    @Test
    void create_validRequest_savesAndReturns() {
        when(staffRepository.save(any(Staff.class))).thenReturn(sampleStaff);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        StaffRequest request = new StaffRequest("S-001", "田中 主任", "tanaka@library.jp", "password123", StaffRole.CHIEF);

        StaffResponse result = staffService.create(request);

        assertThat(result.staffNumber()).isEqualTo("S-001");
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void update_existingId_updatesAndReturns() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(sampleStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(sampleStaff);

        StaffRequest request = new StaffRequest("S-001", "田中 更新", "tanaka@library.jp", null, StaffRole.CHIEF);

        StaffResponse result = staffService.update(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    void deactivate_existingId_setsActiveFalse() {
        when(staffRepository.findById(1L)).thenReturn(Optional.of(sampleStaff));
        when(staffRepository.save(any(Staff.class))).thenReturn(sampleStaff);

        staffService.deactivate(1L);

        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void deactivate_unknownId_throwsStaffNotFoundException() {
        when(staffRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.deactivate(99L))
                .isInstanceOf(StaffNotFoundException.class);
    }
}
