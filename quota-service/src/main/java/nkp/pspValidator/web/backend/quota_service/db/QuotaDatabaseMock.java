package nkp.pspValidator.web.backend.quota_service.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuotaDatabaseMock implements QuotasDatabase {

    Map<String, Integer> values = initQuotas();

    private Map<String, Integer> initQuotas() {
        Map<String, Integer> result = new HashMap<>();
        result.put("maxUploadSizeMB", 100);
        result.put("maxParallelJobs", 20); //
        result.put("maxParallelExtractionJobs", 4); //joby pro extrakci zipu na lokalnim fs
        result.put("maxParallelValidationJobs", 4); //joby resici samotnou validaci
        result.put("maxParallelArchivationJobs", 3); //joby resici archivaci
        result.put("maxParallelDeletionJobs", 3); //joby resici mazani
        result.put("timeToArchiveValidationH", 24); //cas od skonceni validace do smazani balicku (logy a zaznam v DB zustavaji)
        result.put("timeToDeleteValidationH", 48); //cas od skonceni validace do smazani vysledku validace (musi byt >=timeToArchiveValidationH)
        return result;
    }

    @Override
    public Map<String, Integer> getQuotas() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public void setQuota(String key, Integer value) {
        values.put(key, value);
    }
}
