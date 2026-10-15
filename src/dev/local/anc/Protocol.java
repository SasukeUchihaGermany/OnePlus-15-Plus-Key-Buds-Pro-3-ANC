package dev.local.anc;
import java.util.*;
import java.util.function.Consumer;

public final class Protocol {
 // Exact upstream handshake, including HELLO's unusual length/trailing byte.
 public static final byte[] HELLO=hex("aa070000000123000012");
 public static final byte[] REGISTER=hex("aa0c0000008541050000b550a069");
 public static final byte[] QUERY=hex("aa0900000c010002000101");
 public static byte[] mode(int m){return hex("aa0a000004044203000101"+String.format("%02x",m));}
 public static byte[] level(Anc.Mode m){
  int v=m==Anc.Mode.HIGH?16:m==Anc.Mode.MEDIUM?32:m==Anc.Mode.LOW?64:m==Anc.Mode.AUTO?128:0;
  if(v==0)throw new IllegalArgumentException("not ANC strength");
  return hex("aa0b000004044204000101"+String.format("%02x",v)+"00");
 }
 public static byte[] hex(String s){
  byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(2*i,2*i+2),16);return b;
 }
 public static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format("%02x",v&255));return s.toString();}
 public static boolean isQuery(byte[] d){return valid(d)&&d[4]==12&&(d[5]&255)==129;}
 public static boolean valid(byte[] d){return d.length>=9&&(d[0]&255)==170&&(d[1]&255)+2==d.length&&((d[7]&255)|((d[8]&255)<<8))==d.length-9;}
 public static Anc.Mode selected(byte[] d){
  if(!valid(d)||d.length!=14||d[10]!=1||d[11]!=1)return Anc.Mode.UNKNOWN;
  boolean reply=isQuery(d)&&d[9]==0;
  boolean push=d[4]==4&&d[5]==2&&d[9]==3;
  if(!reply&&!push)return Anc.Mode.UNKNOWN;
  return Anc.mask((d[12]&255)|((d[13]&255)<<8));
 }
 /** Bounded stream reassembly; handles split/coalesced frames, clears on reconnect. */
 public static final class Framer {
  private final ArrayList<Byte> acc=new ArrayList<>();
  public void feed(byte[] b,int n,Consumer<byte[]> output){
   for(int i=0;i<n;i++){
    acc.add(b[i]);
    while(!acc.isEmpty()&&(acc.get(0)&255)!=170)acc.remove(0);
    if(acc.size()<2)continue;
    int total=(acc.get(1)&255)+2;
    if(total<9){acc.remove(0);continue;}
    if(acc.size()>=total){byte[] f=new byte[total];for(int j=0;j<total;j++)f[j]=acc.remove(0);output.accept(f);}
   }
  }
 }
 private Protocol(){}
}
