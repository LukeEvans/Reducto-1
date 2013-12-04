package com.winston.nlp.annotate

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import java.util.Properties
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.Annotation
import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.transport.messages._



class NLPParser {
    val maxLen:java.lang.Integer = 2;
	var parseProps = new Properties()
	parseProps.put("annotators", "tokenize, ssplit, pos, parse")
	parseProps.put("parse.maxlen", "30")
	var parseProcessor:StanfordCoreNLP = null;
	
	def init() {
	  parseProcessor = new StanfordCoreNLP(parseProps)
	  println("--Parser Created");
	}
	
	def parseProcess(sentence:NLPSentence): SentenceContainer = {

		var document = new Annotation(sentence.value);

		parseProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])
		var trees = new ArrayList[String];

		for(m <- list){
			trees.add(m.get(classOf[TreeAnnotation]).toString());
		}

		if (trees.size() > 0) {
			sentence.putTree(trees.get(0));
		} 
	   
		SentenceContainer(sentence)
		
//	    sentence.putTree("(ROOT (S (NP (PRP It)) (VP (VBZ 's) (NP (NP (NN kind)) (PP (IN of) (NP (NN fun))) (S (VP (TO to) (VP (VB do) (NP (DT the) (JJ impossible))))))) (. .)))")
//		SentenceContainer(sentence)
	}
	
	def batchProcess(splitSentences:ArrayList[NLPSentence]): ArrayList[NLPSentence] = {
	  	// Split text into managable chunks
		val docList = new ArrayList[Annotation]();
		val sentences = new ArrayList[NLPSentence]();

		// Build list of annotations
		splitSentences.toList map { s => 
		  docList.add(new Annotation(s.grabValue));
		}

		// Annotate all docs
		parseProcessor.annotate(docList);
		
		docList.toList map { document =>
			val list = document.get(classOf[SentencesAnnotation]);
			
			list.toList map { m =>
				var sentence = new NLPSentence(m.get(classOf[TextAnnotation]));
				sentence.index = sentences.size();
				
				for (t <- m.get(classOf[TokensAnnotation])) {
					sentence.addWord(t.get(classOf[TextAnnotation]));
				}
				
				sentence.putTree(m.get(classOf[TreeAnnotation]).toString())
				sentences.add(sentence)
			}
		}
		
		return sentences;
	}
}