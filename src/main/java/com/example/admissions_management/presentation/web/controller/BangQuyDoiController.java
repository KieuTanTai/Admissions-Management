package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.BangQuyDoiService;
import com.example.admissions_management.application.service.ExcelService;
import com.example.admissions_management.domain.model.BangQuyDoi;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quydoi")
public class BangQuyDoiController {

    private final BangQuyDoiService bangQuyDoiService;
    private final ExcelService excelService;

    public BangQuyDoiController(BangQuyDoiService bangQuyDoiService, ExcelService excelService) {
        this.bangQuyDoiService = bangQuyDoiService;
        this.excelService = excelService;
    }

    @GetMapping("/danh-sach")
    public List<BangQuyDoi> layDanhSachQuyDoi() {
        return bangQuyDoiService.getAll();
    }

    @PutMapping("/sua")
    public ResponseEntity<String> suaLuatQuyDoi(@RequestBody BangQuyDoi luatSua) {
        bangQuyDoiService.update(luatSua);
        return ResponseEntity.ok("Đã cập nhật luật quy đổi thành công.");
    }

    @PostMapping("/them")
    public ResponseEntity<String> themLuatQuyDoi(@RequestBody BangQuyDoi luatMoi) {
        bangQuyDoiService.add(luatMoi);
        return ResponseEntity.ok("Đã thêm luật quy đổi thành công.");
    }

    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<String> xoaLuatQuyDoi(@PathVariable Integer id) {
        bangQuyDoiService.delete(id);
        return ResponseEntity.ok("Đã xóa luật quy đổi.");
    }

    @DeleteMapping("/xoa-tat-ca")
    public ResponseEntity<String> xoaTatCaLuatQuyDoi() {
        bangQuyDoiService.deleteAll();
        return ResponseEntity.ok("Đã xóa toàn bộ dữ liệu bảng quy đổi.");
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importBangQuyDoi(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("bang-quy-doi-upload-", ".xlsx");
            file.transferTo(tempFile);
            List<BangQuyDoi> items = excelService.importBangQuyDoiFromFile(tempFile);
            bangQuyDoiService.saveAll(items);
            result.put("success", true);
            result.put("message", "Đã import " + items.size() + " dòng dữ liệu bảng quy đổi.");
            return ResponseEntity.ok(result);
        } catch (Exception exception) {
            result.put("success", false);
            result.put("message", "Import thất bại: " + exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @GetMapping("/download-template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("BangQuyDoi");
            String[] headers = { "Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã quy đổi", "Phân vị" };
            var row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                row.createCell(i).setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bang-quy-doi-template.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tinh-diem")
    public ResponseEntity<?> tinhDiemQuyDoi(
            @RequestParam String phuongThuc,
            @RequestParam String toHopHoacMon,
            @RequestParam BigDecimal diemGoc) {

        Map<String, String> result = bangQuyDoiService.quyDoiDiemKhaoThi(phuongThuc, toHopHoacMon, diemGoc);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy luật quy đổi cho mức điểm này.");
    }
}
