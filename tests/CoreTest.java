package dev.local.anc;
import java.util.*;

public class CoreTest {
 static class Clock implements Gestures.Scheduler {
  long time;TreeMap<Long,List<Runnable>> tasks=new TreeMap<>();
  public void after(long ms,Runnable r){tasks.computeIfAbsent(time+ms,k->new ArrayList<>()).add(r);}
  void advance(long t){while(!tasks.isEmpty()&&tasks.firstKey()<=t){time=tasks.firstKey();for(Runnable r:tasks.remove(time))r.run();}time=t;}
 }
 static int checks;
 static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
 public static void main(String[] args){
  Anc.Mode[] initial={Anc.Mode.HIGH,Anc.Mode.MEDIUM,Anc.Mode.LOW,Anc.Mode.AUTO,Anc.Mode.TRANSPARENCY,Anc.Mode.OFF,Anc.Mode.UNKNOWN};
  Anc.Mode[] single={Anc.Mode.MEDIUM,Anc.Mode.LOW,Anc.Mode.HIGH,Anc.Mode.HIGH,Anc.Mode.HIGH,Anc.Mode.HIGH,Anc.Mode.HIGH};
  for(int i=0;i<initial.length;i++){
   check(Anc.next(initial[i],Anc.Gesture.SINGLE)==single[i],"single "+initial[i]);
   check(Anc.next(initial[i],Anc.Gesture.DOUBLE)==Anc.Mode.AUTO,"double "+initial[i]);
   check(Anc.next(initial[i],Anc.Gesture.LONG)==(initial[i]==Anc.Mode.TRANSPARENCY?Anc.Mode.OFF:Anc.Mode.TRANSPARENCY),"long "+initial[i]);
  }
  byte[] off=Protocol.hex("aa0c00000c810005000001010800");
  check(Protocol.selected(off)==Anc.Mode.OFF,"captured device query OFF");
  byte[] auto=Protocol.hex("aa0c000004020005000301018000");
  byte[] adaptive=Protocol.hex("aa0c000004020005000304014000");
  check(Protocol.selected(auto)==Anc.Mode.AUTO,"selected Auto");
  check(Protocol.selected(adaptive)==Anc.Mode.UNKNOWN,"adaptive sublevel cannot overwrite Auto");
  byte[] trans=Protocol.hex("aa0c00000c810005000001010001");
  check(Protocol.selected(trans)==Anc.Mode.TRANSPARENCY,"transparency overrides remembered strength");
  check(Protocol.selected(Protocol.hex("aa08000004844201000e"))==Anc.Mode.UNKNOWN,"reject ack is not state");
  for(int n=0;n<off.length;n++)check(Protocol.selected(Arrays.copyOf(off,n))==Anc.Mode.UNKNOWN,"truncated query "+n);
  byte[] bad=off.clone();bad[7]=6;check(Protocol.selected(bad)==Anc.Mode.UNKNOWN,"invalid payload length");
  for(int split=0;split<=off.length;split++){
   Protocol.Framer f=new Protocol.Framer();ArrayList<byte[]> frames=new ArrayList<>();
   f.feed(new byte[]{1,2,3},3,frames::add);
   f.feed(Arrays.copyOf(off,split),split,frames::add);
   byte[] rest=Arrays.copyOfRange(off,split,off.length);f.feed(rest,rest.length,frames::add);
   f.feed(trans,trans.length,frames::add);
   check(frames.size()==2&&Arrays.equals(frames.get(0),off)&&Arrays.equals(frames.get(1),trans),"stream split "+split);
  }
  check(Protocol.hex(Protocol.level(Anc.Mode.HIGH)).equals("aa0b0000040442040001011000"),"high packet matches reference");
  Clock clock=new Clock();ArrayList<Anc.Gesture> events=new ArrayList<>();
  Gestures g=new Gestures(clock,events::add,330,700,25);
  g.edge(0,true);clock.advance(100);g.edge(100,false);clock.advance(429);
  check(events.isEmpty(),"single deferred for double window");clock.advance(430);
  check(events.equals(Arrays.asList(Anc.Gesture.SINGLE)),"single exactly once");
  events.clear();clock.advance(1000);g.edge(1000,true);g.edge(1000,true);clock.advance(1100);g.edge(1100,false);g.edge(1100,false);
  clock.advance(1200);g.edge(1200,true);clock.advance(1300);g.edge(1300,false);clock.advance(2100);
  check(events.equals(Arrays.asList(Anc.Gesture.DOUBLE)),"double, deduped edges, no single");
  events.clear();clock.advance(3000);g.edge(3000,true);clock.advance(3700);clock.advance(4000);g.edge(4000,false);clock.advance(5000);
  check(events.equals(Arrays.asList(Anc.Gesture.LONG)),"long exactly once, no single");
  events.clear();g.edge(5000,true);clock.advance(5100);g.edge(5100,false);g.cancel();clock.advance(6000);
  check(events.isEmpty(),"disconnect cancels pending single");
  g.edge(6000,true);g.cancel();clock.advance(6800);g.edge(6800,false);
  check(events.isEmpty(),"disconnect cancels held long");
  clock.advance(7000);g.edge(7000,true);clock.advance(7010);g.edge(7010,false);clock.advance(8000);
  check(events.isEmpty(),"sub-debounce noise ignored");
  clock.advance(9000);g.edge(9000,true);clock.advance(9100);g.edge(9100,false);clock.advance(9200);g.edge(9200,true);clock.advance(9900);g.edge(9900,false);clock.advance(11000);
  check(events.equals(Arrays.asList(Anc.Gesture.LONG)),"second held press cancels deferred first single");
  for(int window:new int[]{150,220,330,600}){
   Clock c=new Clock();ArrayList<Anc.Gesture> out=new ArrayList<>();Gestures recognizer=new Gestures(c,out::add,window,Math.max(600,window+100),25);
   recognizer.edge(0,true);c.advance(100);recognizer.edge(100,false);c.advance(100+window-1);check(out.isEmpty(),"configured window waits "+window);
   c.advance(100+window);check(out.equals(Arrays.asList(Anc.Gesture.SINGLE)),"configured single fires "+window);
  }
  System.out.println("PASS: "+checks+" host assertions; these are not physical acceptance tests.");
 }
}
