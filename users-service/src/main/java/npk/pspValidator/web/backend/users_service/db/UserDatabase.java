package npk.pspValidator.web.backend.users_service.db;

import java.util.List;

public interface UserDatabase {
    List<User> getUsers();

    User getUserById(String id);

    void insertUser(User user);
}
