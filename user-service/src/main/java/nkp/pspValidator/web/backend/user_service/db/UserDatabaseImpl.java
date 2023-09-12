package nkp.pspValidator.web.backend.user_service.db;


import nkp.pspValidator.web.backend.utils.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseImpl implements UserDatabase {

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    Config.instanceOf().getUserServiceDbUrl(),
                    Config.instanceOf().getUserServiceDbLogin(),
                    Config.instanceOf().getUserServiceDbPassword()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getString("id"),
                        rs.getString("email"),
                        rs.getString("picture_url"),
                        rs.getString("given_name"),
                        rs.getString("family_name"),
                        rs.getString("name"),
                        rs.getBoolean("verified"),
                        rs.getBoolean("admin"),
                        rs.getString("institution_name"),
                        rs.getString("institution_sigla")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public User getUserById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getString("id"),
                        rs.getString("email"),
                        rs.getString("picture_url"),
                        rs.getString("given_name"),
                        rs.getString("given_name"),
                        rs.getString("name"),
                        rs.getBoolean("verified"),
                        rs.getBoolean("admin"),
                        rs.getString("institution_name"),
                        rs.getString("institution_sigla")
                );
                return user;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertUser(User user) {
        String SQL = "INSERT INTO users " + "(id, email, picture_url, given_name, family_name, name, verified, admin, institution_name, institution_sigla) VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        //System.out.println(SQL);
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            int index = 1;
            pstmt.setString(index++, user.id);
            pstmt.setString(index++, user.email);
            pstmt.setString(index++, user.pictureUrl);
            pstmt.setString(index++, user.givenName);
            pstmt.setString(index++, user.familyName);
            pstmt.setString(index++, user.name);
            pstmt.setBoolean(index++, user.verified);
            pstmt.setBoolean(index++, user.admin);
            pstmt.setString(index++, user.institutionName);
            pstmt.setString(index++, user.institutionSigla);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateUser(User user) {
        String SQL = "UPDATE users SET " + "email = ?, picture_url = ?, given_name = ?, family_name = ?, name = ?, verified = ?, admin = ?, institution_name = ?, institution_sigla = ? WHERE id = ?;";
        //System.out.println(SQL);
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            int index = 1;
            pstmt.setString(index++, user.email);
            pstmt.setString(index++, user.pictureUrl);
            pstmt.setString(index++, user.givenName);
            pstmt.setString(index++, user.familyName);
            pstmt.setString(index++, user.name);
            pstmt.setBoolean(index++, user.verified);
            pstmt.setBoolean(index++, user.admin);
            pstmt.setString(index++, user.institutionName);
            pstmt.setString(index++, user.institutionSigla);
            pstmt.setString(index++, user.id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
