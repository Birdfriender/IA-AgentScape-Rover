package scenario;

import scenario.ScenarioInfo;

import java.util.HashMap;
import java.util.Map;

public class HardCodedScenarioHelper {

    private static final Map<Integer, ScenarioInfo> SCENARIOS;
    static {
        SCENARIOS = new HashMap<>();
        SCENARIOS.put(0, new ScenarioInfo(1, 10, 1.0));
        SCENARIOS.put(1, new ScenarioInfo(5, 5, 1.0));
        SCENARIOS.put(2, new ScenarioInfo(10, 5, 1.0));
        SCENARIOS.put(3, new ScenarioInfo(10, 1, 1.0));
        SCENARIOS.put(4, new ScenarioInfo(15, 1, 1.0));
        SCENARIOS.put(5, new ScenarioInfo(30, 2, 1.0));
        SCENARIOS.put(6, new ScenarioInfo(10, 1, 0.5));
        SCENARIOS.put(7, new ScenarioInfo(25, 5, 0.25));
        SCENARIOS.put(8, new ScenarioInfo(5, 15, 0.5));
        SCENARIOS.put(9, new ScenarioInfo(50, 2, 0.5));
    }

    public ScenarioInfo getScenarioInfoFor(Integer scenarioNumber) {
        return SCENARIOS.get(scenarioNumber);
    }

}
