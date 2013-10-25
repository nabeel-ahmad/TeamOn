package com.spikenow.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Nabeel
 *
 * @param <T>
 */
@XmlRootElement(name = "dataList")
public class DataList<T> {
	
	private List<T> list;

	public DataList() {
		
	}

	public DataList(List<T> list) {
		this.list = list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public List<T> getList() {
		return list;
	}
	

}
