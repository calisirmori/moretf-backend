package com.moretf.dto;

import java.util.Map;

public class UserProfileDTO {
    public Map<String, MapStats> mapStats;

    public static class MapStats {
        public int count;
        public int wins;
        public int loss;
        public int ties;
        public double mapTime; // Assuming it's in minutes or hours
    }
}
