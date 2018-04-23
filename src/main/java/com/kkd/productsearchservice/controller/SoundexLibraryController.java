package com.kkd.productsearchservice.controller;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kkd.productsearchservice.services.MessageSender;
import com.kkd.productsearchservice.services.Soundex;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Operations related to adding the values in soundex database" })
public class SoundexLibraryController {

	@Autowired
	MessageSender messageSenderService;

	@PostMapping("/add-code")
	@ApiOperation(value = "add new soundex code")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved all the item information"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<HashSet<String>> insert(@RequestBody Document document) {
		try {
			messageSenderService.produceMsg("adding new soundex code");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		MongoClient mongoClient = null;
		MongoDatabase database;
		MongoCollection<Document> collection = null;
		MongoCursor cursor;
		
		String mongoUrl="mongodb://10.151.60.164:27017";//10.151.60.164
		String dbName="kkdDb";
		// establishing connection
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			database = mongoClient.getDatabase(dbName);
			collection = database.getCollection("soundex");
		} catch (MongoException | ClassCastException e) {
			messageSenderService.produceMsg(
					"could not connect to the database or class caste exception occured while adding soundex code");
		}
		HashSet<String> set = new HashSet<String>();
		String title = (String) document.get("ProductTitle");
		String keywords = (String) document.get("Description");
		String category = (String) document.get("Category");
		set.add(category.toLowerCase().trim());
		String regx = "[,\\s]+";
		String[] titleSplit = title.split(regx);
		for (String str : titleSplit) {
			set.add(str.toLowerCase().trim());

		}
		String[] keywordsSplit = keywords.split(regx);
		for (String str : keywordsSplit) {
			set.add(str.toLowerCase().trim());
		}

		try {
			for (String str : set) {
				System.out.println(Soundex.getGode(str));
				cursor = collection.find(new Document("word", str)).iterator();
				if (cursor.hasNext()) {
					cursor.close();
					continue;
				}
				cursor.close();
				collection.insertOne(new Document("code", Soundex.getGode(str)).append("word", str));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			collection.dropIndexes();
			mongoClient.close();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(set);
	}

}
