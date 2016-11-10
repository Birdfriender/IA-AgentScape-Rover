package rover;

import bdi.RoverRoleBelief;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;

/**
 * Created by THH on 08/11/2016.
 */
public class CaptainScoutRover extends ScoutRover {

    private RoverMap map;
    private double xPos = 0, yPos = 0;
    private double currentLoad = 0;
    private static final int MAX_LOAD = 0;
    private static final int SPEED = 1;
    private static final int SCAN_RANGE = 8;
    private static final int COLLECTOR_TYPE = 1;
    private static String role = "CaptainScout";
    private ArrayList<RoverRoleBelief> roverRoleBeliefs;

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
            //start by looking around
            getLog().info("Start by Scanning");
            scan(SCAN_RANGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                Resource r = map.closestResource();
                System.out.println("Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                break;

            case Waiting:
                try {
                    scan(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

    }

    private void roverMove(double x, double y)
    {
        System.out.println("Moving from " + xPos + ", " + yPos + " heading: " + x + ", "  + y);
        try {
            move(x, y, SPEED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        xPos += x;
        yPos += y;
        if(xPos > getWorldWidth()/2)
        {
            xPos -= getWorldWidth();
        }
        else if(xPos < (getWorldWidth()/2) * -1)
        {
            xPos += getWorldWidth();
        }

        if(yPos > getWorldHeight()/2)
        {
            yPos -= getWorldHeight();
        }
        else if(yPos < (getWorldHeight()/2) * -1)
        {
            yPos += getWorldHeight();
        }
    }

    private void updateResource(double offsetX, double offsetY, int type)
    {
        double xCoord = xPos + offsetX;
        double yCoord = yPos + offsetY;

        if(xCoord > getWorldWidth()/2)
        {
            xCoord = xCoord - getWorldWidth();
        }
        else if(xCoord < (getWorldWidth()/2) * -1)
        {
            xCoord = xCoord + getWorldWidth();
        }

        if(yCoord > getWorldHeight()/2)
        {
            yCoord = yCoord - getWorldHeight();
        }
        else if(yCoord < (getWorldHeight()/2) * -1)
        {
            yCoord = yCoord + getWorldHeight();
        }

        Resource newRes = new Resource(xCoord, yCoord, type);
        if(!map.contains(newRes)) {
            shout("Resource",
                    Double.toString(newRes.getxPos()),
                    Double.toString(newRes.getyPos()),
                    Integer.toString(newRes.getType()),
                    "Discovered");
            whisper(this.getID(),
                    "Resource",
                    Double.toString(newRes.getxPos()),
                    Double.toString(newRes.getyPos()),
                    Integer.toString(newRes.getType()),
                    "Discovered");
        }
    }

    private void whisper(String target, String header, String... content)
    {
        String message = "";
        message = message.concat(this.getID());
        message = message.concat("_");
        message = message.concat(header);
        for(String s : content)
        {
            message = message.concat("_");
            message = message.concat(s);
        }
        System.out.println("Whisper " + message + " to " + target);
        broadCastToUnit(target, message);
    }

    public void shout(String header, String... content)
    {
        String message = "";
        message = message.concat(this.getID());
        message = message.concat("_");
        message = message.concat(header);
        for(String s : content)
        {
            message = message.concat("_");
            message = message.concat(s);
        }
        System.out.println("Shouting " + message);
        broadCastToTeam(message);
    }
}
