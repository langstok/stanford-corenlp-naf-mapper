package com.langstok.nlp.corenlpnafmapper;

import com.langstok.nlp.corenlpnafmapper.map.CoreNLPtoKAF;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.GenericAnnotationSerializer;
import edu.stanford.nlp.util.Pair;
import ixa.kaflib.KAFDocument;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;

/**
 * Created by sander.puts on 6/29/2017.
 */
public class SerializedToKAFDocument {

    private final static Logger logger = Logger.getLogger(SerializedToKAFDocument.class);

    private KAFDocument result;
    private Annotation doc;

    @Test
    public void annotatedCorefToKaf() throws Exception {
        KAFDocument kafDocument = new KAFDocument("en","0.0");
        result = CoreNLPtoKAF.convert(doc, kafDocument);
    }

    @Before
    public void loadDocument() throws URISyntaxException, IOException, ClassNotFoundException {
        GenericAnnotationSerializer genericAnnotationSerializer = new GenericAnnotationSerializer();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("standford-out-preann-result.ser.gz").getFile());
        FileInputStream fileStream = new FileInputStream(file);
        Pair<Annotation, InputStream> out = genericAnnotationSerializer.read(fileStream);
        doc = out.first;
    }

    @After
    public void writeDocument() throws IOException {
        Files.write(new File( "./output.naf").toPath(), result.toString().getBytes());
    }
}