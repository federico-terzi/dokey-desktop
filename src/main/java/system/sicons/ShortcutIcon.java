package system.sicons;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ShortcutIcon implements Comparable<ShortcutIcon>{
    private String name;
    private File file;

    public ShortcutIcon(String name, File file) {
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

    @Override
    public int compareTo(@NotNull ShortcutIcon o) {
        return name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
