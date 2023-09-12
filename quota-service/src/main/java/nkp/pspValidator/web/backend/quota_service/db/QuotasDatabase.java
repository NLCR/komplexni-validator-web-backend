package nkp.pspValidator.web.backend.quota_service.db;

import java.util.Map;

public interface QuotasDatabase {
    Map<String, Integer> getQuotas();

    void setQuota(String key, Integer value);
}
