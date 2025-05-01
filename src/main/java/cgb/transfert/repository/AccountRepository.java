package cgb.transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cgb.transfert.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
} 