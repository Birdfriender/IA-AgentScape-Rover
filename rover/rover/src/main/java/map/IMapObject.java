package map;

/**
 * Created by Violet on 19/10/2016.
 */
public interface IMapObject {

    public double getxPos();
    public void setxPos(double xPos);
    public double getyPos();
    public void setyPos(double yPos);
    public double objectiveDistanceTo(IMapObject object);
}
