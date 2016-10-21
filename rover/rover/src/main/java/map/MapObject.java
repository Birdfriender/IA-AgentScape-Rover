package map;

/**
 * Created by Violet on 19/10/2016.
 */
class MapObject implements IMapObject
{
    private double xPos;
    private double yPos;

    public MapObject(double xPos, double yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public double getxPos() {
        return xPos;
    }

    public void setxPos(double xPos) {
        this.xPos = xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public void setyPos(double yPos) {
        this.yPos = yPos;
    }

    public double objectiveDistanceTo(IMapObject object)
    {
        return Math.sqrt(Math.pow(xPos - object.getxPos(), 2) + Math.pow(yPos - object.getyPos(), 2));
    }
}
