package dev.local.anc;
import android.system.*;
import java.io.*;
import java.nio.*;

/** Reads and grabs only the previously verified dedicated Plus Key device. */
final class KeyCapture implements AutoCloseable {
 interface Output {void edge(long ms,boolean down);void status(String message);}
 final FileDescriptor input;final FileDescriptor[] wake;final Thread thread;volatile boolean active=true;
 static void verify()throws Exception{
  if(!"CPH2747".equals(android.os.Build.MODEL))throw new IOException("Unverified phone model");
  Process p=new ProcessBuilder("/system/bin/getevent","-lp","/dev/input/event0").start();
  StringBuilder info=new StringBuilder();
  try(BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()))){String l;while((l=r.readLine())!=null)info.append(l).append('\n');}
  java.util.regex.Matcher names=java.util.regex.Pattern.compile("\\b(?:KEY_|BTN_)[A-Z0-9_]+").matcher(info);
  int count=0;while(names.find())count++;
  if(p.waitFor()!=0||count!=1||!info.toString().contains("BTN_TRIGGER_HAPPY32")||!info.toString().contains("gpio-keys"))throw new IOException("Dedicated input layout changed");
 }
 KeyCapture(Output out)throws Exception{
  verify();
  input=Os.open("/dev/input/event0",OsConstants.O_RDONLY|OsConstants.O_NONBLOCK,0);wake=Os.pipe();
  try{Class.forName("android.system.Os").getMethod("ioctlInt",FileDescriptor.class,int.class).invoke(null,input,0x40044590);}
  catch(Exception e){Os.close(input);Os.close(wake[0]);Os.close(wake[1]);throw e;}
  thread=new Thread(()->{
   try{
    StructPollfd key=new StructPollfd();key.fd=input;key.events=(short)OsConstants.POLLIN;
    StructPollfd quit=new StructPollfd();quit.fd=wake[0];quit.events=(short)OsConstants.POLLIN;
    byte[] raw=new byte[24*16];
    while(active){
     Os.poll(new StructPollfd[]{key,quit},-1);
     if(!active||(quit.revents&OsConstants.POLLIN)!=0)break;
     if((key.revents&OsConstants.POLLIN)==0)continue;
     int n=Os.read(input,raw,0,raw.length);
     for(int i=0;i+24<=n;i+=24){
      ByteBuffer b=ByteBuffer.wrap(raw,i,24).order(ByteOrder.LITTLE_ENDIAN);
      long ms=b.getLong()*1000+b.getLong()/1000;
      int type=b.getShort()&65535,code=b.getShort()&65535,value=b.getInt();
      if(type==1&&code==735&&(value==0||value==1))out.edge(ms,value==1);
     }
    }
   }catch(Exception e){out.status("Input failure: "+e.getClass().getSimpleName());}
   finally{try{Os.close(input);}catch(Exception ignored){}try{Os.close(wake[0]);}catch(Exception ignored){}try{Os.close(wake[1]);}catch(Exception ignored){}}
  },"DedicatedPlusKey");thread.start();
 }
 public void close(){active=false;try{Os.write(wake[1],new byte[]{1},0,1);}catch(Exception ignored){}try{thread.join(1000);}catch(Exception ignored){}}
}
