package map;

/**
 * Created by Violet on 19/10/2016.
 */
public class Resource extends MapObject {

    private int type;

    public Resource(double xPos, double yPos, int type) {
        super(xPos, yPos);
        this.type = type;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
