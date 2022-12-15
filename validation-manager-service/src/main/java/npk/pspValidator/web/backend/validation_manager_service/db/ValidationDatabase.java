package npk.pspValidator.web.backend.validation_manager_service.db;

import java.util.List;

public interface ValidationDatabase {
    List<Validation> getValidations();

    Validation getValidationById(String id);

    void insertValidation(Validation validation);

    void updateValidation(Validation validation);
}
