package com.biosimilarity.seleKt.model.ill.vm.illvm.Absyn; // Java Package generated by the BNF Converter.

public class InrV extends Value {
  public final Value value_;

  public InrV(Value p1) { value_ = p1; }

  public <R,A> R accept(com.biosimilarity.seleKt.model.ill.vm.illvm.Absyn.Value.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof com.biosimilarity.seleKt.model.ill.vm.illvm.Absyn.InrV) {
      com.biosimilarity.seleKt.model.ill.vm.illvm.Absyn.InrV x = (com.biosimilarity.seleKt.model.ill.vm.illvm.Absyn.InrV)o;
      return this.value_.equals(x.value_);
    }
    return false;
  }

  public int hashCode() {
    return this.value_.hashCode();
  }


}
