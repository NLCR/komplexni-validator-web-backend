package nkp.pspValidator.web.backend.validation_manager_service.db;

import java.time.LocalDateTime;

public class Validation {
    public final String id;
    public final String ownerId;
    public final ValidationState state;

    public final String packageName;
    public final int packageSizeMB;

    public final String dmfType;
    public final String preferredDmfVersion; //bude se validovat oproti tomuto, pokud mozno
    public final String forcedDmfVersion; //bude se validovat oproti tomuto, bez ohledu na to, co je v balicku

    public final int priority;
    public final String note;

    public final Timestamps timestamps;

    public Validation withState(ValidationState newState) {
        return new Validation(id, ownerId, newState, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, timestamps.copy());
    }

    public Validation withTsScheduled(LocalDateTime dateTime) {
        return new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, timestamps.copy().withTsScheduled(dateTime));
    }

    public Validation withTsStarted(LocalDateTime dateTime) {
        return new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, timestamps.copy().withTsStarted(dateTime));
    }

    public Validation withTsEnded(LocalDateTime dateTime) {
        return new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, timestamps.copy().withTsEnded(dateTime));
    }

    public Validation withPriority(int priority) {
        return new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, timestamps.copy());
    }

    public static final class Timestamps {
        public final LocalDateTime tsCreated;
        public final LocalDateTime tsScheduled;
        public final LocalDateTime tsStarted;
        public final LocalDateTime tsEnded; //FINISHED/ERROR/CANCELED

        public Timestamps(LocalDateTime tsCreated, LocalDateTime tsScheduled, LocalDateTime tsStarted, LocalDateTime tsEnded) {
            this.tsCreated = tsCreated;
            this.tsScheduled = tsScheduled;
            this.tsStarted = tsStarted;
            this.tsEnded = tsEnded;
        }

        public Timestamps copy() {
            return new Timestamps(tsCreated, tsScheduled, tsStarted, tsEnded);
        }

        public Timestamps withTsScheduled(LocalDateTime scheduled) {
            return new Timestamps(tsCreated, scheduled, tsStarted, tsEnded);
        }

        public Timestamps withTsStarted(LocalDateTime started) {
            return new Timestamps(tsCreated, tsScheduled, started, tsEnded);
        }

        public Timestamps withTsEnded(LocalDateTime ended) {
            return new Timestamps(tsCreated, tsScheduled, tsStarted, ended);
        }
    }

    public Validation(String id, String ownerId, ValidationState state, String packageName, int packageSizeMB, String dmfType, String preferredDmfVersion, String forcedDmfVersion, int priority, String note, Timestamps timestamps) {
        this.id = id;
        this.ownerId = ownerId;
        this.state = state;
        this.packageName = packageName;
        this.packageSizeMB = packageSizeMB;
        this.dmfType = dmfType;
        this.preferredDmfVersion = preferredDmfVersion;
        this.forcedDmfVersion = forcedDmfVersion;
        this.priority = priority;
        this.note = note;
        this.timestamps = timestamps;
    }

}
