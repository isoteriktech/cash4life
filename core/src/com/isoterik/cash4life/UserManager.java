package com.isoterik.cash4life;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import io.github.isoteriktech.xgdx.Component;

import java.io.File;

public class UserManager extends Component {
    private User user;

    @Override
    public void start() {
        user = new Json().fromJson(User.class, getJsonFile());
    }

    private FileHandle getJsonFile() {
        String currentPath = Gdx.files.internal(GlobalConstants.SHARED_ASSETS_HOME).path();
        String fileDirectory = currentPath + File.separatorChar + "json";
        return Gdx.files.local(fileDirectory + File.separatorChar + "user.json");
    }

    public User getUser() {
        return user;
    }

    public void deposit(float amount) {
        user.deposit(amount);
        save();
    }

    public void withdraw(float amount) {
        user.withdraw(amount);
        save();
    }

    public void save() {
        new Json().toJson(user, getJsonFile());
    }

    public void reset() {
        User defaultState = new User(
                "",
                "",
                "",
                0
        );
        new Json().toJson(defaultState, getJsonFile());
    }
}