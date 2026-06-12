package training.aidd.library.staff;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> findAll() {
        return staffRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        return toResponse(staffRepository.findById(id)
                .orElseThrow(() -> new StaffNotFoundException(id)));
    }

    public StaffResponse create(StaffRequest request) {
        Staff staff = new Staff();
        applyRequest(staff, request);
        return toResponse(staffRepository.save(staff));
    }

    public StaffResponse update(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new StaffNotFoundException(id));
        applyRequest(staff, request);
        return toResponse(staffRepository.save(staff));
    }

    public void deactivate(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new StaffNotFoundException(id));
        staff.setActive(false);
        staffRepository.save(staff);
    }

    private void applyRequest(Staff staff, StaffRequest request) {
        staff.setStaffNumber(request.staffNumber());
        staff.setName(request.name());
        staff.setEmail(request.email());
        staff.setRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            staff.setPasswordHash(passwordEncoder.encode(request.password()));
        }
    }

    private StaffResponse toResponse(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getStaffNumber(),
                staff.getName(),
                staff.getEmail(),
                staff.getRole(),
                staff.getActive()
        );
    }
}
