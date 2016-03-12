package cz.cuni.mff.d3s.demo;

public class Settings {
	
	public static final String MATSIM_CONFIG_TEMPLATE = "input/config.xml";
	public static final String ANALYSES_DIRECTORY = "measurements";
	
	public static int VEHICLE_COUNT = 10;
	
	//Router settings
	public static final int ROUTE_CALCULATION_OFFSET = 5;
	
	//ENSEMBLE
	public static final double ENSEMBLE_RADIUS = 700.0; // sqrt(2) * 500
	
	//Network generation
	public static final int CELL_COUNT = 10;
	public static final int LINKS_PER_EDGE = 20;	// each street is 200m long assuming 10m long segment
	public static final double LINK_LENGTH = 10.0; //IN METERS
	public static final String NETWORK_OUTPUT = "input/network.xml";
	public static final boolean BIDIRECTIONAL_STREETS = false;
	public static final int FREE_PARKING_SPOTS_PER_EDGE = 3;
	//Link parameters
	public final static double FREE_SPEED = 15.0;
	public final static double CAPACITY = 5000.0;
	public final static double PERMLANES = 3.0;
	public final static int ONEWAY = 1;
	public final static String MODES = "car";
	
	//OMNetMATSim simulation parameters
	public static String OMNET_CONFIG_TEMPLATE = "omnetpp.ini.templ";
	
	//MATSim
	public static final long FULL_SIMULATION_DURATION = 600000; // IN MILLIS
	public static final long PARTIAL_SIMULATION_DURATION = 600000; // IN MILLIS
	public static final long NONE_SIMULATION_DURATION = 1200000; // IN MILLIS
	public static final String MATSIM_INPUT = "input";
	public static final String MATSIM_OUTPUT = "matsim";
	public static String MATSIM_CONFIG = "";
}
