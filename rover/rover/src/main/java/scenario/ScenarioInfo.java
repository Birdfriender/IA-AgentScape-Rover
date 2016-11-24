package scenario;

public class ScenarioInfo {
    private int numberOfLumps;
    private int numberPerLump;
    private double percentageOfSolid;

    public ScenarioInfo(int numberOfLumps,
                        int numberPerLump,
                        double percentageOfSolid) {
        this.numberOfLumps = numberOfLumps;
        this.numberPerLump = numberPerLump;
        this.percentageOfSolid = percentageOfSolid;
    }

    public int getNumberOfLumps() {
        return numberOfLumps;
    }

    public double getPercentageOfSolid() {
        return percentageOfSolid;
    }

    public int getNumberPerLump() {
        return numberPerLump;
    }
}
