package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.service.ExcelService;
import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/nguyen-vong")
public class NguyenVongXetTuyenController {

    private final NguyenVongXetTuyenService nguyenVongService;
    private final ExcelService excelService;

    public NguyenVongXetTuyenController(NguyenVongXetTuyenService nguyenVongService, ExcelService excelService) {
        this.nguyenVongService = nguyenVongService;
        this.excelService = excelService;
    }

    /**
     * Trang quản lý nguyện vọng
     */
    @GetMapping
    public String index() {
        return "admin/nguyen-vong-list";
    }

    /**
     * API: Lấy tất cả nguyện vọng
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<NguyenVongXetTuyen>> getAll() {
        return ResponseEntity.ok(nguyenVongService.getAll());
    }

    /**
     * API: Lấy nguyện vọng theo CCCD
     */
    @GetMapping("/api/by-cccd/{cccd}")
    @ResponseBody
    public ResponseEntity<List<NguyenVongXetTuyen>> getByNnCccd(@PathVariable String cccd) {
        return ResponseEntity.ok(nguyenVongService.getByNnCccd(cccd));
    }

    /**
     * API: Lấy kết quả xét tuyển theo mã ngành
     */
    @GetMapping("/api/by-nganh/{maNganh}")
    @ResponseBody
    public ResponseEntity<List<NguyenVongXetTuyen>> getByMaNganh(@PathVariable String maNganh) {
        return ResponseEntity.ok(nguyenVongService.getResultByMaNganh(maNganh));
    }

    /**
     * API: Lấy danh sách đậu theo ngành
     */
    @GetMapping("/api/passed/{maNganh}")
    @ResponseBody
    public ResponseEntity<List<NguyenVongXetTuyen>> getPassedApplicants(@PathVariable String maNganh) {
        return ResponseEntity.ok(nguyenVongService.getPassedApplicants(maNganh));
    }

    /**
     * API: Lấy danh sách trượt theo ngành
     */
    @GetMapping("/api/failed/{maNganh}")
    @ResponseBody
    public ResponseEntity<List<NguyenVongXetTuyen>> getFailedApplicants(@PathVariable String maNganh) {
        return ResponseEntity.ok(nguyenVongService.getFailedApplicants(maNganh));
    }

    /**
     * API: Chạy xét tuyển cho một ngành
     */
    @PostMapping("/api/process-selection/{maNganh}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processSelectionForMajor(@PathVariable String maNganh) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = nguyenVongService.performSelectionForMajor(maNganh);
            response.put("success", true);
            response.put("message", "Xét tuyển ngành " + maNganh + " thành công");
            response.putAll(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Chạy xét tuyển cho tất cả ngành
     */
    @PostMapping("/api/process-selection-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processSelectionForAllMajors() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> results = nguyenVongService.performSelectionForAllMajors();
            response.put("success", true);
            response.put("message", "Xét tuyển tất cả ngành thành công");
            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API: Xuất kết quả xét tuyển ra Excel
     */
    @GetMapping("/api/export")
    public ResponseEntity<byte[]> exportResults(
            @RequestParam(value = "maNganh", required = false) String maNganh) {
        try {
            List<NguyenVongXetTuyen> data;
            
            if (maNganh != null && !maNganh.isEmpty()) {
                data = nguyenVongService.getResultByMaNganh(maNganh);
            } else {
                data = nguyenVongService.getAll();
            }
            
            byte[] excelFile = excelService.exportNguyenVong(data);
            
            String filename = maNganh != null ? 
                    "ket-qua-" + maNganh + ".xlsx" : 
                    "ket-qua-xet-tuyen.xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                           ContentDisposition.attachment()
                               .filename(filename)
                               .build()
                               .toString())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);
                    
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * API: Xóa nguyện vọng
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteNguyenVong(@PathVariable Integer id) {
        Map<String, String> response = new HashMap<>();
        try {
            nguyenVongService.deleteNguyenVong(id);
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
