package com.kkd.productsearchservice.controller;

import java.io.IOException;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkd.productsearchservice.FarmerProductServiceProxy;
import com.kkd.productsearchservice.services.MessageSender;
import com.kkd.productsearchservice.services.SpellCheck;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(tags = { "Operations related to search the products" })
public class SearchController {

	MongoClient mongoClient;
	MongoDatabase database;
	MongoCollection<Document> collection;
	Boolean caseSensitive = false;
	Boolean diacriticSensitive = false;
	@Autowired
	private FarmerProductServiceProxy productProxy;
	@Autowired
	MessageSender messageSenderService;
	
	String mongoUrl="mongodb://10.151.60.164:27017";//10.151.60.164
	String dbName="kkdDb";//kkdDb
	
	@HystrixCommand(fallbackMethod = "fallbackSearch")
	@GetMapping("/search-key/{location}/{searchKey}")
	@ApiOperation(value = "Search product")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved all the item information"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<List<Document>> getSearchResults(@PathVariable(value = "searchKey") String searchKey,
			@PathVariable(value = "location") String location) {
		
		searchKey.toLowerCase();
		try {
			searchKey = SpellCheck.spellChecker(searchKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ResponseEntity<List<Document>> data=null;
		
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		
		System.out.println(searchKey);
		
		List<Document> searchResults = new ArrayList<Document>();
		try {
			messageSenderService.produceMsg("searching for the product");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			database = mongoClient.getDatabase(dbName);
			collection = database.getCollection("Products");
		} catch (MongoException | ClassCastException e) {
			messageSenderService.produceMsg("could not connect to the database or class caste exception occured");
		}
		collection.createIndex(new Document("Category", "text").append("ProductTitle", "text")
				.append("Description", "text"), new IndexOptions());

		try {
			 MongoCursor<Document> cursor = collection.find(new Document("$text",new Document("$search", searchKey).append("$caseSensitive",
									 new Boolean(caseSensitive)).append("$diacriticSensitive", new Boolean(diacriticSensitive)))).iterator(); //append("Location",location)).iterator();
			//while (cursor.hasNext()) {
			Document article = cursor.next();
			//System.out.println(article.get("ProductTitle"));
			searchKey=(String) article.get("ProductTitle");
			System.out.println("the name to send:"+searchKey);
			//searchResults.add(article);
			//}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			collection.dropIndexes();
			mongoClient.close();
			try {
				data=productProxy.getSearchProducts(searchKey, location);
			}
			catch(Exception e){
				System.out.println(e.toString());
			}
			//System.out.println(data.getBody().get(0));
			return data;
		}
		// till here's nikhil's service's code

	}

	// Fallback method for getSearchResults method
	public ResponseEntity<List<Document>> fallbackSearch(@PathVariable(value = "searchKey") String searchKey,
			@PathVariable(value = "location") String location) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	@HystrixCommand(fallbackMethod = "fallbackAll")
	@GetMapping("/get-all-products")
	@ApiOperation(value = "get all products")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved all the item information"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<List<Document>> getAllProducts() {

		try {
			messageSenderService.produceMsg("getting all products");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			database = mongoClient.getDatabase(dbName);
			collection = database.getCollection("Products");
		} catch (MongoException | ClassCastException e) {
			messageSenderService.produceMsg("could not connect to the database or class caste exception occured");
		}
		MongoCursor<Document> cursor = null;
		cursor = collection.find().iterator();
		List<Document> allProducts = new ArrayList<Document>();
		while (cursor.hasNext()) {
			Document article = cursor.next();
			allProducts.add(article);
		}
		return ResponseEntity.status(HttpStatus.OK).body(allProducts);
	}

	//fallback for getAll
	public ResponseEntity<List<Document>> fallbackAll() {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	
	
	@HystrixCommand(fallbackMethod = "fallbackAddProduct")
	@PostMapping("/add-product")
	@ApiOperation(value = "add new product")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved all the item information"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<Object> addProduct(@RequestBody Document document) {

		try {
			messageSenderService.produceMsg("adding a new product");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			database = mongoClient.getDatabase(dbName);
			collection = database.getCollection("Products");
		} catch (MongoException | ClassCastException e) {
			messageSenderService.produceMsg("could not connect to the database or class caste exception occured");
		}
		Document newProduct = new Document();
		newProduct.put("ProductTitle", document.get("ProductTitle"));
		newProduct.put("Description", document.get("Description"));
		newProduct.put("Category", document.get("Category"));
		collection.insertOne(newProduct);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	//fallback for addProduct
	public ResponseEntity<Object> fallbackAddProduct(@RequestBody Document document){
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
