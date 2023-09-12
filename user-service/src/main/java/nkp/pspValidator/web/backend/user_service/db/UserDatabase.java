package nkp.pspValidator.web.backend.user_service.db;

import java.util.List;

public interface UserDatabase {
    List<User> getUsers();

    User getUserById(String id);

    void insertUser(User user);

    void updateUser(User user);
}
