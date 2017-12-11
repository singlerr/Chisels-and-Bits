package mod.chiselsandbits.share.output;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.localization.LocalStrings;
import mod.chiselsandbits.localization.LocalizedMessage;
import net.minecraft.client.gui.GuiScreen;

public class PasteBin implements IShareOutput
{
	byte[] data;
	String url;

	public PasteBin()
	{

	}

	@Override
	public LocalizedMessage handleOutput(
			final byte[] compressedData,
			final BufferedImage screenshot ) throws UnsupportedEncodingException, IOException
	{
		String UTF8 = java.nio.charset.StandardCharsets.UTF_8.toString();
		String dataStr = ClipboardText.getShareString( compressedData );
		data = dataStr.getBytes( UTF8 );

		String session = "";

		if ( hasValue( ChiselsAndBits.getConfig().pasteBinUsername ) && hasValue( ChiselsAndBits.getConfig().pasteBinPassword ) )
			session = login( ChiselsAndBits.getConfig().pasteBinUsername, ChiselsAndBits.getConfig().pasteBinPassword );

		// JSONP based loading.
		url = paste( "loadcallback(\"" + dataStr + "\");", session );

		String[] parts = url.split( "/" );
		String fileName = parts[parts.length - 1];

		// append sharing preview url.
		GuiScreen.setClipboardString( url + "\n" + "View preview at http://algorithmx2.github.io/Chisels-and-Bits-Sharing/viewer/#" + fileName );

		return new LocalizedMessage( LocalStrings.ShareURL, url );
	}

	private boolean hasValue(
			String val )
	{
		return val != null && val.length() > 0;
	}

	private String login(
			String api_user_name,
			String api_user_password ) throws MalformedURLException, IOException
	{
		String UTF8 = java.nio.charset.StandardCharsets.UTF_8.toString();
		HttpsURLConnection connection = (HttpsURLConnection) ( new URL( "https://pastebin.com/api/api_login.php" ) ).openConnection();

		StringBuilder encoded = new StringBuilder();
		encoded.append( "api_dev_key=" );
		encoded.append( URLEncoder.encode( ChiselsAndBits.getConfig().pasteBinAPIKey, UTF8 ) );
		encoded.append( "&api_user_name=" );
		encoded.append( URLEncoder.encode( api_user_name, UTF8 ) );
		encoded.append( "&api_user_password=" );
		encoded.append( URLEncoder.encode( api_user_password, UTF8 ) );

		connection.setDoOutput( true );
		connection.setDoInput( true );
		connection.setRequestMethod( "POST" );

		OutputStream o = connection.getOutputStream();
		o.write( encoded.toString().getBytes( UTF8 ) );
		o.flush();
		o.close();

		int code = connection.getResponseCode();

		if ( code == 200 )
			return readInputStream( connection.getInputStream() );

		throw new IOException( "Bad Request" );
	}

	private String paste(
			String dataStr,
			String session ) throws MalformedURLException, IOException
	{
		String UTF8 = java.nio.charset.StandardCharsets.UTF_8.toString();
		HttpsURLConnection connection = (HttpsURLConnection) ( new URL( "https://pastebin.com/api/api_post.php" ) ).openConnection();

		StringBuilder encoded = new StringBuilder();
		encoded.append( "api_dev_key=" );
		encoded.append( URLEncoder.encode( ChiselsAndBits.getConfig().pasteBinAPIKey, UTF8 ) );
		encoded.append( "&api_paste_code=" );
		encoded.append( URLEncoder.encode( dataStr, UTF8 ) );
		encoded.append( "&api_paste_private=" );
		encoded.append( URLEncoder.encode( ChiselsAndBits.getConfig().pasteBinPrivacyMode.value, UTF8 ) );
		encoded.append( "&api_option=" );
		encoded.append( URLEncoder.encode( "paste", UTF8 ) );
		encoded.append( "&api_user_key=" );
		encoded.append( URLEncoder.encode( session, UTF8 ) );

		connection.setDoOutput( true );
		connection.setDoInput( true );
		connection.setRequestMethod( "POST" );

		OutputStream o = connection.getOutputStream();
		o.write( encoded.toString().getBytes( UTF8 ) );
		o.flush();
		o.close();

		int code = connection.getResponseCode();

		if ( code == 200 )
			return readInputStream( connection.getInputStream() );

		throw new IOException( "Bad Request" );
	}

	private String readInputStream(
			InputStream inputStream ) throws IOException
	{
		BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ( ( inputLine = in.readLine() ) != null )
		{
			response.append( inputLine );
		}

		in.close();

		String out = response.toString();

		if ( out.indexOf( "Bad API request" ) != -1 )
		{
			throw new IOException( out );
		}

		return out;
	}

	@Override
	public BlueprintData getData()
	{
		BlueprintData bpd = new BlueprintData( null );

		try
		{
			bpd.setURLSource( new URL( url ) );
			bpd.loadData( data );
		}
		catch ( IOException e )
		{

		}

		return bpd;
	}

}
