package org.paninij.proc.check.capsule.fields.recursive;

import org.paninij.lang.Capsule;
import org.paninij.lang.Local;

@Capsule
public class CycleOkOneTemplate {
    @Local CycleOkTwo two;
    void design(CycleOkOne self) {
        two.imports(self);
    }
}