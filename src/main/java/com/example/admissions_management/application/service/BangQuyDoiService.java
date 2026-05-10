package com.example.admissions_management.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import com.example.admissions_management.domain.model.BangQuyDoi;
import com.example.admissions_management.domain.repository.BangQuyDoiRepository;

@Service
public class BangQuyDoiService {

    // Gọi Interface, KHÔNG gọi BangQuyDoiRepositoryImpl
    private final BangQuyDoiRepository bangQuyDoiRepository;

    // Sử dụng Constructor Injection (Chuẩn Best Practice của Spring Boot)
    public BangQuyDoiService(BangQuyDoiRepository bangQuyDoiRepository) {
        this.bangQuyDoiRepository = bangQuyDoiRepository;
    }

    public List<BangQuyDoi> getAll()
    {
        return bangQuyDoiRepository.getAll();
    }
    public BangQuyDoi add(BangQuyDoi quyTac)
    {
        return bangQuyDoiRepository.add(quyTac);
    }
    public BangQuyDoi update(BangQuyDoi quyTac)
    {
        return bangQuyDoiRepository.update(quyTac);
    }

    public Optional<BangQuyDoi> findById(Integer id)
    {
        return bangQuyDoiRepository.findById(id);
    }

    public List<BangQuyDoi> findByMaQuyDoi(String maQuyDoi)
    {
        return bangQuyDoiRepository.findByMaQuyDoi(maQuyDoi);
    }

    public void delete(Integer id)
    {
        bangQuyDoiRepository.delete(id);
    }
    // Hàm quy đổi điểm ngoại ngữ (IELTS/TOEIC...)
    public Double quyDoiDiemNgoaiNgu(String phuongThuc, String mon, BigDecimal diemChungChi) {
        Optional<BangQuyDoi> quyTac = bangQuyDoiRepository.timQuyTacChinhXac(phuongThuc, mon, diemChungChi);

        if (quyTac.isPresent() && quyTac.get().getPhanVi() != null) {
            try {
                return Double.valueOf(quyTac.get().getPhanVi());
            } catch (NumberFormatException e) {
                // Xử lý lỗi an toàn nếu dữ liệu cột phanVi bị nhập sai (chữ thay vì số)
                System.err.println("Lỗi parse điểm phân vị: " + quyTac.get().getPhanVi());
                return 0.0;
            }
        }
        return 0.0;
    }

    // Hàm quy đổi điểm Đánh giá năng lực (hoặc V-SAT)
    public Map<String, String> quyDoiDiemKhaoThi(String phuongThuc,String toHopHoacMon ,BigDecimal diemThiThucTe) {
        Map<String, String> ketqua = new HashedMap<>();
        Optional<BangQuyDoi> quyTac = bangQuyDoiRepository.timQuyTacTheoKhoang(phuongThuc, toHopHoacMon, diemThiThucTe);
        BigDecimal diemDaDuocQuyDoi = quyTac.get().getDiemC().add(((diemThiThucTe.subtract(quyTac.get().getDiemA())).divide((quyTac.get().getDiemB().subtract(quyTac.get().getDiemA())),4, RoundingMode.HALF_UP)).multiply(quyTac.get().getDiemD().subtract(quyTac.get().getDiemC()))).divide(new BigDecimal(1),2,RoundingMode.HALF_UP);
        String congThuc = quyTac.get().getDiemC() + " + (" + diemThiThucTe + " - " + quyTac.get().getDiemA() + ")/(" + quyTac.get().getDiemB() + " - " + quyTac.get().getDiemA() + ")*(" + quyTac.get().getDiemD() + " - " + quyTac.get().getDiemC() + ")";
        ketqua.put("formula", congThuc);
        ketqua.put("result", diemDaDuocQuyDoi.toString());
        return ketqua;
    }
}