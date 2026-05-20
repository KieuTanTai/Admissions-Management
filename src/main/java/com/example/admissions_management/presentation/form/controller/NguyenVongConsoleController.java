package com.example.admissions_management.presentation.form.controller;

import com.example.admissions_management.application.service.NguyenVongXetTuyenService;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NguyenVongConsoleController {

    private final NguyenVongXetTuyenService nguyenVongService;
    private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();

    static {
        SAX_FACTORY.setNamespaceAware(true);
    }

    public NguyenVongConsoleController(NguyenVongXetTuyenService nguyenVongService) {
        this.nguyenVongService = nguyenVongService;
    }

    public List<NguyenVongXetTuyen> loadAll() {
        return nguyenVongService.getAll();
    }

    public List<NguyenVongXetTuyen> loadByCccd(String nnCccd) {
        return nguyenVongService.getByNnCccd(nnCccd);
    }

    public NguyenVongXetTuyen save(String nnCccd,
                                   String maNganh,
                                   String maToHop,
                                   String nvThuTu,
                                   String diemThxt,
                                   String diemUtqd) {
        NguyenVongXetTuyen created = nguyenVongService.createNguyenVong(
                nnCccd.trim(),
                maNganh.trim(),
                maToHop == null ? "" : maToHop.trim(),
                Integer.parseInt(nvThuTu.trim())
        );
        return nguyenVongService.calculateScore(
                created.getId(),
                parseDecimal(diemThxt),
                parseDecimal(diemUtqd)
        );
    }

    public NguyenVongXetTuyen update(Integer id,
                                      String nnCccd,
                                      String maNganh,
                                      String maToHop,
                                      String nvThuTu,
                                      String diemThxt,
                                      String diemUtqd) {
        return nguyenVongService.updateNguyenVong(
                id,
                nnCccd.trim(),
                maNganh.trim(),
                maToHop == null ? "" : maToHop.trim(),
                Integer.parseInt(nvThuTu.trim()),
                parseDecimal(diemThxt),
                parseDecimal(diemUtqd)
        );
    }

    public void delete(Integer id) {
        nguyenVongService.deleteNguyenVong(id);
    }

    public void deleteAll() {
        nguyenVongService.deleteAll();
    }

    public com.example.admissions_management.application.dto.response.NguyenVongImportSummary importExcelFile(File file) throws Exception {
        return importExcelFileBatch(file, 2000);
    }

    /**
     * Phương thức Import tối ưu hóa vượt bậc cho dữ liệu lớn (~80.000 dòng)
     * Sử dụng cơ chế Streaming hoàn toàn và mở file ở chế độ READ-ONLY thông qua Đường dẫn
     */
    public com.example.admissions_management.application.dto.response.NguyenVongImportSummary importExcelFileBatch(File file, int batchSize) throws Exception {
        try {
            // Cấu hình giới hạn vùng đệm mảng byte lớn để tránh lỗi tệp nén POI
            IOUtils.setByteArrayMaxOverride(250_000_000);
        } catch (Throwable ignored) {
        }

        int targetBatchSize = batchSize > 0 ? batchSize : 2000;

        // Điểm tối ưu quan trọng: Sử dụng đường dẫn File trực tiếp với chế độ Read-Only thay vì truyền InputStream, 
        // giúp POI sử dụng bộ nhớ đệm kênh tệp (FileChannel memory-mapped) thay vì xả toàn bộ vào RAM.
        try (OPCPackage opcPackage = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(opcPackage);
            StylesTable stylesTable = reader.getStylesTable();
            ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(opcPackage);

            DataFormatter formatter = new DataFormatter();
            StreamingImportHandler handler = new StreamingImportHandler(nguyenVongService, targetBatchSize);
            
            // Tối ưu hóa Parser: Sử dụng SAXFactory tĩnh để tái sử dụng cấu hình phân tích cú pháp XML
            XMLReader parser = SAX_FACTORY.newSAXParser().getXMLReader();
            parser.setContentHandler(new XSSFSheetXMLHandler(stylesTable, null, sharedStringsTable, handler, formatter, false));

            try (InputStream sheetData = reader.getSheetsData().next()) {
                parser.parse(new InputSource(sheetData));
            }

            com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary = handler.finish();
            return summary;
        }
    }

    // Các hàm phụ trợ được giữ nguyên để giữ tính nhất quán
    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isEmpty()) return BigDecimal.ZERO;
        String v = value.trim().replace(" ", "");
        if (v.contains(",") && v.contains(".")) {
            if (v.lastIndexOf(',') > v.lastIndexOf('.')) {
                v = v.replace(".", "").replace(',', '.');
            } else {
                v = v.replace(",", "");
            }
        } else if (v.contains(",")) {
            v = v.replace(',', '.');
        }
        try {
            return new BigDecimal(v);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) return null;
        String normalized = value.trim().replaceAll("[^0-9-]", "");
        if (normalized.isEmpty()) return null;
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeHeader(String header) {
        if (header == null) return "";
        String noAccent = Normalizer.normalize(header, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static int findColumnIndex(Map<String, Integer> headerMap, String[] keys) {
        for (String key : keys) {
            if (headerMap.containsKey(key)) {
                return headerMap.get(key);
            }
        }
        return -1;
    }

    /**
     * Handler tối ưu hóa cao độ vòng đời dữ liệu dòng
     */
    private static final class StreamingImportHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final NguyenVongXetTuyenService service;
        private final int batchSize;
        private final Map<String, Integer> headerMap = new HashMap<>(16);
        private final List<NguyenVongXetTuyen> batch;
        private final Map<Integer, String> currentRowValues = new HashMap<>(16);

        private int importedCount = 0;
        private final com.example.admissions_management.application.dto.response.NguyenVongImportSummary summary = new com.example.admissions_management.application.dto.response.NguyenVongImportSummary();
        private boolean isHeaderRow = true;
        private int idxCccd = -1;
        private int idxMaNganh = -1;
        private int idxMaToHop = -1;
        private int idxNvThuTu = -1;
        private int idxDiemThxt = -1;
        private int idxDiemUtqd = -1;
        private int idxNvKetQua = -1;
        private int idxTtPhuongThuc = -1;
        private int idxTtThm = -1;

        private StreamingImportHandler(NguyenVongXetTuyenService service, int batchSize) {
            this.service = service;
            this.batchSize = batchSize;
            this.batch = new ArrayList<>(batchSize);
        }

        @Override
        public void startRow(int rowNum) {
            currentRowValues.clear();
            if (rowNum == 0) {
                isHeaderRow = true;
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (isHeaderRow) {
                for (Map.Entry<Integer, String> entry : currentRowValues.entrySet()) {
                    String name = entry.getValue();
                    if (name != null && !name.isBlank()) {
                        headerMap.put(normalizeHeader(name), entry.getKey());
                    }
                }

                idxCccd = findColumnIndex(headerMap, new String[]{"nncccd", "cccd", "socccd"});
                idxMaNganh = findColumnIndex(headerMap, new String[]{"nvmanganh", "manganh", "nganh"});
                idxMaToHop = findColumnIndex(headerMap, new String[]{"matohop", "tohop"});
                idxNvThuTu = findColumnIndex(headerMap, new String[]{"nvtt", "nvthutu", "thutu"});
                idxDiemThxt = findColumnIndex(headerMap, new String[]{"diemthxt", "diemthi", "diemxettuyenthpt"});
                idxDiemUtqd = findColumnIndex(headerMap, new String[]{"diemutqd", "diemuutien", "utqd"});
                idxNvKetQua = findColumnIndex(headerMap, new String[]{"nvketqua", "ketqua", "kq", "kết quả"});
                idxTtPhuongThuc = findColumnIndex(headerMap, new String[]{"ttphuongthuc", "phuongthuc", "phuong thuc", "phương thức"});
                idxTtThm = findColumnIndex(headerMap, new String[]{"ttthm", "thm", "trangthaithm", "trạng thái thm"});

                if (idxCccd < 0 || idxMaNganh < 0 || idxNvThuTu < 0) {
                    throw new IllegalStateException("Tệp Excel thiếu các cột bắt buộc (CCCD, Mã Ngành, hoặc Thứ Tự NV).");
                }
                isHeaderRow = false;
                return;
            }

            if (currentRowValues.isEmpty()) return;

            String nnCccd = getCellValue(idxCccd);
            String maNganh = getCellValue(idxMaNganh);
            String nvThuTuText = getCellValue(idxNvThuTu);

            if (nnCccd.isEmpty() || maNganh.isEmpty() || nvThuTuText.isEmpty()) return;

            Integer nvThuTu = parseInteger(nvThuTuText);
            if (nvThuTu == null) return;

            String maToHop = getCellValue(idxMaToHop);
            BigDecimal diemThxt = parseDecimal(getCellValue(idxDiemThxt));
            BigDecimal diemUtqd = parseDecimal(getCellValue(idxDiemUtqd));
            String nvKetQua = getCellValue(idxNvKetQua);
            String ttPhuongThuc = getCellValue(idxTtPhuongThuc);
            String ttThm = getCellValue(idxTtThm);

            // Khởi tạo Object trực tiếp hiệu năng cao
            NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
            nv.setNnCccd(nnCccd);
            nv.setNvMaNganh(maNganh);
            nv.setMaToHop(maToHop);
            nv.setNvThuTu(nvThuTu);
            nv.setDiemThxt(diemThxt);
            nv.setDiemUtqd(diemUtqd);
            nv.setDiemCong(BigDecimal.ZERO);
            nv.setDiemXetTuyen(diemThxt.add(diemUtqd));
            nv.setNvKetQua(nvKetQua == null || nvKetQua.isBlank() ? "Đang xét" : nvKetQua);
            nv.setTtPhuongThuc(ttPhuongThuc == null ? "" : ttPhuongThuc);
            nv.setTtThm(ttThm == null ? "" : ttThm);
            nv.setNvKeys(nnCccd + "_" + maNganh + "_" + maToHop + "_" + nvThuTu);

            batch.add(nv);
            
            if (batch.size() >= batchSize) {
                com.example.admissions_management.application.dto.response.NguyenVongImportSummary batchSummary = service.importNguyenVongBatch(batch, batchSize);
                importedCount += batch.size();
                summary.setTotalRows(summary.getTotalRows() + batchSummary.getTotalRows());
                summary.setNewCount(summary.getNewCount() + batchSummary.getNewCount());
                summary.setUpdatedCount(summary.getUpdatedCount() + batchSummary.getUpdatedCount());
                summary.setSkippedCount(summary.getSkippedCount() + batchSummary.getSkippedCount());
                batch.clear();
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
            if (cellReference != null) {
                int columnIndex = columnToIndex(cellReference);
                if (columnIndex >= 0) {
                    currentRowValues.put(columnIndex, formattedValue == null ? "" : formattedValue.trim());
                }
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {}

        private void flushRemaining() {
            if (!batch.isEmpty()) {
                com.example.admissions_management.application.dto.response.NguyenVongImportSummary batchSummary = service.importNguyenVongBatch(batch, batch.size());
                importedCount += batch.size();
                summary.setTotalRows(summary.getTotalRows() + batchSummary.getTotalRows());
                summary.setNewCount(summary.getNewCount() + batchSummary.getNewCount());
                summary.setUpdatedCount(summary.getUpdatedCount() + batchSummary.getUpdatedCount());
                summary.setSkippedCount(summary.getSkippedCount() + batchSummary.getSkippedCount());
                batch.clear();
            }
        }

        private com.example.admissions_management.application.dto.response.NguyenVongImportSummary finish() {
            flushRemaining();
            return summary;
        }

        private String getCellValue(int index) {
            if (index < 0) return "";
            return currentRowValues.getOrDefault(index, "");
        }

        private static int columnToIndex(String cellReference) {
            int col = 0;
            final int len = cellReference.length();
            for (int i = 0; i < len; i++) {
                char ch = cellReference.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    col = col * 26 + (ch - 'A' + 1);
                } else if (ch >= 'a' && ch <= 'z') {
                    col = col * 26 + (ch - 'a' + 1);
                } else {
                    break;
                }
            }
            return col - 1;
        }
    }
}