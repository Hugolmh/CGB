package cgb.transfert.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgb.transfert.dto.TransferRequest;
import cgb.transfert.dto.TransferResponse;
import cgb.transfert.model.Transfer;
import cgb.transfert.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<?> createTransfer(@RequestBody TransferRequest transferRequest) {
        try {
            Transfer transfer = transferService.createTransfer(
                    transferRequest.getSourceAccountNumber(),
                    transferRequest.getDestinationAccountNumber(),
                    transferRequest.getAmount(),
                    transferRequest.getTransferDate(),
                    transferRequest.getDescription()
            );
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            TransferResponse errorResponse = new TransferResponse("FAILURE", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 