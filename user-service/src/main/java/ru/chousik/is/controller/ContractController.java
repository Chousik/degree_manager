package ru.chousik.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.is.dto.contract.ContractResponse;
import ru.chousik.is.services.ContractService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/rentals/{rentalId}")
    public ContractResponse getContract(@PathVariable UUID rentalId, @RequestParam UUID userId) {
        return contractService.getContractResponse(rentalId, userId);
    }

    @GetMapping("/rentals/{rentalId}/file")
    public ResponseEntity<byte[]> downloadContract(@PathVariable UUID rentalId, @RequestParam UUID userId) {
        String content = contractService.renderContractText(rentalId, userId);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract-" + rentalId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
