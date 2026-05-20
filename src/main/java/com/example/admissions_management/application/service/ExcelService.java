package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.DiemCongImportRequest;
import com.example.admissions_management.application.dto.response.DiemCongImportSummary;
import com.example.admissions_management.domain.model.NguyenVongXetTuyen;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.springframework.stereotype.Service;
import org.apache.poi.util.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.DataSource;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExcelService {
    private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelService.class);

    static {
        SAX_FACTORY.setNamespaceAware(true);
    }

    private final DataSource dataSource;
    private final DiemCongXetTuyenService diemCongService;

    public ExcelService(DataSource dataSource, DiemCongXetTuyenService diemCongService) {
        this.dataSource = dataSource;
        this.diemCongService = diemCongService;
        // Increase POI byte-array allocation limit to allow larger Excel files
        try {
            IOUtils.setByteArrayMaxOverride(200_000_000);
        } catch (Throwable t) {
            // ignore when unavailable or restricted
        }
    }

    private static class DiemCongColumnIndexes {
        int headerRowIndex;
        int idxId;
        int idxCccd;
        int idxMaNganh;
        int idxMaToHop;
        int idxPhuongThuc;
        int idxDiemCc;
        int idxDiemUtxt;
        int idxDiemTong;
        int idxGhiChu;
    }
    /**
     * Import dữ liệu điểm cộng từ file Excel
     * Định dạng: [CCCD | Mã Ngành | Mã Tổ Hợp | Phương Thức | Điểm CC | Điểm Ưu Tiên | Tổng Điểm | Ghi Chú]
     */
    public List<DiemCongImportRequest> importDiemCong(MultipartFile file) throws Exception {
        List<DiemCongImportRequest> records = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            records.addAll(parseExcelData(workbook));
        }
        
        return records;
    }

        /**
         * Fast bulk import: convert Excel to CSV (streaming) then LOAD DATA LOCAL INFILE into DB.
         * Returns number of rows processed (approx).
         * Uses fast path - NO formula evaluation (skips expensive parsing).
         */
        public int importDiemCongFromFileBulk(File file) throws Exception {
            int processed = 0;
            File csv = File.createTempFile("diemcong_import_", ".csv");
            java.util.List<com.example.admissions_management.application.dto.request.DiemCongImportRequest> parsedRecords = new java.util.ArrayList<>();
            try (InputStream is = new FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(is);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(csv), java.nio.charset.StandardCharsets.UTF_8))) {

                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) return 0;

                DiemCongColumnIndexes indexes = resolveDiemCongColumnIndexes(sheet);

                int firstRow = indexes.headerRowIndex + 1;
                for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    try {
                        String idVal = indexes.idxId >= 0 ? getCellStringValueFast(row, indexes.idxId) : "";
                        String cccdVal = indexes.idxCccd >= 0 ? getCellStringValueFast(row, indexes.idxCccd) : "";
                        String tsCccd = (cccdVal != null ? cccdVal : "").trim();
                        String nganhVal = indexes.idxMaNganh >= 0 ? getCellStringValueFast(row, indexes.idxMaNganh) : "";
                        String maNganh = (nganhVal != null ? nganhVal : "").trim();
                        String toHopVal = indexes.idxMaToHop >= 0 ? getCellStringValueFast(row, indexes.idxMaToHop) : "";
                        String maToHop = (toHopVal != null ? toHopVal : "").trim();
                        String phuongThucVal = indexes.idxPhuongThuc >= 0 ? getCellStringValueFast(row, indexes.idxPhuongThuc) : "";
                        String phuongThuc = (phuongThucVal != null ? phuongThucVal : "").trim();
                        String diemCc = indexes.idxDiemCc >= 0 ? normalizeDecimalString(getCellStringValueFast(row, indexes.idxDiemCc)) : "0";
                        String diemUtxt = indexes.idxDiemUtxt >= 0 ? normalizeDecimalString(getCellStringValueFast(row, indexes.idxDiemUtxt)) : "0";
                        String diemTong = indexes.idxDiemTong >= 0 ? normalizeDecimalString(getCellStringValueFast(row, indexes.idxDiemTong)) : "0";
                        String ghiChu = indexes.idxGhiChu >= 0 ? getCellStringValueFast(row, indexes.idxGhiChu) : "";

                            if (tsCccd == null || tsCccd.isEmpty() || maNganh == null || maNganh.isEmpty()) {
                            continue;
                        }

                        

                        String dcKeys = tsCccd + "_" + maNganh + "_" + (maToHop == null ? "" : maToHop);

                        // Escape double quotes inside fields
                        java.util.function.Function<String, String> esc = s -> s == null ? "" : s.replace("\"", "\"\"");

                        String line = "\"" + esc.apply(idVal == null ? "" : idVal.trim()) + "\"," +
                            "\"" + esc.apply(tsCccd) + "\"," +
                            "\"" + esc.apply(maNganh) + "\"," +
                            "\"" + esc.apply(maToHop) + "\"," +
                            "\"" + esc.apply(phuongThuc) + "\"," +
                            esc.apply(diemCc) + "," +
                            esc.apply(diemUtxt) + "," +
                            esc.apply(diemTong) + "," +
                            "\"" + esc.apply(ghiChu) + "\"," + "\"" + esc.apply(dcKeys) + "\"";

                        bw.write(line);
                        bw.newLine();
                        // also collect parsed record for fallback bulk upsert paths
                        java.math.BigDecimal bdDiemCc = parseDecimalOrZero(diemCc);
                        java.math.BigDecimal bdDiemUtxt = parseDecimalOrZero(diemUtxt);
                        java.math.BigDecimal bdDiemTong = parseDecimalOrZero(diemTong);
                        Long idLong = parseLongOrNull(idVal);
                        parsedRecords.add(new com.example.admissions_management.application.dto.request.DiemCongImportRequest(
                                idLong, tsCccd, maNganh, maToHop, phuongThuc, bdDiemCc, bdDiemUtxt, bdDiemTong, ghiChu
                        ));
                        processed++;

                    } catch (Exception ex) {
                        // skip problematic row in bulk mode
                    }
                }

                bw.flush();
            }

            // Load CSV into DB using LOAD DATA LOCAL INFILE - requires JDBC URL allowing LOCAL INFILE
            java.sql.Connection conn = null;
            java.sql.Statement st = null;
            try {
                conn = dataSource.getConnection();
                st = conn.createStatement();
                String dbProduct = null;
                try {
                    dbProduct = conn.getMetaData().getDatabaseProductName();
                } catch (Exception ignored) {}
                // Windows path needs escaping backslashes
                String csvPath = csv.getAbsolutePath().replace("\\", "\\\\");
                if (dbProduct != null && (dbProduct.toLowerCase().contains("mysql") || dbProduct.toLowerCase().contains("maria"))) {
                    // MySQL/MariaDB: use LOAD DATA LOCAL INFILE for maximum speed
                    String sql = "LOAD DATA LOCAL INFILE '" + csvPath + "' IGNORE INTO TABLE xt_diemcongxetuyen "
                            + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n' "
                            + "(@iddiemcong,ts_cccd,manganh,matohop,phuongthuc,diemCC,diemUtxt,diemTong,ghichu,dc_keys) "
                            + "SET iddiemcong = NULLIF(@iddiemcong, '')";
                    st.execute(sql);
                } else {
                    // Non-MySQL (H2, others): fallback to service batched upsert which uses optimized JDBC batching
                    if (!parsedRecords.isEmpty()) {
                        // choose a large batch size to minimize round-trips
                        int effectiveBatch = 2000;
                        diemCongService.importInBatches(parsedRecords, effectiveBatch);
                    }
                }
            } finally {
                if (st != null) try { st.close(); } catch (Exception e) {}
                if (conn != null) try { conn.close(); } catch (Exception e) {}
                // delete temp csv
                try { csv.delete(); } catch (Exception e) {}
            }

            return processed;
        }

        /**
         * Batch import fallback (safe): parse Excel streaming and call service.importInBatches per accumulated batch.
         * Uses fast path - NO formula evaluation (skips expensive parsing).
         */
        public DiemCongImportSummary importDiemCongFromFileBatch(File file, int batchSize) throws Exception {
            int effectiveBatchSize = batchSize > 0 ? batchSize : 1000;
            try {
                IOUtils.setByteArrayMaxOverride(200_000_000);
            } catch (Throwable ignored) {
            }

            long t0 = System.currentTimeMillis();
            try (OPCPackage opcPackage = OPCPackage.open(file, PackageAccess.READ)) {
                XSSFReader reader = new XSSFReader(opcPackage);
                StylesTable stylesTable = reader.getStylesTable();
                ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(opcPackage);
                DiemCongStreamingImportHandler handler = new DiemCongStreamingImportHandler(diemCongService, effectiveBatchSize);

                XMLReader parser = SAX_FACTORY.newSAXParser().getXMLReader();
                parser.setContentHandler(new XSSFSheetXMLHandler(stylesTable, null, sharedStringsTable, handler, new DataFormatter(), false));

                try (InputStream sheetData = reader.getSheetsData().next()) {
                    parser.parse(new InputSource(sheetData));
                }

                DiemCongImportSummary res = handler.finish();
                long took = System.currentTimeMillis() - t0;
                LOGGER.info("importDiemCongFromFileBatch: file={} totalRows={} new={} updated={} skipped={} took={}ms",
                    file.getName(), res.getTotalRows(), res.getNewCount(), res.getUpdatedCount(), res.getSkippedCount(), took);
                return res;
            }
        }

        private static final class RowSnapshot {
            private final int rowNum;
            private final Map<Integer, String> cells;

            private RowSnapshot(int rowNum, Map<Integer, String> cells) {
                this.rowNum = rowNum;
                this.cells = cells;
            }
        }

        private final class DiemCongStreamingImportHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
            private final DiemCongXetTuyenService service;
            private final int batchSize;
            private final DiemCongImportSummary summary = new DiemCongImportSummary();
            private final Map<Integer, RowSnapshot> bufferedRows = new LinkedHashMap<>();
            private final List<DiemCongImportRequest> batch;

            private Map<Integer, String> currentRowValues = new HashMap<>(16);
            private boolean headerResolved = false;
            private int headerRowIndex = -1;
            private DiemCongColumnIndexes indexes;

            private DiemCongStreamingImportHandler(DiemCongXetTuyenService service, int batchSize) {
                this.service = service;
                this.batchSize = Math.max(1, batchSize);
                this.batch = new ArrayList<>(this.batchSize);
            }

            @Override
            public void startRow(int rowNum) {
                currentRowValues = new HashMap<>(16);
            }

            @Override
            public void endRow(int rowNum) {
                bufferedRows.put(rowNum, new RowSnapshot(rowNum, new HashMap<>(currentRowValues)));

                if (!headerResolved && rowNum >= 30) {
                    resolveHeaderAndDrain();
                }

                if (headerResolved && rowNum > headerRowIndex) {
                    processSnapshot(bufferedRows.remove(rowNum));
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
            public void headerFooter(String text, boolean isHeader, String tagName) {
                // no-op
            }

            private DiemCongImportSummary finish() {
                if (!headerResolved) {
                    resolveHeaderAndDrain();
                }
                flushBatch();
                return summary;
            }

            private void resolveHeaderAndDrain() {
                if (headerResolved || bufferedRows.isEmpty()) {
                    return;
                }

                int bestRow = -1;
                int bestScore = -1;
                for (RowSnapshot snapshot : bufferedRows.values()) {
                    Map<String, Integer> headerMap = buildNormalizedHeaderMap(snapshot.cells);
                    int score = scoreHeader(headerMap);
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = snapshot.rowNum;
                    }
                }

                if (bestRow < 0 || bestScore < 3) {
                    // Defer until more rows arrive, or finish() will resolve if the file is small.
                    return;
                }

                headerRowIndex = bestRow;
                try {
                    indexes = resolveIndexes(bufferedRows.get(bestRow).cells);
                } catch (Exception ex) {
                    // failed to resolve indexes for detected header, defer
                    return;
                }
                headerResolved = true;

                for (RowSnapshot snapshot : bufferedRows.values()) {
                    if (snapshot.rowNum > headerRowIndex) {
                        processSnapshot(snapshot);
                    }
                }
                bufferedRows.clear();
            }

            private void processSnapshot(RowSnapshot snapshot) {
                if (snapshot == null || indexes == null) {
                    return;
                }

                summary.setTotalRows(summary.getTotalRows() + 1);

                try {
                    Long id = indexes.idxId >= 0 ? parseLongOrNull(getCellValue(snapshot.cells, indexes.idxId)) : null;
                    String tsCccd = indexes.idxCccd >= 0 ? getCellValue(snapshot.cells, indexes.idxCccd).trim() : "";
                    String maNganh = indexes.idxMaNganh >= 0 ? getCellValue(snapshot.cells, indexes.idxMaNganh).trim() : "";
                    String maToHop = indexes.idxMaToHop >= 0 ? getCellValue(snapshot.cells, indexes.idxMaToHop).trim() : "";
                    String phuongThuc = indexes.idxPhuongThuc >= 0 ? getCellValue(snapshot.cells, indexes.idxPhuongThuc).trim() : "";
                    BigDecimal diemCc = indexes.idxDiemCc >= 0 ? parseDecimalOrZero(getCellValue(snapshot.cells, indexes.idxDiemCc)) : BigDecimal.ZERO;
                    BigDecimal diemUtxt = indexes.idxDiemUtxt >= 0 ? parseDecimalOrZero(getCellValue(snapshot.cells, indexes.idxDiemUtxt)) : BigDecimal.ZERO;
                    BigDecimal diemTong = indexes.idxDiemTong >= 0 ? parseDecimalOrZero(getCellValue(snapshot.cells, indexes.idxDiemTong)) : BigDecimal.ZERO;
                    String ghiChu = indexes.idxGhiChu >= 0 ? getCellValue(snapshot.cells, indexes.idxGhiChu).trim() : "";

                    if ((tsCccd == null || tsCccd.isEmpty()) && (maNganh == null || maNganh.isEmpty())) {
                        summary.setSkippedCount(summary.getSkippedCount() + 1);
                        return;
                    }

                    if (tsCccd == null || tsCccd.isEmpty() || maNganh == null || maNganh.isEmpty()) {
                        summary.setSkippedCount(summary.getSkippedCount() + 1);
                        return;
                    }

                    batch.add(new DiemCongImportRequest(id, tsCccd, maNganh, maToHop, phuongThuc, diemCc, diemUtxt, diemTong, ghiChu));
                    if (batch.size() >= batchSize) {
                        flushBatch();
                    }
                } catch (Exception ex) {
                    summary.setSkippedCount(summary.getSkippedCount() + 1);
                }
            }

            private void flushBatch() {
                if (batch.isEmpty()) {
                    return;
                }
                DiemCongImportSummary batchSummary = service.importInBatches(batch, batchSize);
                summary.setNewCount(summary.getNewCount() + batchSummary.getNewCount());
                summary.setUpdatedCount(summary.getUpdatedCount() + batchSummary.getUpdatedCount());
                summary.setSkippedCount(summary.getSkippedCount() + batchSummary.getSkippedCount());
                batch.clear();
            }

            private DiemCongColumnIndexes resolveIndexes(Map<Integer, String> headerCells) throws Exception {
                Map<String, Integer> headerMap = buildNormalizedHeaderMap(headerCells);
                DiemCongColumnIndexes idx = new DiemCongColumnIndexes();
                idx.headerRowIndex = headerRowIndex;
                idx.idxId = findColumnIndex(headerMap, new String[]{"id", "iddiemcong", "ma", "stt"});
                idx.idxCccd = findColumnIndex(headerMap, new String[]{"cccd", "cccdthisinh", "cccd thi sinh", "cccd thí sinh", "tscccd", "cmnd", "cmt"});
                idx.idxMaNganh = findColumnIndex(headerMap, new String[]{"manganh", "ma nganh", "mã ngành"});
                idx.idxMaToHop = findColumnIndex(headerMap, new String[]{"matohop", "ma to hop", "mato hop", "mã tổ hợp"});
                idx.idxPhuongThuc = findColumnIndex(headerMap, new String[]{"phuongthuc", "phuong thuc", "phuongthucxettuyen", "phuong thuc xet tuyen", "phương thức"});
                idx.idxDiemCc = findColumnIndex(headerMap, new String[]{"diemcc", "diem cc", "diemccxt", "diemchungchi", "diem chung chi", "điểm cc"});
                idx.idxDiemUtxt = findColumnIndex(headerMap, new String[]{"diemut", "diem ut", "diemutxt", "diem utxt", "diemuutien", "diem uu tien", "diemuutiendacbiet", "diemutqd", "diem uutien xettuyen", "diemuutienxettuyen", "điểm ut", "điểm ưu tiên"});
                idx.idxDiemTong = findColumnIndex(headerMap, new String[]{"tongdiem", "tong diem", "diemtong", "tongdiemxettuyen", "tongdiemcong", "tongdiem", "tổng điểm", "điểm tổng"});
                idx.idxGhiChu = findColumnIndex(headerMap, new String[]{"ghichu", "ghi chu", "ghichuthongtin", "note", "ghi chú"});

                if (idx.idxCccd < 0 || idx.idxMaNganh < 0) {
                    throw new Exception("Không nhận dạng được cột bắt buộc (CCCD, Mã Ngành). Vui lòng kiểm tra lại header file Excel.");
                }
                return idx;
            }

            private Map<String, Integer> buildNormalizedHeaderMap(Map<Integer, String> headerCells) {
                Map<String, Integer> headerMap = new HashMap<>();
                for (Map.Entry<Integer, String> entry : headerCells.entrySet()) {
                    String normalized = normalizeHeader(entry.getValue());
                    if (!normalized.isEmpty() && !headerMap.containsKey(normalized)) {
                        headerMap.put(normalized, entry.getKey());
                    }
                }
                return headerMap;
            }

            private int scoreHeader(Map<String, Integer> headerMap) {
                int score = 0;
                if (findColumnIndex(headerMap, new String[]{"cccd", "cccdthisinh", "cccd thi sinh", "cccd thí sinh", "tscccd"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"manganh", "ma nganh", "mã ngành"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"matohop", "ma to hop", "mã tổ hợp"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"phuongthuc", "phuong thuc", "phương thức"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"diemcc", "diem cc", "điểm cc"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"diemut", "diem ut", "diemutxt", "diemuutien", "điểm ut", "điểm ưu tiên"}) >= 0) score++;
                if (findColumnIndex(headerMap, new String[]{"tongdiem", "diemtong", "tong diem", "tongdiemxettuyen", "tổng điểm", "điểm tổng"}) >= 0) score++;
                return score;
            }

            private String getCellValue(Map<Integer, String> row, int index) {
                if (index < 0 || row == null) {
                    return "";
                }
                return row.getOrDefault(index, "");
            }

            private int columnToIndex(String cellReference) {
                int col = 0;
                for (int i = 0; i < cellReference.length(); i++) {
                    char ch = cellReference.charAt(i);
                    if (Character.isLetter(ch)) {
                        col = col * 26 + (Character.toUpperCase(ch) - 'A' + 1);
                    } else {
                        break;
                    }
                }
                return col - 1;
            }
        }

    /**
     * Import dữ liệu điểm cộng từ file Excel (hỗ trợ Swing desktop)
     * Định dạng: [CCCD | Mã Ngành | Mã Tổ Hợp | Phương Thức | Điểm CC | Điểm Ưu Tiên | Tổng Điểm | Ghi Chú]
     */
    public List<DiemCongImportRequest> importDiemCongFromFile(File file) throws Exception {
        List<DiemCongImportRequest> records = new ArrayList<>();
        
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(is)) {
            records.addAll(parseExcelData(workbook));
        }
        
        return records;
    }

    /**
     * Parse file Excel cho Bảng Quy Đổi (xt_bangquydoi)
     * Header dự kiến: Phương Thức | Tổ Hợp | Môn | Điểm A | Điểm B | Điểm C | Điểm D | Mã Quy Đổi | Phân Vị
     */
    public java.util.List<com.example.admissions_management.domain.model.BangQuyDoi> importBangQuyDoiFromFile(File file) throws Exception {
        java.util.List<com.example.admissions_management.domain.model.BangQuyDoi> records = new java.util.ArrayList<>();
        try (java.io.InputStream is = new java.io.FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return records;

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new Exception("Không tìm thấy header trong sheet");

            java.util.Map<String, Integer> headerMap = new java.util.HashMap<>();
            for (int c = 0; c <= headerRow.getLastCellNum(); c++) {
                Cell hc = headerRow.getCell(c);
                if (hc == null) continue;
                String h = getCellStringValue(headerRow, c);
                if (h == null || h.trim().isEmpty()) continue;
                headerMap.put(normalizeHeader(h), c);
            }

            java.util.function.Function<String[], Integer> findIndex = (keys) -> {
                for (String k : keys) {
                    String nk = normalizeHeader(k);
                    if (headerMap.containsKey(nk)) return headerMap.get(nk);
                }
                return -1;
            };

            int idxPhuongThuc = findIndex.apply(new String[]{"phuongthuc", "phuong thuc"});
            int idxToHop = findIndex.apply(new String[]{"tohop", "to hop", "tổ hợp", "to hop"});
            int idxMon = findIndex.apply(new String[]{"mon", "môn"});
            int idxA = findIndex.apply(new String[]{"diema", "diem a", "a"});
            int idxB = findIndex.apply(new String[]{"diemb", "diem b", "b"});
            int idxC = findIndex.apply(new String[]{"diemc", "diem c", "c"});
            int idxD = findIndex.apply(new String[]{"diemd", "diem d", "d"});
            int idxMa = findIndex.apply(new String[]{"maquydoi", "ma quy doi", "mã quy đổi"});
            int idxPhanVi = findIndex.apply(new String[]{"phanvi", "phan vi", "phân vị"});

            int firstRow = 1;
            for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String phuongThuc = idxPhuongThuc >= 0 ? getCellStringValue(row, idxPhuongThuc) : "";
                    String toHop = idxToHop >= 0 ? getCellStringValue(row, idxToHop) : "";
                    String mon = idxMon >= 0 ? getCellStringValue(row, idxMon) : "";
                    java.math.BigDecimal a = idxA >= 0 ? getCellBigDecimalValue(row, idxA) : null;
                    java.math.BigDecimal b = idxB >= 0 ? getCellBigDecimalValue(row, idxB) : null;
                    java.math.BigDecimal cval = idxC >= 0 ? getCellBigDecimalValue(row, idxC) : null;
                    java.math.BigDecimal dval = idxD >= 0 ? getCellBigDecimalValue(row, idxD) : null;
                    String maquy = idxMa >= 0 ? getCellStringValue(row, idxMa) : "";
                    String phanVi = idxPhanVi >= 0 ? getCellStringValue(row, idxPhanVi) : "";

                    // skip empty rows
                    if ((phuongThuc == null || phuongThuc.isBlank()) && (toHop == null || toHop.isBlank()) && (mon == null || mon.isBlank()) && (maquy == null || maquy.isBlank())) {
                        continue;
                    }

                    com.example.admissions_management.domain.model.BangQuyDoi d = new com.example.admissions_management.domain.model.BangQuyDoi();
                    d.setPhuongThuc(phuongThuc == null ? "" : phuongThuc.trim());
                    d.setToHop(toHop == null ? "" : toHop.trim());
                    d.setMon(mon == null ? "" : mon.trim());
                    d.setDiemA(a);
                    d.setDiemB(b);
                    d.setDiemC(cval);
                    d.setDiemD(dval);
                    d.setMaQuyDoi(maquy == null ? "" : maquy.trim());
                    d.setPhanVi(phanVi == null ? "" : phanVi.trim());

                    records.add(d);

                } catch (Exception ex) {
                    // skip problematic row
                }
            }
        }

        return records;
    }

    /**
     * Parse dữ liệu từ Workbook - fast path, NO formula evaluation
     */
    private List<DiemCongImportRequest> parseExcelData(Workbook workbook) throws Exception {
        List<DiemCongImportRequest> records = new ArrayList<>();

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) return records;

        DiemCongColumnIndexes indexes = resolveDiemCongColumnIndexes(sheet);

        // Iterate data rows (starting after header row)
        int firstRow = indexes.headerRowIndex + 1;
        for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                Long id = indexes.idxId >= 0 ? parseLongOrNull(getCellStringValueFast(row, indexes.idxId)) : null;
                String cccdVal = indexes.idxCccd >= 0 ? getCellStringValueFast(row, indexes.idxCccd) : "";
                String tsCccd = (cccdVal != null ? cccdVal : "").trim();
                String nganhVal = indexes.idxMaNganh >= 0 ? getCellStringValueFast(row, indexes.idxMaNganh) : "";
                String maNganh = (nganhVal != null ? nganhVal : "").trim();
                String toHopVal = indexes.idxMaToHop >= 0 ? getCellStringValueFast(row, indexes.idxMaToHop) : "";
                String maToHop = (toHopVal != null ? toHopVal : "").trim();
                String phuongThucVal = indexes.idxPhuongThuc >= 0 ? getCellStringValueFast(row, indexes.idxPhuongThuc) : "";
                String phuongThuc = (phuongThucVal != null ? phuongThucVal : "").trim();
                BigDecimal diemCc = indexes.idxDiemCc >= 0 ? getCellBigDecimalValueFast(row, indexes.idxDiemCc) : BigDecimal.ZERO;
                BigDecimal diemUtxt = indexes.idxDiemUtxt >= 0 ? getCellBigDecimalValueFast(row, indexes.idxDiemUtxt) : BigDecimal.ZERO;
                BigDecimal diemTong = indexes.idxDiemTong >= 0 ? getCellBigDecimalValueFast(row, indexes.idxDiemTong) : BigDecimal.ZERO;
                String ghiChuVal = indexes.idxGhiChu >= 0 ? getCellStringValueFast(row, indexes.idxGhiChu) : "";
                String ghiChu = (ghiChuVal != null ? ghiChuVal : "");

                // Skip completely empty rows
                if ((tsCccd == null || tsCccd.isEmpty()) && (maNganh == null || maNganh.isEmpty())) {
                    continue;
                }

                // Validate required fields
                if (tsCccd == null || tsCccd.isEmpty() || maNganh == null || maNganh.isEmpty()) {
                    throw new IllegalArgumentException("Row " + (i + 1) + ": CCCD và Mã Ngành không được để trống");
                }

                records.add(new DiemCongImportRequest(id, tsCccd, maNganh, maToHop, phuongThuc,
                        diemCc, diemUtxt, diemTong, ghiChu));

            } catch (Exception e) {
                throw new Exception("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
            }
        }

        return records;
    }

    private String normalizeHeader(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        try {
            t = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        } catch (Exception ex) {
            // ignore
        }
        t = t.replaceAll("[^a-z0-9]", "");
        return t;
    }

    private DiemCongColumnIndexes resolveDiemCongColumnIndexes(Sheet sheet) throws Exception {
        int headerRowIndex = detectDiemCongHeaderRowIndex(sheet);
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) {
            throw new Exception("Không tìm thấy header trong sheet");
        }

        Map<String, Integer> headerMap = buildNormalizedHeaderMap(headerRow);
        DiemCongColumnIndexes idx = new DiemCongColumnIndexes();
        idx.headerRowIndex = headerRowIndex;
        idx.idxId = findColumnIndex(headerMap, new String[]{"id", "iddiemcong", "ma", "stt"});
        idx.idxCccd = findColumnIndex(headerMap, new String[]{"cccd", "cccdthisinh", "cccd thi sinh", "cccd thí sinh", "tscccd", "cmnd", "cmt"});
        idx.idxMaNganh = findColumnIndex(headerMap, new String[]{"manganh", "ma nganh", "mã ngành"});
        idx.idxMaToHop = findColumnIndex(headerMap, new String[]{"matohop", "ma to hop", "mato hop", "mã tổ hợp"});
        idx.idxPhuongThuc = findColumnIndex(headerMap, new String[]{"phuongthuc", "phuong thuc", "phuongthucxettuyen", "phuong thuc xet tuyen", "phương thức"});
        idx.idxDiemCc = findColumnIndex(headerMap, new String[]{"diemcc", "diem cc", "diemccxt", "diemchungchi", "diem chung chi", "điểm cc"});
        idx.idxDiemUtxt = findColumnIndex(headerMap, new String[]{
                "diemut",
                "diem ut",
                "diemutxt",
                "diem utxt",
                "diemuutien",
                "diem uu tien",
                "diemuutiendacbiet",
                "diemutqd",
                "diem uutien xettuyen",
                "diemuutienxettuyen",
                "điểm ut",
                "điểm ưu tiên"
        });
            idx.idxDiemTong = findColumnIndex(headerMap, new String[]{"tongdiem", "tong diem", "diemtong", "tongdiemxettuyen", "tongdiemcong", "tổng điểm", "điểm tổng"});
            idx.idxGhiChu = findColumnIndex(headerMap, new String[]{"ghichu", "ghi chu", "ghichuthongtin", "note", "ghi chú"});

        if (idx.idxCccd < 0 || idx.idxMaNganh < 0) {
            throw new Exception("Không nhận dạng được cột bắt buộc (CCCD, Mã Ngành). Vui lòng kiểm tra lại header file Excel.");
        }
        return idx;
    }

    private int detectDiemCongHeaderRowIndex(Sheet sheet) throws Exception {
        int bestRow = -1;
        int bestScore = -1;
        int maxScanRow = Math.min(sheet.getLastRowNum(), 30);

        for (int r = 0; r <= maxScanRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headerMap = buildNormalizedHeaderMap(row);
            if (headerMap.isEmpty()) {
                continue;
            }

            int score = 0;
            if (findColumnIndex(headerMap, new String[]{"cccd", "cccdthisinh", "cccd thi sinh", "cccd thí sinh", "tscccd"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"manganh", "ma nganh", "mã ngành"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"matohop", "ma to hop", "mã tổ hợp"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"phuongthuc", "phuong thuc", "phương thức"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"diemcc", "diem cc", "điểm cc"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"diemut", "diem ut", "diemutxt", "diemuutien", "điểm ut", "điểm ưu tiên"}) >= 0) score++;
            if (findColumnIndex(headerMap, new String[]{"tongdiem", "diemtong", "tong diem", "tổng điểm", "điểm tổng"}) >= 0) score++;

            if (score > bestScore) {
                bestScore = score;
                bestRow = r;
            }
        }

        if (bestRow < 0 || bestScore < 3) {
            throw new Exception("Không tìm thấy dòng tiêu đề hợp lệ trong 31 dòng đầu tiên của file Excel");
        }
        return bestRow;
    }

    private Map<String, Integer> buildNormalizedHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int c = 0; c < lastCellNum; c++) {
            String raw = getCellStringValue(headerRow, c);
            String normalized = normalizeHeader(raw);
            if (!normalized.isEmpty() && !headerMap.containsKey(normalized)) {
                headerMap.put(normalized, c);
            }
        }
        return headerMap;
    }

    private int findColumnIndex(Map<String, Integer> headerMap, String[] keys) {
        for (String key : keys) {
            String normalizedKey = normalizeHeader(key);
            if (headerMap.containsKey(normalizedKey)) {
                return headerMap.get(normalizedKey);
            }
        }

        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String normalizedHeader = entry.getKey();
            for (String key : keys) {
                String normalizedKey = normalizeHeader(key);
                if (normalizedKey.isEmpty()) {
                    continue;
                }
                if (normalizedHeader.contains(normalizedKey) || normalizedKey.contains(normalizedHeader)) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }

    /**
     * Export kết quả xét tuyển ra Excel
     */
    public byte[] exportNguyenVong(List<NguyenVongXetTuyen> nguyenVongs) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Kết Quả Xét Tuyển");
            
            // Tạo header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"CCCD", "Mã Ngành", "Mã Tổ Hợp", "Nguyện Vọng", 
                               "Điểm Thi", "Điểm Ưu Tiên", "Điểm Cộng", 
                               "Tổng Điểm", "Kết Quả", "Phương Thức"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Điền dữ liệu
            int rowNum = 1;
            for (NguyenVongXetTuyen nv : nguyenVongs) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(nv.getNnCccd());
                row.createCell(1).setCellValue(nv.getNvMaNganh());
                row.createCell(2).setCellValue(nv.getMaToHop());
                row.createCell(3).setCellValue(nv.getNvThuTu());
                row.createCell(4).setCellValue(nv.getDiemThxt() != null ? nv.getDiemThxt().doubleValue() : 0);
                row.createCell(5).setCellValue(nv.getDiemUtqd() != null ? nv.getDiemUtqd().doubleValue() : 0);
                row.createCell(6).setCellValue(nv.getDiemCong() != null ? nv.getDiemCong().doubleValue() : 0);
                row.createCell(7).setCellValue(nv.getDiemXetTuyen() != null ? nv.getDiemXetTuyen().doubleValue() : 0);
                row.createCell(8).setCellValue(nv.getNvKetQua());
                row.createCell(9).setCellValue(nv.getTtPhuongThuc());
            }
            
            // Auto-fit columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(bos);
            return bos.toByteArray();
            
        }
    }

    /**
     * Optimized import: attempt fast bulk path (CSV -> LOAD DATA LOCAL INFILE) when supported,
     * otherwise fallback to safe streaming batch import.
     */
    public DiemCongImportSummary importDiemCongOptimized(File file, int batchSize) throws Exception {
        // Try detecting DB product and attempting bulk import first for MySQL/MariaDB
        String dbProduct = null;
        try (java.sql.Connection conn = dataSource.getConnection()) {
            try {
                dbProduct = conn.getMetaData().getDatabaseProductName();
            } catch (Exception ignored) {}
        }

        if (dbProduct != null) {
            String db = dbProduct.toLowerCase();
            if (db.contains("mysql") || db.contains("maria")) {
                try {
                    int processed = importDiemCongFromFileBulk(file);
                    DiemCongImportSummary summary = new DiemCongImportSummary();
                    summary.setTotalRows(processed);
                    summary.setNewCount(processed);
                    return summary;
                } catch (Exception ex) {
                    LOGGER.warn("Bulk import failed (will fallback to batch). dbProduct={}, error={}", dbProduct, ex.getMessage());
                    // fallthrough to batch fallback
                }
            }
        }

        // Fallback to streaming batch import
        return importDiemCongFromFileBatch(file, batchSize);
    }

    /**
     * Tạo template Excel cho phần nguyện vọng và xét tuyển theo schema DB.
     */
    public byte[] generateNguyenVongTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            String[] headers = {
                    "idnv", "nn_cccd", "nv_manganh", "matohop", "nv_tt",
                    "diem_thxt", "diem_utqd", "diem_cong", "diem_xettuyen",
                    "nv_ketqua", "nv_keys", "tt_phuongthuc", "tt_thm"
            };

            createTemplateSheet(workbook, "NguyenVong", headers);
            createTemplateSheet(workbook, "XetTuyen", headers);

            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * Tạo template Excel cho import điểm cộng
     */
    public byte[] generateDiemCongTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Dữ Liệu Điểm Cộng");
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "CCCD Thí Sinh", "Mã Ngành", "Mã Tổ Hợp", "Phương Thức",
                               "Điểm CC", "Điểm Ưu Tiên", "Tổng Điểm", "Ghi Chú"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Auto-fit columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private void createTemplateSheet(Workbook workbook, String sheetName, String[] headers) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }

        sheet.createFreezePane(0, 1);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        try {
            DataFormatter formatter = new DataFormatter();
            String formatted = formatter.formatCellValue(cell);
            return formatted == null ? "" : formatted;
        } catch (Exception ex) {
            // fallback to previous simple behavior
            CellType type = cell.getCellType();
            if (type == CellType.FORMULA) {
                type = cell.getCachedFormulaResultType();
            }

            if (type == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (type == CellType.NUMERIC) {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            } else if (type == CellType.BOOLEAN) {
                return String.valueOf(cell.getBooleanCellValue());
            }
            return "";
        }
    }

    @SuppressWarnings("unused")
    private String getCellStringValue(Row row, int cellIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        try {
            String formatted = formatter.formatCellValue(cell, evaluator);
            return formatted == null ? "" : formatted;
        } catch (Exception ex) {
            return getCellStringValue(row, cellIndex);
        }
    }

    private BigDecimal getCellBigDecimalValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return BigDecimal.ZERO;
        }
        try {
            Workbook wb = row.getSheet().getWorkbook();
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            CellValue evaluated = evaluator.evaluate(cell);
            if (evaluated != null) {
                if (evaluated.getCellType() == CellType.NUMERIC) {
                    return BigDecimal.valueOf(evaluated.getNumberValue());
                }
                if (evaluated.getCellType() == CellType.STRING) {
                    return parseDecimalOrZero(evaluated.getStringValue());
                }
                if (evaluated.getCellType() == CellType.BOOLEAN) {
                    return evaluated.getBooleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
                }
            }

            // Fallback: use DataFormatter to read display text (respects format like 0.50)
            DataFormatter formatter = new DataFormatter();
            String text = formatter.formatCellValue(cell, evaluator);
            if (text != null && !text.isBlank()) {
                return parseDecimalOrZero(text);
            }
        } catch (Exception ex) {
            // fallback to simpler handling
        }

        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (type == CellType.STRING) {
            return parseDecimalOrZero(cell.getStringCellValue());
        }
        return BigDecimal.ZERO;
    }

    @SuppressWarnings("unused")
    private BigDecimal getCellBigDecimalValue(Row row, int cellIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return BigDecimal.ZERO;
        }
        try {
            CellValue evaluated = evaluator.evaluate(cell);
            if (evaluated != null) {
                if (evaluated.getCellType() == CellType.NUMERIC) {
                    return BigDecimal.valueOf(evaluated.getNumberValue());
                }
                if (evaluated.getCellType() == CellType.STRING) {
                    return parseDecimalOrZero(evaluated.getStringValue());
                }
                if (evaluated.getCellType() == CellType.BOOLEAN) {
                    return evaluated.getBooleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
                }
            }

            String text = formatter.formatCellValue(cell, evaluator);
            if (text != null && !text.isBlank()) {
                return parseDecimalOrZero(text);
            }
        } catch (Exception ex) {
            return getCellBigDecimalValue(row, cellIndex);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parseDecimalOrZero(String raw) {
        String normalized = normalizeDecimalString(raw);
        if (normalized.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String normalizeDecimalString(String raw) {
        if (raw == null) {
            return "";
        }

        String value = raw.trim().replace(" ", "");
        if (value.isEmpty()) {
            return "";
        }

        // Handle common locale formats: 1,23 / 1.234,56 / 1,234.56
        if (value.contains(",") && value.contains(".")) {
            int lastComma = value.lastIndexOf(',');
            int lastDot = value.lastIndexOf('.');
            if (lastComma > lastDot) {
                value = value.replace(".", "").replace(',', '.');
            } else {
                value = value.replace(",", "");
            }
        } else if (value.contains(",")) {
            value = value.replace(',', '.');
        }

        return value;
    }

    private Long parseLongOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            try {
                return (long) Double.parseDouble(trimmed);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    /**
     * Fast path for import: read cell values WITHOUT formula evaluation (skips expensive parsing).
     * For FORMULA cells, uses DataFormatter to format cached value (if available).
     */
    private String getCellStringValueFast(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        
        try {
            CellType type = cell.getCellType();
            if (type == CellType.FORMULA) {
                // Try using DataFormatter WITHOUT evaluator - formats cached value only
                DataFormatter formatter = new DataFormatter();
                String formatted = formatter.formatCellValue(cell);
                if (formatted != null && !formatted.isEmpty()) {
                    return formatted;
                }
                // Fallback: use cached result type
                type = cell.getCachedFormulaResultType();
            }

            if (type == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (type == CellType.NUMERIC) {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            } else if (type == CellType.BOOLEAN) {
                return String.valueOf(cell.getBooleanCellValue());
            }
        } catch (Exception ex) {
            // ignore and return empty
        }
        return "";
    }

    /**
     * Fast path for import: read numeric values WITHOUT expensive formula evaluation.
     * For formula cells: try DataFormatter first (cached value), then fallback to evaluate if needed.
     */
    private BigDecimal getCellBigDecimalValueFast(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return BigDecimal.ZERO;
        }

        CellType type = cell.getCellType();
        
        // Try direct read for non-formula cells
        if (type == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (type == CellType.STRING) {
            return parseDecimalOrZero(cell.getStringCellValue());
        }
        
        // For formula cells: try DataFormatter (cheap), then evaluate if needed
        if (type == CellType.FORMULA) {
            try {
                // First: try DataFormatter without evaluator (uses cached value if available)
                DataFormatter formatter = new DataFormatter();
                String formatted = formatter.formatCellValue(cell);
                if (formatted != null && !formatted.isEmpty() && !formatted.equals("?")) {
                    return parseDecimalOrZero(formatted);
                }
                
                // Second: check cached formula result type
                CellType cachedType = cell.getCachedFormulaResultType();
                if (cachedType == CellType.NUMERIC) {
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                }
                
                // Last resort: evaluate formula (may be slow but necessary for missing cache)
                Workbook wb = row.getSheet().getWorkbook();
                FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                CellValue evaluated = evaluator.evaluate(cell);
                if (evaluated != null && evaluated.getCellType() == CellType.NUMERIC) {
                    return BigDecimal.valueOf(evaluated.getNumberValue());
                }
            } catch (Exception ex) {
                // Fallback to cached value
                try {
                    CellType cachedType = cell.getCachedFormulaResultType();
                    if (cachedType == CellType.NUMERIC) {
                        return BigDecimal.valueOf(cell.getNumericCellValue());
                    }
                } catch (Exception ignored) {}
            }
        }
        
        return BigDecimal.ZERO;
    }
}
