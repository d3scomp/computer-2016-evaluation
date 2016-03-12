package cz.cuni.mff.d3s.demo.parking.network.generator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "node")
@XmlAccessorType (XmlAccessType.PROPERTY)
public class Node {

	private String id;
	private double x;
	private double y;
	
	public Node() {}
	
	public Node(String id, double x, double y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public String getId() {
		return id;
	}
	
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}
	public double getX() {
		return x;
	}
	
	@XmlAttribute
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	
	@XmlAttribute
	public void setY(double y) {
		this.y = y;
	}
	
	public boolean equals(Object o) {
		return o != null && o instanceof Node && ((Node) o).x == this.x && ((Node) o).y == this.y;
	}
	
	public String toString() {
		return id;
	}
}
