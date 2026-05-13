package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.application.service.DiemCongXetTuyenService;
import com.example.admissions_management.application.service.ExcelService;
import com.example.admissions_management.domain.model.DiemCongXetTuyen;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/diem-cong")
public class DiemCongXetTuyenController {

    private final DiemCongXetTuyenService diemCongService;
    private final ExcelService excelService;

    public DiemCongXetTuyenController(DiemCongXetTuyenService diemCongService, ExcelService excelService) {
        this.diemCongService = diemCongService;
        this.excelService = excelService;
    }

    /**
     * Trang quản lý điểm cộng
     */
    @GetMapping
    public String index() {
        return "admin/diem-cong-list";
    }

    /**
     * API: Lấy tất cả điểm cộng
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<DiemCongXetTuyen>> getAll() {
        return ResponseEntity.ok(diemCongService.getAll());
    }

    /**
     * API: Lấy điểm cộng theo CCCD
     */
    @GetMapping("/api/by-cccd/{cccd}")
    @ResponseBody
    public ResponseEntity<List<DiemCongXetTuyen>> getByTsCccd(@PathVariable String cccd) {
        return ResponseEntity.ok(diemCongService.findByTsCccd(cccd));
    }

    /**
     * API: Lấy điểm cộng theo ngành
     */
    @GetMapping("/api/by-nganh/{maNganh}")
    @ResponseBody
    public ResponseEntity<List<DiemCongXetTuyen>> getByMaNganh(@PathVariable String maNganh) {
        return ResponseEntity.ok(diemCongService.findByMaNganh(maNganh));
    }

    /**
     * API: Upload file Excel import
     */
    @PostMapping("/api/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importDiemCong(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        File tempFile = null;
        
        try {
            tempFile = File.createTempFile("diem-cong-upload-", ".xlsx");
            file.transferTo(tempFile);
            try {
                int imported = excelService.importDiemCongFromFileBulk(tempFile);
                response.put("success", true);
                response.put("message", "Import nhanh thành công: " + imported + " bản ghi");
                response.put("importedCount", imported);
                response.put("mode", "bulk");
                return ResponseEntity.ok(response);
            } catch (Exception bulkEx) {
                DiemCongImportSummary summary = excelService.importDiemCongFromFileBatch(tempFile, 1000);
                response.put("success", true);
                response.put("message", "Import thành công theo batch: mới " + summary.getNewCount() + ", cập nhật " + summary.getUpdatedCount() + ", bỏ qua " + summary.getSkippedCount());
                response.put("totalRows", summary.getTotalRows());
                response.put("newCount", summary.getNewCount());
                response.put("updatedCount", summary.getUpdatedCount());
                response.put("skippedCount", summary.getSkippedCount());
                response.put("mode", "batch");
                response.put("bulkError", bulkEx.getMessage());
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi import: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * API: Download template Excel
     */
    @GetMapping("/api/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] excelFile = excelService.generateDiemCongTemplate();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           ContentDisposition.attachment()
                               .filename("diem-cong-template.xlsx")
                               .build()
                               .toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
                    
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * API: Xóa điểm cộng
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            diemCongService.delete(id);
            response.put("success", "true");
            response.put("message", "Xóa thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "Lỗi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
