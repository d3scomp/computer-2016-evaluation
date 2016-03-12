package cz.cuni.mff.d3s.demo.parking.network.generator;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "links")
@XmlAccessorType(XmlAccessType.FIELD)
public class Links {

	@XmlElement(name="link")
    private List<Link> links = null;
	
	public Links() {}
	
	public Links(List<Link> links) {
		super();
		this.links = links;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
}