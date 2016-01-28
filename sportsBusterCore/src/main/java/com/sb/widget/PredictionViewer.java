package com.sb.widget;

import java.util.ArrayList;

import com.sb.DataFeed;
import com.sb.FeedEntry;
import com.sb.Prediction;

public interface PredictionViewer
{
	public void clear();
	public void addItem(Prediction f, int pos);
	public void addPredictions(ArrayList<Prediction> feed);
	public void setPredictions(ArrayList<Prediction> feed);
}
