package com.biosimilarity.lift.model.specialK.Absyn; // Java Package generated by the BNF Converter.

public abstract class Concretion implements java.io.Serializable {
  public abstract <R,A> R accept(Concretion.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.biosimilarity.lift.model.specialK.Absyn.Applicand p, A arg);

  }

}
