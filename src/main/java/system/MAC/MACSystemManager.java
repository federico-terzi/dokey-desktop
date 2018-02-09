package system.MAC;

import system.SystemManager;

public class MACSystemManager implements SystemManager {
    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public boolean suspend() {
        return false;
    }

    @Override
    public boolean volumeDown() {
        return false;
    }

    @Override
    public boolean volumeUp() {
        return false;
    }

    @Override
    public boolean volumeMute() {
        return false;
    }

    @Override
    public boolean playOrPause() {
        return false;
    }

    @Override
    public boolean nextTrack() {
        return false;
    }

    @Override
    public boolean previousTrack() {
        return false;
    }
}
