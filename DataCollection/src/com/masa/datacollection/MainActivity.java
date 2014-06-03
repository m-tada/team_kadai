package com.masa.datacollection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnInitListener{
	private TextToSpeech tts;
	private WebView varWebView;
	private TextView mView;
	static private String mArticleTitle[];
	static private String mArticleURL[];
	static private String mArticleOverview[];
	static private int mArticleNum;
	static final private String prefName = "MY_PREF";
	private int mScreenId = R.layout.activity_main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tts = new TextToSpeech(getApplicationContext(), this);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

		SharedPreferences prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE);
		int screenId = prefs.getInt("screenId", R.layout.activity_main);
		setScreenContent(screenId);
	}

	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	    SharedPreferences prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putInt("screenId", mScreenId);
	    editor.commit();
	  }

	@Override
	public void onInit(int status){

	}
	private void setScreenContent(int screenId) {
	    mScreenId = screenId;
	    setContentView(screenId);

	    switch (screenId) {
	    	case R.layout.activity_main: {
	    		setFirstScreenContent();
	    		break;
	    	}
	    	case R.layout.webview: {
	    		setSecondScreenContent();
	    		break;
	    	}
	    }
	}


	  private void setFirstScreenContent() {
		   getArticle(createURL());
			ListView list = (ListView)findViewById(R.id.ListView01);
			ArrayList<ListItem> arrayList = new ArrayList<ListItem>();
			for(int i=0;i< mArticleNum;i++){
				arrayList.add(new ListItem(mArticleTitle[i],mArticleURL[i],mArticleOverview[i]));
			}
			list.setAdapter(new ListArrayAdapter(this,arrayList));
	  }

	    private void setSecondScreenContent() {
			varWebView =(WebView)findViewById(R.id.webview);
			varWebView.setWebViewClient(new CustomBrowserClient());
			varWebView.getSettings().setJavaScriptEnabled(true);
	    }

	public String createURL(){
		String apiURL = "http://news.yahooapis.jp/NewsWebService/V2/topics?";
		String appid = "dj0zaiZpPTU3VEFwYzNYYThSQSZzPWNvbnN1bWVyc2VjcmV0Jng9NWQ-";
		String category ="top";
		return String.format("%sappid=%s&pickupcategory=%s", apiURL, appid,category);
	}


	public static String httpGet(String strURL){
		try{
			URL url=new URL(strURL);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			InputStream stream = connection.getInputStream();
			readXML(stream);
			String data="";
			for(int i=0;i<mArticleNum;i++){
				data+=mArticleTitle[i];
			}
			stream.close();
			return data;

		}catch(Exception e){
			return e.toString();
		}
	}

	public static void readXML(InputStream stream) throws XmlPullParserException{
			try{
				XmlPullParser myxmlPullParser = Xml.newPullParser();
				myxmlPullParser.setInput(stream,"UTF-8");

				int cntTitle = 0;
				int cntAddress = 0;
				int cntOverview = 0;
				for(int e = myxmlPullParser.getEventType(); e!= XmlPullParser.END_DOCUMENT;
						e = myxmlPullParser.next()){

					if (e == XmlPullParser.START_TAG) {
		                  if (myxmlPullParser.getName().equals("ResultSet")) {
		                      mArticleNum = Integer.parseInt(myxmlPullParser.getAttributeValue(null, "totalResultsReturned"));
		                      mArticleTitle = new String[mArticleNum];
		                      mArticleURL = new String[mArticleNum];
		                      mArticleOverview = new String[mArticleNum];
		                  } else if (myxmlPullParser.getName().equals("Title")) {
		                      mArticleTitle[cntTitle] = myxmlPullParser.nextText();
		                      cntTitle++;
		                  } else if (myxmlPullParser.getName().equals("Overview")) {
		                      mArticleOverview[cntOverview] = myxmlPullParser.nextText();
		                      cntOverview++;
		                  } else if (myxmlPullParser.getName().equals("SmartphoneUrl")) {
		                      mArticleURL[cntAddress] = myxmlPullParser.nextText();
		                      cntAddress++;
		                  }
		               }
		           }
		     } catch (XmlPullParserException e) {
		    	 e.printStackTrace();
		     } catch (IOException e) {
		    	 e.printStackTrace();
		  }
	}

	public static void getArticle(String strURL){
		try{
			URL url=new URL(strURL);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			InputStream stream = connection.getInputStream();
			readXML(stream);
			stream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public class ListItem{
		public String title;
		public String url;
		public String overview;

		public ListItem(String title, String url, String overview){
			this.title=title;
			this.url=url;
			this.overview=overview;
		}
	}

	private class CustomBrowserClient extends WebViewClient{
		public boolean shoudOverrideUrlLoading(WebView view,String url){
			view.loadUrl(url);
			return true;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

		for(int i=0;i < mArticleNum ;i++){
			menu.add(0,i,i,mArticleTitle[i]);
		}
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		int iid=item.getItemId();
		setScreenContent(R.layout.webview);
		for(int i=0;i < mArticleNum ;i++){
			if(i==iid){
				varWebView.loadUrl(mArticleURL[i]);
			}
		}
		return true;
	}
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if ((keyCode==KeyEvent.KEYCODE_BACK)&&varWebView.canGoBack()){
			varWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}



	public class ListArrayAdapter extends ArrayAdapter<ListItem> implements View.OnClickListener {
		private ArrayList<ListItem> listItem;
		public ListArrayAdapter(Context context, ArrayList<ListItem> listItem){
			super(context,R.layout.rowitem,listItem);
			this.listItem=listItem;
		}


		@Override
		public View getView(int positopn,View view, ViewGroup parent){
			ListItem item = listItem.get(positopn);
			Context context=getContext();
			LinearLayout linearLayout=new LinearLayout(context);
			view=linearLayout;
			TextView textView= new TextView(context);
			textView.setText(item.title);
			linearLayout.addView(textView);
			Button button=new Button(context);
			button.setText("詳細");
			button.setTag(String.valueOf(positopn));
			button.setOnClickListener(this);
			linearLayout.addView(button,0);

			return view;
		}


		public void onClick(View view){
			int tag = Integer.parseInt((String)view.getTag());
			ListItem item = listItem.get(tag);
			tts.speak(item.overview ,TextToSpeech.QUEUE_FLUSH,null);
		}
	}
}

