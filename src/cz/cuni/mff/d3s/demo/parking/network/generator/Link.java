package cz.cuni.mff.d3s.demo.parking.network.generator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import cz.cuni.mff.d3s.demo.Settings;


@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType (propOrder={"id","from","to","length","freespeed","capacity","permlanes","oneway","modes"})
public class Link {	
	
	private String id;
	private String from;
	private String to;
	private double length;
	private double freespeed;
	private double capacity;
	private double permlanes;
	private int oneway;
	private String modes;
	
	public Link() {}

	public Link(String id, String from, String to, double length,
			double freeSpeed, double capacity, double permlanes, int oneway,
			String modes) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.length = length;
		this.freespeed = freeSpeed;
		this.capacity = capacity;
		this.permlanes = permlanes;
		this.oneway = oneway;
		this.modes = modes;
	}
	
	public Link(String id, String from, String to, double length) {
		this(id, from, to, length, Settings.FREE_SPEED, Settings.CAPACITY, Settings.PERMLANES, Settings.ONEWAY, Settings.MODES);
	}

	public String getId() {
		return id;
	}
 
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	@XmlAttribute
	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return this.to;
	}

	@XmlAttribute
	public void setTo(String to) {
		this.to = to;
	}

	public double getLength() {
		return length;
	}

	@XmlAttribute
	public void setLength(double length) {
		this.length = length;
	}

	public double getFreespeed() {
		return freespeed;
	}

	@XmlAttribute
	public void setFreespeed(double freespeed) {
		this.freespeed = freespeed;
	}

	public double getCapacity() {
		return capacity;
	}

	@XmlAttribute
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public double getPermlanes() {
		return permlanes;
	}

	@XmlAttribute
	public void setPermlanes(double permlanes) {
		this.permlanes = permlanes;
	}

	public int getOneway() {
		return oneway;
	}

	@XmlAttribute
	public void setOneway(int oneway) {
		this.oneway = oneway;
	}

	public String getModes() {
		return modes;
	}

	@XmlAttribute
	public void setModes(String modes) {
		this.modes = modes;
	}
	
	public String toString() {
		return id;
	}
}
