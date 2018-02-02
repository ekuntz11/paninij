/*******************************************************************************
 * This file is part of the Panini project at Iowa State University.
 *
 * @PaniniJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * @PaniniJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with @PaniniJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more details and the latest version of this code please see
 * http://paninij.org
 *
 * Contributors:
 * 	Dr. Hridesh Rajan,
 * 	Dalton Mills,
 * 	David Johnston,
 * 	Trey Erenberger
 *******************************************************************************/
package org.paninij.proc.check.core;

import static java.text.MessageFormat.format;

import static javax.lang.model.element.ElementKind.METHOD;

import static javax.lang.model.type.TypeKind.VOID;
import static org.paninij.proc.check.Check.Result.OK;
import static org.paninij.proc.check.Check.Result.error;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import org.paninij.lang.Block;
import org.paninij.lang.Duck;
import org.paninij.lang.Future;
import org.paninij.proc.check.duckability.DuckabilityChecks;

/**
 * Checks that the PaniniJ annotations on procedures (e.g. {@link Duck @Duck},
 * {@link Future @Future}, and {@link Block @Block}) are valid.
 *
 * There are a few ways in which a procedure annotation is invalid. Primarily, a procedure
 * annotation is invalid if it has an {@link Duck @Duck} annotation (either explicitly or implied),
 * but the return type is unduckable. An instance of `DuckabilityChecks` is used to check the
 * duckability of the return types of all `@Duck` procedures of a given capsule core or
 * signature core.
 */
public class CheckProcAnnotations implements CoreCheck
{
    private final DuckabilityChecks checker;

    public CheckProcAnnotations(ProcessingEnvironment procEnv) {
        this.checker = new DuckabilityChecks(procEnv);
    }

	@Override
	public Result checkCore(TypeElement core, CoreKind coreKind) {
		for (Element member : core.getEnclosedElements()) {
			if (isUnannotatedVoidProcedure(member)) {
				continue; // This kind of procedure is ok, so skip the duckability check on it.
			}
			// Check the duckability of a procedure's return type if that procedure is
			// either
			// explicitly or implicitly an `@Duck` procedure:
			if (isDuckProcedure(member)) {
				Result result = checker.checkDuckability(((ExecutableElement) member).getReturnType());
				if (!result.ok()) {
					String err = "A {0} core has a procedure whose return type cannot be " + "ducked. {1}";
					err = format(err, coreKind, result.errMsg());
					return error(err, CheckProcAnnotations.class, member);
				}
			}

			// check if '@Duck' procedure is also annotated as a '@Future' procedure
			if (hasAnnotation(member, Future.class) && hasAnnotation(member, Duck.class)) {
				String err = "A procedure cannot be annotated with both '@Duck' and '@Future'.";
				Result result = error(err, CheckProcAnnotations.class, member);
				return result;

			}
		}
		return OK;
	}

    /**
     * Indicates whether a member has none of the PaniniJ procedure annotations and it returns
     * `void`.
     */
    private static boolean isUnannotatedVoidProcedure(Element member) {
        if (member.getKind() != METHOD) {
            return false;
        }
        ExecutableElement method = (ExecutableElement) member;
        return method.getReturnType().getKind() == VOID
            && !isDecl(method)
            && !hasAnnotation(method, Duck.class)
            && !hasAnnotation(method, Future.class)
            && !hasAnnotation(method, Block.class);
    }

    /**
     * Indicates whether a member is either explicitly or implicitly a {@link Duck @Duck}
     * procedure.
     */
    private static boolean isDuckProcedure(Element member) {
        if (member.getKind() != METHOD){
            return false;
        }
        Set<Modifier> modifiers = ((ExecutableElement)member).getModifiers();
        
        return !isDecl(member)
            && !hasAnnotation(member, Future.class)
            && !hasAnnotation(member, Block.class)
            && !modifiers.contains(Modifier.STATIC)
            && !modifiers.contains(Modifier.PRIVATE);
    }

    private static boolean isDecl(Element member) {
        return isDeclName(member.getSimpleName().toString());
    }
    
    private static boolean isDeclName(String memberName) {
        return memberName.equals("init")
            || memberName.equals("design")
            || memberName.equals("run");
    }
    
    private static <A extends Annotation> boolean hasAnnotation(Element elem, Class<A> anno) {
        return elem.getAnnotation(anno) != null;
    }
}
