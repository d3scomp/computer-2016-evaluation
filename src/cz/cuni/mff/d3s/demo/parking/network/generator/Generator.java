package cz.cuni.mff.d3s.demo.parking.network.generator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import cz.cuni.mff.d3s.demo.Settings;

public class Generator {

	private final String outputFilePath;
	private final int linksPerEdge;
	private final int cellCount;
	private final double linkLength;
	private int bidirectional;
	
	private int idCounter;
	
	public Generator(int cellCount, int linksPerEdge, double linkLength, String outputFilePath, boolean bidirectional) {
		this.cellCount = cellCount;
		this.linksPerEdge = linksPerEdge;
		this.linkLength = linkLength;
		this.outputFilePath = outputFilePath;
		if (bidirectional) {
			this.bidirectional = -1;
		} else {
			this.bidirectional = 0;
		}
	}

	public List<String> generate() {
		this.idCounter = 0;
		List<List<Node>> intersectionNodes = createIntersectionNodes(cellCount, linksPerEdge, linkLength);
		List<Node> nodes = new LinkedList<Node>();
		List<Link> links = new LinkedList<Link>();
		
		List<String> toDisable = new LinkedList<String>();
		List<List<Link>> newLinksCollections;

		for (List<Node> row : intersectionNodes) {
			newLinksCollections = joinNodes(row, nodes, links, linksPerEdge, linkLength, true, bidirectional);
			for (List<Link> newLinks: newLinksCollections) {
				toDisable.addAll(pickRandomLinks(newLinks, linksPerEdge - Settings.FREE_PARKING_SPOTS_PER_EDGE));
			}
			
			
			if (bidirectional >= 0) {
				bidirectional = (bidirectional + 1) % 2;
			}
			nodes.addAll(row);
		}
		if (bidirectional >= 0) {
			bidirectional = 1;
		}
		for (List<Node> column : getColumnsFromRows(intersectionNodes)) {
			newLinksCollections = joinNodes(column, nodes, links, linksPerEdge, linkLength, false, bidirectional);
			for (List<Link> newLinks: newLinksCollections) {
				toDisable.addAll(pickRandomLinks(newLinks, linksPerEdge - Settings.FREE_PARKING_SPOTS_PER_EDGE));
			}
			
			if (bidirectional >= 0) {
				bidirectional = (bidirectional + 1) % 2;
			}
		}

		Network network = new Network();
		network.setLinks(new Links(links));
		network.setNodes(new Nodes(nodes));
		network.setName("generated-grid");

		try {
			File file = new File(outputFilePath);
			JAXBContext jaxbContext = JAXBContext.newInstance(Network.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			jaxbMarshaller
					.setProperty("com.sun.xml.bind.xmlHeaders",
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE network SYSTEM \"http://www.matsim.org/files/dtd/network_v1.dtd\">");
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(network, file);
			//jaxbMarshaller.marshal(network, System.out);
			return toDisable;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<List<Node>> createIntersectionNodes(int cellCount,
			int linksPerCell, double linklength) {
		List<List<Node>> results = new LinkedList<List<Node>>();
		List<Node> row;
		int nodesPerRowCount = cellCount + 1;
		double x, y, cellLength = linklength * linksPerCell;
		for (int r = 0; r < nodesPerRowCount; r++) {
			row = new LinkedList<Node>();
			y = cellLength * r;
			for (int c = 0; c < nodesPerRowCount; c++) {
				x = cellLength * c;
				row.add(new Node(Integer.toString(idCounter), x, y));
				idCounter++;
			}
			results.add(row);
		}
		return results;
	}

	private List<List<Link>> joinNodes(List<Node> toJoin, List<Node> allNodes,
			List<Link> allLinks, int linksPerCell, double linkLength,
			boolean horizontal, int initialDirection) {
		List<List<Link>> newLinksCollections = new LinkedList<List<Link>>();
		List<Link> newLinks;
		Node start, from, to;
		Link link;
		double x, y;
		int index;
		int direction = initialDirection;
		for (int i = 0; i < toJoin.size() - 1; i++) {
			start = toJoin.get(i);
			from = start;
			newLinks = new LinkedList<Link>();
			for (int l = 0; l < linksPerCell; l++) {
				x = from.getX() + ((horizontal) ? linkLength : 0.0);
				y = from.getY() + ((horizontal) ? 0.0 : linkLength);
				to = new Node(Integer.toString(idCounter), x, y);
				index = toJoin.indexOf(to);
				if (index > -1) {
					to = toJoin.get(index);
				} else {
					idCounter++;
					allNodes.add(to);
				}
				if (direction == 0 || direction == -1) {
					link = new Link(Integer.toString(idCounter), from.getId(),
							to.getId(), linkLength);
					idCounter++;
					newLinks.add(link);
				}
				
				//other direction
				if (direction == 1 || direction ==-1) {
					link = new Link(Integer.toString(idCounter), to.getId(),
							from.getId(), linkLength);
					idCounter++;
					newLinks.add(link);
				}
				from = to;
			}
			if (direction >= 0) {
				direction = (direction + 1) % 2;
			}
			allLinks.addAll(newLinks);
			newLinksCollections.add(newLinks);
		}
		return newLinksCollections;
	}

	private List<List<Node>> getColumnsFromRows(List<List<Node>> rows) {
		List<List<Node>> result = new LinkedList<List<Node>>();
		List<Node> column;
		int nodeCount = rows.size();
		for (int c = 0; c < nodeCount; c++) {
			column = new LinkedList<Node>();
			for (List<Node> row : rows) {
				column.add(row.get(c));
			}
			result.add(column);
		}
		return result;
	}
	
	private List<String> pickRandomLinks(List<Link> links, int count) {
		List<String> result = new LinkedList<String>();
		if (count >= links.size()) {
			for (Link l: links) {
				result.add(l.getId());
			}
		} else {
			List<Link> linksCopy = new LinkedList<Link>(links);
			int loops = count;
			Random r = new Random(count);
			while (loops > 0) {
				result.add(linksCopy.remove(r.nextInt(linksCopy.size())).getId());
				loops--;
			}
		}
		return result;
	}
}
