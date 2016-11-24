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
        determineStats();
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

}
