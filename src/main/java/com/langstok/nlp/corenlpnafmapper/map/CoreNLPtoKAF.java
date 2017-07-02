package com.langstok.nlp.corenlpnafmapper.map;

import com.langstok.nlp.corenlpnafmapper.constant.KAFConstants;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Span;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.langstok.nlp.corenlpnafmapper.constant.NLPConstants.PARAGRAPH;


/**
 * Parts copied from @https://github.com/ragerri (TOK, POS)
 */
public final class CoreNLPtoKAF {

    private static final Logger logger = Logger.getLogger(CoreNLPtoKAF.class.getName());

    /**
     * sentiment?: tokenize,ssplit,pos,lemma,ner,parse,depparse,mention,dcoref,sentiment???
     * coref: tokenize, ssplit, pos, lemma, ner, parse, dcoref
     * tempcausal: POS dep timex_id	timex_type	timex_value	entity	pred_class	event_id	role1	role2	role3	is_arg_pred	has_semrole	chunk	main_verb	connectives	morpho	tense+aspect+pol	O

     * @param doc
     * @param kafDocument
     * @return
     */
    public static KAFDocument map(Annotation doc, KAFDocument kafDocument) {
        setTOKToKAF(doc, kafDocument);
        setPOSToKAF(doc, kafDocument);
        setCorefToKAF(doc, kafDocument);
        return kafDocument;
    }




    public static KAFDocument setTOKToKAF(Annotation doc, final KAFDocument kaf){

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
                KAFConstants.LAYER_TEXT, getModelName(KAFConstants.LP_TOK));
        newLp.setBeginTimestamp();
        newLp.setEndTimestamp();
        newLp.setVersion(getCoreNLPVersion());
        return kaf;
    }


    public static KAFDocument setPOSToKAF(Annotation doc, KAFDocument kaf){

        List<List<WF>> sentences = kaf.getSentences();

        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {

            Iterator<CoreMap> itr = doc.get(CoreAnnotations.SentencesAnnotation.class).iterator();
            for (List<WF> sentence : sentences) {

                CoreMap docSentence = itr.next();
                List<CoreLabel> docTokens = docSentence.get(CoreAnnotations.TokensAnnotation.class);

                for (int i = 0; i < docTokens.size(); i++) {
                    List<WF> wfs = new ArrayList<WF>();
                    CoreLabel coreLabel = docTokens.get(i);
                    wfs.add(sentence.get(i));
                    String posTag = coreLabel.tag();
                    String posId = mapEnglishTagSetToKaf(posTag);
                    String type = setTermType(posId); // type
                    String lemma = coreLabel.lemma();
                    kaf.createTermOptions(type, lemma, posId, posTag, wfs);
                }
            }
        }
        KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
                KAFConstants.LAYER_TERMS, getModelName(KAFConstants.LP_POS));
        newLp.setBeginTimestamp();
        newLp.setEndTimestamp();
        newLp.setVersion(getCoreNLPVersion());
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


    private static KAFDocument setCorefToKAF(Annotation doc, KAFDocument kaf) {
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
                KAFConstants.LAYER_COREFENCES, getModelName(KAFConstants.LP_DCOREF));
        newLp.setBeginTimestamp();
        newLp.setEndTimestamp();
        newLp.setVersion(getCoreNLPVersion());
        return kaf;
    }

    private static String getModelName(String lp){
        return "langstok-corenlp-"+lp;
    }

    private static String getCoreNLPVersion(){
        return CoreMap.class.getPackage().getImplementationVersion();
    }
}
