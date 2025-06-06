// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways;

import javax.swing.JMenu;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.footandcycleways.actions.TagWaysAction;

/**
 * Collection of utilities
 */
public class FootAndCyclewaysPlugin extends Plugin {
    private static FootAndCyclewaysPlugin instance;
    
    public FootAndCyclewaysPlugin(PluginInformation info) {
        super(info);
        instance = this;
        
        JMenu toolsMenu = MainApplication.getMenu().moreToolsMenu;
        MainMenu.add(toolsMenu, new TagWaysAction());
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
      return new FootAndCyclePreferences();
    }
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
    }
    
    public static final FootAndCyclewaysPlugin getInstance() {
        return instance;
    }
}
