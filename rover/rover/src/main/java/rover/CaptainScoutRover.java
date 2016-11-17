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

    private static final int MAX_LOAD = 0;
    private static final int SPEED = 1;
    private static final int SCAN_RANGE = 8;
    private static final int COLLECTOR_TYPE = 1;
    private static String role = "CaptainScout";

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
        System.out.println("World size " + getWorldWidth() + "x" + getWorldHeight());
        new Thread(comms()).start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int scoutCount = 1; //start at 1 because we'll be scouting too
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            if(belief.getRole() == "Scout")
            {
                scoutCount++;
            }
        }
        allocateMapAreas(scoutCount);
        //start by moving
        Node n = map.closestNode();
        System.out.println("Attempting Move to Node");
        roverMove(n.getxPos() - xPos, n.getyPos() - yPos);

    }

    void allocateMapAreas(int scoutCount)
    {
        int totalNodes = map.numNodes();
        int allocPerRover = totalNodes / scoutCount;
        int i = 0; //im so tired
        for(RoverRoleBelief belief : roverRoleBeliefs)
        {
            if(belief.getRole() == "Scout")
            {
                whisper(belief.getClientID(), "Allocation", Integer.toString(i), Integer.toString(i + allocPerRover));
            }
            i += allocPerRover;
        }
        whisper(this.getID(), "Allocation", Integer.toString(i), Integer.toString(totalNodes % scoutCount));
    }

    @Override
    void poll(PollResult pr) {
        // This is called when one of the actions has completed

        System.out.println("Remaining Power: " + getEnergy());

        if(pr.getResultStatus() == PollResult.FAILED) {
            System.out.println("Ran out of power...");
            return;
        }

        switch(pr.getResultType()) {
            case PollResult.MOVE:
                state = State.Scanning;
                break;

            case PollResult.SCAN:
                for(ScanItem item : pr.getScanItems()) {
                    System.out.println("Found Item");
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
                    System.out.println("Attempting to Scan for New Resources");
                    scan(SCAN_RANGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.removeExploredNode(xPos,yPos);
                break;

            case Scouting:
                Node n = map.closestNode();
                System.out.println("Attempting Move to Node");
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
