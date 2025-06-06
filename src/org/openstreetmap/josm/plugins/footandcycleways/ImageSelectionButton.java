// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.tools.ImageProvider;

public class ImageSelectionButton extends JPanel {
  private JCheckBox button;
  private JLabel label;
      
  public ImageSelectionButton(String image, String tooltipText) {
    this(image, tooltipText, false);
  }
      
  public ImageSelectionButton(String image, String tooltipText, boolean buttonLast) {
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    setOpaque(false);
    button = new JCheckBox();
    label = new JLabel(ImageProvider.get(image));
    
    if(tooltipText != null) {
      button.setToolTipText(tooltipText);
      label.setToolTipText(tooltipText);
    }
    
    if(!buttonLast) {
      add(button);
      add(Box.createRigidArea(new Dimension(5,0)));
      add(label);
    }
    else {
      add(label);
      add(Box.createRigidArea(new Dimension(5,0)));
      add(button);
    }
  }
  
  public void addItemListener(ItemListener listener) {
    button.addItemListener(listener);
  }
  
  public boolean isSelected() {
    return button.isSelected();
  }
  
  public void setSelected(boolean isSelected) {
    button.setSelected(isSelected);
  }
  
  public boolean isButton(Object o) {
    return Objects.equals(button, o);
  }
  
  public void setEnabled(boolean value) {
    super.setEnabled(value);
    button.setEnabled(value);
    label.setEnabled(value);
  }
  
  public static void handleIncompatible(ImageSelectionButton[] first, boolean addFirst, boolean addSecond, ImageSelectionButton... second) {
    ItemListener listener = e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        boolean deselect = (first == null);
        boolean firstSelected = false;
        
        if(!deselect) {
          for(ImageSelectionButton f : first) {
            if(f.isButton(e.getSource())) {
              firstSelected = true;
              break;
            }
          }
          
          if(!firstSelected) {
            for(ImageSelectionButton f : first) {
              f.setSelected(false);
            }
          }
        }
        
        for(ImageSelectionButton s : second) {
          if(firstSelected || (deselect && !s.isButton(e.getSource()))) {
            s.setSelected(false);
          }
        }
      }
    };
    
    if(addFirst) {
      for(ImageSelectionButton f : first) {
        f.addItemListener(listener);
      }
    }
    
    if(addSecond) {
      for(ImageSelectionButton s : second) {
        s.addItemListener(listener);
      }
    }
  }
}
