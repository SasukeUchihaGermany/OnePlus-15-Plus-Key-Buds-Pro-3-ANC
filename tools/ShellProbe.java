package dev.local.anc;
import java.io.*;
import java.lang.reflect.*;
/** Brief capability probe. Does not persist or inject input. */
public final class ShellProbe {
 public static void main(String[] args)throws Exception{
  try(FileInputStream input=new FileInputStream("/dev/input/event0")){
   System.out.println("Dedicated input node opened");
   try{
    Method ioctl=Class.forName("android.system.Os").getMethod("ioctlInt",FileDescriptor.class,int.class);
    Object result=ioctl.invoke(null,input.getFD(),0x40044590);
    System.out.println("EVIOCGRAB accepted; result="+result+"; releasing immediately by closing descriptor");
   }catch(InvocationTargetException e){System.out.println("EVIOCGRAB rejected: "+e.getCause());}
  }
  System.out.println("Descriptor closed; no input capture retained");
 }
}
