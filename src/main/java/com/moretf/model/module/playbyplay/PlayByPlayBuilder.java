package com.moretf.model.module.playbyplay;

import com.moretf.model.LogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayByPlayBuilder {

    public static List<PlayByPlayEvent> build(List<LogEvent> events) {
        List<PlayByPlayEvent> playByPlay = new ArrayList<>();

        int scoreRed = 0;
        int scoreBlue = 0;
        int roundPauseTime = 0;

        Long matchStartTime = null;
        Long roundStartTime = null;

        boolean gameActive = false;

        for (LogEvent event : events) {
            String type = event.getEventType();

            switch (type) {
                case "round_start":
                    roundStartTime = event.getTimestamp();
                    if (matchStartTime == null) matchStartTime = roundStartTime;
                    roundPauseTime = 0;
                    gameActive = true;

                    playByPlay.add(new PlayByPlayEvent(
                            event.getTimestamp(),
                            convertToClock(event.getTimestamp(), matchStartTime),
                            null, null, null, null, null,
                            null, "round_start", null,
                            scoreRed, scoreBlue
                    ));
                    break;

                case "pause_length":
                    if (event.getExtras() != null && event.getExtras().get("seconds") != null) {
                        roundPauseTime = (int) (Double.parseDouble(event.getExtras().get("seconds").toString()) * 1000);

                        PlayByPlayEvent pauseEvent = new PlayByPlayEvent();
                        pauseEvent.setTimestamp(event.getTimestamp());
                        pauseEvent.setClock(convertToClock(event.getTimestamp(), matchStartTime));
                        pauseEvent.setEventType("pause_length");
                        pauseEvent.setScoreRed(scoreRed);
                        pauseEvent.setScoreBlue(scoreBlue);
                        pauseEvent.setTeam("SYSTEM");
                        playByPlay.add(pauseEvent);

                        gameActive = false;
                    }
                    break;

                case "round_win":
                    String winner = event.getExtras() != null ? event.getExtras().get("winner").toString() : null;
                    if (winner != null) {
                        if (winner.equalsIgnoreCase("Red")) scoreRed++;
                        else scoreBlue++;
                    }

                    gameActive = false;

                    PlayByPlayEvent winEvent = new PlayByPlayEvent();
                    winEvent.setTimestamp(event.getTimestamp());
                    winEvent.setClock(convertToClock(event.getTimestamp(), matchStartTime));
                    winEvent.setEventType("round_win");
                    winEvent.setTeam(winner);
                    winEvent.setScoreRed(scoreRed);
                    winEvent.setScoreBlue(scoreBlue);
                    playByPlay.add(winEvent);
                    break;

                case "game_over":
                case "game_paused":
                case "game_unpaused":
                    PlayByPlayEvent gameEvent = new PlayByPlayEvent();
                    gameEvent.setTimestamp(event.getTimestamp());
                    gameEvent.setClock(convertToClock(event.getTimestamp(), matchStartTime));
                    gameEvent.setEventType(type);
                    playByPlay.add(gameEvent);
                    break;


                case "kill":
                case "chargedeployed":
                case "chargeended":
                case "chargeready":
                case "say":
                case "say_team":
                case "disconnected":
                case "pointcaptured": {
                    if (!gameActive) break;

                    PlayByPlayEvent pbe = new PlayByPlayEvent();
                    pbe.setTimestamp(event.getTimestamp());
                    pbe.setClock(convertToClock(event.getTimestamp(), matchStartTime));
                    pbe.setEventType(type);

                    if(event.getMessage() != null) pbe.setMessage(event.getMessage());

                    if (event.getActor() != null) {
                        pbe.setTeam(event.getActor().getTeam());
                        pbe.setActingPlayerID(event.getActor().getSteamId());

                        String attackerPos = event.getAttackerPosition();
                        if (attackerPos != null) {
                            String[] coords = attackerPos.trim().split("\\s+");
                            if (coords.length == 3) {
                                Map<String, Integer> loc = new HashMap<>();
                                loc.put("x", Integer.parseInt(coords[0]));
                                loc.put("y", Integer.parseInt(coords[1]));
                                loc.put("z", Integer.parseInt(coords[2]));
                                pbe.setActingPlayerLocation(loc);
                            }
                        }
                    }

                    if (event.getTarget() != null) {
                        pbe.setTargetPlayerID(event.getTarget().getSteamId());

                        String victimPos = event.getVictimPosition();
                        if (victimPos != null) {
                            String[] coords = victimPos.trim().split("\\s+");
                            if (coords.length == 3) {
                                Map<String, Integer> loc = new HashMap<>();
                                loc.put("x", Integer.parseInt(coords[0]));
                                loc.put("y", Integer.parseInt(coords[1]));
                                loc.put("z", Integer.parseInt(coords[2]));
                                pbe.setTargetPlayerLocation(loc);
                            }
                        }
                    }

                    pbe.setWeapon(event.getWeapon());


                    playByPlay.add(pbe);
                    break;
                }

                default:
                    break;
            }
        }

        return playByPlay;
    }

    private static String convertToClock(long timestamp, long matchStartTime) {
        if (matchStartTime < 0) return "00:00";
        long elapsed = (timestamp - matchStartTime) / 1000;
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
