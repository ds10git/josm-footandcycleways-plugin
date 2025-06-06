// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

public class FootAndCyclePreferences extends DefaultTabPreferenceSetting {
//  private JCheckBox useLuebeckerMethodeOptional;
  private JCheckBox mapTraffic;
  
  public FootAndCyclePreferences() {
    super("icon", tr("FootAndCycleways"), tr("Change settings for FootAndCycleways plugin."));
  //  useLuebeckerMethodeOptional = new JCheckBox(tr("Map optional cycleways with \"Lübecker Methode\""), FootAndCycleProperties.OPTIONAL_LUEBECK.get());
    mapTraffic = new JCheckBox(tr("Map traffic load for highways"), FootAndCycleProperties.MAP_TRAFFIC.get());
  }
  
  @Override
  public void addGui(PreferenceTabbedPane gui) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
    //panel.add(useLuebeckerMethodeOptional);
    panel.add(mapTraffic);
    
    createPreferenceTabWithScrollPane(gui, panel);
  }

  @Override
  public boolean ok() {
 //   FootAndCycleProperties.OPTIONAL_LUEBECK.put(useLuebeckerMethodeOptional.isSelected());
    FootAndCycleProperties.MAP_TRAFFIC.put(mapTraffic.isSelected());
    
    return false;
  }
  
  @Override
  public boolean isExpert() {
    return false;
  }
}
