package com.sb;

public class Player {
	public int num = 0;
	public String fName = "";
	public String lName = "";
	public Boolean isSubstitute = false;
	public int position = Player.WATERBOY;
	public String xId = "";
	
	public static final int WATERBOY = 0;
	public static final int FORWARD = 1;
	public static final int MIDFIELDER = 2;
	public static final int DEFENDER = 3;
	public static final int GOALKEEPER = 4;

	public Player(int _num, String _fnm, String _lnm, int _pos, Boolean _isSub)
	{
		num = _num;
		fName = _fnm;
		lName = _lnm;
		isSubstitute = _isSub;
	}
	
	public String toString() 
	{
		return name() + " (" + Integer.toString(num) + ")";
	}

	public String name()
	{
		return fName + ' ' + lName;
	}

	public String positionStr()
	{
		switch (position) {
			case Player.WATERBOY: return "unknown";
			case Player.FORWARD: return "forward";
			case Player.MIDFIELDER: return "midfield";
			case Player.DEFENDER: return "defender";
			case Player.GOALKEEPER: return "goalkeeper";
		}
		return "unknown";
	}
}
