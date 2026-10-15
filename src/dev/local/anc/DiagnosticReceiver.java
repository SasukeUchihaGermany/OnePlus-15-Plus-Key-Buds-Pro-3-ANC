package dev.local.anc;
import android.content.*;
/** Shell diagnostics only. Manifest DUMP permission excludes ordinary apps. */
public class DiagnosticReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
  String a=i.getStringExtra("command");
  if(a==null||!a.matches("CONNECT|QUERY|LEVEL_HIGH|HIGH|MEDIUM|LOW|AUTO|OFF|TRANSPARENCY|STOP"))return;
  if(c.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)!=0)return;
  c.startForegroundService(new Intent(c,ControllerService.class).setAction(a));
 }
}
