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

    public static RemoteApplication fromJson(JSONObject jsonApp) {
        RemoteApplication app = new RemoteApplication();
        app.setName(jsonApp.getString("name"));
        app.setPath(jsonApp.getString("path"));
        return app;
    }

    @Override
    public String toString() {
        return "RemoteApplication{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteApplication that = (RemoteApplication) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return path != null ? path.equals(that.path) : that.path == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
