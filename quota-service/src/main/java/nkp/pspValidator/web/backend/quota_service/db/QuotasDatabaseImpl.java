package nkp.pspValidator.web.backend.quota_service.db;

import nkp.pspValidator.web.backend.utils.Config;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class QuotasDatabaseImpl implements QuotasDatabase {

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    Config.instanceOf().getQuotaServiceDbUrl(),
                    Config.instanceOf().getQuotaServiceDbLogin(),
                    Config.instanceOf().getQuotaServiceDbPassword()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    @Override
    public Map<String, Integer> getQuotas() {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT * FROM quotas ORDER BY quota_name";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("quota_name");
                Integer value = rs.getInt("quota_value");
                result.put(name, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void setQuota(String key, Integer value) {
        String SQL = "UPDATE quotas "
                + "SET quota_value = ? "
                + "WHERE quota_name = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, value);
            pstmt.setString(2, key.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
