package com.langstok.nlp.corenlpnafmapper.map;

import com.langstok.nlp.corenlpnafmapper.constant.CoreNLPAnnotator;
import com.langstok.nlp.corenlpnafmapper.constant.KAFConstants;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import ixa.kaflib.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static com.langstok.nlp.corenlpnafmapper.constant.NLPConstants.PARAGRAPH;


/**
 * Parts copied from @https://github.com/ragerri (TOK, POS)
 */
public final class CoreNLPtoKAF {

    private static final Logger logger = Logger.getLogger(CoreNLPtoKAF.class.getName());

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");


    public static KAFDocument mapAnnotations(Annotation doc, KAFDocument kafDocument, String annotators, Instant start, Instant stop) {
        List<String> annotatorsList = Arrays.asList(annotators.replaceAll(" ","").toLowerCase().split(","));

        if(annotatorsList.contains(CoreNLPAnnotator.TOK))
            mapTOKToKAF(doc, kafDocument, start, stop);
        if(annotatorsList.contains(CoreNLPAnnotator.POS))
            mapPOSToKAF(doc, kafDocument, start, stop);
        if(annotatorsList.contains(CoreNLPAnnotator.ENTITYMENTIONS))
            mapNerToKAF(doc, kafDocument, start, stop);
        if(annotatorsList.contains(CoreNLPAnnotator.DCOREF))
            mapCorefToKAF(doc, kafDocument, start, stop, CoreNLPAnnotator.DCOREF);
        if(annotatorsList.contains(CoreNLPAnnotator.COREF))
            mapCorefToKAF(doc, kafDocument, start, stop, CoreNLPAnnotator.COREF);

        return kafDocument;
    }




    public static KAFDocument mapTOKToKAF(Annotation doc, final KAFDocument kaf, Instant start, Instant stop){

        int noSents = 0;
        int noParas = 1;

        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {

            List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
            for(CoreMap sentence : sentences) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                noSents = noSents + 1;

                for(final CoreLabel token : tokens) {
                    if (token.originalText().equals(PARAGRAPH)) {
                        ++noParas;
                        // TODO debug this
                        if (noSents < noParas) {
                            ++noSents;
                        }
                    } else {
                        final WF wf = kaf.newWF(token.beginPosition(), token.originalText(),
                                token.sentIndex());
                        wf.setLength(token.endPosition()-token.beginPosition());
                        wf.setPara(noParas);
                    }
                }
            }
        }
        KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
                KAFConstants.LAYER_TEXT, getModelName(CoreNLPAnnotator.TOK), getCoreNLPVersion());
        newLp.setBeginTimestamp(createTimestamp(start));
        newLp.setEndTimestamp(createTimestamp(stop));
        return kaf;
    }


    public static KAFDocument mapPOSToKAF(Annotation doc, KAFDocument kaf, Instant start, Instant stop){

        List<List<WF>> sentences = kaf.getSentences();
        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {

            Iterator<CoreMap> itr = doc.get(CoreAnnotations.SentencesAnnotation.class).iterator();
            for (List<WF> sentence : sentences) {

                CoreMap docSentence = itr.next();
                List<CoreLabel> docTokens = docSentence.get(CoreAnnotations.TokensAnnotation.class);

                for (int i = 0; i < docTokens.size(); i++) {
                    Span<WF> wfSpan = KAFDocument.newWFSpan();
                    wfSpan.addTarget(sentence.get(i));

                    CoreLabel coreLabel = docTokens.get(i);
                    String posTag = coreLabel.tag();
                    String posId = mapEnglishTagSetToKaf(posTag);
                    String type = setTermType(posId); // type
                    String lemma = coreLabel.lemma();

                    Term term = kaf.newTerm(wfSpan);
                    term.setType(type);
                    term.setLemma(lemma);
                    term.setPos(posId);
                    term.setMorphofeat(posTag);
                }
            }
        }
        KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
                KAFConstants.LAYER_TERMS, getModelName(CoreNLPAnnotator.POS), getCoreNLPVersion());
        newLp.setBeginTimestamp(createTimestamp(start));
        newLp.setEndTimestamp(createTimestamp(stop));
        return kaf;
    }

    private static String setTermType(String postag) {
        if (postag.startsWith("N") || postag.startsWith("V")
                || postag.startsWith("G") || postag.startsWith("A")) {
            return "open";
        } else {
            return "close";
        }
    }


    private static String mapEnglishTagSetToKaf(String postag) {
        if (postag.startsWith("RB")) {
            return "A"; // adverb
        } else if (postag.equalsIgnoreCase("CC")) {
            return "C"; // conjunction
        } else if (postag.startsWith("D") || postag.equalsIgnoreCase("PDT")) {
            return "D"; // determiner and predeterminer
        } else if (postag.startsWith("J")) {
            return "G"; // adjective
        } else if (postag.equalsIgnoreCase("NN") || postag.equalsIgnoreCase("NNS")) {
            return "N"; // common noun
        } else if (postag.startsWith("NNP")) {
            return "R"; // proper noun
        } else if (postag.equalsIgnoreCase("TO") || postag.equalsIgnoreCase("IN")) {
            return "P"; // preposition
        } else if (postag.startsWith("PRP") || postag.startsWith("WP")) {
            return "Q"; // pronoun
        } else if (postag.startsWith("V")) {
            return "V"; // verb
        } else {
            return "O"; // other
        }
    }

    private static KAFDocument mapNerToKAF(Annotation doc, KAFDocument kaf, Instant start, Instant stop) {

        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null) {

            for(CoreMap sentence : sentences){

                List<CoreMap> mentions = sentence.get(CoreAnnotations.MentionsAnnotation.class);
                if (mentions != null) {

                    for(CoreMap mention : mentions){

                        String namedEntityTag = mention.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        int begin = mention.get(CoreAnnotations.TokenBeginAnnotation.class);
                        int end = mention.get(CoreAnnotations.TokenEndAnnotation.class);

                        List<String> wfIds = new ArrayList<>();
                        for(int wfIndex=begin; wfIndex<end; wfIndex++){
                            wfIds.add(kaf.getWFs().get(wfIndex).getId());
                        }

                        List<Term> nameTerms = kaf.getTermsFromWFs(wfIds);
                        Span<Term> neSpan = KAFDocument.newTermSpan(nameTerms);
                        List<Span<Term>> references = new ArrayList<>();
                        references.add(neSpan);
                        Entity neEntity = kaf.newEntity(references);
                        neEntity.setType(namedEntityTag);
                    }
                }
            }
        }
        KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
                KAFConstants.LAYER_ENITITIES, getModelName(CoreNLPAnnotator.ENTITYMENTIONS), getCoreNLPVersion());
        newLp.setBeginTimestamp(createTimestamp(start));
        newLp.setEndTimestamp(createTimestamp(stop));
        return kaf;
    }

    private static KAFDocument mapCorefToKAF(Annotation doc, KAFDocument kaf, Instant start, Instant stop, String languageProcessor) {
        if (doc.get(CorefCoreAnnotations.CorefChainAnnotation.class) != null) {

            Map<Integer, CorefChain> corefChains = doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
            for (CorefChain chain : corefChains.values()) {
                List<Span<Term>> corefList = new ArrayList<>();
                chain.getMentionsInTextualOrder().forEach(mention -> {
                    Span<Term> termSpan = KAFDocument.newTermSpan();
                    for(int termIndex = mention.startIndex-1; termIndex <mention.endIndex-1; termIndex++)
                        termSpan.addTarget(kaf.getSentenceTerms(mention.sentNum-1).get(termIndex));
                    corefList.add(termSpan);
                });
                kaf.newCoref(corefList);
            }
        }
        KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
                KAFConstants.LAYER_COREFENCES, getModelName(languageProcessor), getCoreNLPVersion());
        newLp.setBeginTimestamp(createTimestamp(start));
        newLp.setEndTimestamp(createTimestamp(stop));
        return kaf;
    }


    private static String getModelName(String lp){
        return "langstok-corenlp-"+lp;
    }

    private static String getCoreNLPVersion(){
        return CoreMap.class.getPackage().getImplementationVersion();
    }

    public static String createTimestamp(Instant instant) {
        return sdf.format(Date.from(instant));
    }

}
