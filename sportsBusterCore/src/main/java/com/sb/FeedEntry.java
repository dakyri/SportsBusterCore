/**
 * 
 */
package com.sb;

/**
 * @author dak
 *
 */
public class FeedEntry
{
	public static final int GAME_EVENT = 0;
	public static final int REDCARD_EVENT = 3;
	public static final int GOAL_EVENT = 1;
	public static final int YELLOWCARD_EVENT = 2;
	public static final int FOUL_EVENT = 4;
	public static final int PENALTY_EVENT = 5;
	public static final int CORNER_EVENT = 6;
	public static final int OFFSIDE_EVENT = 7;
	public static final int SHOT_EVENT = 8;
	public static final int SAVE_EVENT = 9;
	public static final int FREEKICK_EVENT = 10;
	public static final int THROWIN_EVENT = 11;
	public static final int SUBSTITUTION_EVENT = 12;
	public static final int SLOT_WIN = 25;
	public static final int SLOT_LOSE = 26;
	public static final int SLOT_PENDING = 27;

	public String txt;
	public MatchTime t;
	public String title;
	public int kind;
	public int team;
	public String xId;
	public String shortTxt;
	
	public FeedEntry(
			int _kind, MatchTime _t, int _teamid,
			String _title,
			String _text)
	{
		kind = _kind;
		if (_t == null){
			_t = new MatchTime(0, 0, 0);
		}
		t = _t;
		team = _teamid;
		title = _title;
		txt = _text;
		xId = null;
		shortTxt = "";
		return;
	}
}
