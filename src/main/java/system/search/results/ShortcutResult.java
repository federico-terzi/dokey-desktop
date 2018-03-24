package system.search.results;

import net.model.KeyboardKeys;
import org.apache.commons.codec.digest.DigestUtils;
import section.model.Item;
import section.model.Section;
import section.model.ShortcutItem;
import system.KeyboardManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.section.SectionInfoResolver;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class ShortcutResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "shortcut_category";

    private ApplicationManager appManager;
    private SectionInfoResolver sectionInfoResolver;
    private Section section;
    private ShortcutItem item;

    public ShortcutResult(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager appManager,
                          SectionInfoResolver sectionInfoResolver, Section section, ShortcutItem item) {
        super(searchEngine, resourceBundle, null);
        this.appManager = appManager;
        this.sectionInfoResolver = sectionInfoResolver;
        this.section = section;
        this.item = item;

        this.isIcon = false;
    }

    @Override
    public String getTitle() {
        return item.getTitle();
    }

    @Override
    public String getDescription() {
        return item.getShortcut();
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                Application application = null;
                if (section.getRelatedAppId() != null) {
                    application = appManager.getApplication(section.getRelatedAppId());
                }
                SectionInfoResolver.SectionInfo sectionInfo = sectionInfoResolver.getSectionInfo(section, 32, application);
                listener.onImageAvailable(sectionInfo.image, getHash(item, section));
            }).start();
        }
    }

    private static String getHash(Item item, Section section) {
        return DigestUtils.md5Hex(section.getStringID());
    }

    @Override
    public String getHash() {
        return getHash(item, section);
    }

    @Override
    public void executeAction() {
        if (section.getRelatedAppId() != null){
            appManager.openApplication(section.getRelatedAppId());
        }

        // Get the shortcut
        List<KeyboardKeys> keys = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(item.getShortcut(), "+");
        while(st.hasMoreTokens()) {
            KeyboardKeys keyboardKey = KeyboardKeys.findFromName(st.nextToken().trim());
            if (keyboardKey != null && !keys.contains(keyboardKey)) {
                keys.add(keyboardKey);
            }
        }

        try {
            new KeyboardManager().sendKeystroke(keys);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
