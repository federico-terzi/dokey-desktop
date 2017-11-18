package net.model;

import org.json.JSONObject;

public class RemoteApplication {
    private String name;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject json() {
        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("path", getPath());
        return json;
    }

    @Override
    public String toString() {
        return "RemoteApplication{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
