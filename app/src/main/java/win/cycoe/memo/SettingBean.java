package win.cycoe.memo;

/**
 * Created by cycoe on 17-8-15.
 */

public class SettingBean {

    public String settingTitle;
    public String settingContent;
    public int settingCheck;

    public SettingBean(String settingTitle, String settingContent, int settingCheck) {
        this.settingTitle = settingTitle;
        this.settingContent = settingContent;
        this.settingCheck = settingCheck;
    }

}
