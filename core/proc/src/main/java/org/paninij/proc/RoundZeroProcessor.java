package org.paninij.proc;

import org.paninij.proc.check.Check.Result;
import org.paninij.proc.check.signature.AllSignatureChecks;
import org.paninij.proc.check.capsule.RoundZeroCapsuleChecks;
import org.paninij.proc.factory.CapsuleInterfaceFactory;
import org.paninij.proc.factory.SignatureInterfaceFactory;
import org.paninij.proc.model.Capsule;
import org.paninij.proc.model.CapsuleElement;
import org.paninij.proc.model.Signature;
import org.paninij.proc.model.SignatureElement;
import org.paninij.proc.util.ArtifactFiler;
import org.paninij.proc.util.ArtifactMaker;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

/**
 * @author dwtj
 */
@SupportedAnnotationTypes({"org.paninij.lang.Capsule", "org.paninij.lang.Signature"})
@SupportedOptions({})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RoundZeroProcessor extends AbstractProcessor {

    private RoundZeroCapsuleChecks capsuleChecks;
    private AllSignatureChecks signatureChecks;
    private ArtifactMaker artifactMaker;
    private CapsuleInterfaceFactory capsuleInterfaceFactory;
    private SignatureInterfaceFactory signatureInterfaceFactory;

    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);
        capsuleChecks = new RoundZeroCapsuleChecks(procEnv);
        signatureChecks = new AllSignatureChecks(procEnv);
        artifactMaker = new ArtifactFiler(processingEnv.getFiler());
        capsuleInterfaceFactory = new CapsuleInterfaceFactory();
        signatureInterfaceFactory = new SignatureInterfaceFactory();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver()) {
            artifactMaker.close();
            return false;
        }

        // For each element annotated `@Capsule`, check it. If it is okay, then create its capsule
        // interface and file it. Otherwise, error.
        for (Element elem : roundEnv.getElementsAnnotatedWith(org.paninij.lang.Capsule.class)) {
            try {
              Result result = capsuleChecks.checkCapsule(elem);
                if (result.ok()) {
                    // Make the capsule interface from this capsule core.
                    Capsule model = CapsuleElement.make((TypeElement) elem);
                    artifactMaker.add(capsuleInterfaceFactory.make(model));
                } else {
                    error(result.errMsg(), result.offender());
                }
            } catch (Exception e) {
                // If processor crashes, output stacktrace along with class name
                // that contained offending element.
                error("Error: \n" + getStackTrace(e), elem);
            }
        }

        // For each element annotated `@Signature`, check it. If it is okay, then create its
        // signature interface and file it. Otherwise, error.
        for (Element elem : roundEnv.getElementsAnnotatedWith(org.paninij.lang.Signature.class)) {
            try {
                Result result = signatureChecks.checkSignature(elem);
                if (result.ok()) {
                    Signature model = SignatureElement.make((TypeElement) elem);
                    artifactMaker.add(signatureInterfaceFactory.make(model));
                } else {
                    error(result.errMsg(), result.offender());
                }
            } catch (Exception e) {
                // If processor crashes, output stacktrace along with class name
                // that contained offending element.
                error("Error: \n" + getStackTrace(e), elem);
            }
        }

        // Make the capsule and signature interfaces now. All other sources will be made by the
        // other processor in the next round.
        artifactMaker.makeAll();

        return false;
    }

    public void error(String msg) {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, msg);
    }

    public void error(String msg, Element offender) {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, msg, offender);
    }
    
    /**
     * Helper method that takes an Exception
     * and converts the stack trace into a
     * String.
     * @param e
     *  Exception e
     * @return
     *  String representation of the stack trace.
     */
    private String getStackTrace(Exception e) {
        StringWriter error = new StringWriter();
        e.printStackTrace(new PrintWriter(error));
        return error.toString();
    }
}
