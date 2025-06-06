// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.footandcycleways.FootAndCycleSeparateTaggingDialog;
import org.openstreetmap.josm.plugins.footandcycleways.FootAndCycleTaggingDialog;

public class TagWaysAction extends JosmAction {
  /**
   * Create a new SplitObjectAction.
   */
  public TagWaysAction() {
      super(tr("Tag Foot and Cycleways"), "preferences/icon", tr("Tag footways and cycleways on the road."),
          null
            /*  Shortcut.registerShortcut("tools:tagsidewalkbicycle", tr("More tools: {0}", tr("FootAndCycleways")), KeyEvent.VK_Z, Shortcut.DIRECT)*/,
              true);
    //  putValue("help", ht("/Action/SplitObject"));
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    if (!checkSelection(getLayerManager().getEditDataSet().getSelectedWays())) {
      showWarningNotification(tr("You have to select at least one way with highway tag to tag foot and cycleways."));
      return;
    }
    
    Collection<Way> selectedWays = getLayerManager().getEditDataSet().getSelectedWays();
    ArrayList<Way> usableRoadWays = new ArrayList<>();
    ArrayList<Way> usableSideWays = new ArrayList<>();
    
    for(Way w : selectedWays) {
        String highway = w.get("highway");
        
        if(((highway != null && (highway.startsWith("primary") || highway.startsWith("secondary") || highway.startsWith("primary")
            || highway.startsWith("tertiary") || highway.startsWith("residential") || highway.startsWith("unclassified"))) || ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK))
            && ((e.getModifiers() & ActionEvent.CTRL_MASK) != ActionEvent.CTRL_MASK)) {
          usableRoadWays.add(w);
        }
        else if(highway == null  || highway.startsWith("path") || highway.startsWith("footway") || highway.startsWith("cycleway") 
            || highway.startsWith("service") || highway.startsWith("track") || ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
          usableSideWays.add(w);
        }
    }
    System.out.println(usableRoadWays.size() + " " +usableSideWays.size());
    if(!usableRoadWays.isEmpty() && (e.getModifiers() & ActionEvent.CTRL_MASK) != ActionEvent.CTRL_MASK) {
      FootAndCycleTaggingDialog input = new FootAndCycleTaggingDialog(usableRoadWays);
      input.showDialog();
    }
    else if(!usableSideWays.isEmpty() && (e.getModifiers() & ActionEvent.SHIFT_MASK) != ActionEvent.SHIFT_MASK) {
      FootAndCycleSeparateTaggingDialog input = new FootAndCycleSeparateTaggingDialog(usableSideWays);
      input.showDialog();      
    }
  }
  
  @Override
  protected void updateEnabledState() {
      updateEnabledStateOnCurrentSelection();
  }
  
  @Override
  protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
      if (selection == null) {
          setEnabled(false);
          return;
      }
      setEnabled(checkSelection(selection));
  }
  
  private static boolean checkSelection(Collection<? extends OsmPrimitive> selection) {
    boolean result = false;
    
    for (OsmPrimitive p : selection) {
      if (p instanceof Way) {
        String highway = p.get("highway");
        
        if(highway == null || (highway.startsWith("primary") || highway.startsWith("secondary") || highway.startsWith("primary")
            || highway.startsWith("tertiary") || highway.startsWith("residential") || highway.startsWith("unclassified") 
            || highway.startsWith("path") || highway.startsWith("footway") || highway.startsWith("cycleway") 
            || highway.startsWith("service") || highway.startsWith("track"))) {
          result = true;
          break;
        }
      }
    }
    
    return result;
  }
  
  private static void showWarningNotification(String msg) {
    new Notification(msg).setIcon(JOptionPane.WARNING_MESSAGE).show();
  }
  
  public static ChangePropertyCommand createCommand(OsmPrimitive object, String key, String value, Hashtable<String,Command> cmdSet) {
    cmdSet.remove(key);
    
    return new ChangePropertyCommand(object, key, value);
  }
}
