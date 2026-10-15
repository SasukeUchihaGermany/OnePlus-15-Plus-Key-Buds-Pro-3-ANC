package com.nish.ancnativeprobe;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.provider.Settings;
public class MainActivity extends Activity {
 public void onCreate(Bundle state){super.onCreate(state);
  LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(32,80,32,32);
  TextView t=new TextView(this);t.setText("Native ANC notification experiment\nNo Bluetooth control, overlay or Shizuku access.\nTest text is simulated, not a measured earbud mode.");box.addView(t);
  for(String action:new String[]{"ALLOW NOTIFICATIONS","LIVE ALERT SETTINGS","SHOW HIGH","CLEAR","STATUS"}){
   Button b=new Button(this);b.setText(action);box.addView(b);b.setOnClickListener(v->{
    if(action.equals("ALLOW NOTIFICATIONS")){requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1);return;}
    if(action.equals("LIVE ALERT SETTINGS")){startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));return;}
    if(action.equals("CLEAR")){getSystemService(NotificationManager.class).cancelAll();return;}
    if(action.equals("STATUS")){ProbeReceiver.status(this);return;}
    ProbeReceiver.post(this,"High",false);
   });
  }setContentView(box);
 }
}
