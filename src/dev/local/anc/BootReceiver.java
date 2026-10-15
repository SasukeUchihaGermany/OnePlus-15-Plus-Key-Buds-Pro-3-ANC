package dev.local.anc;
import android.content.*;
import android.Manifest;
/** Restarts the app service; Shizuku startup is managed separately by its manager. */
public class BootReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
  if(!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||!c.getSharedPreferences("config",0).getBoolean("enabled",false)||c.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=0)return;
  try{c.startForegroundService(new Intent(c,ControllerService.class).setAction("CONNECT"));}catch(Exception ignored){}
 }
}
