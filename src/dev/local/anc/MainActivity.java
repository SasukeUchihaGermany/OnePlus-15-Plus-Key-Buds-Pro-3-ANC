package dev.local.anc;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.Manifest;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
 TextView status;final Handler handler=new Handler(Looper.getMainLooper());
 final Runnable refresh=new Runnable(){public void run(){status.setText(ControllerService.display());handler.postDelayed(this,1000);}};
 void start(String action){
  if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=0){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.POST_NOTIFICATIONS},1);return;}
  startForegroundService(new Intent(this,ControllerService.class).setAction(action));
 }
 public void onCreate(Bundle b){
  super.onCreate(b);
  LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(24,40,24,24);
  TextView title=new TextView(this);title.setTextSize(24);title.setText("Plus Key ANC");box.addView(title);
  TextView help=new TextView(this);help.setText("Single: High → Medium → Low\nDouble: Auto\nHold: Transparency ↔ Off\n\nWear the earbuds. Reboot recovery needs configured Shizuku and Wi-Fi/debugging. If capture is unavailable, start Shizuku, then START here. STOP releases the Plus Key.\n");box.addView(help);
  String[] actions={"ENABLE KEY","CONNECT","QUERY","TIMING","HIGH","MEDIUM","LOW","AUTO","OFF","TRANSPARENCY","STOP"};
  LinearLayout row=null;
  for(int n=0;n<actions.length;n++){
   String action=actions[n];
   if(n%2==0){row=new LinearLayout(this);box.addView(row);}
   Button button=new Button(this);button.setText(action.equals("CONNECT")?"START":action);row.addView(button,new LinearLayout.LayoutParams(0,-2,1));
   button.setOnClickListener(v->{
    if(action.equals("TIMING")){timing();return;}
    if(action.equals("ENABLE KEY")){
     if(!Shizuku.pingBinder()){Intent open=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");if(open!=null)startActivity(open);return;}
     if(Shizuku.checkSelfPermission()!=0)Shizuku.requestPermission(10);else start("CONNECT");return;
    }
    start(action);
   });
  }
  status=new TextView(this);status.setTextSize(14);ScrollView scroll=new ScrollView(this);scroll.addView(status);box.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(box);
 }
 void timing(){
  android.content.SharedPreferences p=getSharedPreferences("config",0);
  LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(40,10,40,10);
  TextView dLabel=new TextView(this);dLabel.setText("Double-press window (ms, 150–600)");content.addView(dLabel);
  EditText dbl=new EditText(this);dbl.setInputType(2);dbl.setText(""+p.getLong("doubleMs",220));content.addView(dbl);
  TextView lLabel=new TextView(this);lLabel.setText("Hold threshold (ms, 450–2000)");content.addView(lLabel);
  EditText lng=new EditText(this);lng.setInputType(2);lng.setText(""+p.getLong("longMs",600));content.addView(lng);
  new AlertDialog.Builder(this).setTitle("Gesture timing").setView(content).setNegativeButton("Cancel",null).setPositiveButton("Save",(dialog,which)->{
   try{long d=Long.parseLong(dbl.getText().toString()),l=Long.parseLong(lng.getText().toString());if(d<150||d>600||l<450||l>2000||l<=d)throw new IllegalArgumentException();p.edit().putLong("doubleMs",d).putLong("longMs",l).apply();start("CONFIG");}
   catch(Exception e){Toast.makeText(this,"Enter valid timings; hold must be longer than double window",Toast.LENGTH_LONG).show();}
  }).show();
 }
 public void onResume(){super.onResume();handler.post(refresh);}
 public void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
