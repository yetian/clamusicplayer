package org.cnloli.cnlolimusicplayer2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.cnloli.cnlolimusicplayer2.utils.PlayListParser;

import android.R.drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.renderscript.Program.TextureType;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

	// music list
	protected String[][] musicList;
	protected int totalNumberOfMusic;
	protected List<Map<String, Object>> listMap;
	protected int currentid;
	protected int selectedid;
	protected String songurl;
	protected ProgressDialog pDialog;
	protected SimpleAdapter sa;
	
	// user interface
	protected ViewFlipper viewFlipper;
	protected Button buttonCloseList;
	protected Button buttonOpenList;
	protected ListView list;
	protected TextView mainTitle;
	protected TextView mainArtist;
	protected TextView mainTime;
	protected ImageView albumPic;
	protected ProgressBar mainProgressBar;
	protected ImageButton buttonNext;
	protected ImageButton buttonPlay;
	protected ImageButton buttonPre;
	
	// Player
	protected MediaPlayer mp;
	protected MediaMetadataRetriever metaRetriever;
	protected byte[] songCover;
	protected int totalLengthofSong;
	protected String totalLengthofSongInTime;
	protected boolean isPlaying;
	protected boolean isPlayed;
	protected AlertDialog alertDialogMusicNotAvailable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        
        initplayer();
        
        
	}
	
	private void initplayer()
	{
		setContentView(R.layout.activity_main);
		
		// initialize
		viewFlipper 		= (ViewFlipper) findViewById(R.id.ViewFlipperMain);
		buttonCloseList 	= (Button) findViewById(R.id.buttonCloseList);
		buttonOpenList 		= (Button) findViewById(R.id.buttonOpenList);
		list 				= (ListView) findViewById(R.id.listViewMList);
		mainTitle 			= (TextView) findViewById(R.id.textViewTitle);
		mainArtist 			= (TextView) findViewById(R.id.textViewAuthor);
		albumPic 			= (ImageView) findViewById(R.id.imageViewAlbum); 
		mainProgressBar 	= (ProgressBar) findViewById(R.id.progressBar1);
		buttonNext 			= (ImageButton) findViewById(R.id.imageButtonNext);
		buttonPlay 			= (ImageButton) findViewById(R.id.imageButtonPlay);
		buttonPre 			= (ImageButton) findViewById(R.id.imageButtonPre);
		mainTime			= (TextView) findViewById(R.id.textViewTime);
		
		// player
		mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        metaRetriever = new MediaMetadataRetriever();
				
				
		listMap = new ArrayList<Map<String, Object>>();
		
		// variables
		totalLengthofSong = 0;
		totalNumberOfMusic = 0;
		currentid = -1;
		selectedid = -1;
		songurl = "";
		isPlaying = false;
		isPlayed = false;
		
		// set animation for viewFlipper
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		viewFlipper.setFlipInterval(2);
		// open list activity 1=list 0=main after loading list.
		// code has been moved to the down part
		
		// set default max height and width for album
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//		int scrheight = displaymetrics.heightPixels;
		int scrwidth = displaymetrics.widthPixels;
		
		albumPic.setMaxWidth((int) (scrwidth * 0.6));
		
		// set button actions
		
		// close play list
		buttonCloseList.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				viewFlipper.setDisplayedChild(0);
			}
		});
		
		// open play list
		buttonOpenList.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				viewFlipper.setDisplayedChild(1);
			}
		});
		
		// play/pause music
		buttonPlay.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{	
				if(isPlaying == false)
				{
					buttonPlay.setImageResource(drawable.ic_media_pause);
					
					// start play thread - only play the current one
					if(isPlayed == false)
					{
						currentid = selectedid;
						playMusic();
					}
					else
					{
						if(selectedid != currentid)
						{
							currentid = selectedid;
							playMusic();
						}
						else
						{
							mp.start();
							isPlaying = true;
						}
						
					}
					
				}
				else
				{
					buttonPlay.setImageResource(drawable.ic_media_play);
					isPlaying = false;
					mp.pause();
				}
			}
		});
		
		buttonPre.setOnClickListener(new android.view.View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				selectedid --;
				if(selectedid < 0)
					selectedid = 0;

				if(isPlaying)
				{
					currentid = selectedid;
					playMusic();
				}
				else
				{
					changeDisplayArea(selectedid);
				}
			}
		});
		
		buttonNext.setOnClickListener(new android.view.View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				selectedid ++;
				
				if(selectedid > totalNumberOfMusic - 1)
					selectedid = totalNumberOfMusic - 1;

				if(isPlaying)
				{
					currentid = selectedid;
					playMusic();
				}
				else
				{
					changeDisplayArea(selectedid);
				}
			}
		});
		
		// music player behavior
		// continuous updating
		mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() 
		{
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) 
			{
				if(mp.isPlaying())
				{
					int currentPosition = mp.getCurrentPosition();
					mainProgressBar.setProgress(currentPosition);
					
					String currentTime = String.format("%02d:%02d", 
							    TimeUnit.MILLISECONDS.toMinutes(currentPosition),
							    TimeUnit.MILLISECONDS.toSeconds(currentPosition) - 
							    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
					
					mainTime.setText(currentTime + "/" + totalLengthofSongInTime); 
				}
				else
				{
					if(selectedid != currentid)
					{
						mainProgressBar.setProgress(0);
						mainTime.setText("00:00/00:00");
					}
				}
			}
		});
		
		// when complete
        mp.setOnCompletionListener(new OnCompletionListener() 
        {	
			@Override
			public void onCompletion(MediaPlayer mp) 
			{
			if(isPlaying && totalNumberOfMusic != 0)
				{
					if(currentid < totalNumberOfMusic -1)
					{
						currentid ++;
						playMusic();
					}
					else
					{
						buttonPlay.setImageResource(drawable.ic_media_play);
						isPlaying = false;
					}
				}
			}
		});
		
		// initialize unable to play alert
		alertDialogMusicNotAvailable = new AlertDialog.Builder(MainActivity.this).create();
		alertDialogMusicNotAvailable.setMessage("无法播放这首歌,服务器傲娇了.\n请稍后再试哦~");
		alertDialogMusicNotAvailable.setTitle("哎呀呀");
		
		alertDialogMusicNotAvailable.setButton("嘛~好吧", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				alertDialogMusicNotAvailable.dismiss();
			}
		});
		
		// load music list - no more password required.
		pDialog = ProgressDialog.show(MainActivity.this, "请稍后...", "努力加载播放列表中...",true) ;
		LoadPlayListThread loadList = new LoadPlayListThread();
		loadList.start();
	}
	
	private void playMusic()
	{
		selectedid = currentid;
		String tmp = musicList[currentid][1];
		final String title;
        final String artist;
            
		if(tmp.indexOf(" - ") != -1)
    	{
    		title = tmp.substring(0, tmp.indexOf(" - "));
        	artist = tmp.substring(tmp.indexOf(" - ")+3);
    	}
    	else
    	{
    		title = musicList[currentid][1];
    		artist = "?";
    	}
		
		mainArtist.setEllipsize(TruncateAt.END);
		mainArtist.setMaxLines(2);
		mainArtist.setText(artist);
		mainTitle.setEllipsize(TruncateAt.END);
		mainTitle.setMaxLines(1);
		mainTitle.setText(String.valueOf(currentid+1) + "." +title);
		mainProgressBar.setProgress(0);
		
		songurl = musicList[currentid][0];
		
		if(isPlayed)
			mp.reset();
		
		pDialog = ProgressDialog.show(MainActivity.this, "请稍后...", "正在加载音乐\n"+title, true);
		pDialog.setCancelable(true);
		
		MusicPlayThread mpt = new MusicPlayThread();
		mpt.start();
	}
	
	private void changeDisplayArea(int id)
	{
		String tmp = musicList[id][1];
		final String title;
        final String artist;
            
		if(tmp.indexOf(" - ") != -1)
    	{
    		title = tmp.substring(0, tmp.indexOf(" - "));
        	artist = tmp.substring(tmp.indexOf(" - ")+3);
    	}
    	else
    	{
    		title = musicList[id][1];
    		artist = "?";
    	}
		
		mainArtist.setEllipsize(TruncateAt.END);
		mainArtist.setMaxLines(2);
		mainArtist.setText(artist);
		mainTitle.setEllipsize(TruncateAt.END);
		mainTitle.setMaxLines(1);
		mainTitle.setText(String.valueOf(id+1) + "." +title);
		mainProgressBar.setProgress(0);	
	}
	
	@Override
    protected void onDestroy() 
	{
    	super.onDestroy();
//    	lock.reenableKeyguard();
    	if(isPlayed)
    		mp.reset();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int itemID = item.getItemId();

		if(itemID == R.id.mlist)
		{
			// switch to list menu
			viewFlipper.setDisplayedChild(1);			
		}
		else if(itemID == R.id.about)
		{
			viewFlipper.setDisplayedChild(2);
		}
		
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		// key back
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(viewFlipper.getDisplayedChild() != 0)
			{
				viewFlipper.setDisplayedChild(0);
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				builder.setMessage("要离开了么? \n按 Home 键可以让萌音在线继续在后台播放哦~");
				builder.setTitle("诶呀~");
				
				builder.setPositiveButton("是呀", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						MainActivity.this.finish();
						dialog.dismiss();
					}
				});
				
				builder.setNegativeButton("继续听", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
					}
				});

				builder.create().show();
			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("HandlerLeak")
	// load play list thread
	class LoadPlayListThread extends Thread
	{
		@Override
		public void run() 
		{
			try
			{
				PlayListParser plp = new PlayListParser();
				musicList = plp.getPlayList();
				totalNumberOfMusic = musicList.length;
				
				if(totalNumberOfMusic <= 0)
				{
					myhandler.sendEmptyMessage(1);
				}
				else
				{
					String title, artist, tmp;
					
					for(int i=0; i< musicList.length; i++)
					{
						tmp = musicList[i][1];
						Map<String, Object> map = new HashMap<String, Object>();
						
						// add image to the map
						map.put("listItemImage", String.valueOf(org.cnloli.cnlolimusicplayer2.R.drawable.picn));
						
						if(tmp.indexOf(" - ") != -1)
                    	{
                    		title = String.valueOf(i+1)+". "+tmp.substring(0, tmp.indexOf(" - "));
                        	artist = tmp.substring(tmp.indexOf(" - ")+3);

                        	map.put("title", title);
                        	map.put("artist", artist);
                    	}
                    	else
                    	{
                    		title = String.valueOf(i+1)+". "+musicList[i][1];
                    		artist = "?";
                    		map.put("title", title);
                        	map.put("artist", artist);
                    	}
						
						listMap.add(map);
					}
					myhandler.sendEmptyMessage(0);
				}
			}
			catch(Exception e)
			{
				myhandler.sendEmptyMessage(1);
			}
			
		}
		
		private Handler myhandler = new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				// dismiss process dialog
				pDialog.dismiss();
				
				// success
				if(msg.what == 0)
				{	
					// set list adapter
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() 
						{
							sa = new SimpleAdapter(MainActivity.this, listMap, 
									org.cnloli.cnlolimusicplayer2.R.layout.list_item,
									new String[] {"listItemImage", "title", "artist"},
									new int[] {
												org.cnloli.cnlolimusicplayer2.R.id.imageViewList,
												org.cnloli.cnlolimusicplayer2.R.id.textViewLTitle,
												org.cnloli.cnlolimusicplayer2.R.id.textViewLAuthor
											});
						}
					});

					list.setAdapter(sa);
					sa.notifyDataSetChanged();
					
					currentid = 0;
					selectedid = 0;
					
					// initialize first item after parsing list
					String tmp = musicList[0][1];
					
					final String title, artist;
					
					if(tmp.indexOf(" - ") != -1)
                	{
                		title = tmp.substring(0, tmp.indexOf(" - "));
                    	artist = tmp.substring(tmp.indexOf(" - ")+3);
                	}
                	else
                	{
                		title = musicList[currentid][1];
                		artist = "?";
                	}
					
					songurl = musicList[0][0];
					mainArtist.setEllipsize(TruncateAt.END);
					mainArtist.setMaxLines(2);
					mainArtist.setText(artist);
					mainTitle.setEllipsize(TruncateAt.END);
					mainTitle.setMaxLines(1);
					mainTitle.setText(String.valueOf(currentid+1) + "." +title);
					
					list.setOnItemClickListener(new AdapterView.OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							
							currentid = arg2;
							
							String tmp = musicList[arg2][1];
							final String title;
				            final String artist;
				                
							if(tmp.indexOf(" - ") != -1)
		                	{
		                		title = tmp.substring(0, tmp.indexOf(" - "));
		                    	artist = tmp.substring(tmp.indexOf(" - ")+3);
		                	}
		                	else
		                	{
		                		title = musicList[arg2][1];
		                		artist = "?";
		                	}
							
							songurl = musicList[arg2][0];
							
							builder.setMessage(title);
							builder.setTitle(artist);
							
							builder.setPositiveButton("播放", new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									try
									{
										if(isPlayed)
										{
											mp.reset();
										}
										
										selectedid = currentid;
										
										pDialog = ProgressDialog.show(MainActivity.this, "请稍后...", "正在加载音乐\n"+title, true);
								        pDialog.setCancelable(true);
								        
										MusicPlayThread mpt = new MusicPlayThread();
										mpt.start();
										
										mainArtist.setEllipsize(TruncateAt.END);
										mainArtist.setMaxLines(2);
										mainArtist.setText(artist);
										mainTitle.setEllipsize(TruncateAt.END);
										mainTitle.setMaxLines(1);
										mainTitle.setText(String.valueOf(currentid+1) + "." +title);
										
										// close play list
										viewFlipper.setDisplayedChild(0);
										buttonPlay.setImageResource(drawable.ic_media_pause);
									}
									catch(Exception e)
									{
										alertDialogMusicNotAvailable.show();
									}
									
									
									dialog.dismiss();
								}
							});
							
							builder.setNegativeButton("取消", new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									dialog.dismiss();
								}
							});

							builder.create().show();
						}
				
					});
					
				}
				// error
				else if(msg.what == 1)
				{					
					AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
					dialog.setMessage("无网络连接或列表中无歌曲.\n播放器将退出.");
					dialog.setTitle("提示");
					
					dialog.setButton("退出", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();
							MainActivity.this.finish();
						}
					});
					
					dialog.show();
				}
				// not used
				else if(msg.what == 2)
				{
		
				}
			}
		};
	}
	
	@SuppressLint({ "HandlerLeak", "NewApi", "DefaultLocale" })
	class MusicPlayThread extends Thread
    {
		@Override
		public void run() 
		{
			try 
			{
				mp.setDataSource(MainActivity.this, Uri.parse(songurl));
				mp.prepare();
				totalLengthofSong = mp.getDuration();
				
				totalLengthofSongInTime = String.format("%02d:%02d", 
					    TimeUnit.MILLISECONDS.toMinutes(totalLengthofSong),
					    TimeUnit.MILLISECONDS.toSeconds(totalLengthofSong) - 
					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalLengthofSong)));
				
				mainProgressBar.setMax(totalLengthofSong);
				pDialog.dismiss();
				mp.start();
				
				isPlayed = true;
				isPlaying = true;
				
				try
				{
					if(Build.VERSION.SDK_INT < 14)
					{
						metaRetriever.setDataSource(songurl);
					}
					else
					{
						metaRetriever.setDataSource(songurl, new HashMap<String, String>());
					}
									
					songCover = metaRetriever.getEmbeddedPicture();
					
					if(songCover == null)
					{
						handler.sendEmptyMessage(1);
					}
					else
					{
						handler.sendEmptyMessage(2);
					}
				}
				catch(Exception e)
				{
					pDialog.dismiss();
					handler.sendEmptyMessage(3);
				}
			} 
			catch (Exception e) 
			{
				pDialog.dismiss();
				handler.sendEmptyMessage(0);
			}
		}
		
		private Handler handler = new Handler() 
		{
			public void handleMessage(Message msg) 
			{
				// stop - music not available
				if(msg.what == 0)
				{
					isPlayed = true;
					isPlaying = false;
					
					alertDialogMusicNotAvailable.show();
				}
				else if(msg.what == 1)
				{
					albumPic.setImageResource(org.cnloli.cnlolimusicplayer2.R.drawable.infochar);
					albumPic.getLayoutParams().height = 150;
					albumPic.getLayoutParams().width = 126;
				}
				else if(msg.what == 2)
				{
					Bitmap b = BitmapFactory.decodeByteArray(songCover, 0, songCover.length);
					
					DisplayMetrics displaymetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//					int scrheight = displaymetrics.heightPixels;
					int w = (int)(displaymetrics.widthPixels * 0.9);
					
					albumPic.setImageBitmap(Bitmap.createScaledBitmap(b,w,w,false));
					albumPic.getLayoutParams().height = w;
					albumPic.getLayoutParams().width = w;
				}
				else if(msg.what == 3)
				{
					alertDialogMusicNotAvailable.show();
				}
			};
		};
    }
}
