// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
import org.openstreetmap.josm.tools.ImageProvider;

public class FootAndCycleTaggingDialog extends ExtendedDialog {
  private Side backward;
  private Side forward;
  private final ArrayList<Way> ways;
  
  private ImageSelectionButton heavy;
  private ImageSelectionButton medium;
  private ImageSelectionButton low;
  
  private boolean isTrafficMapping() {
    return FootAndCycleProperties.MAP_TRAFFIC.get();
  }
  
  private void selectTraffic() {
    int heavy = 0;
    int medium = 0;
    int low = 0;
    
    for(Way w : ways) {
      String test = w.get(Keys.TRAFFIC);
      
      if(Objects.equals(test,Values.HEAVY)) {
        heavy++;
      }
      else if(Objects.equals(test,Values.MEDIUM)) {
        medium++;
      }
      else if(Objects.equals(test,Values.LOW)) {
        low++;
      }
    }
    
    if((heavy > 0 && medium > 0) || (heavy > 0 && low > 0) || (medium > 0 && low > 0)) {
      this.heavy.setEnabled(false);
      this.medium.setEnabled(false);
      this.low.setEnabled(false);
    }
    else if(heavy > 0) {
      this.heavy.setSelected(true);
    }
    else if(medium > 0) {
      this.medium.setSelected(true);
    }
    else if(low > 0) {
      this.low.setSelected(true);
    }
  }
  
  private void addTrafficIncompatibilities() {
    ItemListener l = e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        if(heavy.isButton(e.getSource())) {
          medium.setSelected(false);
          low.setSelected(false);
        }
        else if(medium.isButton(e.getSource())) {
          heavy.setSelected(false);
          low.setSelected(false);
        }
        else if(low.isButton(e.getSource())) {
          medium.setSelected(false);
          heavy.setSelected(false);
        }
      }
    };
    heavy.addItemListener(l);
    medium.addItemListener(l);
    low.addItemListener(l);
  }
  
  public FootAndCycleTaggingDialog(final ArrayList<Way> ways) {
    super(MainApplication.getMainFrame(), tr("Foot and cycleway tagging"), new String[] {tr("OK"), tr("Cancel")}, true /* modal */);
    setRememberWindowGeometry(getClass().getName() + ".geometry",
        WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(750, 500)));
    setButtonIcons("ok", "cancel");
    this.ways = ways;
    
    final ImageIcon background = ImageProvider.get("street");
    
    JPanel content = new JPanel(new GridBagLayout()) {
      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        background.paintIcon(null, g, (getWidth()-background.getIconWidth())/2+2, (getHeight()-background.getIconHeight())/2);
      }
    };
    
    GridBagConstraints gc = new GridBagConstraints();
    
    heavy = new ImageSelectionButton(Values.HEAVY, tr("There is usually a lot or fast traffic on the highway"));
    medium = new ImageSelectionButton(Values.MEDIUM, tr("There is usually avarage traffic on the highway"));
    low = new ImageSelectionButton(Values.LOW, tr("There is usually not much traffic on the highway"));
    
    selectTraffic();
    addTrafficIncompatibilities();
    
    backward = new Side();
    forward = new Side();
    
    backward.separateSidewalk = new ImageSelectionButton("DE_239_SL", tr("The sidewalk in {0} direction exists as separate way", tr(Values.BACKWARD)));
    backward.separateCycleway = new ImageSelectionButton("DE_237_SL", tr("The cycleway in {0} direction exists as separate way and is mandatory", tr(Values.BACKWARD)));
    backward.separateCyclewayFree = new ImageSelectionButton("DE_1022-10_SL", tr("The cycleway in {0} direction exists as separate way and is optional", tr(Values.BACKWARD)));
    backward.sidewalkWithoutSign = new ImageSelectionButton("footway", tr("The street in {0} direction has only a sidewalk without a sign", tr(Values.BACKWARD)));
    backward.sidewalk = new ImageSelectionButton("DE_239", tr("The street in {0} direction has only a sidewalk", tr(Values.BACKWARD)));
    backward.shared = new ImageSelectionButton("DE_240", tr("The street in {0} direction has a sidewalk and a cycleway that are shared", tr(Values.BACKWARD)));
    backward.segregated = new ImageSelectionButton("DE_241-30", tr("The street in {0} direction has a sidewalk and a cycleway that are segregated", tr(Values.BACKWARD)));
    backward.cyclewayOnKerb = new ImageSelectionButton("DE_237", tr("The street in {0} direction has only a cycleway", tr(Values.BACKWARD)));
    backward.bicycleFree = new ImageSelectionButton("DE_1022-10", tr("Usage of the sidewalk/cycleway of the street in {0} direction by bicylcles is optional", tr(Values.BACKWARD)));
    backward.bicycleFreeBotDirection = new ImageSelectionButton("DE_1000-33", tr("Usage of the sidewalk/cycleway of the street in {0} direction by bicylcles is optional in both directions", tr(Values.BACKWARD)));
    backward.bicycleUsable = new ImageSelectionButton("angebotsradweg", tr("Usage of the cycleway of the street in {0} direction by bicylcles is optional (without traffic sign)", tr(Values.BACKWARD)));
    backward.noOneway = new ImageSelectionButton("DE_1000-31", tr("Bicycles can drive on the sidewalk/cycleway in {0} direction in both directions", tr(Values.BACKWARD)));
    backward.negativeOneway = new ImageSelectionButton("oneway_BL", tr("Only bicycles in {1} direction can drive on the sidewalk/cycleway in {0} direction", tr(Values.BACKWARD), tr(Values.FORWARD)));
    
    backward.cyclewayLaneExclusive = new ImageSelectionButton("DE_237", tr("The cycleway in {0} direction is a separate lane and is mandatory", tr(Values.BACKWARD)), true);
    backward.cyclewayLaneAdvisory = new ImageSelectionButton("bike-lane", tr("The cycleway in {0} direction is on a lane and is optional", tr(Values.BACKWARD)), true);
    
    forward.cyclewayLaneExclusive = new ImageSelectionButton("DE_237", tr("The cycleway in {0} direction is a separate lane and is mandatory", tr(Values.FORWARD)));
    forward.cyclewayLaneAdvisory = new ImageSelectionButton("bike-lane", tr("The cycleway in {0} direction is on a lane and is optional", tr(Values.FORWARD)));
    
    forward.sidewalkWithoutSign = new ImageSelectionButton("footway", tr("The street in {0} direction has only a sidewalk without a sign", tr(Values.FORWARD)), true);
    forward.sidewalk = new ImageSelectionButton("DE_239", tr("The street in {0} direction has only a sidewalk", tr(Values.FORWARD)), true);
    forward.shared = new ImageSelectionButton("DE_240", tr("The street in {0} direction has a sidewalk and a cycleway that are shared", tr(Values.FORWARD)), true);
    forward.segregated = new ImageSelectionButton("DE_241-30", tr("The street in {0} direction has a sidewalk and a cycleway that are segregated", tr(Values.FORWARD)), true);
    forward.cyclewayOnKerb = new ImageSelectionButton("DE_237", tr("The street in {0} direction has only a cycleway", tr(Values.FORWARD)), true);
    forward.noOneway = new ImageSelectionButton("DE_1000-31", tr("Bicycles can drive on the sidewalk/cycleway in {0} direction in both directions", tr(Values.FORWARD)), true);
    forward.bicycleFree = new ImageSelectionButton("DE_1022-10", tr("Usage of the sidewalk/cycleway of the street in {0} direction by bicylcles is optional", tr(Values.FORWARD)), true);
    forward.bicycleFreeBotDirection = new ImageSelectionButton("DE_1000-33", tr("Usage of the sidewalk/cycleway of the street in {0} direction by bicylcles is optional in both directions", tr(Values.FORWARD)), true);
    forward.bicycleUsable = new ImageSelectionButton("angebotsradweg", tr("Usage of the cycleway of the street in {0} direction by bicylcles is optional (without traffic sign)", tr(Values.FORWARD)), true);
    forward.separateSidewalk = new ImageSelectionButton("DE_239_SR", tr("The sidewalk in {0} direction exists as separate way", tr(Values.FORWARD)), true);
    forward.separateCycleway = new ImageSelectionButton("DE_237_SR", tr("The cycleway in {0} direction exists as separate way and is mandatory", tr(Values.FORWARD)), true);
    forward.separateCyclewayFree = new ImageSelectionButton("DE_1022-10_SR", tr("The cycleway in {0} direction exists as separate way and is optional", tr(Values.FORWARD)), true);
    forward.negativeOneway = new ImageSelectionButton("oneway_BR", tr("Only bicycles in {1} direction can drive on the sidewalk/cycleway in {0} direction", tr(Values.FORWARD), tr(Values.BACKWARD)), true);

    JLabel gapBackward = new JLabel(" ");
    gapBackward.setPreferredSize(new Dimension(10,10));

    JLabel gapForward = new JLabel(" ");
    gapForward.setPreferredSize(new Dimension(10,10));

    JLabel kerbBackward = new JLabel(" ");
    kerbBackward.setPreferredSize(new Dimension(20,10));
    
    JLabel space = new JLabel(" ");
    space.setPreferredSize(new Dimension(isTrafficMapping() ? 64 : 101,10));

    JLabel space2 = new JLabel(" ");
    space2.setPreferredSize(new Dimension(isTrafficMapping() ? 64 : 101,10));

    JLabel kerbForward = new JLabel(" ");
    kerbForward.setPreferredSize(new Dimension(20,10));
    
    JLabel spaceLeftTop = new JLabel(" ");
    spaceLeftTop.setPreferredSize(new Dimension(15,10));
    
    JLabel spaceRightTop = new JLabel(" ");
    spaceRightTop.setPreferredSize(new Dimension(15,10));
    
    content.add(backward.separateSidewalk, XY(1,3,gc));
    content.add(backward.separateCycleway, XY(1,4,gc));
    content.add(backward.separateCyclewayFree, XY(1,5,gc));
    content.add(gapBackward, XY(2,1,gc));
    content.add(backward.sidewalkWithoutSign, XY(3,1,gc));
    content.add(backward.sidewalk, XY(3,2,gc));
    content.add(backward.shared, Y(3,gc));
    content.add(backward.segregated, Y(4,gc));
    content.add(backward.cyclewayOnKerb, Y(5,gc));
    content.add(backward.bicycleFree, Y(6,gc));
    content.add(backward.bicycleFreeBotDirection, Y(7,gc));
    content.add(backward.bicycleUsable, Y(8,gc));
    content.add(spaceLeftTop, Y(9,gc));
    content.add(backward.negativeOneway, YW(10,3,gc));
    content.add(backward.noOneway, YW(11,3,gc));
    content.add(kerbBackward, XY(4,1,gc));
    content.add(backward.cyclewayLaneExclusive, XY(5,4,gc));
    content.add(backward.cyclewayLaneAdvisory, Y(5,gc));
    content.add(space, XY(6,1,gc));
    
    if(isTrafficMapping()) {
      content.add(this.heavy, XY(7,4,gc));
      content.add(this.medium, Y(5,gc));
      content.add(this.low, Y(6,gc));
    }
    
    content.add(space2, XY(8,1,gc));
    content.add(forward.cyclewayLaneExclusive, XY(9,4,gc));
    content.add(forward.cyclewayLaneAdvisory, Y(5,gc));
    content.add(forward.negativeOneway, YW(10,3,gc));
    content.add(forward.noOneway, YW(11,3,gc));
    content.add(kerbForward, XY(10,1,gc));
    content.add(forward.sidewalkWithoutSign, XY(11,1,gc));
    content.add(forward.sidewalk, XY(11,2,gc));
    content.add(forward.shared, Y(3,gc));
    content.add(forward.segregated, Y(4,gc));
    content.add(forward.cyclewayOnKerb, Y(5,gc));
    content.add(forward.bicycleFree, Y(6,gc));
    content.add(forward.bicycleFreeBotDirection, Y(7,gc));
    content.add(forward.bicycleUsable, Y(8,gc));
    content.add(spaceRightTop, Y(9,gc));
    content.add(gapForward, XY(12,1,gc));
    content.add(forward.separateSidewalk, XY(13,3,gc));
    content.add(forward.separateCycleway, XY(13,4,gc));
    content.add(forward.separateCyclewayFree, XY(13,5,gc));
    
    setContent(content);
    
    select(forward, new InitalValues(Values.RIGHT,ways));
    select(backward, new InitalValues(Values.LEFT,ways));
    
    forward.addIncompatibilities();
    backward.addIncompatibilities();
  }
  
  private void select(Side side, InitalValues values) {
    if(!values.isEmpty()) {
      if(values.separateCyclewayFree >= values.separateCycleway && values.separateCyclewayFree > values.lane && values.separateCyclewayFree > 0) {
        side.separateCyclewayFree.setSelected(true);
      }
      else if(values.separateCycleway > values.track && values.separateCycleway > values.lane) {
        side.separateCycleway.setSelected(true);
      }
      else if(values.track > values.lane) {
        if(values.segregated > values.segregatedNo) {
          side.segregated.setSelected(true);
          
          if(values.trafficSignNone > 0 && values.bicycle > 0) {
            side.bicycleUsable.setSelected(true);
          }
        }
        else if(values.segregatedNo > 0) {
          if(values.trafficSignNone == 0 && values.bicycleFree == 0 && values.bicycleFreeBothways == 0) {
            side.shared.setSelected(true);
          }
          
          if(values.trafficSignNone > 0 && values.bicycle > 0) {
            side.bicycleUsable.setSelected(true);
          }
        }
        else if((values.sidewalk == 0 || values.separateSidewalk > 0) && values.bicycleFree == 0 && values.bicycleFreeBothways == 0) {
          side.cyclewayOnKerb.setSelected(true);
        }
        
        if((values.bicycle > values.designated || values.bicycleFree > 0) && values.trafficSignNone == 0) {
          side.bicycleFree.setSelected(true);
        }
        else if(values.bicycleFreeBothways > 0) {
          side.bicycleFreeBotDirection.setSelected(true);
        }
      }
      else if(values.lane > 0) {
        if(values.exclusive > values.advisory) {
          side.cyclewayLaneExclusive.setSelected(true);
        }
        else if(values.advisory > 0) {
          side.cyclewayLaneAdvisory.setSelected(true);
        }
      }
      else if(values.trafficSignNone > 0 && values.bicycle > 0) {
        side.bicycleUsable.setSelected(true);
      }
      
      if(values.separateSidewalk > values.sidewalkFree) {
        side.separateSidewalk.setSelected(true);
      }
      else if(values.sidewalkFreeWithoutSign >= values.sidewalkFree && values.sidewalkFreeWithoutSign > 0) {
        side.sidewalkWithoutSign.setSelected(true);
      }
      else if(values.sidewalkFree >= values.sidewalk && values.sidewalk > 0) {
        side.sidewalk.setSelected(true);
        side.bicycleFree.setSelected(true);
      }
      else if(values.sidewalk > values.track && values.sidewalk > 0) {
        side.sidewalk.setSelected(true);
      }
      
      if(values.onewayNo > values.negativeOneway && values.onewayNo > 0) {
        side.noOneway.setSelected(true);
      }
      else if(values.negativeOneway > 0) {
        side.negativeOneway.setSelected(true);
      }
    }
  }
  
  private GridBagConstraints Y(int y, GridBagConstraints gc) {
    gc.gridy = y;
    
    return gc;
  }
  
  private GridBagConstraints YW(int y, int w, GridBagConstraints gc) {
    gc.gridy = y;
    gc.gridwidth = w;
    
    return gc;
  }
  
  private GridBagConstraints XY(int x, int y, GridBagConstraints gc) {
    gc.ipady = 5;
    gc.gridx = x;
    gc.gridy = y;
    gc.gridwidth = 1;
    
    return gc;
  }
  
  @Override
  protected void buttonAction(int i, ActionEvent evt) {
      if (i == 0) { // OK Button
        List<Command> cmds = new LinkedList<>();
        
        for(Way w : this.ways) {
          Hashtable<String,Command> cmdSet = new Hashtable<>();
          
          for(String key : Keys.POSSIBLE_MAIN_ROAD) {
            if(w.hasKey(key) && (!Objects.equals(key, Keys.TRAFFIC) || (heavy.isEnabled() && heavy.isVisible()))) {
              cmdSet.put(key, new ChangePropertyCommand(w, key, ""));
            }
          }
          
          if(isTrafficMapping()) {
            if(w.hasKey(Keys.TRAFFIC)) {
              cmdSet.put(Keys.TRAFFIC, new ChangePropertyCommand(w, Keys.TRAFFIC, ""));
            }
          }
          
          if(heavy.isEnabled()) {
            if(heavy.isSelected()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.TRAFFIC, Values.HEAVY, cmdSet));
            }
            else if(medium.isSelected()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.TRAFFIC, Values.MEDIUM, cmdSet));
            }
            else if(low.isSelected()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.TRAFFIC, Values.LOW, cmdSet));
            }
          }
          
          if(Objects.equals(w.get(Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN), Values.NONE)) {
            cmdSet.put(Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN, new ChangePropertyCommand(w, Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN, ""));
          }
          if(Objects.equals(w.get(Keys.CYCLEWAY_RIGHT_TRAFFIC_SIGN), Values.NONE)) {
            cmdSet.put(Keys.CYCLEWAY_RIGHT_TRAFFIC_SIGN, new ChangePropertyCommand(w, Keys.CYCLEWAY_RIGHT_TRAFFIC_SIGN, ""));
          }
          if(Objects.equals(w.get(Keys.CYCLEWAY_LEFT_TRAFFIC_SIGN), Values.NONE)) {
            cmdSet.put(Keys.CYCLEWAY_LEFT_TRAFFIC_SIGN, new ChangePropertyCommand(w, Keys.CYCLEWAY_LEFT_TRAFFIC_SIGN, ""));
          }
          
          String forwardSign = "";
          String backwardSign = "";
          String forwardSignSidewalk = "";
          String backwardSignSidewalk = "";
          
          if(forward.isLaneExclusive()) {
            forwardSign = Values.SIGN_CYCLEWAY;
          }
          if(backward.isLaneExclusive()) {
            backwardSign = Values.SIGN_CYCLEWAY;
          }
          
          if(forward.isSidewalk()) {
            if(!forward.sidewalkWithoutSign.isSelected()) {
              forwardSignSidewalk = Values.SIGN_FOOTWAY;
            }
            else {
              forwardSignSidewalk = Values.NONE;
            }
          }
          else if(forward.shared.isSelected() && !forward.isBicycleUsable()) {
            forwardSign = Values.SIGN_SHARED;
          }
          else if(forward.isSegregated() && !forward.isBicycleUsable()) {
            forwardSign = Values.SIGN_SEGREGATED;
          }
          else if(forward.isLaneExclusive() || forward.cyclewayOnKerb.isSelected()) {
            forwardSign = Values.SIGN_CYCLEWAY;
          }
          
          if(backward.isSidewalk()) {
            if(!backward.sidewalkWithoutSign.isSelected()) {
              backwardSignSidewalk = Values.SIGN_FOOTWAY;
            }
            else {
              backwardSignSidewalk = Values.NONE;
            }
          }
          else if(backward.shared.isSelected() && !backward.isBicycleUsable()) {
            backwardSign = Values.SIGN_SHARED;
          }
          else if(backward.isSegregated() && !backward.isBicycleUsable()) {
            backwardSign = Values.SIGN_SEGREGATED;
          }
          else if(backward.isLaneExclusive() || backward.cyclewayOnKerb.isSelected()) {
            backwardSign = Values.SIGN_CYCLEWAY;
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if(forward.bicycleFree.isSelected()) {
            if(forwardSign.length() > 0) {
              forwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            
            forwardSign += Values.SIGN_BICYLCE_FREE;
            
            if(forward.sidewalk.isSelected()) {
              if(forwardSignSidewalk.length() > 0) {
                forwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR;
              }
              
              forwardSignSidewalk += Values.SIGN_BICYLCE_FREE;
              
              if(forward.noOneway.isSelected()) {
                forwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR + Values.SIGN_BOTH_WAYS;
              }
            }
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if(forward.bicycleFreeBotDirection.isSelected()) {
            if(forwardSign.length() > 0) {
              forwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            forwardSign += Values.SIGN_BICYLCE_BOTH_WAYS;
          }
          else if(forward.isNoPositiveOneway()) {
            if(forwardSign.length() > 0) {
              forwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            
            forwardSign += Values.SIGN_BOTH_WAYS;
          }
          
          
          if(backward.bicycleFree.isSelected()) {
            if(backwardSign.length() > 0) {
              backwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            
            backwardSign += Values.SIGN_BICYLCE_FREE;
            
            if(backward.sidewalk.isSelected()) {
              if(backwardSignSidewalk.length() > 0) {
                backwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR;
              }
              
              backwardSignSidewalk += Values.SIGN_BICYLCE_FREE;
              
              if(backward.noOneway.isSelected()) {
                backwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR + Values.SIGN_BOTH_WAYS;
              }
            }
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if(backward.bicycleFreeBotDirection.isSelected()) {
            if(backwardSign.length() > 0) {
              backwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            backwardSign += Values.SIGN_BICYLCE_BOTH_WAYS;
          }
          else if(backward.isNoPositiveOneway()) {
            if(backwardSign.length() > 0) {
              backwardSign += Values.TRAFFIC_SIGN_SEPARATOR;
            }
            
            backwardSign += Values.SIGN_BOTH_WAYS;
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if(forward.isFree()) {
            if(forward.bicycleFree.isSelected()) {
              forwardSignSidewalk = Values.SIGN_BICYLCE_FREE;
              
              if(forward.noOneway.isSelected()) {
                forwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR + Values.SIGN_BOTH_WAYS;
              }
            }
            else if(forward.bicycleFreeBotDirection.isSelected()) {
              forwardSignSidewalk = Values.SIGN_BICYLCE_BOTH_WAYS;
            }
            else {
              forwardSignSidewalk = Values.NONE;
            }
          }
          
          if(backward.isFree()) {
            if(backward.bicycleFree.isSelected()) {
              backwardSignSidewalk = Values.SIGN_BICYLCE_FREE;
              
              if(backward.noOneway.isSelected()) {
                backwardSignSidewalk += Values.TRAFFIC_SIGN_SEPARATOR + Values.SIGN_BOTH_WAYS;
              }
            }
            else if(backward.bicycleFreeBotDirection.isSelected()) {
              backwardSignSidewalk = Values.SIGN_BICYLCE_BOTH_WAYS;
            }
            else {
              backwardSignSidewalk = Values.NONE;
            }
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if(forward.isBicycleUsable()) {
            forwardSign = Values.NONE;
          }
          
          if(forward.sidewalkWithoutSign.isSelected()) {
            forwardSignSidewalk = Values.NONE;
          }
          
          if(backward.isBicycleUsable()) {
            backwardSign = Values.NONE;
          }
          if(backward.sidewalkWithoutSign.isSelected()) {
            backwardSignSidewalk = Values.NONE;
          }
          System.out.println(forwardSign+ " " + forwardSignSidewalk + " | " + backwardSign + " "+ backwardSignSidewalk);
          
          if(!forwardSign.isBlank()) {
            forwardSign = Values.createTrafficSignEntry(forwardSign);
          }
         
          if(!backwardSign.isBlank()) {
            backwardSign = Values.createTrafficSignEntry(backwardSign);
          }
          
          if(!forwardSignSidewalk.isBlank()) {
            forwardSignSidewalk = Values.createTrafficSignEntry(forwardSignSidewalk);
          }
          
          if(!backwardSignSidewalk.isBlank()) {
            backwardSignSidewalk = Values.createTrafficSignEntry(backwardSignSidewalk);
          }
          System.out.println("backwardSignSidewalk " + backwardSignSidewalk);
          if((forward.isSidewalk() || forward.isSidewalkFree() || forward.sidewalkWithoutSign.isSelected()) && (backward.isSidewalk() || backward.isSidewalkFree() || backward.sidewalkWithoutSign.isSelected())) {
            if(Objects.equals(backwardSignSidewalk, forwardSignSidewalk)) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_TRAFFIC_SIGN, forwardSignSidewalk, cmdSet));
            }
            else {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_TRAFFIC_SIGN, forwardSignSidewalk, cmdSet));
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_TRAFFIC_SIGN, backwardSignSidewalk, cmdSet));
            }
          }
          else if((forward.isSidewalk() || forward.isSidewalkFree() || forward.sidewalkWithoutSign.isSelected())) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_TRAFFIC_SIGN, forwardSignSidewalk, cmdSet));
          }
          else if((backward.isSidewalk() || backward.isSidewalkFree() || backward.sidewalkWithoutSign.isSelected())) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_TRAFFIC_SIGN, backwardSignSidewalk, cmdSet));
          }
          
          if(forward.isCycleway() && backward.isCycleway()) {
            if(Objects.equals(forwardSign, backwardSign)) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN, forwardSign, cmdSet));
            }
            else {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_TRAFFIC_SIGN, forwardSign, cmdSet));
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_TRAFFIC_SIGN, backwardSign, cmdSet));
            }
          }
          else if(forward.isCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_TRAFFIC_SIGN, forwardSign, cmdSet));
          }
          else if(backward.isCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_TRAFFIC_SIGN, backwardSign, cmdSet));
          }
          
          if(forward.isSidewalkFree() && backward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_FOOT, Values.DESIGNATED, cmdSet));
          }
          else if(forward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_FOOT, Values.DESIGNATED, cmdSet));
          }
          else if(backward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_FOOT, Values.DESIGNATED, cmdSet));
          }
          
          if(forward.isSidewalkFree() && backward.isSidewalkFree() && forward.isNoPositiveOneway() && backward.isNoPositiveOneway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_ONEWAY, Values.NO, cmdSet));
          }
          else if(forward.isSidewalkFree() && backward.isSidewalkFree() && forward.isNegativeOneway() && backward.isNegativeOneway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_ONEWAY_BICYCLE, Values.ONEWAY_NEGATIVE, cmdSet));
          }
          else if(forward.isSidewalkFree() && backward.isSidewalkFree() && forward.isOneway() && backward.isOneway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_ONEWAY_BICYCLE, Values.YES, cmdSet));
          }
          else {
            if(forward.isSidewalkFree() && forward.isNoPositiveOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_ONEWAY, Values.NO, cmdSet));
            }
            else if(forward.isSidewalkFree() && forward.isNegativeOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_ONEWAY_BICYCLE, Values.ONEWAY_NEGATIVE, cmdSet));
            }
            else if(forward.isSidewalkFree() && forward.isOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_ONEWAY_BICYCLE, Values.YES, cmdSet));
            }
            
            if(backward.isSidewalkFree() && backward.isNoPositiveOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_ONEWAY, Values.NO, cmdSet));
            }
            else if(backward.isSidewalkFree() && backward.isNegativeOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_ONEWAY_BICYCLE, Values.ONEWAY_NEGATIVE, cmdSet));
            }
            else if(backward.isSidewalkFree() && backward.isOneway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_ONEWAY_BICYCLE, Values.YES, cmdSet));
            }            
          }
          
          if(forward.isNoPositiveOneway() && backward.isNoPositiveOneway() && forward.isCycleway() && backward.isCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_ONEWAY, Values.NO, cmdSet));
          }
          else if(forward.isNegativeOneway() && backward.isNegativeOneway() && forward.isCycleway() && backward.isCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_ONEWAY, Values.ONEWAY_NEGATIVE, cmdSet));
          }
          else if(forward.isOneway() && backward.isOneway() && forward.isCycleway() && backward.isCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_ONEWAY, Values.YES, cmdSet));
          }
          else {
            if(forward.isNoPositiveOneway() && forward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_ONEWAY, Values.NO, cmdSet));
            }
            else if(forward.isNegativeOneway() && forward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_ONEWAY, Values.ONEWAY_NEGATIVE, cmdSet));
            }
            else if(forward.isOneway() && forward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_ONEWAY, Values.YES, cmdSet));
            }

            if(backward.isNoPositiveOneway() && backward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_ONEWAY, Values.NO, cmdSet));
            }
            else if(backward.isNegativeOneway() && backward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_ONEWAY, Values.ONEWAY_NEGATIVE, cmdSet));
            }
            else if(backward.isOneway() && backward.isCycleway()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_ONEWAY, Values.YES, cmdSet));
            }
          }
          
          if(forward.hasSidewalk() && backward.hasSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH, Values.YES, cmdSet));
          }
          else if(forward.hasSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT, Values.YES, cmdSet));
            
            if(!backward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT, Values.NO, cmdSet));
            }
          }
          else if(backward.hasSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT, Values.YES, cmdSet));

            if(!forward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT, Values.NO, cmdSet));
            }
          }
          else if(!forward.isSeparateSidewalk() && !backward.isSeparateSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH, Values.NO, cmdSet));
          }
          
          if(forward.isSeparateSidewalk() && backward.isSeparateSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH, Values.SEPARATE, cmdSet));
          }
          else if(forward.isSeparateSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT, Values.SEPARATE, cmdSet));
            
            if(!backward.hasSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT, Values.NO, cmdSet));
            }
          }
          else if(backward.isSeparateSidewalk()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT, Values.SEPARATE, cmdSet));
            
            if(!forward.hasSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT, Values.NO, cmdSet));
            }
          }
          
          if(forward.isSidewalkFree() && backward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_BICYCLE, Values.YES, cmdSet));
          }
          else if(forward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_BICYCLE, Values.YES, cmdSet));
          }
          else if(backward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_BICYCLE, Values.YES, cmdSet));
          }
        
          if(forward.isSegregated() && backward.isSegregated() && !forward.isSidewalkFree()) {
            if(!forward.isSeparateSidewalk() && !backward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_SEGREGATED, Values.YES, cmdSet));
            }
            else if(forward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_SEGREGATED, Values.YES, cmdSet));
            }
            else if(backward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_SEGREGATED, Values.YES, cmdSet));
            }
            
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_SEGREGATED, Values.YES, cmdSet));
          }
          else if(forward.isShared() && backward.isShared() && !forward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_SEGREGATED, Values.NO, cmdSet));
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_BOTH_SEGREGATED, Values.NO, cmdSet));
          }
          else if(forward.isSegregated() && !forward.isSidewalkFree()) {
            if(!forward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_SEGREGATED, Values.YES, cmdSet));
            }
            
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_SEGREGATED, Values.YES, cmdSet));
          }
          else if(forward.isShared() && !forward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_SEGREGATED, Values.NO, cmdSet));
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_RIGHT_SEGREGATED, Values.NO, cmdSet));
          }
          if(backward.isSegregated() && !forward.isSegregated() && !backward.isSidewalkFree()) {
            if(!backward.isSeparateSidewalk()) {
              cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_SEGREGATED, Values.YES, cmdSet));
            }
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_SEGREGATED, Values.YES, cmdSet));
          }
          else if(backward.isShared() && !forward.isShared() && !backward.isSidewalkFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_SEGREGATED, Values.NO, cmdSet));
            cmds.add(TagWaysAction.createCommand(w, Keys.SIDEWALK_LEFT_SEGREGATED, Values.NO, cmdSet));
          }
          
          if(forward.hasSeparateCycleway() && backward.hasSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH, Values.SEPARATE, cmdSet));            
          }
          else if(forward.hasSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT, Values.SEPARATE, cmdSet));
          }
          else if(backward.hasSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT, Values.SEPARATE, cmdSet));
          }
          
          if(forward.isSeparateCycleway() && backward.isSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE, Values.USE_SIDEPATH, cmdSet));
          }
          else if(forward.isSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE_FORWARD, Values.USE_SIDEPATH, cmdSet));
          }
          else if(backward.isSeparateCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE_BACKWARD, Values.USE_SIDEPATH, cmdSet));
          }
          
          if(forward.isSeparateCyclewayFree() && backward.isSeparateCyclewayFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE, Values.YES, cmdSet));
          }
          else if(forward.isSeparateCyclewayFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE_FORWARD, Values.YES, cmdSet));
          }
          else if(backward.isSeparateCyclewayFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.BICYCLE_BACKWARD, Values.YES, cmdSet));
          }
          
          if(forward.isOnKerb() && backward.isOnKerb()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH, Values.TRACK, cmdSet));
          }
          else if(forward.isOnKerb()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT, Values.TRACK, cmdSet));
          }
          else if(backward.isOnKerb()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT, Values.TRACK, cmdSet));
          }
          
          if(forward.isDesignated() && backward.isDesignated()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          else if(forward.isDesignated()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          else if(backward.isDesignated()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          
          if(forward.isFree() && backward.isFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          else if(forward.isFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          else if(backward.isFree()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_BICYCLE, Values.DESIGNATED, cmdSet));
          }
          
          if(forward.isLane() && backward.isLane()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH, Values.LANE, cmdSet));
          }
          else if(forward.isLane()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT, Values.LANE, cmdSet));
          }
          else if(backward.isLane()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT, Values.LANE, cmdSet));
          }
          
          if(forward.isLaneAdivisory() && backward.isLaneAdivisory()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_LANE, Values.ADVISORY, cmdSet));
          }
          else if(forward.isLaneAdivisory()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_LANE, Values.ADVISORY, cmdSet));
          }
          else if(backward.isLaneAdivisory()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_LANE, Values.ADVISORY, cmdSet));
          }
          
          if(forward.isLaneExclusive() && backward.isLaneExclusive()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH_LANE, Values.EXCLUSIVE, cmdSet));
          }
          else if(forward.isLaneExclusive()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT_LANE, Values.EXCLUSIVE, cmdSet));
          }
          else if(backward.isLaneExclusive()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT_LANE, Values.EXCLUSIVE, cmdSet));
          }
          
          if(!forward.hasCycleway() && !backward.hasCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_BOTH, Values.NO, cmdSet));
          }
          else if(!forward.hasCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_RIGHT, Values.NO, cmdSet));
          }
          else if(!backward.hasCycleway()) {
            cmds.add(TagWaysAction.createCommand(w, Keys.CYCLEWAY_LEFT, Values.NO, cmdSet));
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
      super.buttonAction(i, evt);
  }
  
  private static final class InitalValues {
    int separateCycleway;
    int separateCyclewayFree;
    int separateSidewalk;
    
    int sidewalk;
    int sidewalkFree;
    int sidewalkFreeWithoutSign;
    
    int cycleway;
    
    int lane;
    int exclusive;
    int advisory;
    
    int track;
    int segregated;
    int segregatedNo;
    int onewayNo;
    int negativeOneway;
    
    int designated;
    int bicycle;
    int bicycleFree;
    int bicycleFreeBothways;
    int trafficSignNone;
    
    public boolean isEmpty() {
      return separateCycleway == 0 && separateSidewalk == 0 && sidewalk == 0 && sidewalkFree == 0 && cycleway == 0 && lane == 0
          && exclusive == 0 && advisory == 0 && track == 0 && segregated == 0 && segregatedNo == 0 && onewayNo == 0 && negativeOneway == 0 
          && designated == 0 && bicycle == 0 && bicycleFree == 0 && bicycleFreeBothways == 0 && trafficSignNone == 0;
    }
    
    public InitalValues(String side, ArrayList<Way> ways) {
      for(Way w : ways) {
        String test = w.get(Keys.CYCLEWAY);
        boolean noneSign = false;
        
        if(Objects.equals(test, side) || Objects.equals(test, Values.BOTH) || w.get("cycleway:"+side) != null || w.get(Keys.CYCLEWAY_BOTH) != null) {
          cycleway++;
        }
        
        if(Objects.equals(test, Values.SEPARATE) || Objects.equals(w.get("cycleway:"+side), Values.SEPARATE) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH), Values.SEPARATE)) {
          separateCycleway++;
        }
        
        test = w.get(Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN);
        String test1 = w.get("cycleway:"+side+":traffic_sign");
        
        if((test != null && test.contains(Values.SIGN_BICYLCE_FREE)) || (test1 != null && test1.contains(Values.SIGN_BICYLCE_FREE))) {
          bicycleFree++;
        }
        else if((test != null && test.contains(Values.SIGN_BICYLCE_BOTH_WAYS)) || (test1 != null && test1.contains(Values.SIGN_BICYLCE_BOTH_WAYS))) {
          bicycleFreeBothways++;
        }
        else if((test != null && test.contains(Values.NONE)) || (test1 != null && test1.contains(Values.NONE))) {
          trafficSignNone++;
          noneSign = true;
        }
        
        test = w.get(Keys.SIDEWALK_BOTH_TRAFFIC_SIGN);
        test1 = w.get("sidewalk:"+side+":traffic_sign");
        
        if((test != null && test.contains(Values.SIGN_BICYLCE_FREE) || (test1 != null && test1.contains(Values.SIGN_BICYLCE_FREE)))) {
          bicycleFree++;
        }
        else if((test != null && test.contains(Values.SIGN_BICYLCE_BOTH_WAYS) || (test1 != null && test1.contains(Values.SIGN_BICYLCE_BOTH_WAYS)))) {
          bicycleFreeBothways++;
        }
        else if((test != null && test.contains(Values.NONE) || (test1 != null && test1.contains(Values.NONE)))) {
          trafficSignNone++;
          noneSign = true;
        }
        System.out.println("sidewalk:"+side+":traffic_sign="+test1 +" " + noneSign);
        test = w.get(Keys.SIDEWALK);
        
        if(Objects.equals(test, side) || Objects.equals(test, Values.BOTH) || Objects.equals(w.get("sidewalk:"+side),Values.YES) || Objects.equals(w.get(Keys.SIDEWALK_BOTH),Values.YES)
            || Objects.equals(w.get("sidewalk:"+side),Values.LANE) || Objects.equals(w.get(Keys.SIDEWALK_BOTH),Values.LANE)) {
          if(noneSign) {
            sidewalkFreeWithoutSign++;
          }
          else {
            sidewalk++;
          }
        }
        
        if(Objects.equals(w.get(Keys.BICYCLE), Values.YES) || (side.equals(Values.RIGHT) && Objects.equals(w.get(Keys.BICYCLE_FORWARD), Values.YES))
            || (side.equals(Values.LEFT) && Objects.equals(w.get(Keys.BICYCLE_BACKWARD), Values.YES))) {
          separateCyclewayFree++;
        }
        
        if(Objects.equals(test, Values.SEPARATE) || Objects.equals(w.get("sidewalk:"+side), Values.SEPARATE) || Objects.equals(w.get(Keys.SIDEWALK_BOTH), Values.SEPARATE)) {
          separateSidewalk++;
        }

        if(Objects.equals(w.get("sidewalk:"+side+":bicycle"), Values.YES) || Objects.equals(w.get(Keys.SIDEWALK_BOTH_BICYCLE), Values.YES)) {
          sidewalkFree++;
        }
        
        if(Objects.equals(w.get("cycleway:"+side), Values.TRACK) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH), Values.TRACK)) {
          track++;
        }

        if(Objects.equals(w.get("cycleway:"+side), Values.LANE) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH), Values.LANE)) {
          lane++;
        }

        if(Objects.equals(w.get("cycleway:"+side+":lane"), Values.EXCLUSIVE) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_LANE), Values.EXCLUSIVE)) {
          exclusive++;
        }

        if(Objects.equals(w.get("cycleway:"+side+":lane"), Values.ADVISORY) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_LANE), Values.ADVISORY)) {
          advisory++;
        }

        if(Objects.equals(w.get("cycleway:"+side+":bicycle"), Values.DESIGNATED) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_BICYCLE), Values.DESIGNATED)) {
          designated++;
        }
        
        if(Objects.equals(w.get("cycleway:"+side+":bicycle"), Values.YES) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_BICYCLE), Values.YES)) {
          bicycle++;
        }
        
        if(Objects.equals(w.get("cycleway:"+side+":traffic_sign"), Values.NONE) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_TRAFFIC_SIGN), Values.NONE)) {
          bicycle++;
          designated--;
        }
        
        if(Objects.equals(w.get("cycleway:"+side+":oneway"), Values.NO) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_ONEWAY), Values.NO)
           || Objects.equals(w.get("sidewalk:"+side+":oneway:bicycle"), Values.NO) || Objects.equals(w.get(Keys.SIDEWALK_BOTH_ONEWAY_BICYCLE), Values.NO)
           || Objects.equals(w.get("sidewalk:"+side+":oneway"), Values.NO) || Objects.equals(w.get("sidewalk:both:oneway"), Values.NO)) {
          onewayNo++;
        }
        
        if(Objects.equals(w.get("cycleway:"+side+":oneway"), Values.ONEWAY_NEGATIVE) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_ONEWAY), Values.ONEWAY_NEGATIVE)
            || Objects.equals(w.get("sidewalk:"+side+":oneway:bicycle"), Values.ONEWAY_NEGATIVE) || Objects.equals(w.get(Keys.SIDEWALK_BOTH_ONEWAY_BICYCLE), Values.ONEWAY_NEGATIVE)
            || Objects.equals(w.get("sidewalk:"+side+":oneway"), Values.ONEWAY_NEGATIVE) || Objects.equals(w.get("sidewalk:both:oneway"), Values.ONEWAY_NEGATIVE)) {
           negativeOneway++;
         }
        
        if(Objects.equals(w.get("cycleway:"+side+":segregated"), Values.YES) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_SEGREGATED), Values.YES)
            || Objects.equals(w.get("sidewalk:"+side+":segregated"), Values.YES) || Objects.equals(w.get(Keys.SIDEWALK_BOTH_SEGREGATED), Values.YES)) {
          segregated++;
        }
        
        if((Objects.equals(w.get("cycleway:"+side+":segregated"), Values.NO) || Objects.equals(w.get(Keys.CYCLEWAY_BOTH_SEGREGATED), Values.NO)
            || Objects.equals(w.get("sidewalk:"+side+":segregated"), Values.NO) || Objects.equals(w.get(Keys.SIDEWALK_BOTH_SEGREGATED), Values.NO))
            ) {
          segregatedNo++;
        }
      }
      System.out.println(toString());
    }
    
    @Override
    public String toString() {
      return "\nseparateSidewalk="+separateSidewalk+"\nseparateCycleway="+separateCycleway+"\nseparateCyclewayFree="+separateCyclewayFree+"\ncycleway="+cycleway+"\nsidewalk="+sidewalk+"\nsidewalkWitoutSign="+sidewalkFreeWithoutSign+"\nsidewalkFree="+sidewalkFree+"\nlane="+lane+"\nexclusive="+exclusive+"\nadvisory="+advisory+"\ntrack="+track+"\nsegregated"+segregated+"\nsegregatedNo="+segregatedNo+"\nonewayNo="+onewayNo+"\ndesignated="+designated+"\nbicycle="+bicycle+"\nbicycleFree="+bicycleFree+"\nbicycleFreeBothWays="+bicycleFreeBothways+"\ntrafficSignNone="+trafficSignNone;
    }
  }
  
  private static final class Side {
    private ImageSelectionButton separateCyclewayFree;
    private ImageSelectionButton separateCycleway;
    private ImageSelectionButton separateSidewalk;
    
    private ImageSelectionButton sidewalkWithoutSign;
    private ImageSelectionButton sidewalk;
    private ImageSelectionButton shared;
    private ImageSelectionButton segregated;
    private ImageSelectionButton cyclewayOnKerb;
    private ImageSelectionButton bicycleFree;
    private ImageSelectionButton bicycleUsable;
    private ImageSelectionButton bicycleFreeBotDirection;
    private ImageSelectionButton noOneway;
    private ImageSelectionButton negativeOneway;
    
    private ImageSelectionButton cyclewayLaneExclusive;
    private ImageSelectionButton cyclewayLaneAdvisory;
    
    public void addIncompatibilities() {
      ImageSelectionButton.handleIncompatible(null,false,true,shared,segregated,cyclewayLaneExclusive,cyclewayLaneAdvisory,separateCycleway, separateCyclewayFree);
      ImageSelectionButton.handleIncompatible(null,false,true,noOneway,negativeOneway,cyclewayLaneAdvisory);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalkWithoutSign,bicycleFree);
      ImageSelectionButton.handleIncompatible(null,false,true,bicycleFreeBotDirection, negativeOneway);
      ImageSelectionButton.handleIncompatible(null,false,true,bicycleFree,bicycleFreeBotDirection,bicycleUsable,cyclewayOnKerb,shared,segregated);
      ImageSelectionButton.handleIncompatible(null,false,true,bicycleUsable,bicycleFreeBotDirection);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalkWithoutSign,bicycleFreeBotDirection);
      ImageSelectionButton.handleIncompatible(null,false,true,sidewalk,sidewalkWithoutSign,separateSidewalk,shared,segregated);
      ImageSelectionButton.handleIncompatible(new ImageSelectionButton[] {separateSidewalk},true,true,sidewalk,sidewalkWithoutSign,shared);
      ImageSelectionButton.handleIncompatible(new ImageSelectionButton[] {cyclewayOnKerb},true,true,sidewalk,sidewalkWithoutSign,shared,segregated,cyclewayLaneExclusive,cyclewayLaneAdvisory);
      ImageSelectionButton.handleIncompatible(new ImageSelectionButton[] {bicycleFree,bicycleFreeBotDirection,bicycleUsable},true,true,cyclewayLaneExclusive,cyclewayLaneAdvisory);
      ImageSelectionButton.handleIncompatible(new ImageSelectionButton[] {separateCycleway, separateCyclewayFree},true,true,shared,segregated,cyclewayOnKerb,bicycleFree,noOneway,negativeOneway,cyclewayLaneExclusive,cyclewayLaneAdvisory, bicycleUsable, bicycleFreeBotDirection);
      
      sidewalk.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED && !isSidewalkFree() && !isLane()) {
          negativeOneway.setSelected(false);
        }
      });
      
      sidewalkWithoutSign.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED && !isSidewalkFree() && !isLane() && !bicycleUsable.isSelected()) {
          noOneway.setSelected(false);
          negativeOneway.setSelected(false);
        }
      });
      
      bicycleFreeBotDirection.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          noOneway.setSelected(true);
        }
      });
      
      noOneway.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.DESELECTED) {
          bicycleFreeBotDirection.setSelected(false);
        }
      });
      
      ItemListener oneways = e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          if(!cyclewayOnKerb.isSelected() && !isLane() && isSeparateSidewalk()) {
            separateCycleway.setSelected(false);
          }
          if(((sidewalkWithoutSign.isSelected() && !bicycleUsable.isSelected()) || sidewalk.isSelected()) && !bicycleFree.isSelected() && !bicycleFreeBotDirection.isSelected() && !isLaneExclusive()) {
            shared.setSelected(true);
          }
        }
      };
      
      noOneway.addItemListener(oneways);
      negativeOneway.addItemListener(oneways);
      
      cyclewayLaneExclusive.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.DESELECTED && !isLane() && (sidewalk.isSelected() || sidewalkWithoutSign.isSelected())) {
          noOneway.setSelected(false);
          negativeOneway.setSelected(false);
        }
      });
      
      cyclewayLaneAdvisory.addItemListener(e -> {
        if(e.getStateChange() == ItemEvent.DESELECTED && !isLane() && (sidewalk.isSelected() || sidewalkWithoutSign.isSelected())) {
          noOneway.setSelected(false);
          negativeOneway.setSelected(false);
        }
      });
      
    }
    
    public boolean isSeparateSidewalk() {
      return separateSidewalk.isSelected();
    }
    
    public boolean isSeparateCycleway() {
      return separateCycleway.isSelected();
    }
    
    public boolean hasSeparateCycleway() {
      return isSeparateCycleway() || isSeparateCyclewayFree();
    }
    
    public boolean isSeparateCyclewayFree() {
      return separateCyclewayFree.isSelected();
    }
    
    public boolean isSidewalkFree() {
      return bicycleFree.isSelected() && sidewalk.isSelected();
    }
    
    public boolean isSidewalk() {
      return (sidewalk.isSelected() || sidewalkWithoutSign.isSelected()) && !shared.isSelected() && !segregated.isSelected() && !bicycleUsable.isSelected();
    }
    
    public boolean hasSidewalk() {
      return (sidewalk.isSelected() || sidewalkWithoutSign.isSelected()) || shared.isSelected() || segregated.isSelected() || (!separateSidewalk.isSelected() && (bicycleUsable.isSelected() || bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected()));
    }
    
    public boolean isBicycleUsable() {
      return bicycleUsable.isSelected();
    }
    
    public boolean isCycleway() {
      return shared.isSelected() || cyclewayOnKerb.isSelected() || segregated.isSelected()  || bicycleUsable.isSelected() || (!sidewalk.isSelected() && !sidewalkWithoutSign.isSelected() && (bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected())) || cyclewayLaneAdvisory.isSelected() || cyclewayLaneExclusive.isSelected() || bicycleUsable.isSelected();
    }
    
    public boolean isSegregated() {
      return segregated.isSelected();
    }
    
    public boolean isShared() {
      return shared.isSelected() || (!separateSidewalk.isSelected() && (bicycleUsable.isSelected() || bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected()));
    }
    
    public boolean isOnKerb() {
      return shared.isSelected() || segregated.isSelected() || cyclewayOnKerb.isSelected() || ((bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected()) && !sidewalk.isSelected() && !sidewalkWithoutSign.isSelected()) || bicycleUsable.isSelected();
    }
    
    public boolean isLane() {
      return isLaneAdivisory() || isLaneExclusive();
    }
    
    public boolean isLaneAdivisory() {
      return cyclewayLaneAdvisory.isSelected();
    }
    
    public boolean isLaneExclusive() {
      return cyclewayLaneExclusive.isSelected();
    }
    
    public boolean isOneway() {
      return !isNoPositiveOneway() && !isNegativeOneway();
    }
    
    public boolean isNoPositiveOneway() {
      return noOneway.isSelected();
    }

    public boolean isNegativeOneway() {
      return negativeOneway.isSelected();
    }

    public boolean isDesignated() {
      return (cyclewayOnKerb.isSelected() || shared.isSelected() || segregated.isSelected()) && !bicycleFree.isSelected() || (bicycleUsable.isSelected()) || (bicycleUsable.isSelected());
    }
    
    public boolean isFree() {
      return (bicycleFree.isSelected() || bicycleUsable.isSelected() || (bicycleFreeBotDirection.isSelected() && !isShared() && !isSegregated())) && !sidewalk.isSelected() && !sidewalkWithoutSign.isSelected();
    }
    
    public boolean hasCycleway() {
      return bicycleUsable.isSelected() || cyclewayLaneAdvisory.isSelected() || cyclewayLaneExclusive.isSelected() || cyclewayOnKerb.isSelected() || shared.isSelected() || segregated.isSelected() || separateCycleway.isSelected() || separateCyclewayFree.isSelected() || ((bicycleFree.isSelected() || bicycleFreeBotDirection.isSelected()) && !sidewalk.isSelected() && !sidewalkWithoutSign.isSelected());
    }
  }
}
