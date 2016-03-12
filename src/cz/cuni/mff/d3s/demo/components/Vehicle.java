package cz.cuni.mff.d3s.demo.components;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.scheduler.CurrentTimeProvider;
import cz.cuni.mff.d3s.deeco.simulation.matsim.MATSimRouter;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.demo.MapUtil;
import cz.cuni.mff.d3s.demo.environment.Actuator;
import cz.cuni.mff.d3s.demo.environment.ActuatorProvider;
import cz.cuni.mff.d3s.demo.environment.ActuatorType;
import cz.cuni.mff.d3s.demo.environment.Sensor;
import cz.cuni.mff.d3s.demo.environment.SensorProvider;
import cz.cuni.mff.d3s.demo.environment.SensorType;

@Component
public class Vehicle {

	/**
	 * Distance (as crow flies) from the destination within which the vehicle
	 * tries to find a parking place and park there.
	 */
	public static final double PARKING_DISTANCE_THRESHOLD = 4000;

	/**
	 * Id of the vehicle component.
	 */
	public String id;

	/**
	 * This is where the vehicle is heading to.
	 */
	public Id destinationLink;

	/**
	 * Contains a list of link ids that lead to the destination. It is given to
	 * the MATSim to guide the vehicle which way it should go.
	 */
	public List<Id> route;

	/**
	 * Link, which has been selected to be parked at. Is <code>null</code> if
	 * none has been selected. The link is selected when the vehicle gets close
	 * to its destination. Once it is selected, the vehicle heads to this link
	 * instead of to its destination.
	 */
	public Id linkToBeParkedAt;

	/**
	 * Link where the vehicle is currently at.
	 */
	public Id currentLink;

	/**
	 * Position of the current link.
	 */
	public Coord position;
	
	public Coord destination;

	public Boolean parked;

	/**
	 * A set of links, which have been found fully occupied in the recent time
	 * (defined by OCCUPIED_LINK_ENTRY_VALIDITY). The set is kept in a map,
	 * which maps Id -> timestamp (in miliseconds since midnights).
	 */
	public String sensedOccupiedLinks = "";

	public String learntOccupiedLinks = "";

	public List<String> otherVehicles = new LinkedList<String>();

	@Local
	public Actuator<List<Id>> routeActuator;

	@Local
	public Sensor<Id> currentLinkSensor;
	@Local
	public Sensor<Boolean> isParkedSensor;
	@Local
	public Sensor<Integer> currentLinkFreeCapacitySensor;

	@Local
	public MATSimRouter router;
	@Local
	public CurrentTimeProvider clock;

	public Vehicle(String id, Id destinationLink, Id currentLink,
			ActuatorProvider actuatorProvider, SensorProvider sensorProvider,
			MATSimRouter router, CurrentTimeProvider clock) {
		this.id = id;
		this.destinationLink = destinationLink;
		this.destination = router.findLinkById(destinationLink).getCoord();
		this.parked = false;

		this.routeActuator = actuatorProvider
				.createActuator(ActuatorType.ROUTE);

		this.currentLinkSensor = sensorProvider
				.createSensor(SensorType.CURRENT_LINK);
		this.isParkedSensor = sensorProvider.createSensor(SensorType.IS_PARKED);
		this.currentLinkFreeCapacitySensor = sensorProvider
				.createSensor(SensorType.CURRENT_LINK_FREE_PLACES);

		this.router = router;
		this.clock = clock;
	}

	/**
	 * Periodically prints out the values of sensors and important values of the
	 * knowledge.
	 */
	@Process
	@PeriodicScheduling(period = 5000, order = 10)
	public static void reportStatus(
			@In("id") String id,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("isParkedSensor") Sensor<Boolean> isParkedSensor,
			@In("currentLinkFreeCapacitySensor") Sensor<Integer> currentLinkFreeCapacitySensor,
			@In("destinationLink") Id destinationLink,
			@In("linkToBeParkedAt") Id linkToBeParkedAt,
			@In("otherVehicles") List<String> otherVehicles,
			@In("route") List<Id> route, @In("clock") CurrentTimeProvider clock) {

		Log.d("Entry [" + id + "]:reportStatus");

		long ts = clock.getCurrentMilliseconds();
		int msec = (int) (ts % 1000);
		ts = ts / 1000;
		int sec = (int) (ts % 60);
		ts = ts / 60;
		int min = (int) (ts % 60);
		ts = ts / 60;
		int hour = (int) ts;

		System.out
				.format("<%02d:%02d:%02d.%03d> [%s]  link: %s  currentLinkFreeCapacitySensor: %d  isParked: %s  destinationLink: %s  linkToBeParkedAt: %s othersDestinations: %s route: %s\n",
						hour, min, sec, msec, id, currentLinkSensor.read(),
						currentLinkFreeCapacitySensor.read(),
						isParkedSensor.read(), destinationLink,
						linkToBeParkedAt, otherVehicles.toString(), route);
	}

	/**
	 * Periodically updates knowledge based on sensor readings. These knowledge
	 * fields are updated: currentLink.
	 */
	@Process
	@PeriodicScheduling(period = 200, order = 1)
	public static void updateSensors(@In("id") String id,
			@Out("currentLink") ParamHolder<Id> currentLinkHolder,
			@Out("position") ParamHolder<Coord> position,
			@Out("parked") ParamHolder<Boolean> parked,
			@In("currentLinkSensor") Sensor<Id> currentLinkSensor,
			@In("isParkedSensor") Sensor<Boolean> isParkedSensor,
			@In("router") MATSimRouter router) {

		Log.d("Entry [" + id + "]:updateCurrentLink");

		currentLinkHolder.value = currentLinkSensor.read();
		position.value = router.getLink(currentLinkHolder.value).getCoord();
		parked.value = isParkedSensor.read();
	}

	/**
	 * Periodically updated the map of fully occupied links by information
	 * obtained from the current link free capacity sensor. It removes entries
	 * which are older than
	 */
	@Process
	@PeriodicScheduling(period = 800, order = 2)
	public static void updateOccupiedLinks(
			@In("id") String id,
			@In("currentLink") Id currentLink,
			@In("currentLinkFreeCapacitySensor") Sensor<Integer> currentLinkFreeCapacitySensor,
			@In("learntOccupiedLinks") String learntOccupiedLinks,
			@InOut("sensedOccupiedLinks") ParamHolder<String> sensedOccupiedLinks,
			@In("clock") CurrentTimeProvider clock) {

		Map<String, String> sensedOccupiedLinksMap = MapUtil.stringToMap(sensedOccupiedLinks.value);
		Map<String, String> learntOccupiedLinksMap = MapUtil.stringToMap(learntOccupiedLinks);
		Log.d("Entry [" + id + "]:updateOccupiedLinks");
		
		if (currentLinkFreeCapacitySensor.read() == 0) {
			sensedOccupiedLinksMap.put(currentLink.toString(), Long.toString(clock.getCurrentMilliseconds()));
		} else {
			sensedOccupiedLinksMap.remove(currentLink);
		}

		Iterator<Map.Entry<String, String>> sensedEntries = sensedOccupiedLinksMap.entrySet().iterator();
		Map.Entry<String, String> sensedEntry;
		String lValueStr;
		while (sensedEntries.hasNext()) {
			sensedEntry = sensedEntries.next();
			lValueStr = learntOccupiedLinksMap.get(sensedEntry.getKey());
			if (lValueStr != null
					&& Long.parseLong(lValueStr) > Long.parseLong(sensedEntry.getValue())) {
				sensedEntries.remove();
			}
		}
		sensedOccupiedLinks.value = MapUtil.mapToString(sensedOccupiedLinksMap);
	}

	/**
	 * Selects a link, where the vehicle is to park or sets it <code>null</code>
	 * if no link can be chosen. The link is selected as soon as a vehicle gets
	 * close to its destination.
	 */
	@Process
	@PeriodicScheduling(period = 1000, order = 3)
	public static void selectLinkToBeParkedAt(
			@In("id") String id,
			@In("destinationLink") Id destinationLink,
			@In("currentLink") Id currentLink,
			@In("learntOccupiedLinks") String learntOccupiedLinks,
			@In("sensedOccupiedLinks") String sensedOccupiedLinks,
			@In("otherVehicles") List<String> otherVehicles,
			@InOut("linkToBeParkedAt") ParamHolder<Id> linkToBeParkedAt,
			@In("isParkedSensor") Sensor<Boolean> isParkedSensor,
			@In("route") List<Id> route,
			@In("router") MATSimRouter router,
			@In("clock") CurrentTimeProvider clock) {
		if (!isParkedSensor.read()) {
			List<String> vd = new LinkedList<String>(otherVehicles);
			vd.add(id);
			Map<String, String> sensedOccupiedLinksMap = MapUtil.stringToMap(sensedOccupiedLinks);
			Map<String, String> learntOccupiedLinksMap = MapUtil.stringToMap(learntOccupiedLinks);
			Set<String> occupiedLinks = new HashSet<String>(sensedOccupiedLinksMap.keySet());
			occupiedLinks.addAll(learntOccupiedLinksMap.keySet());
			linkToBeParkedAt.value = planForAllVehicles(id, destinationLink, vd, occupiedLinks, router);
		}
	}

	/**
	 * Plans the route to the destination or to the linkToBeParkedAt (if this is
	 * set) and drives the vehicle accordingly.
	 */
	@Process
	@PeriodicScheduling(period = 2000, order = 4)
	public static void planRouteAndDrive(
			@In("id") String id,
			@In("currentLink") Id currentLink,
			@In("destinationLink") Id destinationLink,
			@In("linkToBeParkedAt") Id linkToBeParkedAt,
			@InOut("route") ParamHolder<List<Id>> route,
			@In("routeActuator") Actuator<List<Id>> routeActuator,
			@In("parked") Boolean parked,
			@In("router") MATSimRouter router,
			@In("currentLinkFreeCapacitySensor") Sensor<Integer> currentLinkFreeCapacitySensor) {
		if (currentLink != null) {
			if (!parked) {
				Log.d("Entry [" + id + "]:planRouteAndDrive");
				Id linkToNavigateTo = linkToBeParkedAt == null ? destinationLink
						: linkToBeParkedAt;
				if (currentLink.equals(linkToNavigateTo)
						&& currentLinkFreeCapacitySensor.read() > 0) {
					route.value = new LinkedList<Id>();
				} else {
					if (route.value != null && !route.value.isEmpty()) {
						if (route.value.get(route.value.size() - 1).equals(
								linkToNavigateTo)) {
							route.value = route.value.subList(route.value.lastIndexOf(currentLink), route.value.size());
						}
					}
					route.value = router.route(currentLink, linkToNavigateTo,
							route.value);
				}
			} else {
				route.value = new LinkedList<Id>();
			}
			routeActuator.set(route.value);
		}
	}

	/**
	 * -------------------------------------- Private functions
	 * ----------------------------------------
	 */

	private static Id findFirstUnoccupiedLink(Id destinationLink,
			Set<String> occupiedLinks, MATSimRouter router) {
		boolean linkToParkNotFound = true;
		List<Id> toAnalyze = new LinkedList<Id>();
		toAnalyze.add(destinationLink);
		List<Id> analyzed = new LinkedList<Id>();
		List<Id> askedForAdjacent = new LinkedList<Id>();
		Id link;
		while (linkToParkNotFound) {
			Iterator<Id> links = toAnalyze.iterator();
			while (links.hasNext()) {
				link = links.next();
				if (!occupiedLinks.contains(link.toString())) {
					return link;
				}
				// If link's capacity is 0 lets continue our search.
				// First we need to remove the link that we are analyzing
				// from the "toAnalyze" collection.
				links.remove();
				// and put it to the analyzed.
				analyzed.add(link);
			}
			// We should ask one of the analyzed links for adjacent links
			if (!analyzed.isEmpty()) {
				link = null;
				for (Id a : analyzed) {
					if (!askedForAdjacent.contains(a)) {
						link = a;
						break;
					}
				}
				if (link != null) {
					toAnalyze.addAll(router.getAdjacentLinks(link));
					toAnalyze.removeAll(analyzed);
					askedForAdjacent.add(link);
				} else {
					linkToParkNotFound = false;
				}
			}
		}
		return null;
	}

	private static Id planForAllVehicles(String id, Id destinationLinkId,
			Collection<String> vehicles, Set<String> occupiedLinks,
			final MATSimRouter router) {
		List<String> sortedVehicles = new LinkedList<String>(vehicles);
		// Sort vehicles based on destination and hashCode
		Collections.sort(sortedVehicles, new Comparator<String>() {
			public int compare(String a,
					String b) {
				if (a == null && b == null) {
					return 0;
				} else if (a == null) {
					return 1;
				} else if (b == null) {
					return -1;
				} else {
					return a.compareTo(b);
				}
			}
		});
		// Starting from the very first try to find first unoccupied and assign
		// the links updating relevant capacities;
		Map<String, Id> linkAssignments = new HashMap<String, Id>();
		Id link;
		for (String v : sortedVehicles) {
			link = findFirstUnoccupiedLink(destinationLinkId, occupiedLinks,
					router);
			linkAssignments.put(v, link);
			if (!occupiedLinks.contains(link.toString())) {
				occupiedLinks.add(link.toString());
			}
		}
		// return my link id.
		return linkAssignments.get(id);
	}
}
