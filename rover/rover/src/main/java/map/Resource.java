package map;

/**
 * Created by Violet on 19/10/2016.
 */
public class Resource extends MapObject {

    private int type;
    private int numResources;

    public Resource(double xPos, double yPos, int type, int numResources) {
        super(xPos, yPos);
        this.type = type;
        this.numResources = numResources;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumResources() {
        return numResources;
    }

    public void decrementResources()
    {
        numResources--;
    }
}
