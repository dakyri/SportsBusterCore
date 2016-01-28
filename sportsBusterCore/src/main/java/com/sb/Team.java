/**
 * 
 */
package com.sb;

import java.util.ArrayList;

import android.util.Log;

/**
 * @author David Karla
 *
 */
public class Team {

	/**
	 * 
	 */
	public String name = "";
	public String fullName = "";
	public String icon = null;
	public int awayColor = 16711680;
	public int homeColor = 255;
	public int teamId;
	public String smallIcon = null;
	public String abbr = "";
	public ArrayList<Player> players = null;
	public Player[] topsLast = null;
	public Player[] tops = null;
	public Player[] topsFirst = null;
	public int poolTteamId = 0;
	public int poolGameColorBase = 0x000000;
	public int poolGameColorText = 0xffffff;
	public static final String HTML_ICON_LEAF_NAME = "/icon.gif";
	public static final String ICON_LEAF_NAME = "/icon.gif";
	public String xId = "";

	public Team(int _teamID)
	{
		this(_teamID, "", "", "", "",  "",  "", "");
	}

	public Team(
			int _teamID, String _fullName, String _name, String _abbrev,
			String _hc, String _ac,
			String _icon, String _sicon)
	 {
		this(_teamID,_fullName, _name, _abbrev, _hc, _ac, _icon, _sicon,
				null, null, null, null);
	 }
	
	public Team(
			int _teamID, String _fullName, String _name, String _abbrev,
			String _hc, String _ac,
			String _icon, String _sicon,
			ArrayList<Player> _players, 
			int [] _tops,
			int [] _topsFirst,
			int [] _topsLast)
	 {
		teamId = _teamID;
//		homeColor = parseColor(_hc);
//		awayColor = parseColor(_ac);
		if (_fullName == null || _fullName.equals("")) {
			if (_name == null || _name.equals("")) {
				if (_abbrev == null || _abbrev.equals("")) {
					_fullName = _name = "team "+Integer.toString(_teamID);
					_abbrev = Integer.toString(_teamID);
				} else {
					_fullName = _name = _abbrev;
				}
			} else {
				_fullName = name;
			}
		} else if (_name == null || _name.equals("")) {
			_name = _fullName;
		}
		if (_abbrev == null || _abbrev.equals("")) {
			if (_fullName.length() <= 3) {
				_abbrev = _fullName;
			} else {
				String[] fns = _fullName.split(" ");
				if (fns.length > 1) {
					String abis="";
					for (int fnsi=0; fnsi<fns.length; fnsi++) {
						abis += fns[fnsi].substring(0,1).toUpperCase();
					}
					_abbrev = abis;
				} else {
					_abbrev = _name.substring(0,3).toUpperCase();
				}
			}
		}
		name = _name;
		fullName = _fullName;
		abbr = _abbrev;
		players = _players;
		tops = setPlayerListArray(_tops);
		topsFirst = setPlayerListArray(_topsFirst);
		topsLast = setPlayerListArray(_topsLast);
		icon = _icon;
		smallIcon = _sicon;
//		trace("hc", homeColor.toString(16));
	}

	public Player getPlayer(String _playerID)
	{
		for (Player player: players){				
//			Log.d("tracker", _playerID+" search "+player.xId);
			if (player.xId.equals(_playerID)){
				return player;
			}
		}
		return null;
	}
	
	public Player getPlayer(int _playerID)
	{
		for (Player player: players){				
			if (player.num == _playerID){
				return player;
			}
		}
		return null;
	}
	
	public String toString()
	{
		return name + " "+(players != null ?
						 (Integer.toString(players.size()) + " players") :
						 ("(no player list)"));
	}
	
	protected Player[] setPlayerListArray(int [] listPlayerIds)
	{
		if (listPlayerIds == null) {
			return null;
		}
		Player player = null;
		Player[] newList = new Player[listPlayerIds.length];
		int i=0;
		for (int playerId: listPlayerIds) {			
			player = getPlayer(playerId);
			newList[i++] = player;
		}
		return newList;
	}

}
