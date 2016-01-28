package com.sb;

import java.util.ArrayList;

public class MatchStatus
{
	protected ArrayList<Goal> goals = null;
	public float freePot = 0;
	public MatchTime time;
	public int t1Score = 0;
	public String timestamp = "";
	public int id = 0;
	public float pot = 0;
	public float estimate = 0;
	public String status = null;
	public float freeEstimate = 0;
	public int t2Score = 0;
	public String currency = "FRE";
	
	public MatchStatus()
	{
		time = new MatchTime(0, 0, 0);
		goals = new ArrayList<Goal>();
	}
	
	public ArrayList<Goal> goalList() 
	{
		return goals;
	}
	
	public Goal goal(int param1)
	{
		if (goals == null){
			return null;
		}
		if (param1 < 0 || param1 >= goals.size()){
			return null;
		}
		return goals.get(param1);
	}
	
	public void addGoal(Goal g)
	{
		if (goals == null){
			goals = new ArrayList<Goal>();
		}
		goals.add(g);
		return;
	}
	
	public Goal lastGoal()
	{
		if (goals == null){
			return null;
		}
		Goal lg = null;
		for (Goal g: goals) {
			if (lg == null || g.half > lg.half || g.half == lg.half && g.min > lg.min){
				lg = g;
			}
		}
		return lg;
	}
	
	public void addSecsToT(int s)
	{
		time.incS(s);
		return;
	}
	
	public void setGoals(ArrayList<Goal> ga) 
	{
		for (Goal g: ga) {
			addGoal(g);
		}
		return;
	}
	public void setGoals(Goal[] ga) 
	{
		for (Goal g: ga) {
			addGoal(g);
		}
		return;
	}
}
