package com.moretf.model.module.rounds;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoundTeamStats {
    private int score = 0;
    private int kills = 0;
    private int dmg = 0;
    private int ubers = 0;

    public void incrementScore() { score++; }
    public void incrementKills() { kills++; }
    public void incrementUbers() { ubers++; }
    public void addDmg(int dmg) { this.dmg += dmg; }
}
