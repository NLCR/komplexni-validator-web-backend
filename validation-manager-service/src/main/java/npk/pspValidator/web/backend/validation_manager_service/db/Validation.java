package npk.pspValidator.web.backend.validation_manager_service.db;

import org.json.JSONObject;

import java.time.LocalDateTime;

public class Validation {
    public String id;

    public String ownerId;

    public int priority;

    public ValidationState state;

    public String note;

    public String preferedDmf; //bude se validovat oproti tomuto, pokud mozno

    public String parcelId; //TODO: extrahovat ze souboru info

    public String parcelSize; //TODO: detekovat po extrakci

    public LocalDateTime tsCreated;

    public LocalDateTime tsScheduled;
    public LocalDateTime tsStarted;
    public LocalDateTime tsEnded; //FINISHED/ERROR/CANCELED

    public String workingDir; //balicek a vystupy validace

    public String errorMessage;

    public Validation(String id, String ownerId, int priority, ValidationState state, String note) {
        this.id = id;
        this.ownerId = ownerId;
        this.priority = priority;
        this.state = state;
        this.note = note;
    }

    public Validation(String id, String ownerId, int priority, ValidationState state) {
        this(id, ownerId, priority, state, null);
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("ownerId", ownerId);
        result.put("priority", priority);
        result.put("note", note);
        result.put("state", state);
        if (preferedDmf != null) {
            result.put("preferedDmf", preferedDmf);
        }
        if (parcelId != null) {
            result.put("parcelId", parcelId);
        }
        if (parcelSize != null) {
            result.put("parcelSize", parcelSize);
        }
        if (workingDir != null) {
            result.put("workingDir", workingDir);
        }
        if (errorMessage != null) {
            result.put("errorMessage", errorMessage);
        }
        //timestamps
        if (tsCreated != null) {
            result.put("tsCreated", tsCreated.toString());
        }
        if (tsScheduled != null) {
            result.put("tsScheduled", tsScheduled.toString());
        }
        if (tsStarted != null) {
            result.put("tsStarted", tsStarted.toString());
        }
        if (tsEnded != null) {
            result.put("tsEnded", tsEnded.toString());
        }
        return result;
    }

}
