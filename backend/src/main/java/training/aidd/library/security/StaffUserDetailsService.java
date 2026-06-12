package training.aidd.library.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import training.aidd.library.staff.StaffRepository;

import java.util.List;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    public StaffUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return staffRepository.findByEmailAndActiveTrue(email)
                .map(staff -> new User(
                        staff.getEmail(),
                        staff.getPasswordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found: " + email));
    }
}
