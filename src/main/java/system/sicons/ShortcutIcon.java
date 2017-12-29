package system.sicons;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ShortcutIcon implements Comparable<ShortcutIcon>{
    private String name;
    private File file;
    private String id;

    public ShortcutIcon(String id, String name, File file) {
        this.id = id;
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NotNull ShortcutIcon o) {
        return name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
