package com.handshape.rdftripleshare;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jturner
 */
public class IntegrationTest {

    @Test
    public void integrationTest() throws Exception {
        System.out.println("RDF model build and export");
        // MutableBoolean that serves as a flag of the "pass" state to the test 
        // framework. Mutable shared state because of the accursed streams 
        // framework in the file tree walker.
        MutableBoolean pass = new MutableBoolean(true);
        Set<String> knownStatutoryInstruments = new TreeSet<>();
        System.err.println();

        // This is the Apache Jena RDF model. In-memory for now. There are persistent implementations we can use later.
        Model model = ModelFactory.createDefaultModel();
        RdfGatheringAgent agent = new RdfGatheringAgent();
        // Add local facts and prefixes to the model.
        agent.fetchAndParseLocalTurtle(model, pass);

        agent.fetchAndParseStatutoryInstruments(model, knownStatutoryInstruments);

        // Add local facts and prefixes to the model.
        agent.fetchAndParseDepartments(model);

        // Add the RIAS facts to the model.
        agent.fetchAndParseRias(model, knownStatutoryInstruments);

        // Add the acts and regs facts to the model.
        agent.fetchAndParseActsAndConsolidatedRegs(model, knownStatutoryInstruments);
        // Add the acts and regs facts to the model.
        agent.fetchAndParseMetadata(model);
        Assertions.assertTrue(pass.getValue(), "RDF parsing errors occurred.");
        System.out.println("Parsed " + model.size() + " triples.");

        // Write the whole model out as a turtle file.
        try (OutputStream ttlOutputStream = new FileOutputStream("target/out.ttl")) {
            model.write(ttlOutputStream, "TTL");
            ttlOutputStream.flush();
        }

        // Write the model out as the WASM-SQLite DB
        agent.writeModelToSqlite(model);
    }

}
