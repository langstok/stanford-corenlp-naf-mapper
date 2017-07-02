package com.langstok.nlp.corenlpnafmapper;

import com.langstok.nlp.corenlpnafmapper.map.KAFtoCoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import ixa.kaflib.KAFDocument;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;


public class MapKAFtoCoreNLPTests {

    private static final Logger logger = Logger.getLogger(MapKAFtoCoreNLPTests.class.getName());

    private KAFDocument kafDocument;
    private KAFDocument result;
    private StanfordCoreNLP pipelineTok;
    private StanfordCoreNLP pipelineSsplit;
    private StanfordCoreNLP pipeline;

    @Test
    public void readKAFDocumentToAnnotation(){
        Annotation document = new Annotation(kafDocument.getRawText());
        pipeline.annotate(document);

        Annotation kafDocumentParsed = KAFtoCoreNLP.getAnnotation(kafDocument);
        kafDocumentParsed.toString();

        List<CoreLabel> documentTokens = document.get(CoreAnnotations.TokensAnnotation.class);
        List<CoreLabel> kafDocumentTokens = kafDocumentParsed.get(CoreAnnotations.TokensAnnotation.class);
        Map<String, Set<String>> posKafToStanford = new HashMap<>();

        for(int i=0; i<300; i++){

            CoreLabel coreLabelDocument = documentTokens.get(i);
            CoreLabel coreLabelKaf = kafDocumentTokens.get(i);
            if(!posKafToStanford.containsKey(coreLabelKaf.get(CoreAnnotations.PartOfSpeechAnnotation.class))){
                posKafToStanford.put(coreLabelKaf.get(CoreAnnotations.PartOfSpeechAnnotation.class), new HashSet<>());
            }
            posKafToStanford.get(coreLabelKaf.get(CoreAnnotations.PartOfSpeechAnnotation.class))
                    .add(coreLabelDocument.get(CoreAnnotations.PartOfSpeechAnnotation.class));

            /*
            assertEquals(coreLabelDocument.get(CoreAnnotations.ValueAnnotation.class), coreLabelKaf.get(CoreAnnotations.ValueAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.TextAnnotation.class), coreLabelKaf.get(CoreAnnotations.TextAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.OriginalTextAnnotation.class), coreLabelKaf.get(CoreAnnotations.OriginalTextAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), coreLabelKaf.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.CharacterOffsetEndAnnotation.class), coreLabelKaf.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.BeforeAnnotation.class), coreLabelKaf.get(CoreAnnotations.BeforeAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.AfterAnnotation.class), coreLabelKaf.get(CoreAnnotations.AfterAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.IndexAnnotation.class), coreLabelKaf.get(CoreAnnotations.IndexAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.SentenceIndexAnnotation.class), coreLabelKaf.get(CoreAnnotations.SentenceIndexAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.PartOfSpeechAnnotation.class), coreLabelKaf.get(CoreAnnotations.PartOfSpeechAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.LemmaAnnotation.class), coreLabelKaf.get(CoreAnnotations.LemmaAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.TokenBeginAnnotation.class), coreLabelKaf.get(CoreAnnotations.TokenBeginAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.TokenEndAnnotation.class), coreLabelKaf.get(CoreAnnotations.TokenEndAnnotation.class));
            assertEquals(coreLabelDocument.get(CoreAnnotations.NamedEntityTagAnnotation.class), coreLabelKaf.get(CoreAnnotations.NamedEntityTagAnnotation.class));*/
        }

        logger.info(posKafToStanford.toString());
    }




}
