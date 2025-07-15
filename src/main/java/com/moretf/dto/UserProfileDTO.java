package com.moretf.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class UserProfileDTO {
    public Map<String, MapStats> mapStats;
    public Map<String, ClassStats> classStats;
    public OverallStats overallStats;
    public Profile profile;
    public List<RecentMatch> recentMatches;
    public List<DailyActivity> activity;
    public List<PeerEntry> topPeers;
    public List<PeerEntry> topEnemies;

    public static class PeerEntry {
        public String peerId64;
        public int count;
        public int wins;
        public int loss;
        public int ties;
    }

    public static class RecentMatch {
        public long logid;
        public int kills;
        public int assists;
        public int deaths;
        public int dpm;
        public long dtm;
        public double hpm;
        public String playerClass;
        public String team;
        public String matchResult;
        public int matchLength;
        public String format;
        public long logDate;
        public String combined;
        public String map;
        public String title;
    }

    public static class DailyActivity {
        public String activityDate;
        public int totalMatches;
        public int wins;
        public int losses;
        public int ties;
    }


    public static class MapStats {
        public int count;
        public int wins;
        public int loss;
        public int ties;
        public double mapTime;
    }

    public static class ClassStats {
        @JsonIgnore
        public String classid;

        public int count;
        public int wins;
        public int loss;
        public int ties;
        public long classTime; // using long for BIGINT
    }

    public static class OverallStats {
        public int count;
        public int wins;
        public int loss;
        public int ties;
        public double totalTime;
    }

    public static class Profile {
        public String steamName;
        public String avatar;
        public String rglName;
        public String etf2lName;
        public String ozfName;
        public String etf2lId;
        public String ozfId;
    }

    public static class ProfileAndStats {
        public Profile profile;
        public OverallStats overallStats;
    }
}
