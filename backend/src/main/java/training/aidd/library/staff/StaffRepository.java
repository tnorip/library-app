package training.aidd.library.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmailAndActiveTrue(String email);
}
