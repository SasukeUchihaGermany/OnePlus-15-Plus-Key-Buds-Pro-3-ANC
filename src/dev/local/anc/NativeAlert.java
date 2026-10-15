package dev.local.anc;
import android.app.*;
import android.content.*;
import android.os.*;

/** Best-effort native UI feedback. Never runs on or throws into the Bluetooth worker. */
final class NativeAlert {
 static final int ID=2;
 static final long DURATION_MS=1500;
 final Context context;final Handler main;boolean closed;
 NativeAlert(Context context,Handler main){this.context=context;this.main=main;}
 void show(Anc.Mode mode){
  if(Build.VERSION.SDK_INT<36||mode==Anc.Mode.UNKNOWN)return;
  main.post(()->{if(closed)return;try{
   NotificationManager nm=context.getSystemService(NotificationManager.class);
   if(!nm.areNotificationsEnabled()||!nm.canPostPromotedNotifications()){
    ControllerService.note("Live Alert unavailable: enable app notifications and Show Live Updates on Live Alerts");return;
   }
   NotificationChannel channel=new NotificationChannel("anc_mode","ANC mode Live Alerts",NotificationManager.IMPORTANCE_DEFAULT);
   channel.setSound(null,null);channel.enableVibration(false);nm.createNotificationChannel(channel);
   String label=label(mode);
   PendingIntent open=PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
   Bundle extras=new Bundle();extras.putBoolean("android.requestPromotedOngoing",true);
   Notification n=new Notification.Builder(context,"anc_mode").setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
    .setContentTitle("Nish's ANC Engine: "+label).setContentText("ANC: "+label)
    .setShortCriticalText(label).setOngoing(true).setShowWhen(false).setOnlyAlertOnce(false)
    .setContentIntent(open).setTimeoutAfter(DURATION_MS).addExtras(extras).build();
   nm.notify(ID,n);ControllerService.note("LIVE ALERT "+mode);
  }catch(RuntimeException e){ControllerService.note("Live Alert failed: "+e.getClass().getSimpleName());}});
 }
 static String label(Anc.Mode mode){
  switch(mode){case HIGH:return "High";case MEDIUM:return "Medium";case LOW:return "Low";
   case AUTO:return "Auto";case OFF:return "Off";case TRANSPARENCY:return "Transparent";default:return "Unknown";}
 }
 void clear(){main.post(()->{try{context.getSystemService(NotificationManager.class).cancel(ID);}catch(RuntimeException ignored){}});}
 void close(){closed=true;try{context.getSystemService(NotificationManager.class).cancel(ID);}catch(RuntimeException ignored){}}
}
