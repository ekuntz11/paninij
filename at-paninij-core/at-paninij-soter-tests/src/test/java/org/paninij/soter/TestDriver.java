package org.paninij.soter;

import org.junit.Test;

public class TestDriver
{
    String[] args = {"-classPath", "target/classes",
                     "-classPathFile", "target/dependencies.txt",
                     "-classOutput", "target/classes",
                     "-analysisReports", "logs/soter/analysis-reports",
                     "-noInstrument",
                     "@target/capsule_list.txt"
    };
    
    /**
     * Tries running the soter analysis (without instrumentation) to see if it crashes.
     */
    @Test
    public void noInstrumentationSmokeTest()
    {
        org.paninij.soter.Main.main(args);
    }

}