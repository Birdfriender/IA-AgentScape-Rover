package rover;

import bdi.RoverRoleBelief;
import map.Resource;

import java.util.ArrayList;

/**
 * Created by Violet on 11/11/2016.
 */
public class CollectorRover extends GenericRover {

    boolean readyToCollect;
    double regionStart, regionEnd;

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
                            Integer.parseInt(splitMessage[4]),
                            scenarioInfo.getNumberPerLump());
                    if(!map.contains(res))
                    {
                        map.addResource(res);
                    }
                }
                else if(splitMessage[5].equals("Depleted"))
                {
                    Resource res = new Resource(Double.parseDouble(splitMessage[2]),
                            Double.parseDouble(splitMessage[3]),
                            Integer.parseInt(splitMessage[4]),
                            scenarioInfo.getNumberPerLump());
                    map.removeResource(res);
                }
                break;

            case "Complete" :
                readyToCollect = true;
                break;

            case "Allocation" :
                regionStart = Double.parseDouble(splitMessage[2]);
                regionEnd = Double.parseDouble(splitMessage[3]);
                break;
        }
    }

    @Override
    void poll(PollResult pr)
    {
        while (!readyToCollect)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        switch(pr.getResultType()) {
            case PollResult.MOVE:
                if(state == State.GoingToResource)
                {
                    state = State.CollectingResource;
                }
                else if (state == State.ReturningResource)
                {
                    state = State.DepositingResource;
                }
                else if (state == State.Started)
                {
                    state = State.GoingToResource;
                }
                break;

            case PollResult.SCAN:
                break;

            case PollResult.COLLECT:
                if(getCurrentLoad() == MAX_LOAD)
                {
                    state = State.ReturningResource;
                }
                break;

            case PollResult.DEPOSIT:
                if(getCurrentLoad() == 0)
                {
                    state = State.GoingToResource;
                }
                break;
        }

        switch (state) {
            case CollectingResource:
                try {
                    System.out.println(getID() + " Collecting");
                    collect();
                } catch (Exception e) {
                    System.out.println(getID() + " Depleted Resource");
                    Resource res = new Resource(xPos, yPos, 0, scenarioInfo.getNumberPerLump()); //type doesnt matter here, bit awkward but oh well
                    shout("Resource",
                            Double.toString(res.getxPos()),
                            Double.toString(res.getyPos()),
                            Integer.toString(res.getType()),
                            "Depleted");
                    whisper(this.getID(),
                            "Resource",
                            Double.toString(res.getxPos()),
                            Double.toString(res.getyPos()),
                            Integer.toString(res.getType()),
                            "Depleted");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if(getCurrentLoad() == MAX_LOAD || !map.hasResourceType(COLLECTOR_TYPE))
                    {
                        System.out.println("Attempting to move to Base");
                        state = State.ReturningResource;
                        roverMove(-xPos, -yPos);
                    }
                    else
                    {
                        System.out.println(this.getID() + " Attempting Move to New Resource");
                        Resource r = map.closestResource(COLLECTOR_TYPE, regionStart, regionEnd);
                        System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                        state = State.GoingToResource;
                        roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                    }
                }
                break;

            case DepositingResource:
                try {
                    System.out.println(getID() + " Depositing");
                    deposit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case GoingToResource:
                System.out.println(getID() + " Going to Resource");
                Resource r = map.closestResource(COLLECTOR_TYPE, regionStart, regionEnd);
                if(r == null)
                {
                    //move nowhere
                    roverMove(xPos, yPos);
                }
                else
                {
                    System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                    roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                }
                break;

            case ReturningResource:
                System.out.println(getID() + " Returning Resource");
                System.out.println(this.getID() + " Attempting Move to Base");
                roverMove(xPos * -1, yPos * -1);
                break;
        }
    }

}
