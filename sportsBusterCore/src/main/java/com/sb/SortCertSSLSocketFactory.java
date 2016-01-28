package com.sb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

/**
 * maladapted from EasyX509TrustManager
 * maladapted from EasySSLSocketFactory
 * http://stackoverflow.com/questions/4115101/apache-httpclient-on-android-producing-certpathvalidatorexception-issuername
 */
public class SortCertSSLSocketFactory implements SocketFactory, LayeredSocketFactory 
{	
    public static String hexify (byte bytes[])
    {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
                        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buf = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
                buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }
        return buf.toString();
    }
    
	public static String getThumbPrint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException
	{
		StringBuffer s = new StringBuffer();
		s.append(cert.getSerialNumber().toString());
		s.append(" Subject(");
		s.append(cert.getSubjectDN().toString());
		s.append(") Issuer(");
		s.append(cert.getIssuerDN().toString());
		s.append(") ");
		try {
			cert.checkValidity();
			s.append(" valid ");
		} catch (CertificateExpiredException e) {
			s.append(" expired ");
		} catch (CertificateNotYetValidException e) {
			s.append(" early ");
		}
	    MessageDigest md = MessageDigest.getInstance("SHA-1");
	    byte[] der = cert.getEncoded();
	    md.update(der);
	    byte[] digest = md.digest();
	    s.append(hexify(digest));
	    return s.toString();
	}
	
	public class SortCertX509TrustManager implements X509TrustManager 
	{	
		private X509TrustManager standardTrustManager = null;

		/** 
	     * Constructor for SortCertX509TrustManager. 
	     */  
	    public SortCertX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException 
	    {  
	    	super();  
	    	TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
	    	factory.init(keystore);  
	    	TrustManager[] trustmanagers = factory.getTrustManagers();  
	    	if (trustmanagers.length == 0) {	
				throw new NoSuchAlgorithmException("no trust manager found");	
			}
	    	Log.d("sortsockettrust", "got trust manager "+Integer.toString(trustmanagers.length));
			this.standardTrustManager = (X509TrustManager) trustmanagers[0];	
		}	

		/** 
		 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType) 
		 */	
		public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException 
		{	
			standardTrustManager.checkClientTrusted(certificates, authType);	
		}	

		/** 
		 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType) 
		 */	
		public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException 
		{	
			// Clean up the certificates chain and build a new one.
			// Theoretically, we shouldn't have to do this, but various web servers
			// in practice are mis-configured to have out-of-order certificates or
			// expired self-issued root certificate.
			int chainLength = certificates.length;
			Log.d("sortsockettrust", "check server trusted, "+Integer.toString(chainLength)+" certificates "+(authType!=null?authType:"null"));
			if (certificates.length > 1) {
				// 1. we clean the received certificates chain.
				// We start from the end-entity certificate, tracing down by matching
				// the "issuer" field and "subject" field until we can't continue.
				// This helps when the certificates are out of order or
				// some certificates are not related to the site.
				int currIndex;
				for (currIndex = 0; currIndex < certificates.length; ++currIndex) {
					try {
						Log.d("sortsockettrust", "cert "+Integer.toString(currIndex)+", "+getThumbPrint(certificates[currIndex]));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						break;
					}
					boolean foundNext = false;
					for (int nextIndex = currIndex + 1;
									 nextIndex < certificates.length;
									 ++nextIndex) {
						if (certificates[currIndex].getIssuerDN().equals(
										certificates[nextIndex].getSubjectDN())) {
							foundNext = true;
							// Exchange certificates so that 0 through currIndex + 1 are in proper order
							if (nextIndex != currIndex + 1) {
								X509Certificate tempCertificate = certificates[nextIndex];
								certificates[nextIndex] = certificates[currIndex + 1];
								certificates[currIndex + 1] = tempCertificate;
							}
							break;
						}
					}
					if (!foundNext) break;
				}

				// 2. we exam if the last traced certificate is self issued and it is expired.
				// If so, we drop it and pass the rest to checkServerTrusted(), hoping we might
				// have a similar but unexpired trusted root.
				chainLength = currIndex + 1;
				X509Certificate lastCertificate = certificates[chainLength - 1];
				Date now = new Date();
				if (lastCertificate.getSubjectDN().equals(lastCertificate.getIssuerDN())
						&& now.after(lastCertificate.getNotAfter())) {
					--chainLength;
				}
			}

			Log.d("sortsockettrust", "sort done, now "+Integer.toString(chainLength)+" certificates "+(authType!=null?authType:"null"));
			for (X509Certificate c: certificates) {
				try {
					Log.d("sortsockettrust", getThumbPrint(c));
				} catch (NoSuchAlgorithmException e) {
					Log.d("sortsockettrust", "no algorithm");
				}
			}
	        try {
	        	standardTrustManager.checkServerTrusted(certificates, authType);	
	        } catch (CertificateException e) {
//	            sslSocket.getSession().invalidate();
	        	
	            Log.d("sortsockettrust",
	                    "certificate error: " + e.getMessage());
	            if (!desperationCheckChain(certificates, authType)) {
		            throw new CertificateException(e);
	            }
	        }
			Log.d("sortsockettrust", "trusted!!");
		}	

		private boolean desperationCheckChain(
				X509Certificate[] certificates,
				String authType) throws CertificateException
		{
//            Log.d("sortsockettrust", "desperation chain check");
			for (X509Certificate c: certificates) {
				try {
					if (!desperationCheckCert(c, authType)) {
						return false;
					}
				} catch (CertificateException e) {
					Log.d("sortsockettrust", "got exception in cert check "+e.getMessage());
					throw new CertificateException(e);
				}
			}
			return true;
		}

		private boolean desperationCheckCert(X509Certificate c, String authType) throws CertificateException
		{
			if (c == null) return false;
			String issuerDN = c.getIssuerX500Principal().getName();
			String subjectDN = c.getSubjectX500Principal().getName();
//	        Log.d("sortsockettrust", "desperation chain cert "+issuerDN+" || "+subjectDN);
			if (issuerDN.equals(subjectDN)) {
				try {
					c.verify(c.getPublicKey());
				} catch (InvalidKeyException e) {
					throw new CertificateException("InvalidKeyException on self-signed certificate \""+subjectDN+"\"");
				} catch (CertificateException e) {
					throw new CertificateException("CertificateException on self-signed certificate \""+subjectDN+"\"");
				} catch (NoSuchAlgorithmException e) {
					throw new CertificateException("NoSuchAlgorithmException on self-signed certificate \""+subjectDN+"\"");
				} catch (NoSuchProviderException e) {
					throw new CertificateException("NoSuchProviderException on self-signed certificate \""+subjectDN+"\"");
				} catch (SignatureException e) {
					throw new CertificateException("SignatureException on self-signed certificate \""+subjectDN+"\"");
				}
				return true;
			} else {
				X509Certificate issuerCert = getKeystoreCert(issuerDN);
				Log.d("sortsockettrust", "got issuer cert");
				if (issuerCert == null) {
					throw new CertificateException("Can't find keystore cert for \""+issuerDN+"\"");
				}
				try {
					c.verify(issuerCert.getPublicKey());
				} catch (InvalidKeyException e) {
					throw new CertificateException("InvalidKeyException on certificate \""+subjectDN+"\" signed by \""+issuerDN+"\"");
				} catch (CertificateException e) {
					throw new CertificateException("CertificateException on certificate \""+subjectDN+"\" signed by \""+issuerDN+"\"");
				} catch (NoSuchAlgorithmException e) {
					throw new CertificateException("NoSuchAlgorithmException on certificate \""+subjectDN+"\" signed by \""+issuerDN+"\"");
				} catch (NoSuchProviderException e) {
					throw new CertificateException("NoSuchProviderException on certificate \""+subjectDN+"\" signed by \""+issuerDN+"\"");
				} catch (SignatureException e) {
					throw new CertificateException("SignatureException on certificate \""+subjectDN+"\" signed by \""+issuerDN+"\"");
				}
				return true;
			}
		}

		private X509Certificate getKeystoreCert(String subjectDN)
		{
			Enumeration<String> keystoreIndex = null;	
			try {
				keystoreIndex = keystore.aliases();
			} catch (KeyStoreException e) {
				return null;
			}
//			Log.d("sortsockettrust", "look for keystore cert "+subjectDN);
			while (keystoreIndex.hasMoreElements()) {
				String alias = keystoreIndex.nextElement();
				Log.d("sortsockettrust", "next alias "+alias);
				X509Certificate c = null;
				try {
					c = (X509Certificate) keystore.getCertificate(alias);
				} catch (KeyStoreException e) {
					return null;
				}
//				Log.d("sortsockettrust", "got keystore cert "+c.getSubjectX500Principal().getName());
				if (c.getSubjectX500Principal().getName().equals(subjectDN)) {
					return c;
				}
			}
			return null;
		}

		/** 
		 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers() 
		 */	
		public X509Certificate[] getAcceptedIssuers() 
		{	
			return this.standardTrustManager.getAcceptedIssuers();	
		}	
	}	

	private SSLContext sslcontext = null;	
	private KeyStore keystore = null;
	
	public SortCertSSLSocketFactory(KeyStore trusted)
	{
		keystore = trusted;
	}

	private SSLContext createSSLContext() throws IOException 
	{	
		try {	
			SSLContext context = SSLContext.getInstance("TLS");	
			context.init(null, new TrustManager[] { new SortCertX509TrustManager(keystore) }, null);	
			return context;	
		}
		catch (Exception e) {	
			throw new IOException(e.getMessage());	
		}	
	}	

	private SSLContext getSSLContext() throws IOException 
	{	
		if (this.sslcontext == null) {	
			this.sslcontext = createSSLContext();	
		}	
		return this.sslcontext;	
	}	

	/** 
	 * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket, java.lang.String, int, 
	 *		java.net.InetAddress, int, org.apache.http.params.HttpParams) 
	 */	
	public Socket connectSocket(Socket sock,
									String host,
									int port, 
									InetAddress localAddress,
									int localPort,
									HttpParams params) 

				throws IOException, UnknownHostException, ConnectTimeoutException 
	{	
		Log.d("sortsockettrust", "connect socket "+Integer.toString(port)+", "+Integer.toString(localPort)+", "+(host!=null?host:"null host"));
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params);	
		int soTimeout = HttpConnectionParams.getSoTimeout(params);	
		InetSocketAddress remoteAddress = new InetSocketAddress(host, port);	
		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());	
	
		if ((localAddress != null) || (localPort > 0)) {	
			// we need to bind explicitly	
			if (localPort < 0) {	
				localPort = 0; // indicates "any"	
			}	
			InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);	
			sslsock.bind(isa);	
		}	

		sslsock.connect(remoteAddress, connTimeout);	
		sslsock.setSoTimeout(soTimeout);	
		return sslsock;	
	}	

	/** 
	 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket) 
	 */	
	public boolean isSecure(Socket socket) throws IllegalArgumentException
	{	
		return true;	
	}	

	/** 
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket() 
	 */	
	public Socket createSocket() throws IOException
	{	
		Log.d("sortsockettrust", "createSocket()");
		return getSSLContext().getSocketFactory().createSocket();	
	}	

	/** 
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket, java.lang.String, int, 
	 *		boolean) 
	 */	
	public Socket createSocket(Socket socket,
									 String host, 
									 int port,
									 boolean autoClose) throws IOException,	
															 UnknownHostException 
	{	
		Log.d("sortsockettrust", "createSocket "+Integer.toString(port)+", "+(host!=null?host:"null host"));
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);	
	}	

	// -------------------------------------------------------------------	
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :	
	// Both Object.equals() and Object.hashCode() must be overridden	
	// for the correct operation of some connection managers	
	// -------------------------------------------------------------------	

	public boolean equals(Object obj) {	
		return ((obj != null) && obj.getClass().equals(SortCertSSLSocketFactory.class));	
	}	

	public int hashCode() {	
		return SortCertSSLSocketFactory.class.hashCode();	
	}
}
