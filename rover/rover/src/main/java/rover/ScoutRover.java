package rover;

import bdi.RoverRoleBelief;
import map.Node;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;

/**
 * Created by Violet on 07/11/2016.
 */
public class ScoutRover extends GenericRover {

    private static String role = "Scout";
    protected boolean gotAllocation = false;

    private enum State
    {
        Scouting,
        Scanning,
        Waiting
    }

    private State state;

    public ScoutRover() {
        super();

        //use your username for team name
        setTeam("thh37");
        MAX_LOAD = 0;
        SPEED = 3;
        SCAN_RANGE = 6;
        COLLECTOR_TYPE = 1;

        try {
            //set attributes for this rover
            //speed, scan range, max load
            //has to add up to <= 9
            //Fourth attribute is the collector type
            setAttributes(SPEED, SCAN_RANGE, MAX_LOAD, COLLECTOR_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    void begin() {
        //called when the world is started
        getLog().info("BEGIN!");
        shout("Hello", role);
        map = new RoverMap(this, SCAN_RANGE, getWorldHeight(), getWorldWidth());
        System.out.println(this.getID() + " World size " + getWorldWidth() + "x" + getWorldHeight());
        new Thread(comms()).start();
        try {
            move(0,0,1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    void poll(PollResult pr) {
        // This is called when one of the actions has completed
        System.out.println("Waiting for allocation");
        while (!gotAllocation)
        {
            //wait
        }

        System.out.println(this.getID() + " Remaining Power: " + getEnergy());

        if(pr.getResultStatus() == PollResult.FAILED) {
            System.out.println(this.getID() + " Ran out of power...");
            return;
        }

        switch(pr.getResultType()) {
            case PollResult.MOVE:
                state = State.Scanning;
                break;

            case PollResult.SCAN:
                for(ScanItem item : pr.getScanItems()) {
                    System.out.println(this.getID() + " Found Item");
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        updateResource(item.getxOffset(), item.getyOffset(), item.getItemType());
                    }
                }
                if(map.existsUnexploredNode())
                {
                    state = State.Scouting;
                }
                else
                {
                    state = State.Waiting;
                }
                break;

            case PollResult.COLLECT:
                //this wont ever happen
                break;

            case PollResult.DEPOSIT:
                //nor this
                break;
        }

        switch (state) {
            case Scanning:
                try {
                    System.out.println(this.getID() + " Attempting to Scan for New Resources");
                    scan(SCAN_RANGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.removeExploredNode(xPos,yPos);
                break;

            case Scouting:
                Node n = map.closestNode();
                System.out.println(this.getID() + " Attempting Move to Node");
                roverMove(n.getxPos() - xPos, n.getyPos() - yPos);
                break;

            case Waiting:
                try {
                    scan(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

    }

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

            case "Allocation":
                System.out.println(this.getID() + " Scouting Allocation");
                map.selectArea(Integer.parseInt(splitMessage[2]), Integer.parseInt(splitMessage[3]));
                gotAllocation = true;
                break;

        }
    }
}
