package npk.pspValidator.web.backend.quotas_service.db;

import java.util.Map;

public interface QuotasDatabase {
    Map<String, Integer> getQuotas();

    void setQuota(String key, Integer value);
}
