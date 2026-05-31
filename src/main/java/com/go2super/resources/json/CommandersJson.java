package com.go2super.resources.json;

import com.go2super.resources.data.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CommandersJson {

    private List<CommanderStatsData> stats;
    private List<CommanderLevelsData> levels;
    private List<CommanderProbabilityData> probabilities;
    private List<CommanderCureData> cures;
    private List<CommanderPullulateData> pullulates;

}
