package rover;

import bdi.RoverRoleBelief;
import map.IMapObject;
import map.Node;
import map.Resource;
import map.RoverMap;

import java.lang.Math;
import java.util.ArrayList;


public class GenericRover extends Rover implements IMapObject {

    RoverMap map;
    double xPos = 0, yPos = 0;
    double currentLoad = 0;

    int MAX_LOAD;
    int SPEED;
    int SCAN_RANGE ;
    int COLLECTOR_TYPE;
    private static String role = "Generic";
    ArrayList<RoverRoleBelief> roverRoleBeliefs;

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
		SPEED = 3;
        SCAN_RANGE = 3;
        MAX_LOAD = 3;
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
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
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
                System.out.println("Attempting Move to Node");
                roverMove(n.getxPos() - xPos, n.getyPos() - yPos);
                break;
            case Scanning:
                try {
                    System.out.println("Attempting to Scan for New Resources");
                    scan(SCAN_RANGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                map.removeExploredNode(xPos,yPos);
                break;
            case GoingToResource:
                Resource r = map.closestResource(COLLECTOR_TYPE);
                System.out.println("Attempting to Move to Resource at " + r.getxPos() + ", " + r.getyPos());
                roverMove(r.getxPos() - xPos,r.getyPos() - yPos);
                break;
            case CollectingResource:
                try {
                    System.out.println("Attempting to Collect");
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
                    System.out.println("Attempting Move to Base");
                    roverMove(xPos * -1, yPos * -1);
                    e.printStackTrace();
                }
                break;
            case ReturningResource:
                System.out.println("Attempting Move to Base");
                roverMove(xPos * -1, yPos * -1);
                break;
            case DepositingResource:
                try {
                    System.out.println("Attempting to Deposit Resource");
                    if(getCurrentLoad() > 0) {
                        deposit();
                    }
                    else
                    {
                        state = State.GoingToResource;
                        Resource re = map.closestResource(COLLECTOR_TYPE);
                        System.out.println("Attempting to Move to Resource");
                        roverMove(re.getxPos() - xPos,re.getyPos() - yPos);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
		
	}

	protected void roverMove(double x, double y)
    {
        System.out.println(getID() + " Moving from " + xPos + ", " + yPos + " heading: " + x + ", "  + y + " Speed: " + SPEED);
        if(x > getWorldWidth() / 2)
        {
            x = getWorldHeight() - x;
        }
        if(x < getWorldWidth() / -2)
        {
            x = getWorldHeight() + x;
        }

        if( y > getWorldHeight() / 2)
        {
            y = getWorldHeight() - y;
        }
        if( y < getWorldHeight() / -2)
        {
            y = getWorldHeight() + y;
        }

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

	protected void updateResource(double offsetX, double offsetY, int type)
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

    protected void whisper(String target, String header, String... content)
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

    protected void shout(String header, String... content)
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

    public Runnable comms()
    {
        return new Runnable() {
            @Override
            public void run() {
                while(getEnergy() > 0.0)
                {
                    retrieveMessages();

                    for(String message : messages)
                    {
                        System.out.println(getID() + " Recieved Message " + message);
                        processMessage(message);
                    }
                    messages.clear();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void processMessage(String message)
    {
        String[] splitMessage = message.split("_");
        switch (splitMessage[1])
        {
            case "Hello" :
                new RoverRoleBelief(splitMessage[0], splitMessage[2]);
                break;

            case "Resource" :
                System.out.println("Resource Info");
                if(splitMessage[5].equals("Discovered"))
                {
                    System.out.println("Recieved new resource");
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

        }
    }

    public String getRoverRole(String rover)
    {
        for(RoverRoleBelief b : roverRoleBeliefs)
        {
            if (b.getClientID().equals(rover))
            {
                return b.getRole();
            }
        }
        return "NULL";
    }

    double energyRequiredToMove(double destinationX, double destinationY)
    {
        return 2 * (Math.hypot(destinationX,destinationY)/SPEED);
    }

    double energyRequiredToScan()
    {
        return 10;
    }

    double energyRequireToCollect()
    {
        return 5;
    }

    double energyRequiredToDeposit()
    {
        return 5;
    }
}
