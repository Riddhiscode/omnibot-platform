package com.omnibot.controller;

import com.omnibot.service.ExportService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@ResponseBody
@RequestMapping("/v1/export")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080"})
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * GET /api/v1/export/excel
     * Returns a downloadable .xlsx expense report for the current user.
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(defaultValue = "1") Long userId) throws IOException {

        byte[] data = exportService.generateExcel(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("omnibot_report.xlsx").build());
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /**
     * GET /api/v1/export/pdf
     * Returns a downloadable .pdf expense report for the current user.
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(defaultValue = "1") Long userId) throws IOException {

        byte[] data = exportService.generatePdf(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("omnibot_report.pdf").build());
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
