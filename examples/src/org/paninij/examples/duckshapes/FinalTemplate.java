package org.paninij.examples.duckshapes;

import org.paninij.lang.Block;
import org.paninij.lang.Capsule;
import org.paninij.lang.Future;


@Capsule
public class FinalTemplate {

    public void unannotatedVoid() {

    }

    @Block
    public void atBlockVoid() {

    }

    @Future
    public void atFutureVoid() {

    }

    @Future
    public String unannotatedFinal(int some, boolean other) {
        return "Hello World!";
    }

    @Future
    public String atBlockFinal() {
        return "I'm blocked";
    }

    @Future
    public String atFutureFinal() {
        return "I'm a future";
    }


    public boolean unannotatedPrimitive() {
        return true;
    }

    @Future
    public boolean atFuturePrimitive() {
        return true;
    }

    @Block
    public boolean atBlockPrimitive() {
        return true;
    }


    public Object unannotatedObject() {
        return new Object();
    }

    @Block
    public Object atBlockObject() {
        return new Object();
    }

    @Future
    public Object atFutureObject() {
        return new Object();
    }
}