package npk.pspValidator.web.backend.validation_manager_service.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationDatabaseMock implements ValidationDatabase {

    private final List<Validation> validations = initValidations();

    private List<Validation> initValidations() {
        List<Validation> result = new ArrayList<>();
        result.add(new Validation("11ed4447-731c-4570-896c-e698aba83a6e", "1", 1, ValidationState.READY_FOR_EXTRACTION));
        result.add(new Validation("39390afc-0ea3-4ca7-bc0f-e274fc8388a3", "1", 1, ValidationState.TO_BE_EXTRACTED));
        result.add(new Validation("47d0d096-ae02-4ee4-a1ee-a4491cf57806", "1", 1, ValidationState.EXTRACTING));
        result.add(new Validation("28549df8-c735-4385-becc-5b94058a9903", "2", 2, ValidationState.READY_FOR_EXECUTION));
        result.add(new Validation("64cb9663-9aac-4800-af5e-d35290e73dff", "3", 2, ValidationState.TO_BE_EXECUTED));
        result.add(new Validation("02b61275-5e73-4c26-9d45-a31e6e24b5ae", "3", 2, ValidationState.EXECUTING));
        result.add(new Validation("9aae67eb-3b98-4bbe-aa8c-2a36fac56108", "3", 2, ValidationState.FINISHED));
        result.add(new Validation("70f17c15-e860-490e-8e52-ffd87e2a95ed", "3", 2, ValidationState.ERROR));
        result.add(new Validation("a50a5340-944e-43fc-a987-3ae5613971fa", "3", 2, ValidationState.CANCELED));
        result.add(new Validation("c81a1a83-063e-4fa0-88bc-9c62382d84cc", "3", 2, ValidationState.TO_BE_ARCHIVED));
        result.add(new Validation("64eb12c9-5b9d-416c-a6fb-d50ffaa69355", "3", 2, ValidationState.ARCHIVING));
        result.add(new Validation("f32f8775-80de-41e1-b368-481035b12358", "3", 2, ValidationState.ARCHIVED));
        result.add(new Validation("686edf9c-d721-45a1-91b2-da9eb6cc36c5", "3", 2, ValidationState.T0_BE_DELETED));
        result.add(new Validation("102fd584-dd59-46e3-9d9e-ea1029818741", "3", 2, ValidationState.DELETING));
        result.add(new Validation("87d5ee48-0467-4212-82e6-56d10f01f13a", "3", 2, ValidationState.DELETED));
        return result;
    }

    @Override
    public List<Validation> getValidations() {
        return Collections.unmodifiableList(validations);
    }

    @Override
    public Validation getValidationById(String id) {
        for (Validation validation : validations) {
            if (validation.id.equals(id)) {
                return validation;
            }
        }
        return null;
    }

    @Override
    public void insertValidation(Validation validation) {
        validations.add(validation);
    }

    @Override
    public void updateValidation(Validation validation) {
        throw new RuntimeException("not implemented");
    }
}
