package cz.cuni.mff.d3s.demo.parking.network.generator;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "nodes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Nodes {

	@XmlElement(name="node")
    private List<Node> nodes = null;
	
	public Nodes() {}
	
	public Nodes(List<Node> nodes) {
		super();
		this.nodes = nodes;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
}