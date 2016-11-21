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
        DepositingResource,
        Started
    }

    private State state;

    public SolidCollectorRover() {
        super();

        MAX_LOAD = 4;
        SPEED = 5;
        SCAN_RANGE = 0;
        COLLECTOR_TYPE = 1;
        roverRoleBeliefs = new ArrayList<>();
        state = State.Started;
        readyToCollect = false;
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
        map = new RoverMap(this);
        System.out.println(this.getID() + " World size " + getWorldWidth() + "x" + getWorldHeight());
        new Thread(comms()).start();

        //start by moving
        try {
            move(0,0, 1);
        } catch (Exception e) {
            e.printStackTrace();
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
                    System.out.println(getID() + " Collecting");
                    collect();
                } catch (Exception e) {
                    System.out.println(getID() + " Depleted Resource");
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
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if(getCurrentLoad() == MAX_LOAD || !map.hasResourceType(COLLECTOR_TYPE))
                    {
                        System.out.println("Attempting to move to Base");
                        roverMove(-xPos, -yPos);
                    }
                    else
                    {
                        System.out.println(this.getID() + " Attempting Move to New Resource");
                        Resource r = map.closestResource(COLLECTOR_TYPE);
                        System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
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
                Resource r = map.closestResource(COLLECTOR_TYPE);
                System.out.println(this.getID() + " Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                break;

            case ReturningResource:
                System.out.println(getID() + " Returning Resource");
                System.out.println(this.getID() + " Attempting Move to Base");
                roverMove(xPos * -1, yPos * -1);
                break;
        }
    }
}
