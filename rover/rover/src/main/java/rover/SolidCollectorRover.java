package rover;

import bdi.RoverRoleBelief;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;

/**
 * Created by Violet on 11/11/2016.
 */
public class SolidCollectorRover extends CollectorRover {

    private static final int MAX_LOAD = 4;
    private static final int SPEED = 5;
    private static final int SCAN_RANGE = 0;
    private static final int COLLECTOR_TYPE = 1;
    private static String role = "Scout";
    private ArrayList<RoverRoleBelief> roverRoleBeliefs;

    private enum State
    {
        GoingToResource,
        CollectingResource,
        ReturningResource,
        DepositingResource
    }

    private State state;

    public SolidCollectorRover() {
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
        while(!map.hasResourceType(COLLECTOR_TYPE))
        {
            //wait
        }
        //start by moving
        Resource r = map.closestResource(COLLECTOR_TYPE);
        System.out.println("Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
        roverMove(r.getxPos() - xPos,r.getyPos() - yPos);

    }

    @Override
    void poll(PollResult pr)
    {
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
                break;

            case PollResult.SCAN:
                break;

            case PollResult.COLLECT:
                if(currentLoad == MAX_LOAD)
                {
                    state = State.ReturningResource;
                }
                break;

            case PollResult.DEPOSIT:
                if(currentLoad == 0)
                {
                    state = State.GoingToResource;
                }
                break;
        }

        switch (state) {
            case CollectingResource:
                break;

            case DepositingResource:
                break;

            case GoingToResource:
                break;

            case ReturningResource:
                break;
        }
    }
}
