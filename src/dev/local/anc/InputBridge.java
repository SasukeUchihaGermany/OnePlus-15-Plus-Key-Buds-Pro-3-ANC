package dev.local.anc;
import android.content.*;
import android.os.*;
import rikka.shizuku.Shizuku;

/** Event-driven Binder transport; no local socket, network or polling. */
public final class InputBridge implements AutoCloseable {
 public interface Listener {void edge(long ms,boolean down);void status(String s);}
 final Listener listener;final Handler main=new Handler(Looper.getMainLooper());
 final Shizuku.UserServiceArgs args;
 IBinder remote;boolean enabled=false,closed=false,binding=false;int failures=0;
 final Binder callback=new Binder(){protected boolean onTransact(int code,Parcel data,Parcel reply,int flags){
  if(Binder.getCallingUid()!=2000)return false;
  data.enforceInterface(InputUserService.DESCRIPTOR);
  if(code==1){long ms=data.readLong();boolean down=data.readInt()!=0;if(enabled)listener.edge(ms,down);return true;}
  if(code==2){listener.status(data.readString());return true;}return false;
 }};
 final ServiceConnection connection=new ServiceConnection(){
  public void onServiceConnected(ComponentName name,IBinder service){
   if(closed)return;remote=service;
   Parcel p=Parcel.obtain(),r=Parcel.obtain();
   try{p.writeInterfaceToken(InputUserService.DESCRIPTOR);p.writeStrongBinder(callback);remote.transact(1,p,r,0);r.readException();if(r.readInt()!=2000)throw new SecurityException("Input service must run as shell");sendEnabled();}
   catch(Exception e){listener.status("Input service: "+e.getMessage());}
   finally{p.recycle();r.recycle();}
  }
  public void onServiceDisconnected(ComponentName n){remote=null;binding=false;listener.status("Input bridge disconnected; native key released");if(!closed&&++failures<3)main.postDelayed(InputBridge.this::bind,1500);}
 };
 final Shizuku.OnBinderReceivedListener received=()->main.post(this::bind);
 final Shizuku.OnRequestPermissionResultListener permitted=(request,result)->{if(result==0)main.post(this::bind);};
 final Shizuku.OnBinderDeadListener died=this::binderDied;
 void binderDied(){remote=null;binding=false;listener.status("Shizuku stopped; restart it on the phone");}
 public InputBridge(Context context,Listener l){
  int version;try{version=(int)context.getPackageManager().getPackageInfo(context.getPackageName(),0).getLongVersionCode();}catch(Exception e){throw new IllegalStateException(e);}
  listener=l;args=new Shizuku.UserServiceArgs(new ComponentName(context,InputUserService.class)).daemon(false).processNameSuffix("pluskey").version(version).debuggable(false);
  Shizuku.addBinderReceivedListenerSticky(received);Shizuku.addBinderDeadListener(died);Shizuku.addRequestPermissionResultListener(permitted);
  main.post(this::bind);
 }
 void bind(){
  if(closed||binding)return;
  try{if(!Shizuku.pingBinder()){listener.status("Start Shizuku, then tap ENABLE KEY");return;}
   if(Shizuku.checkSelfPermission()!=0){listener.status("Tap ENABLE KEY to approve Shizuku");return;}
   binding=true;Shizuku.bindUserService(args,connection);
   main.postDelayed(()->{if(!closed&&binding&&remote==null){binding=false;try{Shizuku.unbindUserService(args,connection,true);}catch(Exception ignored){}if(++failures<3)main.postDelayed(this::bind,1500);else listener.status("Input service unavailable; restart Shizuku, then START");}},10000);
  }catch(Exception e){binding=false;listener.status("Cannot bind input service: "+e.getMessage());}
 }
 void sendEnabled(){
  IBinder s=remote;if(s==null)return;
  Parcel p=Parcel.obtain(),r=Parcel.obtain();
  try{p.writeInterfaceToken(InputUserService.DESCRIPTOR);p.writeInt(enabled?1:0);s.transact(2,p,r,0);r.readException();}
  catch(Exception e){listener.status("Input capture error: "+e.getMessage());}
  finally{p.recycle();r.recycle();}
 }
 public void enabled(boolean v){enabled=v;main.post(this::sendEnabled);}
 public void close(){closed=true;enabled=false;sendEnabled();Shizuku.removeBinderReceivedListener(received);Shizuku.removeBinderDeadListener(died);Shizuku.removeRequestPermissionResultListener(permitted);if(binding)try{Shizuku.unbindUserService(args,connection,true);}catch(Exception ignored){}}
}
