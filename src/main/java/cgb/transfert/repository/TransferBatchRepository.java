package cgb.transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cgb.transfert.model.TransferBatch;
import java.util.Optional;

@Repository
public interface TransferBatchRepository extends JpaRepository<TransferBatch, Long> {
    Optional<TransferBatch> findByBatchReference(String batchReference);
    boolean existsByBatchReference(String batchReference);
} 