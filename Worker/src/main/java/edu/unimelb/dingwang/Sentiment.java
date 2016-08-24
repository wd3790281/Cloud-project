package edu.unimelb.dingwang;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by dingwang on 16/4/24.…
 */
public class Sentiment {
    public TweetWithSentiment findSentiment(String line) {

        String preprocessed = preprocess(line);
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        int mainSentiment = 0;
        if (line != null && line.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(preprocessed);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        if (mainSentiment > 4 || mainSentiment < 0) {
            return null;
        }
        TweetWithSentiment tweetWithSentiment = new TweetWithSentiment(line, toCss(mainSentiment));
        return tweetWithSentiment;
    }

    private String preprocess(String line){
        line = removeUrl(line);
        line = removeAt(line);
        line = line.replaceAll("#", "");
        return line;
    }

    private String removeUrl(String commentstr)
    {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        if (m.find()) {
            for (int i = 0; i < m.groupCount(); i++){
                commentstr = commentstr.replaceAll(m.group(i), "").trim();
            }
        }
        return commentstr;
    }

    private String removeAt(String commentstr)
    {
        String urlPattern = "@\\w+";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        if (m.find()) {
            for (int i = 0; i < m.groupCount(); i++){
                commentstr = commentstr.replaceAll(m.group(i), "Jack").trim();
            }
        }
        return commentstr;
    }

    private String toCss(int sentiment) {
        switch (sentiment) {
            case 0:
                return "negative";
            case 1:
                return "negative";
            case 2:
                return "neutral";
            case 3:
                return "positive";
            case 4:
                return "positve";
            default:
                return "";
        }
    }

}
