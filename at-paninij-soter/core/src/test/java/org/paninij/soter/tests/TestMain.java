package org.paninij.soter.tests;

import org.junit.Test;
import org.paninij.soter.Main;

public class TestMain
{
    @Test
    public void testMainWithLeakyServer() throws Exception
    {
        String[] args = {"-classpath", "lib/at-paninij-soter-tests.jar:lib/at-paninij-runtime.jar",
                         "Lorg/paninij/soter/tests/LeakyServerTemplate"};
        Main.main(args);
    }
    
    @Test
    public void testMainWithActiveClient() throws Exception
    {
        String[] args = {"-classpath", "lib/at-paninij-soter-tests.jar:lib/at-paninij-runtime.jar",
                         "Lorg/paninij/soter/tests/ActiveClientTemplate"};
        Main.main(args);
    }
}
