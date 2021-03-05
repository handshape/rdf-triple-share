package com.handshape.rdftripleshare;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jturner
 */
public class IntegrationTest {

    public static final String BASE_URL = "http://handshape.com/rdf/";

    @Test
    public void integrationTest() throws IOException {
        System.out.println("RDF validation Test");
        File root = new File("rdf");
        Assertions.assertTrue(root.exists());
        Assertions.assertTrue(root.canRead());
        Assertions.assertTrue(root.isDirectory());
        MutableBoolean pass = new MutableBoolean(true);
        MutableLong count = new MutableLong(0L);
        System.err.println();

        Files.walk(root.toPath()).filter(path -> Files.exists(path, LinkOption.NOFOLLOW_LINKS)).filter(path -> path.toFile().isFile()).forEach(path -> {
            ErrorHandler handler = new RdfParsingErrorHandler(pass, path);
            Model model = ModelFactory.createDefaultModel();
            switch (FilenameUtils.getExtension(path.toString())) {
                case "ttl":
                case "nt":
                    RDFParser.source(path).checking(true).errorHandler(handler).build().parse(model);
                    count.add(model.size());
                    break;
                default:
                    System.err.println("ERROR: Unexpected file extension at " + path);
                    pass.setFalse();
                    break;
            }
        });
        Assertions.assertTrue(pass.getValue(), "RDF parsng errors occurred.");
        System.out.println("Parsed " + count.getValue() + " triples.");
    }

    private class RdfParsingErrorHandler implements ErrorHandler {

        private final MutableBoolean pass;
        private final Path path;

        public RdfParsingErrorHandler(MutableBoolean pass, Path path) {
            this.pass = pass;
            this.path = path;
        }

        @Override
        public void warning(String message, long line, long col) {
            logParseEvent("WARN", line, col, message);
        }

        @Override
        public void error(String message, long line, long col) {
            logParseEvent("ERROR", line, col, message);
            pass.setFalse();
        }

        @Override
        public void fatal(String message, long line, long col) {
            logParseEvent("FATAL", line, col, message);
            pass.setFalse();
        }

        private void logParseEvent(String level, long line, long col, String message) {
            System.err.println(level + ": " + path + " " + line + ":" + col + " - " + message);
        }
    }

}
