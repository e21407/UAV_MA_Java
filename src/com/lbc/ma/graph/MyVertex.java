package com.lbc.ma.graph;

import java.util.ArrayList;

import com.lbc.ma.dataStructure.Task;

public class MyVertex {

	private Integer id;

	/** 1:UAV, 2:Edge Server, 3: Cloud Server */
	private Integer type;

	private Integer capacity;

	private ArrayList<Task> assignedTask;

	public MyVertex(Integer id, Integer type, Integer capacity) {
		super();
		this.id = id;
		this.type = type;
		this.capacity = capacity;
		assignedTask = new ArrayList<>();
	}

	public MyVertex(Integer id, Integer type, Integer capacity, ArrayList<Task> assignedTask) {
		super();
		this.id = id;
		this.type = type;
		this.capacity = capacity;
		if (null != assignedTask) {
			this.assignedTask = assignedTask;
		} 
		else {
			this.assignedTask = new ArrayList<>();
		}
	}

	/**
	 * 添加任务成功返回true，否则返回false
	 * 
	 * @param WF_ID
	 * @param task_id
	 * @return
	 */
	public boolean addTask(Task task) {
		if (null == task) {
			return false;
		}
		assignedTask.add(task);
		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public ArrayList<Task> getAssignedTask() {
		return assignedTask;
	}

	public void setAssignedTask(ArrayList<Task> assignedTask) {
		this.assignedTask = assignedTask;
	}

}
