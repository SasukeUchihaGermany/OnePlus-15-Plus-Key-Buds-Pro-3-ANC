package dev.local.anc;

/** Business logic has no Android dependencies. */
public final class Anc {
 public enum Mode {TRANSPARENCY,OFF,LOW,MEDIUM,HIGH,AUTO,UNKNOWN}
 public enum Gesture {SINGLE,DOUBLE,LONG}
 public static Mode next(Mode m, Gesture g) {
  if(g==Gesture.DOUBLE) return Mode.AUTO;
  if(g==Gesture.LONG) return m==Mode.TRANSPARENCY?Mode.OFF:Mode.TRANSPARENCY;
  return m==Mode.HIGH?Mode.MEDIUM:m==Mode.MEDIUM?Mode.LOW:Mode.HIGH;
 }
 public static Mode mask(int v) {
  switch(v){case 8:return Mode.OFF;case 256:return Mode.TRANSPARENCY;
   case 16:return Mode.HIGH;case 32:return Mode.MEDIUM;case 64:return Mode.LOW;
   case 128:return Mode.AUTO;default:return Mode.UNKNOWN;}
 }
 private Anc(){}
}
