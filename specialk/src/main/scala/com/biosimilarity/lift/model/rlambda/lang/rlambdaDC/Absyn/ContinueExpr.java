package com.biosimilarity.rlambdaDC.lang.rlambdaDC.Absyn; // Java Package generated by the BNF Converter.

public abstract class ContinueExpr implements java.io.Serializable {
  public abstract <R,A> R accept(ContinueExpr.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.biosimilarity.rlambdaDC.lang.rlambdaDC.Absyn.Prompt p, A arg);
    public R visit(com.biosimilarity.rlambdaDC.lang.rlambdaDC.Absyn.PushPrompt p, A arg);
    public R visit(com.biosimilarity.rlambdaDC.lang.rlambdaDC.Absyn.Subcontinuation p, A arg);
    public R visit(com.biosimilarity.rlambdaDC.lang.rlambdaDC.Absyn.PushSubCont p, A arg);

  }

}