package com.sb;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Interface wrappers for talking with and parsing responses from the
 * Winstant games server. 
 */
public class GameServerConnection {
	private static final String TAG = "GameServerConnection";

	/**
	 * {@link StatusLine} HTTP status code when no server error has occurred.
	 */
	private static final int HTTP_STATUS_OK = 200;

	/**
	 * ERROR CODES
	 */
	/*
	* MAJOR ERRORS
	*/
	public static final int GTP_FATAL = 400;
	public static final int GTP_NO_ATTRIBUTES = 401;
	public static final int GTP_NO_KEY = 402;
	public static final int GTP_NO_GID = 403;
	public static final int GTP_BAD_GID = 404;
	public static final int GTP_NO_TS = 405;
	public static final int GTP_BAD_TS = 406;
	public static final int GTP_BAD_RISK = 407;
	public static final int GTP_NO_USER_ID = 408;
	public static final int GTP_NO_BET = 409;
	public static final int GTP_BAD_TID = 410;
	public static final int GTP_TKT_NOT_PURCHASED = 411;
	public static final int GTP_TKT_ALREADY_PLAYED = 412;
	public static final int GTP_GAME_NO_REPLAY = 420;
	public static final int GTP_GAME_NOT_UNIQUE = 425;	
	public static final int XMLP_PARSE_FAIL = 200;
	public static final int PT_INVALID_TICKET = 602;
	public static final int TS_INSUFFICIENT_FUNDS = 600;
	public static final int TS_NO_PLAYER = 601;
	public static final int TS_NO_SESSION = 604;
	public static final int ME_GAME_NOT_FOUND = 603;
	public static final int ME_GAME_NOT_ACTIVE = 605;
	/**
	 * game logic error
	 */
	public static final int GAME_IS_PAUSED = 1002;
	public static final int BET_IN_THE_PAST = 1002;
	public static final int NO_PROCESS_OUTCOMES = 1010;
	public static final int TOTAL_PROCESS_OUTCOMES_ERROR = 1011;
	public static final int BAD_RANGE_SET = 1012;
	public static final int INVALID_CHANCES_COUNT = 1013;
	public static final int CHANCES_COUNT_PARSE = 1015;
	public static final int CHANCES_COUNT_NEGATIVE = 1016;
	public static final int PROCESS_OUTCOMES_PARSE = 1016;
	public static final int MIN_BET_PARSE = 1017;
	public static final int MAX_BET_PARSE = 1018;
	public static final int SET_CHANCES_ERROR = 1019;
	public static final int TOTAL_PCT_PARSE = 1020;

	public static final int LOGIN_ERROR = 9;
	public static final int NETWORK_TIMEOUT_ERROR = 8;
	public static final int INTERNAL_ERROR = 7;
	public static final int FLASH_SECURITY_ERROR = 6;
	public static final int NETWORK_IO_ERROR = 5;
	public static final int INCORRECT_ELEMENT = 4;
	public static final int INCORRECT_ATTRIBUTE = 3;
	public static final int UNEXPECTED_RESPONSE = 2;
	public static final int GENERIC_ERROR = 1;
	public static final int NO_ERROR = 0;
	
	public static final int SERVER_ERROR_EVENT = 2000;
	public static final int SERVER_ACCOUNT_EVENT = SERVER_ERROR_EVENT+1;
	public static final int SERVER_CONFIG_EVENT = SERVER_ERROR_EVENT+2;
	public static final int SERVER_UPDATE_JACKPOT_EVENT = SERVER_ERROR_EVENT+3;
	public static final int SERVER_5R_JACKPOT_EVENT = SERVER_ERROR_EVENT+4;
	public static final int SERVER_PLAY_TICKET_EVENT = SERVER_ERROR_EVENT+5;
	public static final int SERVER_LIST_PREDICTIONS_EVENT = SERVER_ERROR_EVENT+6;
	public static final int SERVER_LIST_ALL_RESULTS_EVENT = SERVER_ERROR_EVENT+7;
	public static final int SERVER_LIST_ALL_TEAMS_EVENT = SERVER_ERROR_EVENT+8;
	public static final int SERVER_LAST_PAYOUT_EVENT = SERVER_ERROR_EVENT+9;
	public static final int SERVER_LIST_MATCHES_EVENT = SERVER_ERROR_EVENT+10;
	public static final int SERVER_MATCH_RESULT_EVENT = SERVER_ERROR_EVENT+11;
	public static final int SERVER_MATCH_STATUS_EVENT = SERVER_ERROR_EVENT+12;
	public static final int SERVER_MATCH_POT_EVENT = SERVER_ERROR_EVENT+13;
	public static final int SERVER_GAME_FEED_EVENT = SERVER_ERROR_EVENT+14;
	public static final int SERVER_SLOT_ODDS_EVENT = SERVER_ERROR_EVENT+15;
	public static final int SERVER_TEAM_DETAILS_EVENT = SERVER_ERROR_EVENT+16;
	public static final int SERVER_TICKET_DETAILS_EVENT = SERVER_ERROR_EVENT+17;
	public static final int SERVER_LOGIN_EVENT = SERVER_ERROR_EVENT+20;
	public static final int SERVER_LOGOUT_EVENT = SERVER_ERROR_EVENT+21;

	/**
	 * User-agent string to use when making requests. Should be filled using
	 * {@link #prepareUserAgent(Context)} before making any other calls.
	 */
	private String sUserAgent = null;
	private String serverURL = null;
	private String wnsServerURL = null;
 
	protected XPath xp = null;
	protected DocumentBuilder db = null;

	public int matchPotId=-1;
	public String matchPotCurrency="FRE";
	public float matchPot=0;

	public Jackpot lastPayoutJackpot=null;
	protected Boolean freePlayMode=false;
//	public var freebies:Object=null;
	public String gameInstance=null;
	public String playerID=null;
	public String gameConfig=null;
	public Match listPredictionResultsMatch=null;
	public String sessionKey=null;
	public String ticketResponseResult=null;
	public float ticketResponseWinnings=0;
	public String ticketResponseTimestamp=null;
	public MatchStatus matchStatus=null;

	public float maximumBet=0;
	public float minimumBet=0;
	public String accountTimestamp=null;
	public float accountBalance=0;
	public String accountCurrency="FRE";
	public String accountKey=null;
	public String playerCurrency=null;
	protected ArrayList<Jackpot> jackpots=null;
	public Prediction currentPrediction=null;

	public int latencyCorrection = 0;
	public Match lastPayoutMatch=null;
	
	protected Context context = null;
	protected Handler handler = null;
	public int keystoreId=0;
	public String keystorePass=null;
		
	public int lastErrorCode=0;
	public String lastErrorMessage="unknown error";
	
	/*
	public Document lpXML=null;
	public Document matchesXML=null;
	public Document propertiesXML=null;
	public Document jrXML=null;
	public Document matchPotXML=null;
	public Document accountXML=null;
	public Document matchResultXML=null;
	public Document matchStatusXML=null;
	*/
	public String jackpotCurrency="FRE";
	public ArrayList<Prediction> lastPredictionList;

	private ArrayList<Match> liveMatches;
	private ArrayList<Match> nextMatches;
	private ArrayList<Match> reqMatches;

	public float matchResultWinnings;
	public float matchResultEstimate;
	public Date matchResultStartTime;
	public ArrayList<Jackpot> matchResultJackpots;
	public ArrayList<Prediction> matchResultResults;

	public Match teamDetailsMatch = null;
	public Match feedDetailsMatch = null;
	public ArrayList<Team> allTeams = null;

	public Prediction ticketDetailsPrediction;

	public ArrayList<Prediction> currentPredictionList;
	/*
	private TrustManager[] trustAllCerts = new TrustManager[] {  		
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}
		
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
		
				}
		
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				{
		
				}
			}
		};
	*/
	/**
	 * Thrown when there were problems contacting the remote API server, either
	 * because of a network error, or the server returned a bad status code.
	 */
	public static class ApiException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public ApiException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public ApiException(String detailMessage) {
			super(detailMessage);
		}
	}

	/**
	 * Thrown when there were problems parsing the response to an API call,
	 * either because the response was empty, or it was malformed.
	 */
	public static class ParseException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public ParseException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
	}

	private HttpClient httpClient = null;
	/**
	 * Base class to do asynchronous access to the game server with the
	 * specified {@link HttpUriRequest}, and convert the response into a {@link Document}.
	 * Specific instances created as anonymous extensions overriding {@link #onPostExecute}. 
	 */
	private class XMLSlurper extends AsyncTask<HttpUriRequest, Integer, Document>
	{
		 /** keep the error exception state or null if all is ok */
		protected Exception shitHappened = null;
		
		/**
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object)
		 */
		@Override
		protected Document doInBackground(HttpUriRequest... request)
		{
			Log.d("GB3P", "request "+request[0].getURI().toString());
			shitHappened = null;
			if (sUserAgent == null) {
				shitHappened = new ApiException("User-Agent string must be prepared");
				return null;
			}
			/*
			HttpClient httpClient = null;
			if (context != null && keystoreId > 0 && keystorePass != null) {
				Log.d("keystore", keystorePass);
				httpClient = new AlCoHttpClient(context, keystoreId, keystorePass);
			} else {
				httpClient = new DefaultHttpClient();
			}
			*/
			try {
				HttpResponse response = httpClient.execute(request[0]);

				// Check if server response is valid
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() != HTTP_STATUS_OK) {
					Log.d("GB3P", "http status exception");
					shitHappened = new ApiException("Invalid response from server: " + status.toString()+" , url "+request[0].getURI().toString());
					return null;
				}

				// Pull content stream from response
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				Document d = null;
				try {
					d = dbParse(inputStream);
				} catch (SAXException e) {
					Log.d("GB3P", "Sax exception");
					shitHappened = new ApiException("Invalid xml from server: "+e.getMessage()+" , url "+request[0].getURI().toString());
					return null;
				}
				return d;				 
			} catch (ClientProtocolException e) {
				Log.d("GB3P", "ClientProtocolException");
				e.printStackTrace();
				String msg = e.getMessage();
				if (msg == null) {
					msg = "Client protocol exception for request '"+
							request[0].getURI().toString()+"'";
				}
				shitHappened = new ApiException(msg);
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				String msg = e.getMessage();
				if (msg == null) {
					msg = "IO exception for request '"+
							request[0].getURI().toString()+"'";
				}
				Log.d("GB3P", "IOException "+msg);
				shitHappened = new ApiException(msg);
				return null;
			}
		}
 
		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Document result)
		{
			super.onPostExecute(result);
		}
 
		/**
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}
 
		/**
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer ... progressValues)
		{
			super.onProgressUpdate(progressValues);
		}
	}
	
	/**
	 * Initialise various bits and pieces for connecting to the game server.
	 * It uses {@link Context} to pull the package name and version number for this
	 * application, {@link #userAgentResId} and {@link #apiUrlResId} locate the user
	 * agent string and url base in the resources.
	 * 
	 * @param _context the context in which to locate resources
	 * @param _handler message handler to recieve notifications
	 * @param userAgentResId resource id of user agent string
	 * @param apiUrlResId resource id of api url
	 * @param _freePlayMode indicates game is played in free play mode
	 * @param _latencyCorrection correction factor for game times for network latency
	 */
	public GameServerConnection(
			Context _context, Handler _handler, int userAgentResId, int apiUrlResId, int apiWnsUrlResId,
			Boolean _freePlayMode, int _latencyCorrection)
	{
		this(_context, _handler, userAgentResId, apiUrlResId, apiWnsUrlResId, _freePlayMode, _latencyCorrection,-1, null);
	}
	public GameServerConnection(
			Context _context, Handler _handler, int userAgentResId, int apiUrlResId, int apiWnsUrlResId,
			Boolean _freePlayMode, int _latencyCorrection, int keystoreId, String keystorePass)
	{
		context = _context;
		handler = _handler;
		latencyCorrection = _latencyCorrection;
		freePlayMode = _freePlayMode;
		this.keystoreId = keystoreId;
		this.keystorePass = keystorePass;
		try {
			// Read package name and version number from manifest
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			sUserAgent = String.format(context.getString(userAgentResId),
					info.packageName, info.versionName);
			serverURL = context.getString(apiUrlResId);
			if (apiWnsUrlResId > 0) {
				wnsServerURL = context.getString(apiWnsUrlResId);
			}
		} catch(NameNotFoundException e) {
			Log.e(TAG, "Couldn't find package information in PackageManager", e);
		}
		xp = XPathFactory.newInstance().newXPath();
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		if (context != null && keystoreId > 0 && keystorePass != null) {
			Log.d("keystore", keystorePass);
			httpClient = new AlCoHttpClient(context, keystoreId, keystorePass);
		} else {
			httpClient = new DefaultHttpClient();
		}
		/*
		SSLContext sc=null;
		try {
			sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}*/
	}
	
	/**
	 * simple wrapper to set the error state variables 
	 * @param en
	 * @param em
	 */
	protected void setError(int en, String em)
	{
		lastErrorCode = en;
		lastErrorMessage = em;
	}
	
	/**
	 *  simple wrapper to allocate a message of the given message id and send it to the global
	 *  handler
	 * @param mid message id
	 */
	protected void dispatchEvent(int mid)
	{
		Message msg = Message.obtain(handler, mid);
		msg.sendToTarget();
	}
	
	/**
	 * thread safe wrapper for xpath calls 
	 * @see XPath#evaluate(String, org.xml.sax.InputSource, QName)
	 * @param expression
	 * @param object
	 * @param returnType
	 * @return a string for the given expression
	 * @throws XPathExpressionException
	 */
	protected synchronized String xpe(String expression, Object object)
		throws XPathExpressionException
	{
		if (xp == null) return null;
		return xp.evaluate(expression, object);
	}

	/**
	 * thread safe wrapper for xpath calls 
	 * @see XPath#evaluate(String, org.xml.sax.InputSource, QName)
	 * @param expression
	 * @param object
	 * @param returnType
	 * @return an object of the given type
	 * @throws XPathExpressionException
	 */
	protected synchronized Object xpe(String expression, Object object, QName returnType)
		throws XPathExpressionException
	{
		if (xp == null) return null;
		return xp.evaluate(expression, object, returnType);
	}
	
	/**
	 * (hopefully) make the {@link DocumentBuilder} calls thread safe
	 * @param inputStream
	 * @return null or a constructed {@link Document}
	 * @throws SAXException
	 * @throws IOException
	 */
	protected synchronized Document dbParse(InputStream inputStream)
			throws SAXException, IOException
	{
		if (db == null) return null;
		return db.parse(inputStream);
		
	}
	
	/**
	 * data wrappers and access
	 */
	public ArrayList<Prediction> finalResults()
	{
		return matchResultResults;
	}

	public ArrayList<Match> nextMatchList()
	{
		return nextMatches;
	}

	public Match nextMatch(int i)
	{
		if ((nextMatches != null) && i >= 0 && i < nextMatches.size()) {
			return nextMatches.get(i);
		}
		return null;
	}

	public ArrayList<Match> reqMatchList()
	{
		return reqMatches;
	}

	public Match reqMatch(int i)
	{
		if ((reqMatches != null) && i >= 0 && i < reqMatches.size()) {
			return reqMatches.get(i);
		}
		return null;
	}
	
	public ArrayList<Match> liveMatchList()
	{
		return liveMatches;
	}

	public Match liveMatch(int i)
	{
		if ((liveMatches != null) && i >= 0 && i < liveMatches.size()) {
			return liveMatches.get(i);
		}
		return null;
	}
	
	public Jackpot jackpot4Id(int jid)
	{
		if (jackpots == null) {
			return null;
		}
		for (Jackpot jkpot : jackpots) {
			if (jkpot.id == jid) {
				return jkpot;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param matchId
	 * @return
	 */
	public Match findResultsMatch(int matchId)
	{
		if (matchId < 0) {
			return null;
		}
		
		if (allResultsList != null) {
			for (Match m: allResultsList) {
				if (m.matchId == matchId) {
					return m;
				}
			}
		}
		return null;
	}

	public Match findActiveMatch(int matchId)
	{
		if (matchId < 0) {
			return null;
		}
		if (liveMatches != null) {
			for (Match m: liveMatches) {
				if (m.matchId == matchId) {
					return m;
				}
			}
		}
		if (nextMatches != null) {
			for (Match m:  nextMatches) {
				if (m.matchId == matchId) {
					return m;
				}
			}
		}
		if (reqMatches != null) {
			for (Match m:  reqMatches) {
				if (m.matchId == matchId) {
					return m;
				}
			}
		}
		return null;
	}
	
	/**
	 * finds the ith team reference to given teamId
	 */

	public Team findTeam(int teamId)
	{
		return findTeam(teamId, 0);
	}
	
	public Team findTeam(int teamId, int i)
	{
		if (teamId < 0) {
			return null;
		}
		Match m = null;
		Team t = null;
		int j = 0;
		if (liveMatches != null) {
			for (j=0; j<liveMatches.size(); j++) {
				m = liveMatches.get(j);
				if (m.awayTeam.teamId == teamId) {
					if (i-- <= 0) return m.awayTeam;
				}
				if (m.homeTeam.teamId == teamId) {
					if (i-- <= 0) return m.homeTeam;
				}
			}
		}
		if (nextMatches != null) {
			for (j=0; j<nextMatches.size(); j++) {
				m = nextMatches.get(j);
				if (m.awayTeam.teamId == teamId) {
					if (i-- <= 0) return m.awayTeam;
				}
				if (m.homeTeam.teamId == teamId) {
					if (i-- <= 0) return m.homeTeam;
				}
			}
		}
		if (reqMatches != null) {
			for (j=0; j<reqMatches.size(); j++) {
				m = reqMatches.get(j);
				if (m.awayTeam.teamId == teamId) {
					if (i-- <= 0) return m.awayTeam;
				}
				if (m.homeTeam.teamId == teamId) {
					if (i-- <= 0) return m.homeTeam;
				}
			}
		}
		if (allTeams != null && allTeams.size() > 0) {
			for (j=0; j<allTeams.size(); j++) {
				t=allTeams.get(j);
				if (t != null && t.teamId == teamId) {
					return t;
				}
			}
			return null;
		}
		return null;
	};

	/**
	 *  create a HttpPost request structure from the parameters
	 * @param reqURL
	 * @param inputStr
	 * @param isFreePlay
	 * @param betAmoun
	 * @param rkLevel
	 * @return
	 */
	public HttpPost createTicketRequest(
			String requestURL,
			Prediction p,
			Boolean isFreePlay,
			int rkLevel)
	{
		String inputStr= p.ticketInputString();
		HttpPost req = new HttpPost(requestURL);
		if (inputStr == null) inputStr = "";
		Log.d("connection", "bet input "+inputStr);
		Element tktXML;
		try {
			tktXML = dbParse(
					new ByteArrayInputStream(
						("<gt><i><s>"+inputStr+"</s></i></gt>").getBytes())).getDocumentElement();
			tktXML.setAttribute("k", sessionKey);
			tktXML.setAttribute("g", gameInstance);
			tktXML.setAttribute("gc", gameConfig);
			tktXML.setAttribute("ts", getTimeStamp());
			tktXML.setAttribute("u", playerID);
			tktXML.setAttribute("b", Float.toString(p.bet >= minimumBet ? p.bet : minimumBet));
			tktXML.setAttribute("rk", Integer.toString(rkLevel >= 1 ? rkLevel : 1));
			tktXML.setAttribute("fp", isFreePlay ? "1" : "0");
			/*
			if (inputStr != null && !inputStr.equals("")) {
				Element inputElement=dbParse(
						new ByteArrayInputStream(inputStr.getBytes())).getDocumentElement();
				((Node)xpe("//i/s/", tktXML, XPathConstants.NODE)).appendChild(inputElement);
			}*/
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(tktXML);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("_XML", xmlString));
			req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (TransformerException e) {
			setError(INTERNAL_ERROR, "createTicketRequest, TransformerException");
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			setError(INTERNAL_ERROR, "createTicketRequest, SAXException");
			e.printStackTrace();
			return null;
		/*
		} catch (XPathExpressionException e) {
			setError(INTERNAL_ERROR, "createTicketRequest, XPathExpressionException");
			e.printStackTrace();
			return null;*/
		} catch (IOException e) {
			setError(INTERNAL_ERROR, "createTicketRequest, IOException");
			e.printStackTrace();
			return null;
		}
		
		return req;
	}

	public static String getTimeStamp()
	{
		return date2timeStampStr(new Date());
	}
	
	public static String date2timeStampStr(Date theTime)
	{
		
		int month = theTime.getMonth();
		month++;
		int date = theTime.getDate();
		String dateStr = Integer.toString(date);
		String monthStr = Integer.toString(month);
		if (month<10) monthStr = '0'+monthStr;
		if (date<10) dateStr = '0'+dateStr;
		int mins = theTime.getMinutes();
		String minstr = Integer.toString(mins);
		if (mins<10) minstr = '0'+minstr;
		return	Integer.toString(theTime.getYear()+1900) +
				monthStr +
				dateStr + '.' +
				Integer.toString(theTime.getHours())+ ':' +
				minstr+':' + 
				Integer.toString(theTime.getSeconds());
	}

	public static Date timeStampStr2Date(String ts)
	{
		Log.d("GB3P", ts + " is ts");
		String [] tsa = ts.split(".");
		if (tsa.length < 2) {
			return new Date();
		}
		String [] tsta= tsa[1].split(":");
		if (tsta.length < 3) {
			return new Date();
		}
		String ys = tsa[0].substring(0, 3);
		String ds = tsa[0].substring(4, 5);
		String das = tsa[0].substring(6, 7);
		Date d = new Date(Integer.parseInt(ys)-1900, Integer.parseInt(ds)-1, Integer.parseInt(das),
					Integer.parseInt(tsta[0]), Integer.parseInt(tsta[1]), Integer.parseInt(tsta[2]));
		return d;
	}

//////////////////////////////////////////////////////////////////////////////////////
// XML PROCESSING ROUTINES
//////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  check that this is a simple acknowledge response from server
	 * @param rxXML
	 * @return true if it's an <ok> packet
	 */
	protected Boolean okXML(Document rxXML)
	{
		if (rxXML == null) return false;
		if (rxXML.getDocumentElement().getTagName().equals("ok")) {
			return true;
		}
		return false;
	}

	/**
	 *  check whether this is an error packet, and set the current error state if so
	 * @param rxXML
	 * @return true if it is an error packet
	 */
	protected Boolean errorXML(Document rxXML)
	{
		if (rxXML == null) {
			return false;
		}
		if (rxXML.getDocumentElement().getTagName().equals("e")) {
			Log.d("GB3P", "got an error packet");
			String ec = null;
			String em = null;
			try {
				ec = xpe("/e/o/ec", rxXML);
				em = xpe("/e/o/em", rxXML);
			} catch (XPathExpressionException e) {
			}
			
			if (ec == null || em == null) {
				setError(GENERIC_ERROR, "Malformed error packet");
			} else {
				setError(Integer.parseInt(ec), em);
			}
			return true;
		} else if (rxXML.getDocumentElement().getTagName().equals("auth_error")) {
			setError(LOGIN_ERROR, rxXML.getDocumentElement().getTextContent());
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param rxXML
	 * @return
	 */
	protected ArrayList<Goal> processGoalListXML(Element rxXML)
	{
		Element newGoalXML=null;
		int goalHalf=0;
		int mins=0;
		int secs=0;
		int teamId=0;
		Boolean isOwn=false;
		String timeStr=null;
		String []timeStrArr=null;
		if (!rxXML.getTagName().equals("gls")) {
			return null;
		}
		ArrayList<Goal> newGoalList=new ArrayList<Goal>();
		NodeList newGoalListXML;
		try {
			newGoalListXML = (NodeList) xpe("//gl", rxXML, XPathConstants.NODESET);
		} catch (XPathExpressionException e1) {
			return newGoalList;
		}
		for (int i=0; i<newGoalListXML.getLength(); i++) {
			newGoalXML = (Element) newGoalListXML.item(i);
			goalHalf = 1;
			mins = 0;
			secs = 0;
			teamId = -1;
			isOwn = false;
			String ghs = newGoalXML.getAttribute("hf");
			if (ghs != null && !ghs.equals("")) {
				goalHalf = Integer.parseInt(ghs);
			}
			
			timeStr = newGoalXML.getAttribute("tm");
			if (timeStr != null && !timeStr.equals("")) {
				timeStrArr = timeStr.split(":");
				mins = Integer.parseInt(timeStrArr[0]);
				secs = Integer.parseInt(timeStrArr[1]);
			}
			String tis = newGoalXML.getAttribute("t");
			if (tis != null && !tis.equals("")) {
				teamId = Integer.parseInt(tis);
			}
			isOwn = newGoalXML.getAttribute("o").equals("true");
			if (goalHalf > 1 && mins >= 45) {
				mins = mins - 45;
				if (goalHalf > 2 && mins >= 45) {
					mins = mins - 45;
					if (goalHalf > 3 && mins >= 15) {
						mins = mins - 15;
					}
				}
			}
			newGoalList.add(new Goal(goalHalf, mins, secs, teamId, isOwn));
		}
		return newGoalList;
	}

	/**
	 * 
	 * @param rxXML
	 * @param mtp
	 * @return
	 */
	protected Match processMatchXML(Element rxXML, int mtp)
	{
		Team _team2=null;
		Team _team1=null;
		MatchStatus _matchStatus=null;
		Broadcast _broadcast=null;
		int id=0;
		String n="";
		String s="";
		String ids = rxXML.getAttribute("id");
		if (ids == null || ids.equals("")) {
			setError(UNEXPECTED_RESPONSE, "list matches, expected \'id\' attribute");
			return null;
		}
		id = Integer.parseInt(ids);
		
		n = rxXML.getAttribute("n");
		if (n == null || n.equals("")) {
			setError(UNEXPECTED_RESPONSE, "list matches, expected \'n\' attribute");
			return null;
		}
		
		s = rxXML.getAttribute("s");
		if (s == null || s.equals("")) {
			setError(UNEXPECTED_RESPONSE, "list matches, expected \'s\' attribute");
			return null;
		}
		try {
			_broadcast = processBroadcastXML(
					(Element)xpe("tv[1]", rxXML, XPathConstants.NODE));
			if (_broadcast == null) {
				return null;
			}
		} catch (XPathExpressionException e) {
		}
		try {
			_matchStatus = processMatchStatusXML(
					(Element)xpe("mst[1]", rxXML, XPathConstants.NODE));
			if (_matchStatus == null) {
				return null;
			}
		} catch (XPathExpressionException e) {
		}
		try {
			_team1 = processTeamXML(
					(Element)xpe("te[1]", rxXML, XPathConstants.NODE));
			if (_team1 == null) {
				return null;
			}
		} catch (XPathExpressionException e) {
		}
		try {
			_team2 = processTeamXML(
					(Element)xpe("te[2]", rxXML, XPathConstants.NODE));
			if (_team2 == null) {
				return null;
			}
		} catch (XPathExpressionException e) {
		}
		return new Match(
				id, n , s, "somewhere",
				_team1, _team2, _matchStatus, _broadcast);
	}

	protected Broadcast processBroadcastXML(Element rxXML)
	{
		String name="";
		String id="";
		String icon="";
		name = rxXML.getAttribute("n");
		id = rxXML.getAttribute("id");
		icon = rxXML.getAttribute("ic");
		return new Broadcast(name, id, icon);
	}
	
	protected Team processTeamXML(Element rxXML)
	{
		if (rxXML == null) {
			return null;
		}
		int id=0;
		String ids = rxXML.getAttribute("id");
		if (ids == null || ids.equals("")) {
			setError(INCORRECT_ELEMENT, "processTeamXML(), expected \'id\' attribute");
			return null;
		}
		id = Integer.parseInt(ids);
		
		String n="";
		n = rxXML.getAttribute("n");
//		if (n == null || n.equals("")) {
//			setError(UNEXPECTED_RESPONSE, "processTeamXML(), expected \'n\' attribute");
//			return null;
//		}
		String an="";
		an = rxXML.getAttribute("an");
//		if (an == null || an.equals("")) {
//			setError(UNEXPECTED_RESPONSE, "processTeamXML(), expected \'an\' attribute");
//			return null;
//		}
		String fn="";
		fn = rxXML.getAttribute("fn");
//		if (fn == null || fn.equals("")) {
//			setError(UNEXPECTED_RESPONSE, "processTeamXML(), expected \'fn\' attribute");
//			return null;
//		}
		String awayCol="#29ABE2";
		String homeCol="red";
		homeCol = rxXML.getAttribute("h");
		awayCol = rxXML.getAttribute("a");
		if (homeCol == null || homeCol.equals("")) {
			homeCol="red";
		}
		if (awayCol == null || awayCol.equals("")) {
			awayCol="#29ABE2";
		}
		String iu = rxXML.getAttribute("iu");
		if (iu == null || iu.equals("")) {
			setError(UNEXPECTED_RESPONSE, "processTeamXML(), expected \'iu\' attribute");
			return null;
		}
		String ic=rxXML.getAttribute("ic");
		if (ic == null || ic.equals("")) {
			setError(UNEXPECTED_RESPONSE, "processTeamXML(), expected \'ic\' attribute");
			return null;
		}
		String flashIconPath=iu + ic + Team.ICON_LEAF_NAME;
		String htmlIconPath=iu + ic + Team.HTML_ICON_LEAF_NAME;
		String teamName= (n.equals(""))? an : n;
		if (teamName.equals("")) {
			teamName = fn;
		}
		Team t = new Team(
				id,
				fn, teamName, an.equals("") ? teamName : an,
				homeCol, awayCol, flashIconPath, htmlIconPath);
		Team oldTeam = findTeam(t.teamId);
		if (oldTeam != null) {
			t.players = oldTeam.players;
			t.tops = oldTeam.tops;
			t.topsFirst = oldTeam.topsFirst;
			t.topsLast = oldTeam.topsLast;
		}
		return t;
	}

	protected PredictionLine processLineXML(Element rxXML)
	{
		if (rxXML == null) {
			return null;
		}
		String ids = rxXML.getAttribute("id");
		if (ids == null || ids.equals("")) {
			setError(INCORRECT_ELEMENT, "processLineXML(), expected \'id\' attribute");
			return null;
		}
		String pdts = rxXML.getAttribute("pdt");
		if (pdts == null || pdts.equals("")) {
			setError(INCORRECT_ELEMENT, "processLineXML(), expected \'pdt\' attribute");
			return null;
		}
		
		String state = rxXML.getAttribute("st");
		if (state == null || state.equals("")) {
			setError(INCORRECT_ELEMENT, "processLineXML(), expected \'st\' attribute");
			return null;
		}
		
		PredictionLine pl = new PredictionLine(ids, timeStampStr2Date(pdts), state);
		return pl;
	}

	protected MatchStatus processMatchStatusXML(Element rxXML)
	{
		if (rxXML == null) return null;
		String[] tstr2=null;
		MatchStatus ms = new MatchStatus();
		if (!rxXML.getTagName().equals("mst")) {
			setError(UNEXPECTED_RESPONSE, "match status, expected \'mst\' packet");
			return null;
		}
		String hf=rxXML.getAttribute("hf");
		if (hf == null || hf.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected hf attribute");
			return null;
		}
		ms.time.half = Integer.parseInt(hf);
		String tm=rxXML.getAttribute("tm");
		if (tm == null || tm.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected tm attribute");
			return null;
		}
		tstr2 = tm.split(":");
		ms.time.min = Integer.parseInt(tstr2[0]);
		ms.time.sec = Integer.parseInt(tstr2[1]);

		ms.status = rxXML.getAttribute("s");
		if (ms.status == null || ms.status.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected s attribute");
			return null;
		}
		String ss = rxXML.getAttribute("t1");
		if (ss == null || ss.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected t1 attribute");
			return null;
		}
		ms.t1Score = Integer.parseInt(ss);
		ss = rxXML.getAttribute("t2");
		if (ss == null || ss.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected t2 attribute");
			return null;
		}
		ms.t2Score = Integer.parseInt(ss);
		
		String pots = rxXML.getAttribute("p");
		if (pots == null || pots.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected pot attribute");
			return null;
		}
		ms.pot = Float.parseFloat(pots);
		
		String c = rxXML.getAttribute("c");
		if (c == null || c.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected currency attribute");
			return null;
		}
		ms.currency = c;
		
		String estimates = rxXML.getAttribute("ew");
		if (estimates == null || estimates.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected ew attribute");
			return null;
		}
		ms.estimate = Float.parseFloat(estimates);
		
		String freePots = rxXML.getAttribute("fp");
		if (freePots == null || freePots.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected fp attribute");
			return null;
		}
		ms.freePot = Float.parseFloat(freePots);
		
		String efws = rxXML.getAttribute("efw");
		if (efws == null || efws.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected efw attribute");
			return null;
		}
		ms.freeEstimate = Float.parseFloat(efws);
		
		String tss = rxXML.getAttribute("ts");
		if (tss == null || tss.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected tss attribute");
			return null;
		}
		ms.timestamp = tss;
		
		try {
			ms.setGoals(processGoalListXML(
					(Element)(xpe("//gls[1]", rxXML, XPathConstants.NODE))));
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ATTRIBUTE, "match status, expected ts attribute");
			return null;
		}
		if (ms.status == MatchState.PLAYING) {
			if (latencyCorrection != 0) {
				ms.addSecsToT(latencyCorrection);
			}
		}
		return ms;
	}

	/**
	 *  process the xml fragment as a jackpot list
	 * @param node xml fragment
	 * @return the jackpot list passed if it is, or null if it is not a jackpot packet
	 */
	private ArrayList<Jackpot> processJackpotXML(Element node)
	{
		ArrayList<Jackpot> jackpotArray = new ArrayList<Jackpot>();
		String jcss = node.getAttribute("c");
		if (jcss == null || jcss.equals("")) {
			setError(INCORRECT_ELEMENT, "list jackpot, expected currency attribute");
			return null;
		}
		jackpotCurrency = jcss;
		NodeList jackpotListXML=null;
		try {
			jackpotListXML = (NodeList) xpe("jb", node, XPathConstants.NODESET);
		} catch (XPathExpressionException e1) {
			return jackpotArray;
		}
		for (int i=0; i<jackpotListXML.getLength(); i++) {
			Element jbNode = (Element) jackpotListXML.item(i);
			String n, lw;
			int id;
			float b;
			String idss = jbNode.getAttribute("id");
			if (idss == null || idss.equals("")) {
				setError(INCORRECT_ELEMENT, "list jackpot, expected jackpot id attribute");
				return null;
			}
			id = Integer.parseInt(idss);
			
			String bss = jbNode.getAttribute("b");
			if (bss == null || bss.equals("")) {
				setError(INCORRECT_ELEMENT, "list jackpot, expected jackpot balance attribute");
				return null;
			}
			b = Float.parseFloat(bss);
			
			n = jbNode.getAttribute("n");
			if (n == null || n.equals("")) {
				setError(INCORRECT_ELEMENT, "list jackpot, expected jackpot name attribute");
				return null;
			}
			lw = jbNode.getAttribute("lw");
			if (lw == null || lw.equals("")) {
				setError(INCORRECT_ELEMENT, "list jackpot, expected jackpot last won attribute");
				return null;
			}
			jackpotArray.add(
					new Jackpot(n, id, b, jackpotCurrency, false, lw));
		}

		return jackpotArray;
	}
	
	private Boolean processGoalTicketXML(Prediction p, ArrayList<Prediction> pl, Document rxXML)
	{
		if (!rxXML.getDocumentElement().getTagName().equals("gt")) {
			setError(UNEXPECTED_RESPONSE, "expected \'gt\' packet, got a \'"+
					rxXML.getDocumentElement().getTagName()+"\'");
			return false;
		}
		/*
		if (sessionKey != rxXML.getDocumentElement().getAttribute("k")) {
			setError(INCORRECT_ATTRIBUTE, "mis-matched key attribute");
			return false;
		}*/
		ticketResponseTimestamp = rxXML.getDocumentElement().getAttribute("ts");
		/*
		if (ticketResponseTimestamp == null || ticketResponseTimestamp.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected timestamp attribute");
			return false;
		}*/
		/*
		if ((playerID != null) && !(playerID.equals(rxXML.getDocumentElement().getAttribute("u")))) {
			setError(INCORRECT_ATTRIBUTE, "player ID attribute doesn\'t match config");
			return false;
		}*/
		/*
		if ((gameInstance != null) && !(gameInstance.equals(rxXML.getDocumentElement().getAttribute("g")))) {
			setError(INCORRECT_ATTRIBUTE, "game instance attribute doesn\'t match config");
			return false;
		}*/
		/*
		ticketResponseResult = rxXML.getDocumentElement().getAttribute("r");
		if (ticketResponseResult == null || ticketResponseResult.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected result attribute");
			return false;
		}*/
		String tid = rxXML.getDocumentElement().getAttribute("ti");;
		if (tid == null || tid.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected ticket id attribute");
			return false;
		}
//		ticketResponseId[matchid.toString()] = tid ;
		p.ticketId = tid;
		Log.d("GB3P", "ticket"+tid+", "+p.ticketId);
		String ws = rxXML.getDocumentElement().getAttribute("w");;
		if (ws == null || ws.equals("")) {
			/*setError(INCORRECT_ATTRIBUTE, "expected winnings attribute");
			return false;*/
			ws = "0";
		}
		
		ticketResponseWinnings = Float.parseFloat(ws);
		String bls = rxXML.getDocumentElement().getAttribute("bl");;
		if (bls == null || bls.equals("")) {
			/*setError(INCORRECT_ATTRIBUTE, "expected account balance attribute");
			return false;*/
			bls = "0";
		}
		accountBalance = Float.parseFloat(bls);
		
		if (pl != null) {
			pl.add(p);
		}
		return true;
	}
	
	/**
	 *  process the xml document as a 'gp' packet
	 * @param rxXML
	 * @return true if there is no error
	 */
	private Boolean processGamePropertiesXML(Document rxXML)
	{
		if (!rxXML.getDocumentElement().getTagName().equals("gp")) {
			setError(UNEXPECTED_RESPONSE, "expected \'gp\' packet, got a \'"+
					rxXML.getDocumentElement().getTagName()+"\'");
			return false;
		}
		sessionKey = rxXML.getDocumentElement().getAttribute("k");
		if (sessionKey == null || sessionKey.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected key attribute");
			return false;
		}
		
		gameInstance = rxXML.getDocumentElement().getAttribute("g");
		if (gameInstance == null || gameInstance.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected game instance attribute");
			return false;
		}
		
		String gc=null;
		gc = rxXML.getDocumentElement().getAttribute("gc");
		if (gc == "" || gc.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected game config attribute");
			return false;
		}
		if (!gameConfig.equals(gc)) {
			setError(INCORRECT_ATTRIBUTE, "incorrect game config " + gameConfig + ", got " + gc);
			return false;
		}
		playerID = rxXML.getDocumentElement().getAttribute("u");
		if (playerID == "" || playerID.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected player ID attribute");
			return false;
		}
		playerCurrency = rxXML.getDocumentElement().getAttribute("c");
		if (playerCurrency == null || playerCurrency.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected currency attribute");
			return false;
		}
		try {
			minimumBet = Float.parseFloat(xpe("/gp/o/min", rxXML));
		} catch (NumberFormatException e) {
			setError(INCORRECT_ELEMENT, "expected minimum bet element");
			return false;
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ELEMENT, "expected minimum bet element");
			return false;
		}
		try {
			maximumBet = Float.parseFloat(xpe("/gp/o/max", rxXML));
		} catch (NumberFormatException e) {
			setError(INCORRECT_ELEMENT, "expected maximum bet element");
			return false;
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ELEMENT, "expected maximum bet element");
			return false;
		}
		
		try {
			Node n = (Node) xpe("/gp/jbs", rxXML, XPathConstants.NODE);
			jackpots = processJackpotXML((Element)n);
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ELEMENT, "expected xpath expression exception");
			return false;
		}
		if (jackpots == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param rxXML
	 * @return
	 */
	private Boolean processAccountXML(Document rxXML)
	{
		if (!rxXML.getDocumentElement().getTagName().equals("a")) {
			setError(UNEXPECTED_RESPONSE, "expected \'a\' packet, got a \'"+
					rxXML.getDocumentElement().getTagName()+"\'");
			return false;
		}
		accountKey = rxXML.getDocumentElement().getAttribute("k");
		if (accountKey == null || accountKey.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected key attribute");
			return false;
		}
		accountTimestamp = rxXML.getDocumentElement().getAttribute("ts");
		if (accountTimestamp == null || accountTimestamp.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected ts attribute");
			return false;
		}
		if ((playerID != null) && !(playerID.equals(rxXML.getDocumentElement().getAttribute("u")))) {
			setError(INCORRECT_ATTRIBUTE, "player ID attribute doesn\'t match config");
			return false;
		}
		accountCurrency = rxXML.getDocumentElement().getAttribute("c");
		if (accountCurrency == null || accountCurrency.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "expected c attribute");
			return false;
		}
		try {
			accountBalance = Float.parseFloat(xpe("/a/o/b", rxXML));
		} catch (NumberFormatException e) {
			setError(INCORRECT_ELEMENT, "expected b element");
			return false;
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ELEMENT, "expected b element");
			return false;
		}
		NodeList fpl;
		try {
			fpl = (NodeList) xpe("/a/o/fp", rxXML, XPathConstants.NODESET);
//			freebies = new Object();
			for (int i=0; i<fpl.getLength(); i++) {
				Element fpi = (Element) fpl.item(i);
				String gc = fpi.getAttribute("gc");
				float fb = Float.parseFloat(fpi.getTextContent());
				for (String gcs: gc.split(",")) {
//					freebies[gcs] = fb;;
				}
			}
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ELEMENT, "expected fp element");
			return false;
		}
//		accountXML = rxXML;
		return true;
	}
	
	protected Boolean processGetMatchesXML(Document rxXML)
	{
		Match newMatch=null;
		Match origMatch=null;
		Node lMatchXML=null;
		Node nMatchXML=null;
		Node rMatchXML=null;
		NodeList matchListXML=null;
		NodeList nmatchListXML=null;
		if (!rxXML.getDocumentElement().getTagName().equals("ms")) {
			setError(UNEXPECTED_RESPONSE, "list matches, expected \'ms\' packet");
			return false;
		}
		ArrayList<Match> newLiveMatches=new ArrayList<Match>();
		ArrayList<Match> newNextMatches=new ArrayList<Match>();
		ArrayList<Match> newReqMatches=new ArrayList<Match>();

		try {
			matchListXML = (NodeList) xpe("//lm/m", rxXML, XPathConstants.NODESET);
			for (int i=0; i<matchListXML.getLength(); i++) {
				lMatchXML = matchListXML.item(i);
				newMatch = processMatchXML((Element)lMatchXML, 0);
				if (newMatch == null) {
					return false;
				}
				origMatch = findActiveMatch(newMatch.matchId);
				if (origMatch != null) {
					newMatch.predictions = origMatch.predictions;
				}
				newLiveMatches.add(newMatch);
			}
		} catch (XPathExpressionException e) {
			
		}
		
		try {
			nmatchListXML = (NodeList) xpe("//nm/m", rxXML, XPathConstants.NODESET);
			for (int i=0; i<nmatchListXML.getLength(); i++) {
				nMatchXML = nmatchListXML.item(i);
				newMatch = processMatchXML((Element)nMatchXML, 1);
				if (newMatch == null) {
					return false;
				}
				origMatch = findActiveMatch(newMatch.matchId);
				if (origMatch != null) {
					newMatch.predictions = origMatch.predictions;
				}
				newNextMatches.add(newMatch);
			}
		} catch (XPathExpressionException e) {
		}

		try {
			matchListXML = (NodeList) xpe("//rm/m", rxXML, XPathConstants.NODESET);
			for (int i=0; i<matchListXML.getLength(); i++) {
				rMatchXML = matchListXML.item(i);
				newMatch = processMatchXML((Element)rMatchXML, 1);
				if (newMatch == null) {
					return false;
				}
				origMatch = findActiveMatch(newMatch.matchId);
				if (origMatch != null) {
					newMatch.predictions = origMatch.predictions;
				}
				newReqMatches.add(newMatch);
			}
		} catch (XPathExpressionException e) {
		}
		liveMatches = newLiveMatches;
		nextMatches = newNextMatches;
		reqMatches = newReqMatches;
//		matchesXML = rxXML;
		return true;
	}
	
	protected Boolean processLastPayoutXML(Document rxXML)
	{
		if (!rxXML.getDocumentElement().getTagName().equals("lp")) {
			setError(UNEXPECTED_RESPONSE, "last payout, expected \'lp\' packet");
			return false;
		}
		try {
			lastPayoutMatch = processMatchXML(
					(Element)xpe("m[1]", rxXML, XPathConstants.NODE), -1);
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ATTRIBUTE, "last payout, expected \'m\' element");
			return false;
		}
		ArrayList<Jackpot> jpa=null;
		try {
			jpa = processJackpotXML(
					(Element)xpe("jbs", rxXML, XPathConstants.NODE));
		} catch (XPathExpressionException e) {
			setError(INCORRECT_ATTRIBUTE, "last payout, expected \'m\' element");
			return false;
		}
		matchResultJackpots = jpa;
//		lpXML = rxXML;
		return true;
	}
	
	protected Boolean processMatchResultXML(Document rxXML, int pmId, final PredictionFactory factory)
	{
		if (!rxXML.getDocumentElement().getTagName().equals("mrs")) {
			setError(UNEXPECTED_RESPONSE,
					"getMatchResults, expected \'mrs\' packet, got " +
						rxXML.getDocumentElement().getTagName());
			return false;
		}
		Element jackresXML=null;
		Element predXML=null;
		try {
			matchResultJackpots = new ArrayList<Jackpot>();
			NodeList jackreslistXML =((NodeList)xpe("//jr", rxXML, XPathConstants.NODESET));
			for (int i=0; i<jackreslistXML.getLength(); i++) {
				jackresXML = (Element) jackreslistXML.item(i);
				int id;
				String ids = jackresXML.getAttribute("id");
				if (ids == null || ids.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected id attribute");
					return false;
				}
				id = Integer.parseInt(ids);
				String p;
				p = jackresXML.getAttribute("p");
				if (p == null || p.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected p attribute");
					return false;
				}
//				String c;
//				c = jackresXML.getAttribute("c");
//				if (c == null || c.equals("")) {
//					setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected c attribute");
//					return false;
//				}
				float a;
				String as = jackresXML.getAttribute("a");
				a = Float.parseFloat(as);
				if (as == null || as.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected a attribute");
					return false;
				}
				matchResultJackpots.add(new Jackpot("", id, a, jackpotCurrency, p.equals("y"), ""));
			}
		} catch (XPathExpressionException e) {
			
		}
		Element mr=null;
		if (freePlayMode){
			try {
				mr = (Element)xpe("//mrfp[1]", rxXML, XPathConstants.NODE);
			} catch (XPathExpressionException e) {
				setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected \'mrfp\' packet");
				return false;
			}
		} else {
			try {
				mr = (Element)xpe("//mr[1]", rxXML, XPathConstants.NODE);
			} catch (XPathExpressionException e) {
				setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected \'mr\' packet");
				return false;
			}
		}
		if (mr == null) {
			setError(UNEXPECTED_RESPONSE, "Expected mr element");
		}
		float w;
		String ws = mr.getAttribute("w");
		w = Float.parseFloat(ws);
		if (ws == null || ws.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected \'w\' attribute");
			return false;
		}
		float ew;
		String ews = mr.getAttribute("ew");
		ew = Float.parseFloat(ews);
		if (ews == null || ews.equals("")) {
			setError(INCORRECT_ATTRIBUTE, "getMatchResults, expected \'ew\' attribute");
			return false;
		}
		matchResultResults = new ArrayList<Prediction>();
		Element pmlsXML = null;
		try {
			pmlsXML= (Element) xpe("//pmls", mr, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
		} catch (ClassCastException e) {
		}
		ArrayList<Prediction> a = null;
		if (pmlsXML != null) {
			if ((a=processPredictionListXML(pmlsXML, factory)) != null) {
				matchResultResults = a;
			}
		}
		Match m=findActiveMatch(pmId);
		if (m != null) {
			m.predictions = matchResultResults;
		}
		Match rm = this.findResultsMatch(pmId);
		if (rm != null) {
			rm.predictions = matchResultResults;
		}
		
		matchResultWinnings = w;
		matchResultEstimate = ew;
		String s = mr.getAttribute("s");
		if (s == null || s.equals("")) {
			matchResultStartTime = m.startDate;
		} else {
			matchResultStartTime = SBTime.timeStr2date(s);
		}
//		matchResultXML = rxXML;
		return true;
	}

	protected ArrayList<Prediction> processPredictionListXML(Element rxXML, PredictionFactory factory)
	{
		String selStr=null;
		String[] selArr=null;
		if (!rxXML.getTagName().equals("pmls")) {
			setError(UNEXPECTED_RESPONSE, "list predictions, expected \'pmls\' packet");
			return null;
		}
		ArrayList<Prediction> a = new ArrayList<Prediction>();
		NodeList pmlXML = null;
		try {
			pmlXML = (NodeList) xpe("pml", rxXML, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
		}
		if (pmlXML != null) {
			for (int i=0; i<pmlXML.getLength(); i++) {
				Element predXML = (Element) pmlXML.item(i);
				String ti;
				ti = predXML.getAttribute("ti");
				if (ti == null || ti.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction ticket id (\'ti\' attribute)");
					return null;
				}
				String ts;

				ts = predXML.getAttribute("ts");
				if (ts == null || ts.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction timestamp attribute");
					return null;
				}
				float b;
				String bs = predXML.getAttribute("b");
				b = Float.parseFloat(bs);
				if (bs == null || bs.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction bet attribute");
					return null;
				}
				String r;
				r = predXML.getAttribute("r");
				if (r == null || r.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction result attribute");
					return null;
				}
				String st;
				st = predXML.getAttribute("st");
				if (st.equals("O")) {
					r = "U";
				}
				float jp;
				String jps = predXML.getAttribute("jp");
				jp = Float.parseFloat(jps);
				if (jps == null || jps.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction jackpot attribute");
					return null;
				}
				String fl;
				fl = predXML.getAttribute("fl");
				if (fl == null || fl.equals("")) {
					setError(INCORRECT_ATTRIBUTE, "list predictions, expected result freeplay attribute");
					return null;
				}
				if ((freePlayMode && fl.equals("1") || !freePlayMode && fl.equals("0"))) {
					try {
						selStr = xpe("i/s", predXML);
						Prediction s = factory.prediction(
								0, ti, b, "FRE", timeStampStr2Date(ts), r, selStr, jp
							);
						if (s == null) {
							setError(INCORRECT_ATTRIBUTE, "list predictions, malformed prediction selection, got " + selStr);
							return null;
						}
						Log.d("GB3P", "pret "+s.toString());
						a.add(s);
					} catch (XPathExpressionException e) {
						setError(INCORRECT_ATTRIBUTE, "list predictions, expected prediction selection");
						return null;
					}
				}
			}
		}
		return a;
	}

	protected Boolean processTeamDetailXML(Match m, Node teamListXML)
	{
//		<tm id="731334312" tId="t7" hm="Y" nm="Aston Villa">
//			<plyr id="p1822" fNm="Shay" lNm="Given" lx="50" ly="12" pos="Goalkeeper" sh="1" stat="G"/>
//			<plyr id="p15405" fNm="Alan" lNm="Hutton" lx="13" ly="38" pos="Defender" sh="2" stat="G"/>
		if (m == null) {
			setError(XMLP_PARSE_FAIL, "processTeamDetailXML, bad input, null match");
			return false;
		}
		if (teamListXML == null) {
			setError(XMLP_PARSE_FAIL, "processTeamDetailXML, bad input, null xml");
			return false;
		}
		if (m.homeTeam == null || m.awayTeam == null) {
			setError(XMLP_PARSE_FAIL, "processTeamDetailXML, bad input, null teams");
			return false;
		}
		NodeList tmXML = null;
		try {
			tmXML = (NodeList) xpe("tm", teamListXML, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
		}
		if (tmXML != null) {
			Log.d("GB3P", "going for teams ... ");
			for (int i=0; i<tmXML.getLength(); i++) {
				Element predXML = (Element) tmXML.item(i);
				String hm;
				hm = predXML.getAttribute("hm");
				String tId;
				tId = predXML.getAttribute("tId");
				
				Log.d("GB3P", "doing team "+tId);
				if (hm != null && tId != null) {
					Boolean isHome = hm.equals("Y");
					if (isHome) {
						m.homeTeam.xId = tId;
					} else {
						m.awayTeam.xId = tId;
					}
					
					ArrayList<Player> playerList = new ArrayList<Player>();
					
					NodeList plyrNL = null;
					try {
						plyrNL = (NodeList) xpe("plyr", predXML, XPathConstants.NODESET);
					} catch (XPathExpressionException e) {
					}
					for (int j=0; j<plyrNL.getLength(); j++) {
						Element plyrXML = (Element) plyrNL.item(j);
						Player p = processPlayerXML(plyrXML);
						if (p == null) {
							setError(XMLP_PARSE_FAIL, "unknown player processing match details");
							return false;
						}
						playerList.add(p);
						Log.d("GB3P", p.xId+" "+p.name());
					}
					if (isHome) {
						m.homeTeam.players = playerList;
					} else {
						m.awayTeam.players = playerList;
					}
				} else {
					setError(XMLP_PARSE_FAIL, "team details 'hm' and 'tId' are undefined");
					return false;
				}
			}
		}
		return true;
	}
	
	protected Player processPlayerXML(Element rxXML)
	{
		int n = 0;
		String id = "";
		String fnm = "";
		String lnm = "";
		int pos = Player.WATERBOY;
		Boolean sub = false;
		
		if (rxXML == null) {
			setError(UNEXPECTED_RESPONSE, "null player xml");
			return null;
		}
		id = rxXML.getAttribute("id");
		if (id == null) {
			setError(UNEXPECTED_RESPONSE, "process player xml, expected \'id\' attribute");
			return null;
		}
		fnm = rxXML.getAttribute("fNm");
		if (fnm == null) {
			setError(UNEXPECTED_RESPONSE, "process player xml, expected \'fNm\' attribute");
			return null;
		}
		lnm = rxXML.getAttribute("lNm");
		if (lnm == null) {
			setError(UNEXPECTED_RESPONSE, "process player xml, expected \'lNm\' attribute");
			return null;
		}
		String ns  = rxXML.getAttribute("sh");
		if (ns != null) {
			n = Integer.parseInt(ns, 10);
		}
		String _pos = rxXML.getAttribute("pos");
		if (_pos.equals("Defender")) {			pos = Player.DEFENDER;
		} else if (_pos.equals("Midfielder")) {	pos = Player.MIDFIELDER;
		} else if (_pos.equals("Forward")) {	pos = Player.FORWARD;
		} else if (_pos.equals("Goalkeeper")) {	pos = Player.GOALKEEPER;
		}
		String _stat = rxXML.getAttribute("stat");
		if (_stat != null && _stat.equals("S")) {
			sub = true; 
		}
		Player p = new Player(n, fnm, lnm, pos, sub);
		p.xId = id;
		return p;
	}
	
	protected Boolean processSiSGameFeedXML(Match m, Document rxXMLd)
	{
		if (m == null) {
			setError(XMLP_PARSE_FAIL, "processSiSGameFeedXML, bad input, null match");
			return false;
		}
		if (rxXMLd == null) {
			setError(XMLP_PARSE_FAIL, "processSiSGameFeedXML, bad input, null xml");
			return false;
		}
		Element rxXML = rxXMLd.getDocumentElement();
		if (!rxXML.getTagName().equals("m")) {
			setError(XMLP_PARSE_FAIL, "processSiSGameFeedXML, bad input");
			return false;
		}
		String loc = rxXML.getAttribute("loc");
		if (loc != null) {
			m.location = loc;
		}

		DataFeed d = m.feed;
		if (d == null) {
			m.feed = d = new DataFeed();
		} else {
			d.clear();
		}
		Node childn= rxXML.getFirstChild();
		Element child;
		while (childn != null) {
			if (childn.getNodeType() == Node.ELEMENT_NODE) {
				child = (Element) childn;
				MatchTime t = null;
				Player p = null;
				Team tm = null;
				String eid = "";
				String bodypart = null;
				Boolean ontarget = false;
				Boolean outcome = false;
				Player oPlayer = null;
				Player assist = null;
				String range = null;
				String kind = null;
				String pd = child.getAttribute("pd");
				String ti = child.getAttribute("ti");
				try {
					if (pd != null && ti != null) {
						String[] sa = ti.split(":");
						int tpriod = Integer.parseInt(pd, 10);
						int tmin = Integer.parseInt(sa[0], 10);
						int tsec = Integer.parseInt(sa[1], 10);
						if (tpriod == 2) tmin -= 45;
						else if (tpriod == 3) tmin -= 90;
						else if (tpriod >= 4) tmin -= 105;
						t = new MatchTime(tpriod, tmin, tsec);
					}
				} catch (NumberFormatException e) {
					
				}
				tm = m.team(child.getAttribute("tId"));
				p = m.player(child.getAttribute("plyr"));
				eid = child.getAttribute("id");
				FeedEntry f = null;
				String tsstr = null;
				if (t != null && tm != null) {
					String n = child.getTagName();
					String timestr = t.toString();
					String pstr = ((p != null)? p.name()+" of ":"");
					
					if (n.equals("off")) {
//	<offence elapsedTime="28:02" id="1484451563" period="1" type="offside">
//	<offence correction="Y" elapsedTime="17:24" id="991648777" period="1" type="foul">
//<off pd="1" id="743334015" ti="00:23" plyr="p49493" tId="t7" type="foul"/>
						tsstr = child.getAttribute("type");
						if (tsstr != null) {
							if (tsstr.equals("foul")) {
								f = new FeedEntry(FeedEntry.FOUL_EVENT, t, tm.teamId, "Foul",
									"Foul by "+pstr+tm.fullName+" at "+timestr);
								f.shortTxt = "by "+pstr+tm.fullName;
								f.xId = eid;
							} else if (tsstr.equals("offside")) {
								f = new FeedEntry(FeedEntry.OFFSIDE_EVENT, t, tm.teamId, "Offside",
									pstr+tm.fullName+" offside at "+timestr);
								f.shortTxt = pstr+tm.fullName;
								f.xId = eid;
							}
						}
					} else if (n.equals("fk")) {
//<freeKick elapsedTime="00:36" id="256067219" period="1"><player id="p37055" teamId="t6"/>				  </freeKick>				
//<fk pd="1" id="256067219" ti="00:36" plyr="p37055" tId="t6"/>
						f = new FeedEntry(FeedEntry.FREEKICK_EVENT, t, tm.teamId, "Free Kick",
							"Free kick to "+pstr+tm.fullName+" at "+timestr);
						f.shortTxt = "to "+pstr+tm.fullName;
						f.xId = eid;
					} else if (n.equals("s")) {
//<shot bodyPart="left footed" correction="Y" elapsedTime="01:55" id="1161613318" onTarget="N" outcome="N" period="1" type="block"><player id="p18737" teamId="t7"/>				</shot>				
//<s pd="1" id="1161613318" ti="01:55" plyr="p18737" tId="t7" type="block" tar="N" out="N" bp="left footed"/>
						bodypart = child.getAttribute("bp");
						String bs = (child.getAttribute("out"));
						outcome = (bs != null && !bs.equals("N"));
						bs = (child.getAttribute("tar"));
						ontarget = (bs != null && !bs.equals("N"));
						f = new FeedEntry(FeedEntry.SHOT_EVENT, t, tm.teamId, "Shot",
							"Shot"+(bodypart != null?" with "+bodypart:"")+" by "+pstr+tm.fullName+" at "+timestr+(ontarget?", on target...":""));
						f.shortTxt = "by "+pstr+tm.fullName+(bodypart != null?" with "+bodypart:"")+(ontarget?", on target...":"");
						f.xId = eid;	
					} else if (n.equals("pen")) {
//<penaltyAward elapsedTime="59:48" id="552277985" period="2" teamId="t6"></penaltyAward>				
//<pen pd="2" id="552277985" ti="59:48" tId="t6"/>
						f = new FeedEntry(FeedEntry.PENALTY_EVENT, t, tm.teamId, "Penalty",
								"Penalty to "+tm.fullName+" at "+timestr);
						f.shortTxt = "to "+tm.fullName;
						f.xId = eid;
					} else if (n.equals("cd")) {
//<card elapsedTime="59:58" id="1866756446" period="2" points="10" reason="foul" type="yellow"><player id="p1764" teamId="t7"/></card>			
//<cd pd="2" id="1866756446" ti="59:58" plyr="p1764" tId="t7" type="yellow"/>
						tsstr = child.getAttribute("type");
						if (tsstr != null) {
							if (tsstr.equals( "yellow")) {
								f = new FeedEntry(FeedEntry.YELLOWCARD_EVENT, t, tm.teamId, "Yellow Card",
									"Yellow Card to "+pstr+tm.fullName+" at "+timestr);
								f.shortTxt = "to "+pstr+tm.fullName;
								f.xId = eid;
							} else if (tsstr.equals( "secondyellow")) {
									f = new FeedEntry(FeedEntry.YELLOWCARD_EVENT, t, tm.teamId, "Yellow Card",
										"Second yellow Card to "+pstr+tm.fullName+" at "+timestr);
									f.shortTxt = "to "+pstr+tm.fullName;
									f.xId = eid;
							} else if (tsstr.equals("red")) {
								f = new FeedEntry(FeedEntry.REDCARD_EVENT, t, tm.teamId, "Red Card",
									"Red Card to "+pstr+tm.fullName+" at "+timestr);
								f.shortTxt = "to "+pstr+tm.fullName;
								f.xId = eid;
							}
						}
					} else if (n.equals("g")) {
/*
<goal bodyPart="left footed" elapsedTime="34:18" id="1711005230" period="1" score="1-0" scoring_range="Out Of Box, Centre" teamId="t7" type="openPlay">
<player id="p58845"/>
<assist>
<player id="p18737"/>
</assist>
</goal><goal bodyPart="right footed" elapsedTime="60:42" id="1226137628" period="2" score="1-1" scoring_range="Box, Centre" teamId="t6" type="penalty">
<player id="p17500"/>
</goal>
*/
//<g pd="2" id="1226137628" ti="60:42" plyr="p17500" tId="t6" type="penalty" sc="1-1" rng="Box, Centre" bp="right footed" ast=""/>
						String an = child.getAttribute("ast");
						if (an != null) {
							assist = m.player(an);
						}
						bodypart = child.getAttribute("bp");
						range = child.getAttribute("rng");
						kind = child.getAttribute("type");
						if (kind != null) {
							kind = Character.toString(kind.charAt(0)).toUpperCase()+kind.substring(1);
						}

						String tx = null;
						if (kind == null) {
							tx = "Goal";
						} else {
							tx = kind + " goal";
						}
						pstr = ((p != null)? " by "+p.name():"");
						tx = tx+ " to "+tm.fullName +" at "+timestr+ ", shot"+pstr;
						if (bodypart != null) tx = tx + " with "+bodypart;
						if (range != null) tx = tx + " from "+range;
						if (assist != null) tx = tx + ", assisted by "+assist.name();
						f = new FeedEntry(FeedEntry.GOAL_EVENT, t, tm.teamId, "GOAL!!!!!", tx);
						f.xId = eid;
						f.shortTxt = tx;
					} else if (n.equals("thr")) {
						f = new FeedEntry(FeedEntry.THROWIN_EVENT, t, tm.teamId, "Throwin",
								"Throwin to "+tm.fullName+" at "+timestr);
						f.shortTxt = "to "+tm.fullName;
						f.xId = eid;
						if (p != null) {
							f.txt = f.txt + " taken by "+p.name();
							f.shortTxt = f.shortTxt + " taken by "+p.name();
						}
					} else if (n.equals("cr")) {
/*
<corner elapsedTime="02:34" id="1627829942" period="1">
<player id="p18737" teamId="t7"/>
</corner><corner elapsedTime="13:14" id="2010476970" period="1">
<player id="p4611" teamId="t6"/>
</corner>
*/
//<cr pd="2" id="1905767564" ti="67:38" plyr="p4611" tId="t6"/>
						f = new FeedEntry(FeedEntry.CORNER_EVENT, t, tm.teamId, "Corner",
							"Corner to "+tm.fullName+" at "+timestr);
						f.xId = eid;
						f.shortTxt = "to "+tm.fullName;
						if (p != null) {
							f.txt = f.txt + " taken by "+p.name();
							f.shortTxt = f.shortTxt + " taken by "+p.name();
						}
					} else if (n.equals("sub")) {
/*
<substitution elapsedTime="52:56" id="69321441" period="2" teamId="t7">
<player id="p8380" onField="Y"/>
<player id="p15405" onField="N"/>
</substitution><substitution elapsedTime="69:44" id="1200571960" period="2" teamId="t7">
<player id="p51484" onField="Y"/>
<player id="p80979" onField="N"/>
</substitution>
*/
//<sub pd="2" id="1200571960" ti="69:44" plyr="p51484" tId="t7" offPlyr="p80979"/>
						String ops = child.getAttribute("offPlyr");
						if (ops != null) {
							oPlayer = m.player(ops);
						}
						String opstr = ((oPlayer != null)?" for "+oPlayer.name():"");
						pstr = (p != null)? p.name():"";
						f = new FeedEntry(FeedEntry.SUBSTITUTION_EVENT, t, tm.teamId, "Substitution",
							tm.fullName+" substitutes "+pstr+opstr+" at "+timestr);
						f.shortTxt = tm.fullName+" substitutes "+pstr+opstr;
						f.xId = eid;
					} else if (n.equals("bk")) {
/*
<block bodyPart="hands" correction="Y" elapsedTime="81:04" id="767263138" period="2">
<player action="save" id="p1822" teamId="t7"/>
</block><block correction="Y" elapsedTime="75:55" id="929065401" period="2">
<player action="block" id="p37742" teamId="t6"/>
</block><block correction="Y" elapsedTime="67:42" id="50844549" period="2">
<player action="block" id="p8380" teamId="t7"/>
</block><block bodyPart="hands" correction="Y" elapsedTime="54:25" id="1381260740" period="2">
<player action="save" id="p1822" teamId="t7"/>
</block>
*/
//	<bk pd="2" id="50844549" ti="67:42" plyr="p8380" tId="t7" act="block"/>
						bodypart = child.getAttribute("bp");
						kind = "Save";
						String acts = child.getAttribute("act");
						if (acts != null) {
							if (acts.equals("block")) kind = "Block";
						}
						f = new FeedEntry(FeedEntry.SAVE_EVENT, t, tm.teamId, kind,
								kind+
								((bodypart!=null)?" with "+bodypart:"")+
								" by "+pstr+tm.fullName+" at "+timestr);
						f.shortTxt = kind+
							((bodypart!=null)?" with "+bodypart:"")+
							" by "+pstr+tm.fullName;
						f.xId = eid;
					}
				}
				if (f != null) {
					d.add(f);
				}
			}
			childn = childn.getNextSibling();
		}
		return true;
	}
	
//////////////////////////////////////////////////////////////////////////////////////
// MAIN HOOKS TO TALK TO SERVER
//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 */
	public void playParimutuelTicket(int pmId, Prediction p, ArrayList<Prediction> pl)
	{
		playParimutuelTicket(pmId, p, pl, freePlayMode, 1);
	}
	
	/**
	 * 
	 * @param pmId
	 * @param p
	 * @param pl
	 * @param fpMode
	 * @param risk
	 */
	public void playParimutuelTicket(int pmId, Prediction p, final ArrayList<Prediction> pl, Boolean fpMode, int risk)
	{
		String requestURL = serverURL + "action=1&gc=" + gameConfig + "&g=" + gameInstance + "&pm=" + pmId;
		final Prediction cp = p.clone();
		currentPrediction = cp;//new Prediction(matchid, teamid, timeHalf, timeMin, predictType, bet);
		
		HttpPost request = createTicketRequest(requestURL, p, fpMode, risk);
		if (request == null) {
			dispatchEvent(SERVER_ERROR_EVENT);  
			return;
		}
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "playGameTicket, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processGoalTicketXML(cp, pl, rxXML)) {
						currentPredictionList = pl;
						dispatchEvent(SERVER_PLAY_TICKET_EVENT);
					} else {
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});
	}


	/**
	 *  main hook to request the game properties from the server
	 * @param _gameConfig the game config to pass to the server
	 */
	public void getGameProperties(String _gameConfig) 
	{
		gameConfig = _gameConfig;
		String requestURL = serverURL + "action=3&gc=" + _gameConfig;
		if (freePlayMode) {
			requestURL = requestURL + "&fp=1";
		} else {
			requestURL = requestURL + "&fp=0";
		}
		
		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getGameProperties, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processGamePropertiesXML(rxXML)) {
//						propertiesXML = rxXML;
						dispatchEvent(SERVER_CONFIG_EVENT);
					} else {
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});
	}
	
	/**
	 *  main hook to request the game properties from the server
	 * @param _gameConfig the game config to pass to the server
	 */
	public void updateJackpots() 
	{
		String requestURL = serverURL + "action=9&gc=" + gameConfig;
		if (freePlayMode) {
			requestURL = requestURL + "&fp=1";
		} else {
			requestURL = requestURL + "&fp=0";
		}
		
		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			protected Boolean processXML(Document rxXML)
			{
				if (!rxXML.getDocumentElement().getTagName().equals("jbs")) {
					setError(UNEXPECTED_RESPONSE, "expected \"jbs\' packet, got a \'"+
							rxXML.getDocumentElement().getTagName()+"\'");
					return false;
				}
				
				jackpots = processJackpotXML(rxXML.getDocumentElement());
				if (jackpots == null) {
					return false;
				}
				return true;
			}
			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "updateJackpot, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
//						propertiesXML = rxXML;
						dispatchEvent(SERVER_UPDATE_JACKPOT_EVENT);
					} else {
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});
	}
	
	/**
	 * 
	 * @param getFreePlay
	 */
	public void getPlayerAccount(Boolean getFreePlay)
	{
		String requestURL = serverURL + "action=2";
		if (getFreePlay) {
			requestURL = requestURL + "&fp=1";
		}
		
		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getGameProperties, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processAccountXML(rxXML)) {
//						accountXML = rxXML;
						dispatchEvent(SERVER_ACCOUNT_EVENT);
					} else {
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});
	}
	
	/**
	 * @param match
	 */
	public boolean listPredictionResults(final Match match, final PredictionFactory factory)
	{
		return listPredictionResults(match, factory, playerID);
	}
	
	/**
	 * 
	 * @param match
	 * @param player
	 */
	public boolean listPredictionResults(final Match match, final PredictionFactory factory, String player)
	{
		String requestURL;
		if (player == null) {
			if (playerID == null) {
				return false;
			}
			player = playerID;
		}
		if (match == null) {
			return false;
		}
		requestURL = serverURL + "action=14&pm=" +
			Integer.toString(match.matchId) + "&u=" + player;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "listPredictionResults, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);
				} else {
					ArrayList<Prediction> a=null;
					if (errorXML(rxXML)) {
						setError(XMLP_PARSE_FAIL, "Error Response, processing listPredictionResults xml response: "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					} else if ((a=processPredictionListXML(rxXML.getDocumentElement(), factory)) != null) {						
						lastPredictionList = a;
						match.predictions = lastPredictionList;
						listPredictionResultsMatch = match;
						Match rm = findResultsMatch(match.matchId);
						if (rm != null) {
							rm.predictions = lastPredictionList;
						}
						dispatchEvent(SERVER_LIST_PREDICTIONS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing listPredictionResults xml response "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});  
		
		return true;
	}


	public ArrayList<Match> allResultsList = null;
	/**
	 * 
	 * @param match
	 * @param player
	 */
	public boolean listAllPredictionResults(final PredictionFactory factory)
	{
		return listAllPredictionResults(factory, playerID, null, null);
	}
	
	public boolean listAllPredictionResults(final PredictionFactory factory, Date startDate, Date endDate)
	{
		return listAllPredictionResults(factory, playerID, startDate, endDate);
	}
	
	public boolean listAllPredictionResults(final PredictionFactory factory, String _playerID, Date startDate, Date endDate)
	{
		if (playerID == null) {
			return false;
		}
		String requestURL;
		requestURL = serverURL + "action=14&pm=-1" + "&u=" + playerID;
		if (startDate != null) {
			String sds = "&sdate="+Integer.toString(startDate.getDate())+"/"+
								Integer.toString(startDate.getMonth()+1)+"/"+
								Integer.toString(1900+startDate.getYear());
			requestURL += sds;
		}
		if (endDate != null) {
			String eds = "&edate="+Integer.toString(endDate.getDate())+"/"+
								Integer.toString(endDate.getMonth()+1)+"/"+
								Integer.toString(1900+endDate.getYear());
			requestURL += eds;
		}

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			Boolean processXML(Element rxXML)
			{
				if (!rxXML.getTagName().equals("ms")) {
					setError(UNEXPECTED_RESPONSE, "listAllPredictionResults, expected \'ms\' packet");
					return false;
				}
				NodeList matchListXML=null;
				try {
					matchListXML = (NodeList) xpe("m", rxXML, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
					return false;
				}
				ArrayList<Match> arl = new ArrayList<Match>();
				for (int i=0; i<matchListXML.getLength(); i++) {
					Node lMatchXML = matchListXML.item(i);
					Element mx = (Element) lMatchXML;
					String n = mx.getAttribute("n");
					String s = mx.getAttribute("s");
					int id = Integer.parseInt(mx.getAttribute("id"), 10);
					
					
					NodeList tListXML = null;
					try {
						tListXML = (NodeList) xpe("//te", lMatchXML, XPathConstants.NODESET);
					} catch (XPathExpressionException e) {
						e.printStackTrace();
						return false;
					}

					Node pListXML = null;
					try {
						pListXML = (Node) xpe("pmls", lMatchXML, XPathConstants.NODE);
					} catch (XPathExpressionException e) {
						e.printStackTrace();
						return false;
					}
					ArrayList<Prediction> pmlss = processPredictionListXML((Element)pListXML, factory);
					if (pmlss == null) return false;
					if (pmlss.size() > 0) {
						Match m = new Match(id, n, s, "", null, null);
						String mxst = mx.getAttribute("st");
						if (mxst != null && mxst.equals("O")) {
							for (Prediction pp: pmlss) {
								pp.result = "U";
							}
						}
						m.predictions = pmlss;
						arl.add(m);
						if (tListXML != null) {
							for (i=0; i<tListXML.getLength(); i++) {
								Element tn = (Element)tListXML.item(i);
								String tid = tn.getAttribute("id");
								String tpos = tn.getAttribute("pos");
								Log.d("GB3P", tid+", "+tpos);
								if (tpos.equals("1")) {
									m.homeTeam = new Team(Integer.parseInt(tid));
								} else if (tpos.equals("2")) {
									m.awayTeam = new Team(Integer.parseInt(tid));
								}
							}
						}
					}
				}
				if (allResultsList == null || allResultsList.size() == 0) {
					allResultsList = arl;
				} else {
					int j = 0;
					int i = 0;
					for (Match m: arl) {
						int mid = m.matchId;
						Match am = null;
						long dtm = m.startDate.getTime();
						boolean inserted = false;
						while (j<allResultsList.size()) {
							am = allResultsList.get(j);
							if (mid == am.matchId) {
								allResultsList.set(j, m);
								inserted = true;
								break;
							} else {
								long dtam = am.startDate.getTime();
								if (dtm > dtam) {
									allResultsList.add(j, m);
									inserted = true;
									break;
								}
							}
							j++;
						}
						if (!inserted) {
							allResultsList.add(m);
						}
					}
				}
				return true;
			}

			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "listPredictionResults, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);
				} else {
					ArrayList<Prediction> a=null;
					if (errorXML(rxXML)) {
						setError(XMLP_PARSE_FAIL, "Error Response, processing listPredictionResults xml response: "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					} else if (!errorXML(rxXML) && processXML(rxXML.getDocumentElement())) {						
						dispatchEvent(SERVER_LIST_ALL_RESULTS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing listPredictionResults xml response "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
		
		return true;
	}


	public void getMatchResults(final int pmId, final PredictionFactory factory, String player)
	{
		String requestURL = serverURL + "action=15&pm=" +
				Integer.toString(pmId) + "&u=" + player +
				"&fp=" + (freePlayMode ? "1" : "2");

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getMatchResults, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processMatchResultXML(rxXML, pmId, factory)) {
//						matchResultXML = rxXML;
						dispatchEvent(SERVER_MATCH_RESULT_EVENT);
					} else {
//						setError(XMLP_PARSE_FAIL, "Bad Response, processing getMatchResults xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}

	/**
	 * 
	 */
	public void getMatches()
	{
		getMatches(-1);
	}
	
	/**
	 * 
	 * @param pmId
	 */
	public void getMatches(int pmId)
	{
		String requestURL;

		requestURL = serverURL + "action=16&subact=2" + "&gc=" + gameConfig;
		if (pmId > 0) {
			requestURL = requestURL + "&pm=" + Integer.toString(pmId);
		}
		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getGameProperties, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processGetMatchesXML(rxXML)) {
//						matchesXML = rxXML;
						dispatchEvent(SERVER_LIST_MATCHES_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad response, processing list matches xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}
	

	public void getMatchStatus(int id)
	{
		final int pmId = id;
		String requestURL = serverURL + "action=16&subact=1&pm=" +
						Integer.toString(pmId) + "&gc=" + gameConfig;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			protected Boolean processXML(Document rxXML)
			{
				Match m=null;
				matchStatus = processMatchStatusXML(rxXML.getDocumentElement());
				if (matchStatus == null) {
					return false;
				} else {
					matchStatus.id = pmId;
					m = findActiveMatch(pmId);
					if (m != null) {
						m.status = matchStatus;
					}
//					matchStatusXML = rxXML;
					matchPot = freePlayMode ? matchStatus.freePot : matchStatus.pot;
					matchPotCurrency = matchStatus.currency;
					matchPotId = pmId;
				}
				return true;
			}
			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getMatchStatus, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
//						matchStatusXML = rxXML;
						dispatchEvent(SERVER_MATCH_STATUS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing getMatchStatus xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   

	}

	public void getMatchPotUpdate(final int pmId)
	{
		String requestURL = serverURL + "action=16&subact=1&pm=" + Integer.toString(pmId) + "&gc=" + gameConfig;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			protected Boolean processXML(Document rxXML)
			{
				Match m=null;
				MatchStatus ms=processMatchStatusXML(rxXML.getDocumentElement());
				if (ms == null) {
					return false;
				} else {
					ms.id = pmId;
					m = findActiveMatch(pmId);
					if (m != null) {
						m.status = ms;
					}
					matchPot = freePlayMode ? ms.freePot : ms.pot;
					matchPotCurrency = ms.currency;
					matchPotId = pmId;
//					matchPotXML = rxXML;
				}
				return true;
			}
			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getMatchPotUpdate, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
						dispatchEvent(SERVER_MATCH_POT_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing getMatchPotUpdate xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}

	/**
	 * getTeamDetails:
	 */
	public void getTeamDetails(int id)
	{
		final int pmId=id;
		if ((teamDetailsMatch=findActiveMatch(pmId)) == null) {
			setError(INTERNAL_ERROR, "team details request, bad match id "+Integer.toString(pmId));
			dispatchEvent(SERVER_ERROR_EVENT);
			return;
		}
		
		String requestURL = serverURL + "action=16&subact=7&pm=" + Integer.toString(pmId) + "&gc=" + gameConfig;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getTeamDetails, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
						dispatchEvent(SERVER_TEAM_DETAILS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad response, processing getTeamDetails xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
			
			protected Boolean processXML(Document rxXMLd)
			{
				if (errorXML(rxXMLd)) return false;
				Element rxXML = rxXMLd.getDocumentElement();
				if (!rxXML.getTagName().equals("m")) {
					setError(XMLP_PARSE_FAIL, "Expecting m element in team details response, got "+rxXML.getTagName());
					return false;
				}
				teamDetailsMatch.leagueId = rxXML.getAttribute("co");
				teamDetailsMatch.location = rxXML.getAttribute("loc");
				teamDetailsMatch.leagueName = rxXML.getAttribute("coNm");
				teamDetailsMatch.xId = rxXML.getAttribute("id");
				Node teamListXML=null;
				try {
					teamListXML = (Node) xpe("//tms", rxXML, XPathConstants.NODE);
				} catch (XPathExpressionException e1) {
					return false;
				}
				if (teamListXML == null) {
					setError(XMLP_PARSE_FAIL, "Expecting tms element in team details response");
					return false;
				}
				return processTeamDetailXML(teamDetailsMatch, teamListXML);
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
				
	}
	
	/**
	 * getGameEventFeed:
	 */
	public void getGameEventFeed(int id)
	{
		final int pmId = id;
		if ((feedDetailsMatch=findActiveMatch(pmId)) == null) {
			setError(INTERNAL_ERROR, "feed details request, bad match id "+Integer.toString(pmId));
			dispatchEvent(SERVER_ERROR_EVENT);
			return;
		}
		String requestURL = serverURL + "action=16&subact=6&pm=" + Integer.toString(pmId) + "&gc=" + gameConfig;
		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getGameEventFeed, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (errorXML(rxXML)) {
						dispatchEvent(SERVER_ERROR_EVENT);				
					} else if (processSiSGameFeedXML(feedDetailsMatch, rxXML)) {
						dispatchEvent(SERVER_GAME_FEED_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad response, processing getGameEventFeed xml response: "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request}); 	
	}

	/**
	 * listAllTeams
	 */
	public void listAllTeams()
	{
		String requestURL = serverURL + "action=16&subact=8" + "&gc=" + gameConfig;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {			
			protected Boolean processXML(Document rxXMLd)
			{
				Element rxXML = rxXMLd.getDocumentElement();
				if (!rxXML.getTagName().equals("tes")) {
					setError(UNEXPECTED_RESPONSE, "listAllTeams, expected \'tes\' packet, got "+rxXML.getTagName());
					return false;
				}
				ArrayList<Team> a = new ArrayList<Team>();
				NodeList teamListXML;
				try {
					teamListXML = (NodeList) xpe("//te", rxXML, XPathConstants.NODESET);
				} catch (XPathExpressionException e1) {
					return false;
				}
				for (int i=0; i<teamListXML.getLength(); i++) {
					Element n = (Element)teamListXML.item(i);
					Team t = processTeamXML(n);
					if (t == null) {
						return false;
					}
					a.add(t);
				}
				allTeams = a;
				return true;
			}
			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "listAllTeams, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
						dispatchEvent(SERVER_LIST_ALL_TEAMS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad response, processing listAllTeams xml response: "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}

	/**
	 * listAllTeams
	 */
	public void getTicketDetails(final Prediction p)
	{
		if (p == null) return;
		
		String requestURL = serverURL + "action=23" + "&id=" + p.ticketId;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {			

			protected Boolean processXML(Document rxXMLd)
			{
				Element rxXML = rxXMLd.getDocumentElement();
				if (!rxXML.getTagName().equals("t")) {
					setError(UNEXPECTED_RESPONSE, "getTicketDetails, expected \'t\' packet, got "+rxXML.getTagName());
					return false;
				}
				ArrayList<PredictionLine> a = new ArrayList<PredictionLine>();
				NodeList lineListXML;
				try {
					lineListXML = (NodeList) xpe("//pml", rxXML, XPathConstants.NODESET);
				} catch (XPathExpressionException e1) {
					return false;
				}
				for (int i=0; i<lineListXML.getLength(); i++) {
					Element n = (Element)lineListXML.item(i);
					PredictionLine pl = processLineXML(n);
					if (pl == null) {
						return false;
					}
					a.add(pl);
				}
				p.details = a;
				ticketDetailsPrediction = p;
				return true;
			}
			
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getTicketDetails, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processXML(rxXML)) {
						dispatchEvent(SERVER_TICKET_DETAILS_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad response, processing getTicketDetails xml response: "+lastErrorMessage);
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}

	public void getLastPayout(String uid)
	{
		String requestURL = serverURL + "action=17&u=" + (uid == null ? playerID : uid) + "&gc=" + gameConfig;

		HttpGet request = new HttpGet(requestURL);
		request.setHeader("User-Agent", sUserAgent);
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "getLastPayout, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML) && processLastPayoutXML(rxXML)) {
						dispatchEvent(SERVER_LAST_PAYOUT_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing getLastPayout xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request});   
	}

	/**
	 * @param username
	 * @param password
	 */
	public Boolean login(String username, String password)
	{
		if (wnsServerURL == null || wnsServerURL.equals("")) return false;
		Log.d("login", username+" and "+password);
		HttpPost request = new HttpPost(wnsServerURL);
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("action", "509"));
		nameValuePairs.add(new BasicNameValuePair("xml", "1"));
		nameValuePairs.add(new BasicNameValuePair("j_username", username));
		nameValuePairs.add(new BasicNameValuePair("j_password", password));
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			setError(GENERIC_ERROR, "login UnsupportedEncodingException");
			e.printStackTrace();
		}
		
		XMLSlurper loader = new XMLSlurper() {
			Boolean successfulLoginXML(Element rxXML)
			{
				Log.d("login", rxXML.getTagName()+" ... "+rxXML.getTextContent());
				if (rxXML.getTagName().equals("auth_error")) {
					setError(LOGIN_ERROR, rxXML.getTextContent() );
					return false;
				}
				if (rxXML.getTagName().equals("auth")) {
					return true;
				}
				setError(TS_NO_SESSION, "Unexpected Response, processing login xml response: " );
				return false;
			}
			
			@Override
			protected void onPostExecute(Document rxXMLd)
			{
				super.onPostExecute(rxXMLd);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "login, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXMLd)) {
						if (successfulLoginXML(rxXMLd.getDocumentElement())) {
							dispatchEvent(SERVER_LOGIN_EVENT);
						} else {
							dispatchEvent(SERVER_ERROR_EVENT);				
						}
					} else {
						setError(TS_NO_SESSION, "Bad Response, processing login xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request}); 
		return true;
	}
	
	/**
	 * 
	 */
	public Boolean logout()
	{
		if (wnsServerURL == null || wnsServerURL.equals("")) return false;
		HttpPost request = new HttpPost(wnsServerURL);
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("action", "500"));
		nameValuePairs.add(new BasicNameValuePair("xml", "1"));
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			setError(GENERIC_ERROR, "logout UnsupportedEncodingException");
			e.printStackTrace();
		}
		
		XMLSlurper loader = new XMLSlurper() {
			@Override
			protected void onPostExecute(Document rxXML)
			{
				super.onPostExecute(rxXML);
				if (shitHappened != null) {
					setError(XMLP_PARSE_FAIL, "login, error processing xml response: " + shitHappened.getMessage());
					dispatchEvent(SERVER_ERROR_EVENT);		
				} else {
					if (!errorXML(rxXML)) {
						dispatchEvent(SERVER_LOGOUT_EVENT);
					} else {
						setError(XMLP_PARSE_FAIL, "Bad Response, processing login xml response: " );
						dispatchEvent(SERVER_ERROR_EVENT);				
					}
				}
			}
		};
		loader.execute(new HttpUriRequest[] {request}); 
		return true;
	}
}
