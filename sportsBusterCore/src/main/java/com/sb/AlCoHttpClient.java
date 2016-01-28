package com.sb;

import android.R;
import android.content.Context;
import android.util.Log;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
/**
 * @author dak
 *	ugly hack to fix android certificate problem
 * @see <a href="http://blog.antoine.li/2010/10/22/android-trusting-ssl-certificates/">blog.antoine.li</a>
 * @see <a href="http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html">crazy bob</a>
 */
public class AlCoHttpClient extends DefaultHttpClient
{
	final Context context;
	final int keystoreId;
	final String keystorePass;

	public AlCoHttpClient(Context context, int keystoreId, String keystorePass)
	{
		this.context = context;
		this.keystoreId = keystoreId;
		this.keystorePass = keystorePass;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager()
	{
		Log.d("alco http", "in createClientConnectionManager{}");
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", newSSLSocketFactory(), 443));
		return new ThreadSafeClientConnManager(getParams(), registry);
	}

	private SocketFactory newSSLSocketFactory()
	{
		Log.d("alco http", "in newSslSocketFactory");
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(keystoreId);
			try {
				trusted.load(in, keystorePass.toCharArray());
			} finally {
				in.close();
			}
			SortCertSSLSocketFactory sf = new SortCertSSLSocketFactory(trusted);
			// Hostname verification from certificate
			// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
// the sort certing ssl socket factory doesn't do any hostname verification
//			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
			return sf;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}