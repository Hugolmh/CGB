package cgb.transfert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgb.transfert.dto.BatchReport;
import cgb.transfert.dto.TransferResponse;
import cgb.transfert.dto.TransferSearchCriteria;
import cgb.transfert.model.BatchTransfer;
import cgb.transfert.service.ReportService;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour les endpoints de rapport et de recherche
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Endpoint pour générer un rapport détaillé pour un lot
     * @param batchId L'ID du lot
     * @return Le rapport détaillé
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<?> generateBatchReport(@PathVariable Long batchId) {
        try {
            BatchReport report = reportService.generateBatchReport(batchId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rechercher les transferts échoués par ID de lot
     * @param batchId L'ID du lot
     * @return La liste des transferts échoués
     */
    @GetMapping("/failed-transfers/batch/{batchId}")
    public ResponseEntity<?> getFailedTransfersByBatch(@PathVariable Long batchId) {
        try {
            TransferSearchCriteria criteria = new TransferSearchCriteria();
            criteria.setBatchId(batchId);
            List<BatchTransfer> failedTransfers = reportService.searchFailedTransfers(criteria);
            return ResponseEntity.ok(failedTransfers);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rechercher les transferts échoués par période
     * @param startDate La date de début au format ISO (YYYY-MM-DD)
     * @param endDate La date de fin au format ISO (YYYY-MM-DD)
     * @return La liste des transferts échoués
     */
    @GetMapping("/failed-transfers/date-range")
    public ResponseEntity<?> getFailedTransfersByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            // Vérifier que les dates sont au bon format
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // Vérifier que la date de début est avant la date de fin
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            
            // Limiter la plage de dates à 31 jours maximum pour éviter les requêtes trop volumineuses
            if (start.plusDays(31).isBefore(end)) {
                throw new IllegalArgumentException("La plage de dates ne doit pas dépasser 31 jours");
            }
            
            TransferSearchCriteria criteria = new TransferSearchCriteria();
            criteria.setStartDate(start);
            criteria.setEndDate(end);
            List<BatchTransfer> failedTransfers = reportService.searchFailedTransfers(criteria);
            return ResponseEntity.ok(failedTransfers);
        } catch (IllegalArgumentException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rechercher les transferts échoués par compte destinataire
     * @param iban L'IBAN du compte destinataire
     * @return La liste des transferts échoués
     */
    @GetMapping("/failed-transfers/destination/{iban}")
    public ResponseEntity<?> getFailedTransfersByDestination(@PathVariable String iban) {
        try {
            TransferSearchCriteria criteria = new TransferSearchCriteria();
            criteria.setDestinationIban(iban);
            List<BatchTransfer> failedTransfers = reportService.searchFailedTransfers(criteria);
            return ResponseEntity.ok(failedTransfers);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rechercher les transferts échoués avec des critères combinés
     * @param criteria Les critères de recherche
     * @return La liste des transferts échoués
     */
    @PostMapping("/failed-transfers/search")
    public ResponseEntity<?> searchFailedTransfers(@RequestBody TransferSearchCriteria criteria) {
        try {
            List<BatchTransfer> failedTransfers = reportService.searchFailedTransfers(criteria);
            return ResponseEntity.ok(failedTransfers);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 