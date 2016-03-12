package cz.cuni.mff.d3s.demo.parking.network.generator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "network")
@XmlAccessorType (XmlAccessType.PROPERTY)
@XmlType (propOrder={"nodes","links"})
public class Network {
	
	private String name;
	private Nodes nodes;
	private Links links;
	
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}
	
	public Nodes getNodes() {
		return nodes;
	}
	
	@XmlElement
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}
	
	public Links getLinks() {
		return links;
	}
	
	@XmlElement
	public void setLinks(Links links) {
		this.links = links;
	}
	
}
