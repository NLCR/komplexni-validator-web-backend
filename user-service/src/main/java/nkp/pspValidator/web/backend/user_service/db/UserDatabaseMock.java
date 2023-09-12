package nkp.pspValidator.web.backend.user_service.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UserDatabaseMock implements UserDatabase {

    private final List<User> users = initUsers();

    private List<User> initUsers() {
        List<User> result = new ArrayList<>();
        /*result.add(new User("1", "kv-admin@nkp.cz", Role.ADMIN));
        result.add(new User("2", "pepa@seznam.cz", Role.USER_FROM_PUBLIC));
        result.add(new User("3", "kv-person@mzk.cz", Role.USER_FROM_INSTITUTION));*/
        return result;
    }

    @Override
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public User getUserById(String id) {
        for (User user : users) {
            if (user.id.equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void insertUser(User user) {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void updateUser(User user) {
        throw new RuntimeException("not implemented yet");
    }
}
