package com.nish.ancnativeprobe;
import android.app.*;
import android.os.*;
import android.content.*;
import android.util.Log;
public class ProbeReceiver extends BroadcastReceiver {
 static final String TAG="AncNativeProbe";
 public void onReceive(Context c,Intent i){
  if("CLEAR".equals(i.getStringExtra("command"))){c.getSystemService(NotificationManager.class).cancelAll();return;}
  if("STATUS".equals(i.getStringExtra("command"))){status(c);return;}
  String mode=i.getStringExtra("mode");if(mode==null)mode="High";
  if(!java.util.Arrays.asList("High","Medium","Low","Auto","Off","Transparent").contains(mode))return;
  post(c,mode,i.getBooleanExtra("big",false),Math.max(1000,Math.min(60000,i.getIntExtra("ttlMs",60000))));
 }
 static void post(Context c,String mode,boolean big){post(c,mode,big,60000);}
 static void post(Context c,String mode,boolean big,int ttlMs){try{
  NotificationManager nm=c.getSystemService(NotificationManager.class);
  NotificationChannel channel=new NotificationChannel("anc_native","ANC native capsule test",NotificationManager.IMPORTANCE_DEFAULT);channel.setSound(null,null);channel.enableVibration(false);nm.createNotificationChannel(channel);
  PendingIntent open=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
  Bundle extras=new Bundle();extras.putBoolean("android.requestPromotedOngoing",true);
  Notification.Builder b=new Notification.Builder(c,"anc_native").setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
   .setContentTitle("Nish's ANC Engine: "+mode).setContentText("ANC: "+mode)
   .setShortCriticalText(big?"Nish's ANC Engine: "+mode:mode).setOngoing(true).setShowWhen(false).setOnlyAlertOnce(false)
   .setContentIntent(open).setTimeoutAfter(ttlMs).addExtras(extras);
  if(big)b.setStyle(new Notification.BigTextStyle().bigText("Nish's ANC Engine: "+mode));
  Notification n=b.build();Log.i(TAG,"POST simulated="+mode+" big="+big+" eligible="+n.hasPromotableCharacteristics()+" canPromote="+nm.canPostPromotedNotifications());
  nm.notify(1,n);new Handler(Looper.getMainLooper()).postDelayed(()->status(c),1000);
 }catch(Exception e){Log.e(TAG,"POST failed",e);}}
 static void status(Context c){try{NotificationManager nm=c.getSystemService(NotificationManager.class);Log.i(TAG,"STATUS enabled="+nm.areNotificationsEnabled()+" canPromote="+nm.canPostPromotedNotifications());for(android.service.notification.StatusBarNotification s:nm.getActiveNotifications()){Notification n=s.getNotification();Log.i(TAG,"ACTIVE flags=0x"+Integer.toHexString(n.flags)+" promoted="+((n.flags&Notification.FLAG_PROMOTED_ONGOING)!=0)+" eligible="+n.hasPromotableCharacteristics());}}catch(Exception e){Log.e(TAG,"STATUS",e);}}
}
