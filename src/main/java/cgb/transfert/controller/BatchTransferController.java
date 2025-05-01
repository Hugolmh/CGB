package cgb.transfert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgb.transfert.dto.BatchResponse;
import cgb.transfert.dto.BatchTransferRequest;
import cgb.transfert.dto.TransferResponse;
import cgb.transfert.model.BatchTransfer;
import cgb.transfert.model.TransferBatch;
import cgb.transfert.service.BatchTransferService;

/**
 * Contrôleur pour les endpoints de traitement des lots de virements
 */
@RestController
@RequestMapping("/api/batch-transfers")
public class BatchTransferController {

    @Autowired
    private BatchTransferService batchTransferService;

    /**
     * Endpoint pour créer un nouveau lot de virements
     * @param request La requête contenant les informations du lot
     * @return Une réponse contenant l'ID du lot et son statut
     */
    @PostMapping
    public ResponseEntity<?> createBatch(@RequestBody BatchTransferRequest request) {
        try {
            BatchResponse response = batchTransferService.createBatch(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour récupérer les informations d'un lot
     * @param batchId L'ID du lot
     * @return Le lot
     */
    @GetMapping("/{batchId}")
    public ResponseEntity<?> getBatch(@PathVariable Long batchId) {
        try {
            TransferBatch batch = batchTransferService.getBatchById(batchId);
            return ResponseEntity.ok(batch);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rejouer les transferts en échec d'un lot
     * @param batchId L'ID du lot
     * @return Le nombre de transferts rejoués
     */
    @PostMapping("/{batchId}/replay")
    public ResponseEntity<?> replayFailedTransfers(@PathVariable Long batchId) {
        try {
            int replayedCount = batchTransferService.replayFailedTransfers(batchId);
            return ResponseEntity.ok(new TransferResponse("SUCCESS", "Replayed " + replayedCount + " failed transfers"));
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour annuler un transfert
     * @param transferId L'ID du transfert
     * @return Le transfert annulé
     */
    @PostMapping("/transfer/{transferId}/cancel")
    public ResponseEntity<?> cancelTransfer(@PathVariable Long transferId) {
        try {
            BatchTransfer transfer = batchTransferService.cancelTransfer(transferId);
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint pour récupérer les informations d'un transfert
     * @param transferId L'ID du transfert
     * @return Le transfert
     */
    @GetMapping("/transfer/{transferId}")
    public ResponseEntity<?> getTransfer(@PathVariable Long transferId) {
        try {
            BatchTransfer transfer = batchTransferService.getTransferById(transferId);
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 