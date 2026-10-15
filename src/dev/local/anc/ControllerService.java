package dev.local.anc;
import android.app.*;
import android.os.*;
import android.bluetooth.*;
import android.content.*;
import android.util.Log;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ControllerService extends Service {
 static final String TAG="BudsAncLab";
 static final ArrayDeque<String> lines=new ArrayDeque<>();
 static volatile Anc.Mode observedMode=Anc.Mode.UNKNOWN;static volatile long observedAt=0;
 static synchronized void note(String s){Log.i(TAG,s);lines.addLast(s);while(lines.size()>60)lines.removeFirst();}
 public static synchronized String display(){
  StringBuilder result=new StringBuilder("Observed: "+observedMode);
  if(observedAt>0)result.append(" · ").append((SystemClock.elapsedRealtime()-observedAt)/1000).append("s ago");
  result.append("\n");int count=0;
  for(java.util.Iterator<String> it=lines.descendingIterator();it.hasNext()&&count<8;){String s=it.next();if(s.startsWith("TX ")||s.startsWith("RX ")||s.startsWith("Target ")||s.startsWith("OBSERVED "))continue;result.append("\n").append(s);count++;}
  return result.toString();
 }
 final ExecutorService worker=Executors.newSingleThreadExecutor();
 final ScheduledExecutorService timer=Executors.newSingleThreadScheduledExecutor();
 final Object state=new Object();
 volatile BluetoothSocket socket;
 volatile boolean running=true;
 long replies=0; Anc.Mode reply=Anc.Mode.UNKNOWN;int querySequence=0,awaitedSequence=-1;
 BluetoothAdapter adapter; BluetoothA2dp a2dp;
 InputBridge bridge; Gestures gestures; Handler main=new Handler(Looper.getMainLooper());
 PowerManager.WakeLock gestureWake;
 NativeAlert nativeAlert;
 final BroadcastReceiver connections=new BroadcastReceiver(){public void onReceive(Context c,Intent i){updateCapture();}};
 void updateCapture(){
  boolean connected=false;try{target();connected=true;}catch(Exception ignored){}
  if(bridge!=null)bridge.enabled(connected);
  if(!connected){gestures.cancel();close();if(nativeAlert!=null)nativeAlert.clear();note("Buds disconnected: native key released");}
 }
 final BluetoothProfile.ServiceListener profile=new BluetoothProfile.ServiceListener(){
  public void onServiceConnected(int p,BluetoothProfile proxy){a2dp=(BluetoothA2dp)proxy;note("A2DP proxy ready");updateCapture();}
  public void onServiceDisconnected(int p){a2dp=null;updateCapture();}
 };
 public void onCreate(){super.onCreate();
  nativeAlert=new NativeAlert(this,main);
  NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("anc","Buds ANC diagnostics",NotificationManager.IMPORTANCE_LOW));
  PendingIntent openApp=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,ControllerService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  startForeground(1,new Notification.Builder(this,"anc").setContentTitle("Plus Key ANC").setContentText("Tap for status and gesture timing").setContentIntent(openApp).addAction(new Notification.Action.Builder(null,"STOP",stop).build()).setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).build());
  adapter=getSystemService(BluetoothManager.class).getAdapter();adapter.getProfileProxy(this,profile,BluetoothProfile.A2DP);
  gestureWake=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"BudsAnc:gesture");gestureWake.setReferenceCounted(false);
  android.content.SharedPreferences prefs=getSharedPreferences("config",0);
  configureGestures();
  bridge=new InputBridge(this,new InputBridge.Listener(){
   public void edge(long ms,boolean down){main.post(()->{gestureWake.acquire(6000);gestures.edge(ms,down);});}
   public void status(String s){note(s);if(s.startsWith("Input bridge disconnected")||s.startsWith("Shizuku stopped")||s.startsWith("Input failure")||s.startsWith("Input capture error"))main.post(()->gestures.cancel());}
  });
  registerReceiver(connections,new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED),Context.RECEIVER_EXPORTED);
 }
 void configureGestures(){
  if(gestures!=null)gestures.cancel();
  android.content.SharedPreferences prefs=getSharedPreferences("config",0);
  gestures=new Gestures((ms,r)->main.postDelayed(r,ms),g->{note("GESTURE "+g);worker.execute(()->executeGesture(g));},prefs.getLong("doubleMs",220),prefs.getLong("longMs",600),25);
 }
 public int onStartCommand(Intent i,int flags,int id){
  if(i!=null&&i.getAction()!=null){getSharedPreferences("config",0).edit().putBoolean("enabled",!i.getAction().equals("STOP")).apply();worker.execute(()->execute(i.getAction()));}
  if(bridge!=null)bridge.bind();
  return START_STICKY;
 }
 BluetoothDevice target() throws IOException {
  if(a2dp==null)throw new IOException("A2DP proxy not ready; retry CONNECT");
  BluetoothDevice found=null;
  for(BluetoothDevice d:a2dp.getConnectedDevices())if("OnePlus Buds Pro 3".equals(d.getName())&&d.getBondState()==BluetoothDevice.BOND_BONDED){if(found!=null)throw new IOException("Ambiguous Buds identity");found=d;}
  if(found==null)throw new IOException("Verified paired Buds Pro 3 not A2DP-connected");return found;
 }
 void connect()throws Exception{
  for(int attempt=0;;attempt++)try{open();return;}catch(IOException e){if(attempt>=1)throw e;note("Connection retry after "+e.getMessage());Thread.sleep(300);}
 }
 void open()throws Exception{
  target();if(socket!=null&&socket.isConnected())return;
  BluetoothDevice device=target();
  note("Target paired and A2DP-connected; UUIDs="+Arrays.toString(device.getUuids()));
  // Device discovery verified 079a is advertised on this Buds Pro 3.
  BluetoothSocket candidate=device.createRfcommSocketToServiceRecord(UUID.fromString("0000079a-d102-11e1-9b23-00025b00a5a5"));
  socket=candidate;
  ScheduledFuture<?> deadline=timer.schedule(()->{try{candidate.close();}catch(IOException ignored){}},7,TimeUnit.SECONDS);
  try{candidate.connect();}catch(Exception e){close();throw e;}finally{deadline.cancel(false);}
  note("RFCOMM connected via public UUID API");
  Thread reader=new Thread(()->read(candidate),"BudsReader");reader.start();
  send(Protocol.HELLO);
  // Reference timing only during POC handshake, not a success criterion.
  Thread.sleep(700);send(Protocol.REGISTER);Thread.sleep(600);
 }
 void read(BluetoothSocket owned){
  Protocol.Framer framer=new Protocol.Framer();byte[] b=new byte[2048];
  try{InputStream input=owned.getInputStream();int n;while(running&&(n=input.read(b))!=-1){framer.feed(b,n,f->{
   boolean relevant=f.length>=6&&((f[4]==12&&(f[5]&255)==129)||(f.length==14&&f[4]==4&&f[5]==2)||(f[4]==4&&(f[5]&255)==132));
   if(relevant)note("RX "+Protocol.hex(f));Anc.Mode mode=Protocol.selected(f);
   if(mode!=Anc.Mode.UNKNOWN){observedMode=mode;observedAt=SystemClock.elapsedRealtime();note("OBSERVED "+mode+(Protocol.isQuery(f)?" query":" push"));}
   if(Protocol.isQuery(f)){synchronized(state){if((f[6]&255)==awaitedSequence){reply=mode;replies++;state.notifyAll();}}}
  });}}catch(IOException e){if(running)note("Reader closed: "+e.getMessage());}
  finally{if(socket==owned){socket=null;observedMode=Anc.Mode.UNKNOWN;observedAt=0;synchronized(state){state.notifyAll();}}try{owned.close();}catch(Exception ignored){}}
 }
 void send(byte[] bytes)throws IOException{
  BluetoothSocket s=socket;if(s==null)throw new IOException("No RFCOMM connection");s.getOutputStream().write(bytes);s.getOutputStream().flush();note("TX "+Protocol.hex(bytes));
 }
 Anc.Mode query()throws Exception{
  target();synchronized(state){long before=replies;byte[] query=Protocol.QUERY.clone();querySequence=querySequence>=254?1:querySequence+1;awaitedSequence=querySequence;query[6]=(byte)querySequence;send(query);long end=SystemClock.elapsedRealtime()+2500;
   while(replies==before&&socket!=null){long left=end-SystemClock.elapsedRealtime();if(left<=0)break;state.wait(left);}
   if(replies==before)throw new IOException("Current-state query timed out; state UNKNOWN");return reply;
  }
 }
 void execute(String action){
  try{
   note("REQUEST "+action);
   if(action.equals("CONFIG")){main.post(this::configureGestures);return;}
   if(action.equals("STOP")){close();stopSelf();return;}
   connect();
   if(action.equals("CONNECT")||action.equals("QUERY")){note("CURRENT "+query());return;}
   if(action.equals("LEVEL_HIGH")){send(Protocol.level(Anc.Mode.HIGH));note("LEVEL_ONLY result="+query());return;}
   Anc.Mode desired=Anc.Mode.valueOf(action);set(desired);
  }catch(Exception e){note("ERROR "+e.getClass().getSimpleName()+": "+e.getMessage());close();}
 }
 void set(Anc.Mode desired)throws Exception{
   target();
   if(desired==Anc.Mode.OFF)send(Protocol.mode(1));
   else if(desired==Anc.Mode.TRANSPARENCY)send(Protocol.mode(4));
   else send(Protocol.level(desired)); // Verified to enable ANC from OFF and transparency.
   Anc.Mode actual=query();note(actual==desired?"CONFIRMED "+actual:"MISMATCH desired="+desired+" actual="+actual);
   if(actual==desired)nativeAlert.show(actual);
 }
 void executeGesture(Anc.Gesture gesture){
  try{
   connect();
   if(gesture==Anc.Gesture.DOUBLE){note("TRANSITION any connected mode + DOUBLE -> AUTO");set(Anc.Mode.AUTO);return;}
   Anc.Mode current;
   try{current=query();}catch(IOException e){if(socket==null)throw e;current=Anc.Mode.UNKNOWN;note("Fresh query unavailable; explicit UNKNOWN fallback");}
   Anc.Mode desired=Anc.next(current,gesture);note("TRANSITION "+current+" + "+gesture+" -> "+desired);set(desired);
  }catch(Exception e){note("GESTURE ERROR "+e.getMessage());close();}
 }
 void close(){observedMode=Anc.Mode.UNKNOWN;observedAt=0;BluetoothSocket s=socket;socket=null;if(s!=null)try{s.close();}catch(Exception ignored){}synchronized(state){reply=Anc.Mode.UNKNOWN;state.notifyAll();}}
 public void onDestroy(){running=false;if(nativeAlert!=null)nativeAlert.close();if(bridge!=null)bridge.close();gestures.cancel();unregisterReceiver(connections);if(gestureWake.isHeld())gestureWake.release();close();worker.shutdownNow();timer.shutdownNow();if(a2dp!=null)adapter.closeProfileProxy(BluetoothProfile.A2DP,a2dp);super.onDestroy();}
 public IBinder onBind(Intent i){return null;}
}
