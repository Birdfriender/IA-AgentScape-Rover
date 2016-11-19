package rover;

import bdi.RoverRoleBelief;
import map.Resource;

import java.util.ArrayList;

/**
 * Created by Violet on 11/11/2016.
 */
public class CollectorRover extends GenericRover {

    boolean readyToCollect;

    @Override
    public void processMessage(String message)
    {
        String[] splitMessage = message.split("_");
        switch (splitMessage[1])
        {
            case "Hello" :
                roverRoleBeliefs.add(new RoverRoleBelief(splitMessage[0], splitMessage[2]));
                break;

            case "Resource" :
                System.out.println(this.getID() + "Resource Info");
                if(splitMessage[5].equals("Discovered"))
                {
                    System.out.println(this.getID() + "Recieved new resource");
                    Resource res = new Resource(Float.parseFloat(splitMessage[2]),
                            Float.parseFloat(splitMessage[3]),
                            Integer.parseInt(splitMessage[4]));
                    if(!map.contains(res))
                    {
                        map.addResource(res);
                    }
                }
                else if(splitMessage[5].equals("Depleted"))
                {
                    Resource res = new Resource(Float.parseFloat(splitMessage[2]),
                            Float.parseFloat(splitMessage[3]),
                            Integer.parseInt(splitMessage[4]));
                    if(map.contains(res))
                    {
                        map.removeResource(res);
                    }
                }
                break;

            case "Complete" :
                readyToCollect = true;
                break;
        }
    }

}
