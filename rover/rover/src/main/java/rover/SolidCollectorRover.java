package rover;

import bdi.RoverRoleBelief;
import map.Resource;
import map.RoverMap;

import java.util.ArrayList;

/**
 * Created by Violet on 11/11/2016.
 */
public class SolidCollectorRover extends CollectorRover {

    private static String role = "SolidCollector";
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

        MAX_LOAD = 4;
        SPEED = 5;
        SCAN_RANGE = 0;
        COLLECTOR_TYPE = 1;
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
        System.out.println(this.getID() + " World size " + getWorldWidth() + "x" + getWorldHeight());
        new Thread(comms()).start();
        while(!map.hasResourceType(COLLECTOR_TYPE))
        {
            //wait
        }
        //start by moving
        Resource r = map.closestResource(COLLECTOR_TYPE);
        System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
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
                try {
                    collect();
                } catch (Exception e) {
                    state = State.ReturningResource;
                    Resource res = new Resource(xPos, yPos, 0); //type doesnt matter here, bit awkward but oh well
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
                    System.out.println(this.getID() + " Attempting Move to New Resource");
                    while(!map.hasResourceType(COLLECTOR_TYPE))
                    {
                        //wait
                    }
                    Resource r = map.closestResource(COLLECTOR_TYPE);
                    System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                    roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                }
                break;

            case DepositingResource:
                try {
                    deposit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case GoingToResource:
                while(!map.hasResourceType(COLLECTOR_TYPE))
                {
                    //wait
                }
                Resource r = map.closestResource(COLLECTOR_TYPE);
                System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                break;

            case ReturningResource:
                System.out.println(this.getID() + " Attempting Move to Base");
                roverMove(xPos * -1, yPos * -1);
                break;
        }
    }
}
