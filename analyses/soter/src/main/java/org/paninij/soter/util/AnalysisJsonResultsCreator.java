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
package org.paninij.soter.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.paninij.runtime.util.IdentitySet;

import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.util.collections.Pair;


/**
 * Visits the results of a (performed) `SoterAnalysis` and creates JSON to describe those
 * results. Note that the analysis need not be performed before construction of the creator; the
 * analysis only needs to be performed before calling `toJson()` for the first time.
 */
public abstract class AnalysisJsonResultsCreator
{
    private final static JsonGeneratorFactory jsonGeneratorFactory;

    static {
        Map<String, ?> JSON_GENERATOR_PROPERTIES = new HashMap<String, Void>();
        JSON_GENERATOR_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, null);
        jsonGeneratorFactory = Json.createGeneratorFactory(JSON_GENERATOR_PROPERTIES);
    }

    // Cache the created JSON artifacts; lazily evaluate them.
    protected JsonObject json;
    protected String jsonString;

    protected final Analysis analysis;

    protected AnalysisJsonResultsCreator(Analysis analysis)
    {
        assert analysis != null;
        this.analysis = analysis;
    }


    public abstract JsonObject toJson();
    
    
    // TODO: Refactor to remove this.
    public abstract CallGraph getCallGraph();


    public String toJsonString()
    {
        assert analysis.hasBeenPerformed;

        if (jsonString != null) {
            return jsonString;
        }
        if (json == null) {
            toJson();  // Caches the result in `json`.
            assert json != null;
        }

        StringWriter writer = new StringWriter();
        JsonGenerator generator = jsonGeneratorFactory.createGenerator(writer);
        generator.writeStartArray();
        generator.write(json);
        generator.writeEnd();
        generator.close();
        return writer.toString();
    }

    protected JsonArray toJson(Set<PointerKey> set)
    {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (PointerKey ptr : set) {
            builder.add(toJson(ptr));
        }
        return builder.build();
    }

    protected JsonObject toJson(PointerKey ptr)
    {
        if (ptr instanceof InstanceFieldKey) return toJson((InstanceFieldKey) ptr);
        else if (ptr instanceof LocalPointerKey) return toJson((LocalPointerKey) ptr);
        else if (ptr instanceof StaticFieldKey) return toJson((StaticFieldKey) ptr);
        else if (ptr instanceof ArrayContentsKey) return toJson((ArrayContentsKey) ptr);
        else throw new RuntimeException("Found `PointerKey` which can't be handled: " + ptr);
    }

    protected JsonObject toJson(InstanceFieldKey ptr)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "InstanceFieldKey");
        builder.add("field", ptr.getField().toString());
        return builder.build();
    }

    protected JsonObject toJson(LocalPointerKey ptr)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "LocalPointerKey");
        builder.add("method", ptr.getNode().getMethod().getSignature());
        builder.add("valueNumber", ptr.getValueNumber());
        return builder.build();
    }

    protected JsonObject toJson(StaticFieldKey ptr)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "StaticFieldKey");
        builder.add("field", ptr.getField().toString());
        return builder.build();
    }

    protected JsonObject toJson(ArrayContentsKey ptr)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "ArrayContentsKey");
        return builder.build();
    }

    protected JsonArray toJson(IdentitySet<InstanceKey> set)
    {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (InstanceKey inst : set) {
            builder.add(toJson(inst));
        }
        return builder.build();
    }

    protected JsonObject toJson(InstanceKey inst)
    {
        Iterator<Pair<CGNode, NewSiteReference>> iter = inst.getCreationSites(getCallGraph());

        // Assume that the returned iterator has exactly one `next()`.
        assert iter.hasNext() == true;
        Pair<CGNode, NewSiteReference> creationSite = iter.next();
        assert iter.hasNext() == false;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("type", "InstanceKey");
        builder.add("concreteType", inst.getConcreteType().toString());
        builder.add("creationSiteMethod", creationSite.fst.getMethod().getSignature());
        builder.add("creationSiteProgramCounter", creationSite.snd.getProgramCounter());
        builder.add("context", creationSite.fst.getContext().toString());
        return builder.build();
    }
}
