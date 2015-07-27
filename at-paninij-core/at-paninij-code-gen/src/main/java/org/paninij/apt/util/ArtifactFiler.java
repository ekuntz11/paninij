package org.paninij.apt.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

public class ArtifactFiler implements ArtifactMaker
{
    protected final Filer filer;
    protected final Set<Artifact> artifacts = new HashSet<Artifact>();

    private ArtifactFiler(Filer filer)
    {
        this.filer = filer;
    }
    
    public static ArtifactFiler make(Filer filer)
    {
        return new ArtifactFiler(filer);
    }

    @Override
    public void add(Artifact artifact)
    {
        if (artifact != null) {
            artifacts.add(artifact);
        }
    }

    @Override
    public void makeAll()
    {
        try
        {
            for (Artifact artifact : artifacts)
            {
                // Ignore any pre-made, user-defined artifacts.
                if (artifact instanceof UserArtifact)
                    continue;
                
                JavaFileObject sourceFile = filer.createSourceFile(artifact.getQualifiedName());
                sourceFile.openWriter().append(artifact.getContent()).close();
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Failed to make all artifacts: " + ex, ex);
        }
        artifacts.clear();
    }
    
    @Override
    public void close()
    {
        // Nothing to do.
    }
}
