// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.footandcycleways.constants;

public final class Keys {
  public static final String FOOT = "foot";
  public static final String BICYCLE = "bicycle";
  public static final String BICYCLE_FORWARD = "bicycle:forward";
  public static final String BICYCLE_BACKWARD = "bicycle:backward";
  public static final String BICYCLE_ROAD = "bicycle_road";
  public static final String MAXSPEED = "maxspeed";
  public static final String SOURCE_MAXSPEED = "source:maxspeed";
  public static final String VEHICLE = "vehicle";
  public static final String MOTOR_VEHICLE = "motor_vehicle";
  
  public static final String TRAFFIC_SIGN = "traffic_sign";
  
  public static final String ONEWAY_BICYCLE = "oneway:bicycle";
  public static final String ONEWAY = "oneway";
  public static final String SEGREGATED = "segregated";
  
  public static final String CYCLEWAY = "cycleway";
  public static final String CYCLEWAY_LEFT = "cycleway:left";
  public static final String CYCLEWAY_LEFT_BICYCLE = "cycleway:left:bicycle";
  public static final String CYCLEWAY_LEFT_LANE = "cycleway:left:lane";
  public static final String CYCLEWAY_LEFT_ONEWAY = "cycleway:left:oneway";
  public static final String CYCLEWAY_LEFT_SEGREGATED = "cycleway:left:segregated";
  public static final String CYCLEWAY_LEFT_TRAFFIC_SIGN = "cycleway:left:traffic_sign";
  
  public static final String CYCLEWAY_RIGHT = "cycleway:right";
  public static final String CYCLEWAY_RIGHT_BICYCLE = "cycleway:right:bicycle";
  public static final String CYCLEWAY_RIGHT_LANE = "cycleway:right:lane";
  public static final String CYCLEWAY_RIGHT_ONEWAY = "cycleway:right:oneway";
  public static final String CYCLEWAY_RIGHT_SEGREGATED = "cycleway:right:segregated";
  public static final String CYCLEWAY_RIGHT_TRAFFIC_SIGN = "cycleway:right:traffic_sign";
  
  public static final String CYCLEWAY_BOTH = "cycleway:both";
  public static final String CYCLEWAY_BOTH_BICYCLE = "cycleway:both:bicycle";
  public static final String CYCLEWAY_BOTH_LANE = "cycleway:both:lane";
  public static final String CYCLEWAY_BOTH_ONEWAY = "cycleway:both:oneway";
  public static final String CYCLEWAY_BOTH_SEGREGATED = "cycleway:both:segregated";
  public static final String CYCLEWAY_BOTH_TRAFFIC_SIGN = "cycleway:both:traffic_sign";
  
  public static final String SIDEWALK = "sidewalk";
  public static final String SIDEWALK_LEFT = "sidewalk:left";
  public static final String SIDEWALK_LEFT_FOOT = "sidewalk:left:foot";
  public static final String SIDEWALK_LEFT_BICYCLE = "sidewalk:left:bicycle";
  public static final String SIDEWALK_LEFT_ONEWAY_BICYCLE = "sidewalk:left:oneway:bicycle";
  public static final String SIDEWALK_LEFT_ONEWAY = "sidewalk:both:oneway";
  public static final String SIDEWALK_LEFT_SEGREGATED = "sidewalk:left:segregated";
  
  public static final String SIDEWALK_RIGHT = "sidewalk:right";
  public static final String SIDEWALK_RIGHT_FOOT = "sidewalk:right:foot";
  public static final String SIDEWALK_RIGHT_BICYCLE = "sidewalk:right:bicycle";
  public static final String SIDEWALK_RIGHT_ONEWAY_BICYCLE = "sidewalk:right:oneway:bicycle";
  public static final String SIDEWALK_RIGHT_ONEWAY = "sidewalk:right:oneway";
  public static final String SIDEWALK_RIGHT_SEGREGATED = "sidewalk:right:segregated";
  
  public static final String SIDEWALK_BOTH = "sidewalk:both";
  public static final String SIDEWALK_BOTH_FOOT = "sidewalk:both:foot";
  public static final String SIDEWALK_BOTH_BICYCLE = "sidewalk:both:bicycle";
  public static final String SIDEWALK_BOTH_ONEWAY_BICYCLE = "sidewalk:both:oneway:bicycle";
  public static final String SIDEWALK_BOTH_ONEWAY = "sidewalk:both:oneway";
  public static final String SIDEWALK_BOTH_SEGREGATED = "sidewalk:both:segregated";
  
  public static final String SIDEWALK_BOTH_TRAFFIC_SIGN = "sidewalk:both:traffic_sign";
  public static final String SIDEWALK_LEFT_TRAFFIC_SIGN = "sidewalk:left:traffic_sign";
  public static final String SIDEWALK_RIGHT_TRAFFIC_SIGN = "sidewalk:right:traffic_sign";
  
  
  
  public static final String TRAFFIC = "traffic";
  
  public static final String HIGHWAY = "highway";
 
  
  public static final String[] POSSIBLE_MAIN_ROAD = {
      CYCLEWAY,CYCLEWAY_BOTH,CYCLEWAY_RIGHT,CYCLEWAY_LEFT,SIDEWALK,SIDEWALK_BOTH,SIDEWALK_LEFT,SIDEWALK_RIGHT,
      SIDEWALK_BOTH_SEGREGATED,SIDEWALK_RIGHT_SEGREGATED,SIDEWALK_LEFT_SEGREGATED,SIDEWALK_BOTH_BICYCLE,SIDEWALK_RIGHT_BICYCLE,
      SIDEWALK_LEFT_BICYCLE, SIDEWALK_BOTH_ONEWAY_BICYCLE, SIDEWALK_RIGHT_ONEWAY_BICYCLE, SIDEWALK_LEFT_ONEWAY_BICYCLE,
      CYCLEWAY_BOTH_LANE,CYCLEWAY_LEFT_LANE,CYCLEWAY_RIGHT_LANE,CYCLEWAY_BOTH_ONEWAY,CYCLEWAY_LEFT_ONEWAY,CYCLEWAY_RIGHT_ONEWAY,
      CYCLEWAY_BOTH_BICYCLE,CYCLEWAY_LEFT_BICYCLE,CYCLEWAY_RIGHT_BICYCLE, CYCLEWAY_BOTH_SEGREGATED, CYCLEWAY_LEFT_SEGREGATED, CYCLEWAY_RIGHT_SEGREGATED,
      BICYCLE, BICYCLE_FORWARD, BICYCLE_BACKWARD,CYCLEWAY_BOTH_TRAFFIC_SIGN,CYCLEWAY_LEFT_TRAFFIC_SIGN,CYCLEWAY_RIGHT_TRAFFIC_SIGN,
      SIDEWALK_BOTH_TRAFFIC_SIGN,SIDEWALK_LEFT_TRAFFIC_SIGN,SIDEWALK_RIGHT_TRAFFIC_SIGN
  };
  
  public static final String[] POSSIBLE_SEPERATE = {
      FOOT, BICYCLE, BICYCLE_ROAD, TRAFFIC_SIGN, ONEWAY_BICYCLE, ONEWAY, SEGREGATED
  };
}
