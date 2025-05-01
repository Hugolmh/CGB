package cgb.transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import cgb.transfert.model.BatchTransfer;
import cgb.transfert.model.BatchTransfer.TransferStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BatchTransferRepository extends JpaRepository<BatchTransfer, Long> {
    List<BatchTransfer> findByBatchId(Long batchId);
    List<BatchTransfer> findByStatus(TransferStatus status);
    List<BatchTransfer> findByBatchIdAndStatus(Long batchId, TransferStatus status);
    
    @Query("SELECT bt FROM BatchTransfer bt WHERE bt.status = :status AND bt.transferDate BETWEEN :startDate AND :endDate")
    List<BatchTransfer> findByStatusAndDateRange(
        @Param("status") TransferStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT bt FROM BatchTransfer bt WHERE bt.status = :status AND bt.destinationIban = :iban")
    List<BatchTransfer> findByStatusAndDestinationIban(
        @Param("status") TransferStatus status,
        @Param("iban") String iban
    );
} 