package com.example.admissions_management.domain.repository;

import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import java.util.List;
import java.util.Optional;

public interface NguyenVongXetTuyenRepository {

    List<NguyenVongXetTuyen> findAll();
    Optional<NguyenVongXetTuyen> findById(Integer id);
    

    List<NguyenVongXetTuyen> findByNnCccd(String nnCccd);

    List<NguyenVongXetTuyen> findByNvMaNganh(String nvMaNganh);

    Optional<NguyenVongXetTuyen> findByNvKeys(String nvKeys);

    List<NguyenVongXetTuyen> findByNvKetQua(String nvKetQua);

    List<NguyenVongXetTuyen> findByNvMaNganhOrderByDiemXetTuyenDesc(String nvMaNganh);

    List<NguyenVongXetTuyen> findByNnCccdOrderByNvThuTuAsc(String nnCccd);

    NguyenVongXetTuyen save(NguyenVongXetTuyen nguyenVong);

    NguyenVongXetTuyen update(NguyenVongXetTuyen nguyenVong);

    void delete(Integer id);

    void deleteAll();

    void updateBatch(List<NguyenVongXetTuyen> nguyenVongs);
    /**
     * Bulk insert domain objects using efficient JDBC batch insert.
     */
    void bulkInsert(List<NguyenVongXetTuyen> nguyenVongs, int batchSize);
}
