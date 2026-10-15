package dev.local.anc;
import java.util.function.Consumer;

/** Event-time recognizer. Scheduler uses a monotonic clock; repeated edges are ignored. */
public final class Gestures {
 public interface Scheduler {void after(long ms,Runnable r);}
 final Scheduler scheduler; final Consumer<Anc.Gesture> output;
 final long doubleMs,longMs,debounceMs;
 boolean down,pending,second,longFired;long downAt,lastUp;int token;
 public Gestures(Scheduler s,Consumer<Anc.Gesture> o,long dbl,long lng,long debounce){
  if(dbl<=0||lng<=dbl||debounce<0)throw new IllegalArgumentException();
  scheduler=s;output=o;doubleMs=dbl;longMs=lng;debounceMs=debounce;
 }
 public void cancel(){token++;down=pending=second=longFired=false;}
 public void edge(long time,boolean pressed){
  if(pressed){
   if(down)return;
   if(pending&&time-lastUp>doubleMs){pending=false;output.accept(Anc.Gesture.SINGLE);}
   second=pending;pending=false;down=true;downAt=time;longFired=false;
   int generation=++token;
   scheduler.after(longMs,()->{if(token==generation&&down){longFired=true;second=false;output.accept(Anc.Gesture.LONG);}});
  }else{
   if(!down)return;down=false;token++;
   if(longFired){second=false;return;}
   long duration=time-downAt;
   if(duration<debounceMs){second=false;return;}
   if(duration>=longMs){second=false;output.accept(Anc.Gesture.LONG);return;}
   if(second){second=false;output.accept(Anc.Gesture.DOUBLE);return;}
   pending=true;lastUp=time;int generation=token;
   scheduler.after(doubleMs,()->{if(token==generation&&pending&&!down){pending=false;output.accept(Anc.Gesture.SINGLE);}});
  }
 }
}
