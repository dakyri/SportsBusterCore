package com.sb;

public class Goal {
	public int sec;
	public int min;
	public int half;
	public int teamId;
	public Boolean own;
	
	public Goal(
			int _half, int _min, int _sec, int _teamId, Boolean _own)
	{
		half = _half;
		min = _min;
		sec = _sec;
		teamId = _teamId;
		own = _own;
		return;
	}

}
