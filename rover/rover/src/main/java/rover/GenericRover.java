package rover;

import map.IMapObject;
import map.Node;
import map.Resource;
import map.RoverMap;

import java.lang.Math;


public class GenericRover extends Rover implements IMapObject {

    private RoverMap map;
    private double xPos = 0, yPos = 0;
    private static final int MAX_LOAD = 3;
    private static final int SPEED = 3;
    private static final int SCAN_RANGE = 3;
	private enum State
	{
		Scouting,
        Scanning,
        GoingToResource,
        CollectingResource,
        ReturningResource,
        DepositingResource
	}

	private State state;

	public GenericRover() {
        super();

		//use your username for team name
		setTeam("thh37");
		
		try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			//Fourth attribute is the collector type
			setAttributes(3, 3, 3, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void begin() {
		//called when the world is started
        getLog().info("BEGIN!");
		
		try {
			//start by looking around
			scan(SCAN_RANGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());
		
		if(pr.getResultStatus() == PollResult.FAILED) {
            getLog().info("Ran out of power...");
			return;
		}
		
		switch(pr.getResultType()) {
			case PollResult.MOVE:
                switch (state) {
                    case Scouting:
                        state = State.Scanning;
                        break;
                    case GoingToResource:
                        state = State.CollectingResource;
                        break;
                    case ReturningResource:
                        state = State.DepositingResource;
                        break;
                }
				break;

		    case PollResult.SCAN:
                for(ScanItem item : pr.getScanItems()) {
                    if (item.getItemType() == ScanItem.RESOURCE) {
                        Resource res = new Resource(item.getxOffset() + xPos,
                                item.getyOffset() + yPos, item.getResourceType());
                        if(!map.contains(res))
                        {
                            map.addResource(res);
                        }
                    }
                }
		        if(map.existsUnexploredNode())
                {
                    state = State.Scouting;
                }
                else
                {
                    state = State.GoingToResource;
                }
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

        switch(state){
            case Scouting:
                Node n = map.closestNode();
                try {
                    move(n.getxPos(), n.getyPos(), SPEED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Scanning:
                try {
                    scan(SCAN_RANGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.removeExploredNode(xPos,yPos);
                break;
            case GoingToResource:
                Resource r = map.closestResource();
                try {
                    move(r.getxPos(),r.getyPos(),SPEED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CollectingResource:
                try {
                    collect();
                } catch (Exception e) {
                    state = State.ReturningResource;
                    e.printStackTrace();
                }
                break;
            case ReturningResource:
                try {
                    move(xPos * -1, yPos * -1, SPEED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DepositingResource:
                try {
                    deposit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
		
	}

	private void updateResource(double offsetX, double offsetY, int type)
    {
        double xCoord = xPos + offsetX;
        double yCoord = yPos + offsetY;
        double distance = Math.sqrt((offsetX * offsetX) + (offsetY * offsetY));

        Resource newRes = new Resource(xCoord, yCoord, type);
        if(!map.contains(newRes)) {
            map.addResource(newRes);
        }
    }

    @Override
    public double getxPos() {
        return xPos;
    }

    @Override
    public void setxPos(double xPos) {
        this.xPos = xPos;
    }

    @Override
    public double getyPos() {
        return yPos;
    }

    @Override
    public void setyPos(double yPos) {
        this.yPos = yPos;
    }

    @Override
    public double objectiveDistanceTo(IMapObject object) {
        return Math.sqrt(Math.pow(xPos - object.getxPos(), 2) + Math.pow(yPos - object.getyPos(), 2));
    }
}
