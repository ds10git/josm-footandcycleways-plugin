// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.plugins.footandcycleways.actions.TagWaysAction;
import org.openstreetmap.josm.plugins.footandcycleways.constants.Keys;
import org.openstreetmap.josm.plugins.footandcycleways.constants.Values;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

public class FootAndCycleSeparateTaggingDialog extends ExtendedDialog {
  private final ArrayList<Way> ways;
  private Direction direction;
  
  public FootAndCycleSeparateTaggingDialog(final ArrayList<Way> ways) {
    super(MainApplication.getMainFrame(), tr("Foot and cycleway tagging"), new String[] {tr("OK"), tr("Cancel")}, true /* modal */);
    setRememberWindowGeometry(getClass().getName() + ".geometry",
        WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(270, 750)));
    setButtonIcons("ok", "cancel");
    this.ways = ways;
    
    direction = new Direction(ways);
    
    setContent(direction.createPanel());
  }
  
  @Override
  protected void buttonAction(int i, ActionEvent evt) {
      if (i == 0) { // OK Button
        direction.handleChangedTags(this.ways);
      }
      
      super.buttonAction(i, evt);
  }
  
  private final static class Direction {
    private ImageSelectionButton sidewalkWithoutSign;
    private ImageSelectionButton sidewalk;
    private ImageSelectionButton shared;
    private ImageSelectionButton segregated;
    private ImageSelectionButton cyclewayOnKerb;
    private ImageSelectionButton cycleStreet;
    private ImageSelectionButton cycleZone;
    private ImageSelectionButton bicycleFree;
    private ImageSelectionButton bicycleUsable;
    private ImageSelectionButton bicycleFreeBotDirection;
    private ImageSelectionButton noOneway;
    
    private ImageSelectionButton agriculturalFree;
    private ImageSelectionButton forestryFree;
    private ImageSelectionButton destinationFree;
    
    public Direction(final ArrayList<Way> ways) {
      sidewalkWithoutSign = new ImageSelectionButton("footway", tr("The way is only a sidewalk without a sign"));
      sidewalk = new ImageSelectionButton("DE_239", tr("The way is only a sidewalk"));
      shared = new ImageSelectionButton("DE_240", tr("The way is a sidewalk and a cycleway that are shared"));
      segregated = new ImageSelectionButton("DE_241-30", tr("The way is a sidewalk and a cycleway that are segregated"));
      cyclewayOnKerb = new ImageSelectionButton("DE_237", tr("The way is only a cycleway"));
      cycleStreet = new ImageSelectionButton("DE_244_1", tr("The way is a bicycle street"));
      cycleZone = new ImageSelectionButton("DE_244_3", tr("The way is a bicycle zone"));
      bicycleFree = new ImageSelectionButton("DE_1022-10", tr("Usage of the way by bicylcles is optional"));
      bicycleFreeBotDirection = new ImageSelectionButton("DE_1000-33", tr("Usage of the way by bicylcles is optional in both directions"));
      destinationFree = new ImageSelectionButton("DE_1020-30", tr("Usage of the way by other vehicles is granted for destionation"));
      agriculturalFree = new ImageSelectionButton("DE_1026-36", tr("Usage of the way by other vehicles is granted for agricultural"));
      forestryFree = new ImageSelectionButton("DE_1026-37", tr("Usage of the way by other vehicles is granted for forestry"));
      bicycleUsable = new ImageSelectionButton("angebotsradweg", tr("Usage of the cycleway by bicylcles is optional (without traffic sign)"));
      noOneway = new ImageSelectionButton("DE_1000-31", tr("Bicycles can drive on the way in both directions"));
      
      select(ways);
      addIncompatibilities();
    }
    
    public void addIncompatibilities() {
      ImageSelectionButton.handleIncompatible(null,false,true,shared,segregated);
      ImageSelectionButton.handleIncompatible(null,false,true,bicycleFree,bicycleFreeBotDirection,bicycleUsable,cyclewayOnKerb,cycleStreet,cycleZone,shared,segregated);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalkWithoutSign,sidewalk,bicycleUsable,bicycleFreeBotDirection,cyclewayOnKerb,cycleStreet,cycleZone,shared,segregated);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalkWithoutSign,sidewalk,shared,segregated);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalkWithoutSign,bicycleFree);
      ImageSelectionButton.handleIncompatible(new ImageSelectionButton[] {destinationFree,forestryFree,agriculturalFree},true,true,bicycleFree,bicycleFreeBotDirection,bicycleUsable);
      
      ItemListener access = e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          if(!cyclewayOnKerb.isSelected() && !shared.isSelected() && !segregated.isSelected() && !sidewalk.isSelected()
              && !sidewalkWithoutSign.isSelected() &&!cycleStreet.isSelected() && !cycleZone.isSelected()) {
            shared.setSelected(true);
          }
        }
      };
      
      destinationFree.addItemListener(access);
      agriculturalFree.addItemListener(access);
      forestryFree.addItemListener(access);
      
      bicycleFreeBotDirection.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          noOneway.setSelected(true);
        }
      });
    }
    
    private void select(ArrayList<Way> ways) {
      int footway = 0;
      int footwayWithoutSign = 0;
      int cyclewayOnKerb = 0;
      int segregated = 0;
      int shared = 0;
      int cycleStreet = 0;
      int cycleZone = 0;
      
      int bicycleFree = 0;
      int bicycleFreeBothWays = 0;
      int bicycleUsable = 0;
      
      int oneway = 0;
      
      for(Way w : ways) {
        String trafficSign = w.get(Keys.TRAFFIC_SIGN);
        boolean noneSign = Objects.equals(trafficSign, Values.NONE);
        String highway = w.get(Keys.HIGHWAY);
        
        if(noneSign && !Objects.equals(highway, Values.FOOTWAY)) {
          bicycleUsable++;
        }
        else if(Objects.equals(w.get(Keys.SEGREGATED), Values.YES)) {
          segregated++;
        }
        else if(Objects.equals(w.get(Keys.SEGREGATED), Values.NO) && trafficSign != null && !trafficSign.contains(Values.SIGN_BICYLCE_FREE) && !trafficSign.contains(Values.SIGN_BICYLCE_BOTH_WAYS)) {
          shared++;
        }
        
        if(Objects.equals(highway, Values.CYCLEWAY)) {
          if(noneSign) {
            bicycleUsable++;
          }
          else {
            cyclewayOnKerb++;
          }
        }
        else if(Objects.equals(highway, Values.FOOTWAY)) {
          if(noneSign) {
            footwayWithoutSign++;
          }
          else {
            footway++;
          }
        }
        System.out.println("agricultural="+(trafficSign != null && trafficSign.contains(Values.SIGN_AGRICULTURAL)));
        if(trafficSign != null && (trafficSign.contains(Values.SIGN_AGRICULTURAL) ||  trafficSign.contains(Values.SIGN_AGRICULTURAL_FORESTRY))) {
          this.agriculturalFree.setSelected(true);
        }
        if(trafficSign != null && trafficSign.contains(Values.SIGN_DESTINATION)) {
          this.destinationFree.setSelected(true);
        }
        if(trafficSign != null && (trafficSign.contains(Values.SIGN_FORESTRY) || trafficSign.contains(Values.SIGN_AGRICULTURAL_FORESTRY))) {
          this.forestryFree.setSelected(true);
        }
        if(trafficSign != null && trafficSign.contains(Values.SIGN_BICYLCE_FREE)) {
          bicycleFree++;
        }
        if(trafficSign != null && trafficSign.contains(Values.SIGN_BICYCLE_ROAD) 
            || (Objects.equals(w.get(Keys.BICYCLE_ROAD),Values.YES) && (trafficSign == null || !trafficSign.contains(Values.SIGN_BICYCLE_ZONE)))) {
          cycleStreet++;
        }
        if(trafficSign != null && trafficSign.contains(Values.SIGN_BICYCLE_ZONE)) {
          cycleZone++;
        }
        if(Objects.equals(w.get(Keys.ONEWAY_BICYCLE), Values.YES) || Objects.equals(w.get(Keys.ONEWAY), Values.YES)) {
          oneway++;
        }
        if(trafficSign != null && trafficSign.contains(Values.SIGN_BICYLCE_BOTH_WAYS)) {
          bicycleFreeBothWays++;
        }
System.out.println(footwayWithoutSign + " " + bicycleFree +" "+bicycleUsable+" "+noneSign);
        if(footwayWithoutSign > 0 && footwayWithoutSign >= footway && footwayWithoutSign >= shared && footwayWithoutSign >= segregated && footwayWithoutSign >= bicycleUsable && footwayWithoutSign >= bicycleFree && footwayWithoutSign >= cycleZone && footwayWithoutSign >= cycleStreet && footwayWithoutSign >= cyclewayOnKerb) {
          this.sidewalkWithoutSign.setSelected(true);
        }
        else if(cycleZone > 0 && cycleZone >= cycleStreet && cycleZone >= cyclewayOnKerb && cycleZone >= footway && cycleZone >= shared && cycleZone >= segregated && cycleZone >= bicycleUsable && cycleZone >= bicycleFree) {
          this.cycleZone.setSelected(true);
        }
        else if(cycleStreet > 0 && cycleStreet >= cyclewayOnKerb && cycleStreet >= footway && cycleStreet >= shared && cycleStreet >= segregated && cycleStreet >= bicycleUsable && cycleStreet >= bicycleFree) {
          this.cycleStreet.setSelected(true);
        }
        else if(cyclewayOnKerb > 0 && cyclewayOnKerb >= footway&& cyclewayOnKerb >= shared && cyclewayOnKerb >= segregated && cyclewayOnKerb >= bicycleUsable && cyclewayOnKerb >= bicycleFree) {
          this.cyclewayOnKerb.setSelected(true);
        }
        else if(footway > 0 && footway >= shared && footway >= segregated && footway >= bicycleUsable && footway >= bicycleFree) {
          this.sidewalk.setSelected(true);
          this.bicycleFree.setSelected(Objects.equals(w.get(Keys.BICYCLE), Values.YES));
        }
        else if(shared > 0 && shared >= segregated && shared >= bicycleUsable && shared >= bicycleFree) {
          this.shared.setSelected(true);
        }
        else if(segregated > 0 && segregated >= bicycleUsable && segregated >= bicycleFree) {
          this.segregated.setSelected(true);
        }
        else if(bicycleUsable > 0 && bicycleUsable >= bicycleFree) {
          this.bicycleUsable.setSelected(true);
        }
        else if(bicycleFree > 0 && bicycleFree >= bicycleFreeBothWays) {
          this.bicycleFree.setSelected(true);
        }
        else if(bicycleFreeBothWays > 0) {
          this.bicycleFreeBotDirection.setSelected(true);
          this.noOneway.setSelected(true);
        }
        
        if(oneway == 0 && ((footway > 0 && bicycleFree > 0) || shared > 0 || segregated > 0 || cyclewayOnKerb > 0 || bicycleUsable > 0 || bicycleFreeBothWays > 0)) {
          this.noOneway.setSelected(true);
        }
      }
      
      System.out.println("shared="+shared+"\nsegregated="+segregated+"\noneway="+oneway);
    }
    
    private JPanel createPanel() {
      final ImageIcon background = ImageProvider.get("cycleway");
      JPanel content = new JPanel(new GridBagLayout()) {
        @Override
        public void paintComponent(Graphics g) {
          super.paintComponent(g);
          
          background.paintIcon(null, g, (getWidth()-background.getIconWidth())/2+2, (getHeight()-background.getIconHeight())/2);
        }
      };
      content.setLayout(new GridBagLayout());
      
      GBC gc = GBC.std(2, 1).insets(3, 5, 0, 5);
      
      content.add(sidewalkWithoutSign, gc);
      content.add(sidewalk, gc.grid(2, 2).insets(3, 0, 0, 5));
      content.add(shared, gc.grid(2, 3));
      content.add(segregated, gc.grid(2, 4));
      content.add(cyclewayOnKerb, gc.grid(2, 5));
      content.add(cycleStreet, gc.grid(2, 6));
      content.add(cycleZone, gc.grid(2, 7));
      content.add(bicycleFree, gc.grid(2, 8));
      content.add(bicycleFreeBotDirection, gc.grid(2, 9));
      content.add(destinationFree, gc.grid(2, 10));
      content.add(agriculturalFree, gc.grid(2, 11));
      content.add(forestryFree, gc.grid(2, 12));
      content.add(bicycleUsable, gc.grid(2, 13));
      content.add(Box.createRigidArea(new Dimension(0,10)), gc.grid(2, 14));
      content.add(noOneway, gc.grid(2, 15));
      
      return content;
    }
    
    private void handleChangedTags(ArrayList<Way> ways) {
      List<Command> cmds = new LinkedList<>();
      
      for(Way w : ways) {
        Hashtable<String,Command> cmdSet = new Hashtable<>();
        
        for(String key : Keys.POSSIBLE_SEPERATE) {
          if(w.hasKey(key)) {
            cmdSet.put(key, new ChangePropertyCommand(w, key, ""));
          }
        }
        
        if(destinationFree.isSelected() || cycleStreet.isSelected() || cycleZone.isSelected()) {
          String test = w.get(Keys.HIGHWAY);
          
          if(test == null || (!Objects.equals(test, Values.RESIDENTIAL) && !Objects.equals(test, Values.SERVICE)
              && !Objects.equals(test, Values.UNCLASSIFIED))) {
            cmds.add(TagWaysAction.createCommand(w, Keys.HIGHWAY, Values.SERVICE, cmdSet));
          }
        }
        else if(agriculturalFree.isSelected() || forestryFree.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.HIGHWAY, Values.TRACK, cmdSet));
        }
        else if(shared.isSelected() || segregated.isSelected() || bicycleUsable.isSelected() || (bicycleFree.isSelected() && !sidewalk.isSelected()) || bicycleFreeBotDirection.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.HIGHWAY, Values.PATH, cmdSet));
        }
        else if((bicycleUsable.isSelected() && !sidewalk.isSelected() && !sidewalkWithoutSign.isSelected()) || cyclewayOnKerb.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.HIGHWAY, Values.CYCLEWAY, cmdSet));
        }
        else if(sidewalk.isSelected() || sidewalkWithoutSign.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.HIGHWAY, Values.FOOTWAY, cmdSet));
        }
        
        if(shared.isSelected() || cyclewayOnKerb.isSelected() || segregated.isSelected() || cycleStreet.isSelected() || cycleZone.isSelected()|| (bicycleFree.isSelected() && !sidewalk.isSelected()) || bicycleFreeBotDirection.isSelected() || bicycleUsable.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE, Values.DESIGNATED, cmdSet));
        }
        else if(bicycleFree.isSelected() && sidewalk.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE, Values.YES, cmdSet));
        }
        
        if(sidewalk.isSelected() || shared.isSelected() || segregated.isSelected() || sidewalkWithoutSign.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.FOOT, Values.DESIGNATED, cmdSet));
        }
        else if(bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected() || bicycleUsable.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.FOOT, Values.YES, cmdSet));
        }
        
        if(!noOneway.isSelected() && !cycleStreet.isSelected() && !cycleZone.isSelected()) {
          if(shared.isSelected() || (sidewalk.isSelected() && bicycleFree.isSelected()) || segregated.isSelected()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.ONEWAY_BICYCLE, Values.YES, cmdSet));
          }
          else if(!sidewalk.isSelected() && !sidewalkWithoutSign.isSelected()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.ONEWAY, Values.YES, cmdSet));
          }
        }
        else {
          cmds.add(TagWaysAction.createCommand(w, Keys.ONEWAY, Values.NO, cmdSet));
        }
        System.out.println(cycleStreet.isSelected() +" "+cycleZone.isSelected());
        if(cycleStreet.isSelected() || cycleZone.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.MAXSPEED, "30", cmdSet));
          cmds.add(TagWaysAction.createCommand(w, Keys.SOURCE_MAXSPEED, Values.SOURCE_MAXSPEED_BICYLCE_ROAD, cmdSet));
          cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE_ROAD, Values.YES, cmdSet));
          
          String motor_vehicle = "";
          
          if(destinationFree.isSelected()) {
            motor_vehicle = Values.DESTINATION;
          }
          if(agriculturalFree.isSelected()) {
            if(motor_vehicle.length() > 0) {
              motor_vehicle += ";";
            }
            
            motor_vehicle += Values.AGRICULTURAL;
          }
          if(forestryFree.isSelected()) {
            if(motor_vehicle.length() > 0) {
              motor_vehicle += ";";
            }
            
            motor_vehicle += Values.FORESTRY;
          }
          System.out.println(motor_vehicle);
          if(!motor_vehicle.isBlank()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.VEHICLE, motor_vehicle, cmdSet));
          }
          else {
            cmds.add(TagWaysAction.createCommand(w, Keys.VEHICLE, Values.NO, cmdSet));
          }
        }
        else {
          String access = "";
          
          if(destinationFree.isSelected()) {
            access = Values.DESTINATION;
          }
          if(agriculturalFree.isSelected()) {
            if(access.length() > 0) {
              access += ";";
            }
            
            access += Values.AGRICULTURAL;
          }
          if(forestryFree.isSelected()) {
            if(access.length() > 0) {
              access += ";";
            }
            
            access += Values.FORESTRY;
          }
          
          if(access.length() > 0) {
            cmds.add(TagWaysAction.createCommand(w, Keys.VEHICLE, access, cmdSet));
          }
        }
          
        String trafficSign = "";
        
        if(sidewalk.isSelected()) {
          trafficSign = Values.SIGN_FOOTWAY;
        }
        else if(shared.isSelected()) {
          trafficSign = Values.SIGN_SHARED;
        }
        else if(segregated.isSelected()) {
          trafficSign = Values.SIGN_SEGREGATED;
        }
        else if(cyclewayOnKerb.isSelected()) {
          trafficSign = Values.SIGN_CYCLEWAY;
        }
        else if(cycleStreet.isSelected()) {
          trafficSign = Values.SIGN_BICYCLE_ROAD;
        }
        else if(cycleZone.isSelected()) {
          trafficSign = Values.SIGN_BICYCLE_ZONE;
        }
        
        if(segregated.isSelected()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.SEGREGATED, Values.YES, cmdSet));
        }
        else if(!cycleStreet.isSelected() && !cycleZone.isSelected() && !cyclewayOnKerb.isSelected() && !sidewalkWithoutSign.isSelected() && (!sidewalk.isSelected() || bicycleFree.isSelected())) {
          cmds.add(TagWaysAction.createCommand(w, Keys.SEGREGATED, Values.NO, cmdSet));
        }
        
        if(destinationFree.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_DESTINATION;
        }
        
        if(agriculturalFree.isSelected() && forestryFree.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_AGRICULTURAL_FORESTRY;
        }
        else if(agriculturalFree.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_AGRICULTURAL;
        }
        else if(forestryFree.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_FORESTRY;
        }
        
        if(bicycleFree.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_BICYLCE_FREE;
        }
        else if(bicycleFreeBotDirection.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_BICYLCE_BOTH_WAYS;
        }
        
        if(noOneway.isSelected() && !bicycleFreeBotDirection.isSelected()) {
          if(trafficSign.length() > 0) {
            trafficSign += ",";
          }
          
          trafficSign += Values.SIGN_BOTH_WAYS;
        }
        
        if(trafficSign.isBlank() && bicycleFree.isSelected() || bicycleUsable.isSelected() || sidewalk.isSelected() || sidewalkWithoutSign.isSelected()) {
          trafficSign += Values.NONE;
        }
        
        
        if(!trafficSign.isBlank()) {
          cmds.add(TagWaysAction.createCommand(w, Keys.TRAFFIC_SIGN, Values.createTrafficSignEntry(trafficSign), cmdSet));
        }
        
        if(!cmdSet.isEmpty()) {
          cmdSet.forEach((key, cmd) -> {
            cmds.add(cmd);
          });
        }
      }
      Command cmd = new SequenceCommand(tr("Change foot and cycleway Properties"), cmds);
      
      if (cmd != null) {
          UndoRedoHandler.getInstance().add(cmd);
      }
    }
  }
}
