package nkp.pspValidator.web.backend.validation_manager_service.db;

import java.util.List;
import java.util.Map;

public interface ValidationDatabase {
    List<Validation> getValidations();

    List<Validation> getValidationsByOwnerId(String id);

    Validation getValidationById(String id);

    void insertValidation(Validation validation);

    void updateValidation(Validation validation);

    Map<String, Integer> getCounters(String email);
}
