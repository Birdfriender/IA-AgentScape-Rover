package rover;

import bdi.RoverRoleBelief;
import map.Node;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;

/**
 * Created by THH on 08/11/2016.
 */
public class CaptainScoutRover extends ScoutRover {

    private static String role = "CaptainScout";
    private boolean allocatedMap = false;
    private int activeScoutCount;
    private int solidCollectorCount;
    private int liquidCollectorCount;

    public CaptainScoutRover() {
        super();

        //use your username for team name
        setTeam("thh37");
        MAX_LOAD = 0;
        SPEED = 3;
        SCAN_RANGE = 6;
        COLLECTOR_TYPE = 1;
        roverRoleBeliefs = new ArrayList<>();

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
        map = new RoverMap(this);
        map.generateNodes(SCAN_RANGE, getWorldHeight(), getWorldWidth());
        System.out.println(this.getID() + " World size " + getWorldWidth() + "x" + getWorldHeight());
        new Thread(comms()).start();
        try {
            move(0, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void allocateMapAreas()
    {
        double scoutCounter = getWorldHeight()/-2;
        double solidCounter = -getWorldWidth()/2;
        double liquidCounter = -getWorldWidth()/2;
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            if(belief.getRole().equals("Scout"))
            {
                whisper(belief.getClientID(), "Allocation", Double.toString(scoutCounter),
                Double.toString(scoutCounter + (getWorldWidth()/activeScoutCount)));
                scoutCounter += getWorldWidth()/activeScoutCount;
            }
            if(belief.getRole().equals("SolidCollector"))
            {
                whisper(belief.getClientID(), "Allocation", Double.toString(solidCounter),
                        Double.toString(solidCounter + (getWorldWidth()/solidCollectorCount)));
                solidCounter += getWorldWidth()/solidCollectorCount;
            }
            if(belief.getRole().equals("LiquidCollector"))
            {
                whisper(belief.getClientID(), "Allocation", Double.toString(liquidCounter),
                        Double.toString(liquidCounter + (getWorldWidth()/liquidCollectorCount)));
                solidCounter += getWorldWidth()/liquidCollectorCount;
            }

        }
        whisper(getID(), "Allocation", Double.toString(scoutCounter),
                Double.toString(scoutCounter + getWorldWidth()/activeScoutCount));
    }

    private void initialAllocation()
    {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activeScoutCount = 1; //start at 1 because we'll be scouting too
        solidCollectorCount = 0;
        liquidCollectorCount = 0;
        System.out.println(this.getID() + " Allocating Map");
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            switch (belief.getRole()) {
                case "Scout":
                    activeScoutCount++;
                    break;
                case "SolidCollector":
                    solidCollectorCount++;
                    break;
                case "LiquidCollector":
                    liquidCollectorCount++;
                    break;
            }
        }
        allocateMapAreas();
        allocatedMap = true;
    }

    @Override
    void poll(PollResult pr) {
        // This is called when one of the actions has completed

        System.out.println(this.getID() + " Remaining Power: " + getEnergy());

        if(pr.getResultStatus() == PollResult.FAILED) {
            System.out.println(this.getID() + " Ran out of power...");
            return;
        }

        switch(pr.getResultType()) {
            case PollResult.MOVE:
                if(state != State.Waiting)
                {
                    if(!allocatedMap)
                    {
                        initialAllocation();
                    }
                    state = State.Scanning;
                }
                break;

            case PollResult.SCAN:
                if(state != State.Waiting)
                {
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
                        if(state != State.Waiting)
                        {
                            whisper(getID(), "Complete");
                            state = State.Waiting;
                        }
                        state = State.Waiting;
                    }
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
                if(!map.existsUnexploredNode())
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(energyRequiredToScan() > getEnergy()) {
                    whisper(getID(), "Complete");
                    state = State.Waiting;
                    try {
                        scan(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        System.out.println(this.getID() + " Attempting to Scan for New Resources");
                        scan(SCAN_RANGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.removeExploredNode(xPos, yPos);
                }
                break;

            case Scouting:
                Node n = determineNextNode();
                if(energyRequiredToMove(n.getxPos() - xPos, n.getyPos() - yPos) > getEnergy()) {
                    whisper(getID(), "Complete");
                    state = State.Waiting;
                    try {
                        scan(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    System.out.println(this.getID() + " Attempting Move to Node");
                    roverMove(n.getxPos() - xPos, n.getyPos() - yPos);
                }
                break;

            case Waiting:
                System.out.println(getID() + " Scout Count: " + Integer.toString(activeScoutCount));
                try {
                    scan(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(activeScoutCount == 0)
                {
                    for(RoverRoleBelief r : roverRoleBeliefs)
                    {
                        if(r.getRole().equals("SolidCollector") || r.getRole().equals("LiquidCollector"))
                        {
                            whisper(r.getClientID(), "Complete");
                        }
                    }
                    activeScoutCount -= 1; //so it only sends once;
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
                map.addNode(Double.parseDouble(splitMessage[2]), Double.parseDouble(splitMessage[3]));
                break;

            case "Complete":
                activeScoutCount -= 1;
                break;

        }
    }
}
