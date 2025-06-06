// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways.constants;

public final class Values {
  public static final String YES = "yes";
  public static final String NO = "no";
  public static final String NONE = "none";
  public static final String ONEWAY_NEGATIVE = "-1";
  
  public static final String LEFT = "left";
  public static final String RIGHT = "right";
  public static final String BOTH = "both";
  
  public static final String ADVISORY = "advisory";
  public static final String EXCLUSIVE = "exclusive";
  public static final String DESIGNATED = "designated";
  
  public static final String LANE = "lane";
  public static final String TRACK = "track";
  public static final String RESIDENTIAL = "residential";
  public static final String SERVICE = "service";
  public static final String PATH = "path";
  public static final String SEPARATE = "separate";
  public static final String CYCLEWAY = "cycleway";
  public static final String FOOTWAY = "footway";
  public static final String UNCLASSIFIED = "unclassified";
  
  public static final String SOURCE_MAXSPEED_BICYLCE_ROAD = "DE:bicycle_road"; 
  public static final String DESTINATION = "destination";
  public static final String AGRICULTURAL = "agricultural";
  public static final String FORESTRY = "forestry";
  
  public static final String USE_SIDEPATH = "use_sidepath";
  
  public static final String HEAVY = "heavy";
  public static final String MEDIUM = "medium";
  public static final String LOW = "low";
  
  public static final String FORWARD = "forward";
  public static final String BACKWARD = "backward";
  
  public static final String SIGN_CYCLEWAY = "237";
  public static final String SIGN_FOOTWAY = "239";
  public static final String SIGN_SHARED = "240";
  public static final String SIGN_SEGREGATED = "241";
  public static final String SIGN_BICYCLE_ROAD = "244.1";
  public static final String SIGN_BICYCLE_ZONE = "244.3";
  
  public static final String SIGN_BICYLCE_FREE = "1022-10";
  public static final String SIGN_BICYLCE_BOTH_WAYS = "1000-33";
  public static final String SIGN_BOTH_WAYS = "1000-31";
  
  public static final String SIGN_DESTINATION = "1026-30";
  public static final String SIGN_AGRICULTURAL = "1026-36";
  public static final String SIGN_FORESTRY = "1026-37";
  public static final String SIGN_AGRICULTURAL_FORESTRY = "1026-38";
  
  public static final String DE_PREFIX = "DE:";
  public static final String TRAFFIC_SIGN_SEPARATOR = ",";
  
  
  public static final String createTrafficSignEntry(String... trafficSigns) {
    StringBuilder b = new StringBuilder();
    
    boolean none = trafficSigns[trafficSigns.length-1].contains(NONE);
    
    for(int i = 0; i < trafficSigns.length-1; i++) {
      if(!trafficSigns[i].isBlank()) {
        b.append(trafficSigns[i]).append(TRAFFIC_SIGN_SEPARATOR);
      }
      
      if(trafficSigns[i].contains(NONE)) {
        none = true;
      }
    }
    
    if(!none) {
      b.insert(0, DE_PREFIX);
    }
    
    b.append(trafficSigns[trafficSigns.length-1]);
    
    return b.toString();
  }
}

