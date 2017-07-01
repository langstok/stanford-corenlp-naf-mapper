package com.langstok.nlp.corenlptonaf.map;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import ixa.kaflib.Entity;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sander.puts on 5/25/2017.
 */

/**
 * KAFDocument to Annotation for: tokenize,ssplit,pos,lemma,ner,parse
 */
public final class KAFtoCoreNLP {

    /**
     * // these are all the sentences in this document
     // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
     List<CoreMap> sentences = document.get(SentencesAnnotation.class);

     for(CoreMap sentence: sentences) {
     // traversing the words in the current sentence
     // a CoreLabel is a CoreMap with additional token-specific methods
     for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
     // this is the text of the token
     String word = token.get(TextAnnotation.class);
     // this is the POS tag of the token
     String pos = token.get(PartOfSpeechAnnotation.class);
     // this is the NER label of the token
     String ne = token.get(NamedEntityTagAnnotation.class);
     }

     // this is the parse tree of the current sentence
     Tree tree = sentence.get(TreeAnnotation.class);

     // this is the Stanford dependency graph of the current sentence
     SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
     }
     * @param kafDocument
     * @return
     */


    public static Annotation getAnnotation(KAFDocument kafDocument){


        StringBuilder builder = new StringBuilder();

        Annotation document = new Annotation(kafDocument.getRawText());

        document.set(CoreAnnotations.TokensAnnotation.class, getCorelabels(kafDocument));


        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        //List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);


        /*for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        }*/



//        Annotation document = new Annotation(getTokenize(kafDocument));
//        if(kafDocument.getWFs()!=null && !kafDocument.getWFs().isEmpty())
//            return document;
//        else
//            getTokenize(kafDocument);

        return document;
    }

    public static List<CoreLabel> getCorelabels(KAFDocument kafDocument) {

        int sentenceIndex = -1;
        int wfSentenceIndex = -1;
        int coreLabelCount = 0;

        List<CoreLabel> coreLabels = new ArrayList<>();
        for(Term term : kafDocument.getTerms()){

            if(term.getSent()!=sentenceIndex){
                sentenceIndex = term.getSent();
                wfSentenceIndex = 1;
            }
            WF wf = term.getSpan().getFirstTarget();

            String token = encodeToken(term.getStr());
            CoreLabel coreLabel = new CoreLabel();
            coreLabel.set(CoreAnnotations.ValueAnnotation.class, token); //0
            coreLabel.set(CoreAnnotations.TextAnnotation.class, token); //1
            coreLabel.set(CoreAnnotations.OriginalTextAnnotation.class, token); //2
            coreLabel.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, wf.getOffset()); //3
            coreLabel.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, wf.getOffset()+term.getStr().length()); //4
            coreLabel.set(CoreAnnotations.BeforeAnnotation.class, getBefore(wf, kafDocument)); //5
            coreLabel.set(CoreAnnotations.AfterAnnotation.class,getAfter(wf, kafDocument)); //6
            coreLabel.set(CoreAnnotations.IndexAnnotation.class, wfSentenceIndex); //7
            coreLabel.set(CoreAnnotations.SentenceIndexAnnotation.class, 1); //8 check if indexes are equal
            coreLabel.set(CoreAnnotations.PartOfSpeechAnnotation.class, term.getPos()); //9
            coreLabel.set(CoreAnnotations.LemmaAnnotation.class, term.getLemma()); //10
            coreLabel.set(CoreAnnotations.TokenBeginAnnotation.class, coreLabelCount); //11
            coreLabel.set(CoreAnnotations.TokenEndAnnotation.class, coreLabelCount+1); //2

            List<Entity> entityList = kafDocument.getEntitiesByTerm(term);
            if(!entityList.isEmpty())
                coreLabel.set(CoreAnnotations.NamedEntityTagAnnotation.class, entityList.get(0).getType()); //13 NER
            else
                //coreLabel.set(CoreAnnotations.NamedEntityTagAnnotation.class, NO_ENTITY); //13 NER


            //coreLabel.set(CoreAnnotations.ParagraphAnnotation.class, 0); //14 PARSE
            //coreLabel.set(CoreAnnotations.UtteranceAnnotation.class, 0); //15 PARSE
            //coreLabel.set(CoreAnnotations.SpeakerAnnotation.class, ""); //16 PARSE
            coreLabel.setDocID(wf.getId());
            coreLabels.add(coreLabel);
            wfSentenceIndex++;
            coreLabelCount++;
        }


        return coreLabels;
    }


    /**
     * 0 = "Airbus is owned by European Aeronautic Defense & Space Company (EADS)."
     1 = {Integer@8810} "1466"
     2 = {Integer@8811} "1536"
     3 = {ArrayList@8812}  size = 14
     4 = {Integer@8813} "242"
     5 = {Integer@8814} "256"
     6 = {Integer@8815} "10"
     7 = {ArrayList@8816}  size = 14

     0 = {CoreLabel@8837} "Airbus-1"
     1 = {CoreLabel@8838} "is-2"
     2 = {CoreLabel@8839} "owned-3"
     3 = {CoreLabel@8840} "by-4"
     4 = {CoreLabel@8841} "European-5"
     5 = {CoreLabel@8842} "Aeronautic-6"
     6 = {CoreLabel@8843} "Defense-7"
     7 = {CoreLabel@8844} "&-8"
     8 = {CoreLabel@8845} "Space-9"
     9 = {CoreLabel@8846} "Company-10"
     10 = {CoreLabel@8847} "-LRB--11"
     11 = {CoreLabel@8848} "EADS-12"
     12 = {CoreLabel@8849} "-RRB--13"
     13 = {CoreLabel@8850} ".-14"

     8 = {LabeledScoredTreeNode@8817}  size = 38 //PARSE
     9 = {SemanticGraph@8818} "-> owned/VBN (root)\n  -> Airbus/NNP (nsubjpass)\n  -> is/VBZ (auxpass)\n  -> Defense/NNP (nmod:agent)\n    -> by/IN (case)\n    -> European/JJ (amod)\n    -> Aeronautic/NNP (compound)\n    -> &/CC (cc)\n    -> Company/NNP (conj:&)\n      -> Space/NNP (compound)\n    -> EADS/NNP (appos)\n      -> -LRB-/-LRB- (punct)\n      -> -RRB-/-RRB- (punct)\n  -> ./. (punct)\n"
     10 = {SemanticGraph@8819} "-> owned/VBN (root)\n  -> Airbus/NNP (nsubjpass)\n  -> is/VBZ (auxpass)\n  -> Defense/NNP (nmod)\n    -> by/IN (case)\n    -> European/JJ (amod)\n    -> Aeronautic/NNP (compound)\n    -> &/CC (cc)\n    -> Company/NNP (conj)\n      -> Space/NNP (compound)\n    -> EADS/NNP (appos)\n      -> -LRB-/-LRB- (punct)\n      -> -RRB-/-RRB- (punct)\n  -> ./. (punct)\n"
     11 = {SemanticGraph@8820} "-> owned/VBN (root)\n  -> Airbus/NNP (nsubjpass)\n  -> is/VBZ (auxpass)\n  -> Defense/NNP (nmod:agent)\n    -> by/IN (case)\n    -> European/JJ (amod)\n    -> Aeronautic/NNP (compound)\n    -> &/CC (cc)\n    -> Company/NNP (conj:&)\n      -> Space/NNP (compound)\n    -> EADS/NNP (appos)\n      -> -LRB-/-LRB- (punct)\n      -> -RRB-/-RRB- (punct)\n  -> Company/NNP (nmod:agent)\n  -> ./. (punct)\n"
     12 = {SemanticGraph@8821} "-> owned/VBN (root)\n  -> Airbus/NNP (nsubjpass)\n  -> is/VBZ (auxpass)\n  -> Defense/NNP (nmod:agent)\n    -> by/IN (case)\n    -> European/JJ (amod)\n    -> Aeronautic/NNP (compound)\n    -> &/CC (cc)\n    -> Company/NNP (conj:&)\n      -> Space/NNP (compound)\n    -> EADS/NNP (appos)\n      -> -LRB-/-LRB- (punct)\n      -> -RRB-/-RRB- (punct)\n  -> Company/NNP (nmod:agent)\n  -> ./. (punct)\n"
     13 = {SemanticGraph@8822} "-> owned/VBN (root)\n  -> Airbus/NNP (nsubjpass)\n  -> is/VBZ (auxpass)\n  -> Defense/NNP (nmod:agent)\n    -> by/IN (case)\n    -> European/JJ (amod)\n    -> Aeronautic/NNP (compound)\n    -> &/CC (cc)\n    -> Company/NNP (conj:&)\n      -> Space/NNP (compound)\n    -> EADS/NNP (appos)\n      -> -LRB-/-LRB- (punct)\n      -> -RRB-/-RRB- (punct)\n  -> Company/NNP (nmod:agent)\n  -> ./. (punct)\n"
     */
    /*List<Annotation> getSentenceAnnotations(){

        CoreAnnotations.SentencesAnnotation sentencesAnnotation = new CoreAnnotations.SentencesAnnotation();

        CoreAnnotations.TextAnnotation //0
        CoreAnnotations.CharacterOffsetBeginAnnotation
        CoreAnnotations.CharacterOffsetEndAnnotation
        CoreAnnotations.TokensAnnotation
        CoreAnnotations.TokenBeginAnnotation 5 = {Class@8994} "class edu.stanford.nlp.ling.CoreAnnotations.TokenEndAnnotation"
        CoreAnnotations.TokenEndAnnotation
        CoreAnnotations.SentenceIndexAnnotation
        CoreAnnotations.NumerizedTokensAnnotation
        TreeCoreAnnotations.TreeAnnotation
        SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation
        SemanticGraphCoreAnnotations.BasicDependenciesAnnotation
        SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
        SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation
        SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation
    }*/

    /**
     * new line logic does not hold
     * @param wf
     * @param kafDocument
     * @return
     */
    private static String getBefore(WF wf, KAFDocument kafDocument){
        int sentenceIndex = wf.getSent();
        if(sentenceIndex==1)
            return "";

        List<WF> sentence = kafDocument.getSentences().get(sentenceIndex-1);
        if(wf.getId().equals(sentence.get(0).getId()) )
            return "\n\n";
        else return "";
    }

    /**
     *      * new line logic does not hold
     * @param wf
     * @param kafDocument
     * @return
     */
    private static String getAfter(WF wf, KAFDocument kafDocument){
        int sentenceIndex = wf.getSent();
        if(sentenceIndex-1==kafDocument.getNumSentences())
            return "";

        List<WF> sentence = kafDocument.getSentences().get(sentenceIndex-1);
        if(wf.getId().equals(sentence.get(sentence.size()-1).getId()) )
            return "\n\n";
        else return "";
    }


    private static String encodeToken(String token) {
//        if (BRACKET_LRB.equals(token)) {
//            return "-LRB-";
//        } else if (BRACKET_RRB.equals(token)) {
//            return "-RRB-";
//        } else if (BRACKET_LCB.equals(token)) {
//            return "-LCB-";
//        } else if (BRACKET_RCB.equals(token)) {
//            return "-RCB-";
//        }
        return token;
    }

    public Annotation tokenSentencePos(KAFDocument kafDocument, Annotation annotation){
        kafDocument.getTerms().forEach(term -> {
//                stringBuilder
//                        .append(term.getStr().replaceAll(" ",""))
//                        .append(delimiter).append(term.getLemma())
//                        .append(delimiter).append(term.getMorphofeat()).append(" ");
        });
        return annotation;
    }

    private int getTokenIndex(WF wf){
        return Integer.parseInt(wf.getId().replace("w", ""))-1;
    }

}

