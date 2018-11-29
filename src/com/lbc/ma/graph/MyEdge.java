package com.lbc.ma.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class MyEdge extends DefaultWeightedEdge{

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Double bandwidth;
	
	/**链路负载*/
	private Double load;
	
	/**延迟系数*/
	private Double linkCoefficient;

	public MyEdge(Integer id, Double bandwidth, Double load, Double linkCoefficient) {
		super();
		this.id = id;
		this.bandwidth = bandwidth;
		this.load = load;
		this.linkCoefficient = linkCoefficient;
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MyEdge)) {
			return false;
		}

		MyEdge edge = (MyEdge) obj;
		return id.equals(edge.id);
	}
	
	public String toString() {
		return this.id.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}

	public Double getLoad() {
		return load;
	}

	public void setLoad(Double load) {
		this.load = load;
	}

	public Double getLinkCoefficient() {
		return linkCoefficient;
	}

	public void setLinkCoefficient(Double linkCoefficient) {
		this.linkCoefficient = linkCoefficient;
	}
	
	
	
}
