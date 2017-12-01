package com.langstok.nlp.corenlpnafmapper;

import com.langstok.nlp.corenlpnafmapper.map.CoreNLPtoKAF;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.GenericAnnotationSerializer;
import edu.stanford.nlp.util.Pair;
import ixa.kaflib.KAFDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Created by sander.puts on 6/29/2017.
 */
public class MapAnnotationToKAFTests {

    private final static Logger logger = Logger.getLogger(MapAnnotationToKAFTests.class.getName());

    private static final File MAVEN_TEST_RESOURCES = new File("src/test/resources");
    private static final File MAVEN_TEST_RESOURCES_RESULT = new File(MAVEN_TEST_RESOURCES+"/results");

    private KAFDocument result;
    private Annotation doc;

    @Test
    public void annotatedCorefToKaf() throws Exception {
        KAFDocument kafDocument = new KAFDocument("en","0.0");
        String start = CoreNLPtoKAF.createTimestamp();
        String stop = CoreNLPtoKAF.createTimestamp();

        result = CoreNLPtoKAF.mapAnnotations(doc, kafDocument, start, stop, "tokenize, pos");
    }

    @Before
    public void loadDocument() throws URISyntaxException, IOException, ClassNotFoundException {
        GenericAnnotationSerializer genericAnnotationSerializer = new GenericAnnotationSerializer();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("wikinews_1173_en-corenlp-annotated.ser.gz").getFile());
        FileInputStream fileStream = new FileInputStream(file);
        Pair<Annotation, InputStream> out = genericAnnotationSerializer.read(fileStream);
        doc = out.first;
    }

    @After
    public void writeDocument() throws IOException {
        Files.write(new File(MAVEN_TEST_RESOURCES_RESULT+"/output.xml").toPath(), result.toString().getBytes());
    }
}