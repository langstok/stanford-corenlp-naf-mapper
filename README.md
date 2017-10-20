# stanford-corenlp-kaf-mapper
Map Stanford CoreNLP Annotation to [NAF format](https://github.com/newsreader/NAF)

Work in progress
Get Stanford [dcoref](https://nlp.stanford.edu/software/dcoref.shtml) in KAFDocument format

Annotate using Stanford CoreNLP with the following annotators to map to naf.
    
    "tokenize, ssplit, pos, lemma, ner, entitymentions, parse, dcoref";

Note:
- parse is required for dcoref, but not mapped to naf.
- entitymentions are required for ner mapping implementation

   
Inspired by the work of [@ragerri](@https://github.com/ragerri)