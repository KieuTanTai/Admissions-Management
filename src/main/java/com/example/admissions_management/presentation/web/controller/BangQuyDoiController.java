package com.example.admissions_management.presentation.web.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.admissions_management.application.service.BangQuyDoiService;
import com.example.admissions_management.domain.model.BangQuyDoi;

@RestController
@RequestMapping("/api/quydoi")
public class BangQuyDoiController {

    @Autowired
    private BangQuyDoiService bangQuyDoiService; // Chứa logic tìm kiếm điểm mà ta đã viết

    // Lấy toàn bộ danh sách các luật quy đổi hiện có
    @GetMapping("/danh-sach")
    public List<BangQuyDoi> layDanhSachQuyDoi() 
    {
        return bangQuyDoiService.getAll();
    }

    @PutMapping("/sua")
    public ResponseEntity<String> suaLuaQuyDoi(@RequestBody BangQuyDoi luatSua)
    {
        bangQuyDoiService.update(luatSua);
        return ResponseEntity.ok("Đã sửa luật quy đổi thành công!");
    }

    // Thêm luật quy đổi 
    @PostMapping("/them")
    public ResponseEntity<String> themLuatQuyDoi(@RequestBody BangQuyDoi luatMoi) {
        bangQuyDoiService.add(luatMoi);
        return ResponseEntity.ok("Đã thêm luật quy đổi thành công!");
    }

    // Xóa một luật quy đổi
    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<String> xoaLuatQuyDoi(@PathVariable Integer id) {
        bangQuyDoiService.delete(id);
        return ResponseEntity.ok("Đã xóa luật quy đổi!");
    }

    // ========================================================
    // TÍNH ĐIỂM QUY ĐỔI
    // ========================================================
    @GetMapping("/tinh-diem")
    public ResponseEntity<String> tinhDiemQuyDoi(
            @RequestParam String phuongThuc,
            @RequestParam String toHop,
            @RequestParam BigDecimal diemGoc) {

        // Gọi Service để dò trong DB xem điểm này thuộc mức nào
        Double diemSauQuyDoi = bangQuyDoiService.quyDoiDiemKhaoThi(phuongThuc, diemGoc);

        if (diemSauQuyDoi > 0) {
            return ResponseEntity.ok("Chứng chỉ " + toHop + " " + diemGoc + " được quy đổi thành: " + diemSauQuyDoi + " điểm.");
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy luật quy đổi cho mức điểm này!");
        }
    }
}
