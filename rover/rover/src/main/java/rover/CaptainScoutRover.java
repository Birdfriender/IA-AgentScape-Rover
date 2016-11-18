package rover;

import bdi.RoverRoleBelief;
import map.Node;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by THH on 08/11/2016.
 */
public class CaptainScoutRover extends ScoutRover {

    private static String role = "CaptainScout";
    private boolean allocatedMap = false;

    private enum State
    {
        Scouting,
        Scanning,
        Waiting
    }

    private State state;

    public CaptainScoutRover() {
        super();

        //use your username for team name
        setTeam("thh37");
        MAX_LOAD = 0;
        SPEED = 3;
        SCAN_RANGE = 6;
        COLLECTOR_TYPE = 1;
        roverRoleBeliefs = new ArrayList<RoverRoleBelief>();

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
            move(0, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void allocateMapAreas(int scoutCount)
    {
        int totalNodes = map.numNodes();
        int allocPerRover = (totalNodes / scoutCount);
        int i = 0; //im so tired
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            if(belief.getRole() == "Scout")
            {
                whisper(belief.getClientID(), "Allocation", Integer.toString(i), Integer.toString(i + allocPerRover));
            }
            i += allocPerRover;
        }
        whisper(this.getID(), "Allocation", Integer.toString(i), Integer.toString(i + allocPerRover + (totalNodes % scoutCount)));
    }

    void initialAllocation()
    {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int scoutCount = 1; //start at 1 because we'll be scouting too
        System.out.println(this.getID() + " Allocating Map");
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            if(belief.getRole() == "Scout")
            {
                scoutCount++;
            }
        }
        allocateMapAreas(scoutCount);
        allocatedMap = true;
        //start by moving
        Node n = map.closestNode();
        System.out.println(this.getID() + " Attempting Move to Node");
        roverMove(n.getxPos() - xPos, n.getyPos() - yPos);
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
                if(!allocatedMap)
                {
                    initialAllocation();
                }
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
}
