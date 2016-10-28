package map;

import rover.GenericRover;

import java.util.ArrayList;

import static java.lang.Math.sqrt;

/**
 * Created by Violet on 19/10/2016.
 */
public class RoverMap {
    private ArrayList<Resource> resources = new ArrayList<Resource>();
    private GenericRover parent;
    private ArrayList<Node> unexploredNodes = new ArrayList<Node>();

    public RoverMap(GenericRover parent, int scanRange, int worldY, int worldX) {
        this.parent = parent;
        int alternator = 0;
        for(double i = worldY/2 * -1; i< worldY/2; i+= 1.5 * scanRange)
        {
            if(alternator == 0)
            {
                for(double j = 0; j < worldX; j += sqrt(3) * scanRange)
                {
                    unexploredNodes.add(new Node(j,i));
                }
                alternator = 1;
            }
            else
            {
                for(double j = (sqrt(3) * scanRange)/2; j < worldX; j += sqrt(3) * scanRange)
                {
                    unexploredNodes.add(new Node(j,i));
                }
                alternator = 0;
            }
        }
    }

    public boolean contains(Resource newResource)
    {
        for(Resource res : resources)
        {
            if (res.getxPos() == newResource.getxPos() &&  res.getyPos() == newResource.getyPos())
            {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty()
    {
        return resources.isEmpty();
    }

    public void addResource(Resource newResource)
    {
        resources.add(newResource);
    }

    public Resource closestResource()
    {
        Resource closest = resources.get(0);
        for (Resource res : resources)
        {
            if(res.objectiveDistanceTo(parent) < closest.objectiveDistanceTo(parent))
            {
                closest = res;
            }
        }
        return closest;
    }

    public Node closestNode()
    {
        Node closest = unexploredNodes.get(0);
        for (Node node : unexploredNodes)
        {
            if(node.objectiveDistanceTo(parent) < closest.objectiveDistanceTo(parent))
            {
                closest = node;
            }
        }
        return closest;
    }

    public void removeExploredNode(double x, double y)
    {
        int r = 0;
        for(int i = 0; i < unexploredNodes.size(); i++)
        {
            if(unexploredNodes.get(i).getyPos() == y && unexploredNodes.get(i).getxPos() == x)
            {
                r = i;
            }
        }
        System.out.println(unexploredNodes.size());
        unexploredNodes.remove(r);
    }

    public boolean existsUnexploredNode()
    {
        return !unexploredNodes.isEmpty();
    }

}
