package npk.pspValidator.web.backend.validation_manager_service.db;


import npk.pspValidator.web.backend.utils.Config;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationDatabaseImpl implements ValidationDatabase {

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    Config.instanceOf().getValidationMgrServiceDbUrl(),
                    Config.instanceOf().getValidationMgrServiceDbLogin(),
                    Config.instanceOf().getValidationMgrServiceDbPassword()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    @Override
    public List<Validation> getValidations() {
        List<Validation> validations = new ArrayList<>();
        String sql = "SELECT * FROM validations ORDER BY ts_created DESC";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String ownerId = rs.getString("owner_id");
                Integer priority = rs.getInt("priority");
                ValidationState state = ValidationState.valueOf(rs.getString("state"));
                String note = rs.getString("note");
                Validation validation = new Validation(id, ownerId, priority, state, note);
                appendTimestamps(rs, validation);
                validations.add(validation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return validations;
    }

    private void appendTimestamps(ResultSet rs, Validation validation) throws SQLException {
        //created
        String createdStr = rs.getString("ts_created");
        if (createdStr != null) {
            validation.tsCreated = LocalDateTime.parse(createdStr.replace(" ", "T"));
        }
        //scheduled
        String scheduledStr = rs.getString("ts_scheduled");
        if (scheduledStr != null) {
            validation.tsScheduled = LocalDateTime.parse(createdStr.replace(" ", "T"));
        }
        //started
        String startedStr = rs.getString("ts_started");
        if (startedStr != null) {
            validation.tsStarted = LocalDateTime.parse(startedStr.replace(" ", "T"));
        }
        //ended
        String endedStr = rs.getString("ts_ended");
        if (endedStr != null) {
            validation.tsEnded = LocalDateTime.parse(endedStr.replace(" ", "T"));
        }
    }

    @Override
    public Validation getValidationById(String id) {
        String sql = "SELECT * FROM validations WHERE id = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String ownerId = rs.getString("owner_id");
                Integer priority = rs.getInt("priority");
                ValidationState state = ValidationState.valueOf(rs.getString("state"));
                String note = rs.getString("note");
                Validation validation = new Validation(id, ownerId, priority, state, note);
                appendTimestamps(rs, validation);
                return validation;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertValidation(Validation validation) {
        String SQL = "INSERT INTO validations " + "(id, owner_id, priority, state, note, ts_created) VALUES " + "(?, ?, ?, ?, ?, ?);";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            int index = 1;
            pstmt.setString(index++, validation.id);
            pstmt.setString(index++, validation.ownerId);
            pstmt.setInt(index++, validation.priority);
            pstmt.setString(index++, validation.state.name());
            pstmt.setString(index++, validation.note);
            pstmt.setObject(index++, validation.tsCreated);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateValidation(Validation validation) {
        String SQL = "UPDATE validations SET "
                + "priority = ?, "
                + "state = ?, "
                + "note = ?, "
                + "ts_scheduled = ?, "
                + "ts_started = ?, "
                + "ts_ended = ? "
                + "WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            int index = 1;
            pstmt.setInt(index++, validation.priority);
            pstmt.setString(index++, validation.state.name());
            pstmt.setString(index++, validation.note);
            pstmt.setObject(index++, validation.tsScheduled);
            pstmt.setObject(index++, validation.tsStarted);
            pstmt.setObject(index++, validation.tsEnded);
            pstmt.setString(index++, validation.id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
