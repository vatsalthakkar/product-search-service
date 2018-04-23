package com.kkd.productsearchservice.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class SpellCheck {

  // public class SpellCheckApplication {

  public static String spellChecker(String input) throws IOException {

    JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
    /*
     * for (Rule rule : langTool.getAllRules()) { if
     * (!rule.isDictionaryBasedSpellingRule()) { langTool.disableRule(rule.getId());
     * } }
     */

    MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    MongoDatabase database = mongoClient.getDatabase("KkdDb");
    MongoCollection<Document> collection = database.getCollection("soundex");
    MongoCursor cursor;
    String output="";
    List<String> misSpelled = new ArrayList<String>();
    int i = 0;
    String replacement="";
    List<RuleMatch> matches = langTool.check(input);
    try {
    for (RuleMatch match : matches) {
      misSpelled.add(input.substring(match.getFromPos(), match.getToPos()));
      String soundexCode = Soundex.getGode(input.substring(match.getFromPos(), match.getToPos()));
      cursor = collection.find(new Document("code", soundexCode)).iterator();
      if(!cursor.hasNext())
    	 replacement=input.substring(match.getFromPos(), match.getToPos())+" "; 
     // System.out.println(replacement+" repllll");
      while (cursor.hasNext()) {
          Document article = (Document) cursor.next();
         // System.out.println(article.get("word")+"wordddddddd");
          replacement=article.get("word")+"";
        }
        //System.out.println(""+replacement+" repllllllll");
        //System.out.println(match.getFromPos()+" from pos"+ match.getToPos()  +" to pos" );
        
        
        input=input.replace(input.substring(match.getFromPos(), match.getToPos()), replacement);
        //input=output;
       // System.out.println("output "+output);
        for (String str : misSpelled) {
        //  System.out.println(str+"++++miss");
        }

    }



  
  
} catch (Exception e) {
  e.printStackTrace();
} finally {
  collection.dropIndexes();
  mongoClient.close();
}
return input;
  }
}