package com.example.admissions_management.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.admissions_management.infrastructure.persistence.entity.xettuyen2026.XtBangQuyDoiEntity;

@Repository
public interface SpringDataBangQuyDoiRepository extends JpaRepository<XtBangQuyDoiEntity, Integer> {

    // Đã sửa lại lỗi chữ hoa/chữ thường ở :phuongThuc
    @Query("SELECT b FROM XtBangQuyDoiEntity b WHERE b.phuongThuc = :phuongThuc AND b.mon = :mon AND b.diemA = :diemGoc")
    Optional<XtBangQuyDoiEntity> timQuyTacChinhXac(
            @Param("phuongThuc") String phuongThuc,
            @Param("mon") String mon,
            @Param("diemGoc") BigDecimal diemGoc);

    // Đã sửa lại lỗi chữ hoa/chữ thường ở :phuongThuc
    @Query("SELECT b FROM XtBangQuyDoiEntity b WHERE b.phuongThuc = :phuongThuc AND :diemGoc >= b.diemA AND :diemGoc <= b.diemB")
    Optional<XtBangQuyDoiEntity> timQuyTacTheoKhoang(
            @Param("phuongThuc") String phuongThuc,
            @Param("diemGoc") BigDecimal diemGoc);

    @Query("SELECT b FROM XtBangQuyDoiEntity b WHERE b.maQuyDoi LIKE %:maQuyDoi%")
    List<XtBangQuyDoiEntity> timTheoMaQuyDoi(
            @Param("maQuyDoi") String maQuyDoi);
}