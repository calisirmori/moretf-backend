package com.moretf.LogMetaData;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogSummary {

    @Id
    private long logid;

    private String title;

    private int matchLength;

    private short redscore;

    private short bluescore;

    private String format;

    private int players;

    private String map;

    private long logDate;

    private String gameType;

    private String combined;
}
