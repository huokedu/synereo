package com.biosimilarity.lift.lib.term.Prolog.Absyn; // Java Package generated by the BNF Converter.

public abstract class Functor implements java.io.Serializable {
  public abstract <R,A> R accept(Functor.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.biosimilarity.lift.lib.term.Prolog.Absyn.FAtm p, A arg);

  }

}
