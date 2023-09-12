package nkp.pspValidator.web.backend.validation_manager_service.db;


import nkp.pspValidator.web.backend.utils.Config;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String ownerId = rs.getString("owner_id");
                ValidationState state = ValidationState.valueOf(rs.getString("state"));
                String packageName = rs.getString("package_name");
                int packageSizeMB = rs.getInt("package_size_mb");
                String dmfType = rs.getString("dmf_type");
                String preferredDmfVersion = rs.getString("preferred_dmf_version");
                String forcedDmfVersion = rs.getString("forced_dmf_version");
                Integer priority = rs.getInt("priority");
                String note = rs.getString("note");
                Validation validation = new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, extractTimestamps(rs));
                validations.add(validation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return validations;
    }

    @Override
    public List<Validation> getValidationsByOwnerId(String ownerId) {
        List<Validation> validations = new ArrayList<>();
        String sql = "SELECT * FROM validations WHERE owner_id = ? ORDER BY ts_created DESC";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                ValidationState state = ValidationState.valueOf(rs.getString("state"));
                String packageName = rs.getString("package_name");
                int packageSizeMB = rs.getInt("package_size_mb");
                String dmfType = rs.getString("dmf_type");
                String preferredDmfVersion = rs.getString("preferred_dmf_version");
                String forcedDmfVersion = rs.getString("forced_dmf_version");
                Integer priority = rs.getInt("priority");
                String note = rs.getString("note");
                Validation validation = new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, extractTimestamps(rs));
                validations.add(validation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return validations;
    }

    private Validation.Timestamps extractTimestamps(ResultSet rs) throws SQLException {
        String createdStr = rs.getString("ts_created");
        String scheduledStr = rs.getString("ts_scheduled");
        String startedStr = rs.getString("ts_started");
        String endedStr = rs.getString("ts_ended");
        return new Validation.Timestamps(
                createdStr != null ? LocalDateTime.parse(createdStr.replace(" ", "T")) : null,
                scheduledStr != null ? LocalDateTime.parse(scheduledStr.replace(" ", "T")) : null,
                startedStr != null ? LocalDateTime.parse(startedStr.replace(" ", "T")) : null,
                endedStr != null ? LocalDateTime.parse(endedStr.replace(" ", "T")) : null
        );
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
                ValidationState state = ValidationState.valueOf(rs.getString("state"));
                String packageName = rs.getString("package_name");
                int packageSizeMB = rs.getInt("package_size_mb");
                String dmfType = rs.getString("dmf_type");
                String preferredDmfVersion = rs.getString("preferred_dmf_version");
                String forcedDmfVersion = rs.getString("forced_dmf_version");
                Integer priority = rs.getInt("priority");
                String note = rs.getString("note");
                Validation validation = new Validation(id, ownerId, state, packageName, packageSizeMB, dmfType, preferredDmfVersion, forcedDmfVersion, priority, note, extractTimestamps(rs));
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
        String SQL = "INSERT INTO validations " + "(id, owner_id, package_name, package_size_mb, dmf_type, preferred_dmf_version, forced_dmf_version, state, priority, note, ts_created) VALUES " + "(?,?,?,?,?,?,?,?,?,?,?);";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            int index = 1;
            pstmt.setString(index++, validation.id);
            pstmt.setString(index++, validation.ownerId);
            pstmt.setString(index++, validation.packageName);
            pstmt.setInt(index++, validation.packageSizeMB);
            pstmt.setString(index++, validation.dmfType);
            pstmt.setString(index++, validation.preferredDmfVersion);
            pstmt.setString(index++, validation.forcedDmfVersion);
            pstmt.setString(index++, validation.state.name());
            pstmt.setInt(index++, validation.priority);
            pstmt.setString(index++, validation.note);
            pstmt.setObject(index++, validation.timestamps.tsCreated);
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
            pstmt.setObject(index++, validation.timestamps.tsScheduled);
            pstmt.setObject(index++, validation.timestamps.tsStarted);
            pstmt.setObject(index++, validation.timestamps.tsEnded);
            pstmt.setString(index++, validation.id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Integer> getCounters(String email) {
        String sql = "SELECT state, count(*) as count FROM validations WHERE owner_id = ? GROUP BY state";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("state"), rs.getInt("count"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
