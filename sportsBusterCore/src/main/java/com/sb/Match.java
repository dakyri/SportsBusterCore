package com.sb;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

public class Match
{
	public int matchId = 0;
	public String name = "";
	public ArrayList<Prediction> predictions = null;
	public Team homeTeam = null;
	public DataFeed feed;
	public ArrayList<String> stats;
//	public String start = "";
	public Date startDate = new Date();
	public Team awayTeam = null;
	public MatchStatus status = null;
	public String location = "";
	public String leagueName = "";
	public String leagueId = "";
	public String xId = "";
	
	public Match(
			int _matchId, String _matchName,
			String _startTime, String _location,
			Team _homeTeam, Team _awayTeam)
	{
		this(_matchId, _matchName, _startTime, _location, _homeTeam, _awayTeam,
				null, null, null, null);
	}
	
	public Match(int id, String n, String s, String string, Team _homeTeam,
			Team _awayTeam, MatchStatus matchStatus, Broadcast broadcast) {
		this(id, n, s, string, _homeTeam, _awayTeam,
				matchStatus, broadcast, null, null);
	}

	public Match(
			int _matchId, String _matchName,
			String _startTime, String _location,
			Team _homeTeam, Team _awayTeam,
			MatchStatus _status,
			Broadcast _broadcast,
			ArrayList<String> _stats,
			DataFeed _dataFeed)
	{
		feed = new DataFeed();
		matchId = _matchId;
		startDate = SBTime.timeStr2date(_startTime);
		location = _location;
		homeTeam = _homeTeam;
		awayTeam = _awayTeam;
		name = _matchName;
		status = _status;
		if (_dataFeed != null){
			feed = _dataFeed;
		}
		if (_stats != null){
			stats = _stats;
		} else {
			stats = new ArrayList<String>();
		}
		return;
	}

	public String matchName()
	{
		if (name == null || name.equals("")) {
			if (homeTeam != null && homeTeam.name != null && awayTeam != null && awayTeam.name != null)	{
				return homeTeam.name+" v "+ awayTeam.name;
			}
		} else {
			return name;
		}
		return "Unknown Match";
	}

	public String teamName(int teamId)
	{
		return homeTeam.teamId == teamId ? (homeTeam.name) : (awayTeam.teamId == teamId ? (awayTeam.name) : null);
	}

	public Boolean addPrediction(Prediction pred)
	{
		if (pred == null){
			return false;
		}
		/*
		if (pred.matchId != matchId){
			return false;
		}
		if (homeTeam != null && pred.teamId != homeTeam.teamId && awayTeam != null && pred.teamId != awayTeam.teamId){
			return false;
		}
		*/
		if (predictions == null){
			predictions = new ArrayList<Prediction>();
		}
		predictions.add(pred);
		return true;
	}

	public Boolean addPredictions(ArrayList<Prediction> newPreds)
	{
		for (Prediction pred: newPreds){	
			if (!addPrediction(pred)){
				return false;
			}
		}
		return true;
	}

	public String toString()
	{
		return "Match [" + name.toString() + "]";
	}

	public void setLiveStatus(int param1)
	{
		if (status == null){
			return;
		}
		if (param1 > 0){
			status.status = MatchState.OPEN;
		} else if (param1 == 0) {
			status.status = MatchState.OPEN;
		} else {
			status.status = MatchState.FINISHED;
		}
		return;
	}

	public int liveStatus()
	{
		if (status == null || status.status == MatchState.OPEN){
			return 1;
		}
		if (status.status == MatchState.FINISHED){
			return -1;
		}
		return 0;
	}
	
	public Team team(String xId)
	{
//		Log.d("tracker", xId+" search "+homeTeam.xId+", "+awayTeam.xId);
		if (homeTeam != null && homeTeam.xId != null && homeTeam.xId.equals(xId)) return homeTeam;
		if (awayTeam != null && awayTeam.xId != null && awayTeam.xId.equals(xId)) return awayTeam;
		return null;
	}
	
	public Player player(String xId)
	{
		Player p = null;
		if (homeTeam != null && ((p=homeTeam.getPlayer(xId)) != null)) { return p; }
		if (awayTeam != null && ((p=awayTeam.getPlayer(xId)) != null)) { return p; }
		return null;
	}
}
