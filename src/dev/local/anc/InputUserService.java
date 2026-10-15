package dev.local.anc;
import android.os.*;
import android.content.Context;
import android.system.Os;

/** Shizuku UserService. Binder surface is limited to this app's dedicated key. */
public class InputUserService extends Binder {
 static final String DESCRIPTOR="dev.local.anc.INPUT";
 final int appUid;IBinder callback;KeyCapture capture;
 public InputUserService(Context context){appUid=context.getApplicationInfo().uid;}
 synchronized void release(){if(capture!=null){capture.close();capture=null;}}
 void emit(int code,long ms,boolean down,String message){
  IBinder c=callback;if(c==null)return;
  Parcel p=Parcel.obtain();try{p.writeInterfaceToken(DESCRIPTOR);if(code==1){p.writeLong(ms);p.writeInt(down?1:0);}else p.writeString(message);c.transact(code,p,null,IBinder.FLAG_ONEWAY);}
  catch(Exception e){new Thread(this::release).start();}finally{p.recycle();}
 }
 protected synchronized boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException{
  if(code==rikka.shizuku.ShizukuApiConstants.USER_SERVICE_TRANSACTION_destroy){release();System.exit(0);return true;}
  if(Binder.getCallingUid()!=appUid)throw new SecurityException("Wrong controller UID");
  data.enforceInterface(DESCRIPTOR);
  try{
   if(code==1){callback=data.readStrongBinder();callback.linkToDeath(()->{release();System.exit(0);},0);reply.writeNoException();reply.writeInt(Os.getuid());return true;}
   if(code==2){
    boolean enable=data.readInt()!=0;
    if(enable&&capture==null)capture=new KeyCapture(new KeyCapture.Output(){
     public void edge(long ms,boolean down){emit(1,ms,down,null);}
     public void status(String s){emit(2,0,false,s);}
    });
    if(!enable)release();reply.writeNoException();emit(2,0,false,enable?"CAPTURE ON (Shizuku)":"CAPTURE OFF (native)");return true;
   }
  }catch(Exception e){reply.writeException(new IllegalStateException(e.toString()));return true;}
  return super.onTransact(code,data,reply,flags);
 }
}
