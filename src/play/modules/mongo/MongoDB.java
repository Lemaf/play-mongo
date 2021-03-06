package play.modules.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import play.Logger;
import play.Play;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.operation.CreateUserOperation;


public class MongoDB {
	
    private static MongoClient mongo;
    private static MongoCredential credential;
    private static MongoDatabase db;
    
    private static String host;
    private static Integer port;
    private static String dbname;

    /**
     * Obtain a reference to the mongo database.
     * 
     * @return - a reference to the Mongo database
     */
	public static MongoDatabase db() {
		
		if (db==null){
			
			init();
			
		}
		
		return db;
		
	}
	
	/**
	 * Static initialiser.
	 * 
	 * @throws UnknownHostException
	 * @throws MongoException
	 */
	public static void init() {
		
		if (host == null || port == null || dbname == null){
			host = Play.configuration.getProperty("mongo.host", "localhost");
			port = Integer.parseInt(Play.configuration.getProperty("mongo.port", "27017"));
			dbname = Play.configuration.getProperty("mongo.database", "play." + Play.configuration.getProperty("application.name"));
		}
		
		Logger.info("initializing DB ["+host+"]["+port+"]["+dbname+"]");
		
		try {
			
			if(Play.configuration.containsKey("mongo.username") && Play.configuration.containsKey("mongo.password")){
			
				String username = Play.configuration.getProperty("mongo.username");
				String password = Play.configuration.getProperty("mongo.password");
				
				credential = MongoCredential.createCredential(username, dbname, password.toCharArray());
				mongo = new MongoClient(new ServerAddress(host, port), Collections.singletonList(credential));
				
			} else{
				
				mongo = new MongoClient(new ServerAddress(host, port));
				
			}
			
			db = mongo.getDatabase(dbname);
			
		} catch (MongoException e) {
			e.printStackTrace();
		}
		
	}
		
	/**
	 * Creates an index.
	 * 
	 * @param collectionName
	 * @param indexString
	 */
	public static void index(String collectionName, String indexString){
		
		MongoCollection<Document> c = db().getCollection(collectionName);
		BasicDBObject indexKeys = createOrderDbObject(indexString);		
		c.createIndex(indexKeys);
		
	}
	
	/**
	 * Removes an index. 
	 * 
	 * @param collectionName
	 * @param indexString
	 */
	public static void dropIndex(String collectionName, String indexString){
		
		MongoCollection<Document> c = db().getCollection(collectionName);
		BasicDBObject indexKeys = createOrderDbObject(indexString);
		c.dropIndex(indexKeys);
		
	}
	
	/** 
	 * Removes all indexes.
	 * 
	 * @param collectionName
	 */
	public static void dropIndexes(String collectionName){
		
		MongoCollection<Document> c = db().getCollection(collectionName);
		c.dropIndexes();
		
	}
	
	/**
	 * Return a list of index names.
	 * 
	 * @param collectionName
	 * @return
	 */
	public static String[] getIndexes(String collectionName){
		
		List<String> indexNames = new ArrayList<String>();
		MongoCollection<Document> c = db().getCollection(collectionName);
		
		ListIndexesIterable<Document> indexes = c.listIndexes();
		
		for (Document o : indexes){
			indexNames.add(o.getString("name"));
		}
		
		return indexNames.toArray(new String[indexNames.size()]);
		
	}	
	
	/**
	 * Counts the records in the collection.
	 * 
	 * @param collectionName
	 * @return - number of records in the collection
	 */
	public static long count(String collectionName){		
		return db().getCollection(collectionName).count();
	}
	
	/**
	 * Counts the records in the collection matching the query string.
	 * 
	 * @param collectionName - the queried collection
	 * @param query - the query string
	 * @param params - parameters for the query string
	 * @return
	 */
	public static long count(String collectionName, String query, Object[] params){
		return db().getCollection(collectionName).count(createQueryDbObject(query, params));
	}
	
	/**
	 * Counts the records in the collection matching the filter.
	 * 
	 * @param collectionName - the queried collection
	 * @param filter - the filter
	 * @return
	 */
	public static long count(String collectionName, Bson filter){
		return db().getCollection(collectionName).count(filter);
	}
	
	/**
	 * Counts the records in the collection matching the filter and options.
	 * 
	 * @param collectionName - the queried collection
	 * @param filter - the filter
	 * @param option - the count options
	 * @return
	 */
	public static long count(String collectionName, Bson filter, CountOptions options){
		return db().getCollection(collectionName).count(filter, options);
	}
	
	/**
	 * Provides a cursor to the objects in a collection, matching the query string.
	 * 
	 * @param collectionName - the target collection
	 * @param query - the query string
	 * @param params - parameters for the query
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */
	public static FindCursor find(String collectionName, String query, Object[] params, Class clazz){
		return new FindCursor(db().getCollection(collectionName).find(createQueryDbObject(query, params)),clazz);
	}
	
	/**
	 * Provides a cursor to the objects in a collection.
	 * 
	 * 
	 * @param collectionName - the target collection
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */ 
	public static FindCursor find(String collectionName, Class clazz){
		return new FindCursor(db().getCollection(collectionName).find(),clazz);
	}
	
	/**
	 * Provides a cursor to the objects in a collection, matching the query string.
	 * 
	 * @param collectionName - the target collection
	 * @param filter - the filter
	 * @param sort - the sort
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */
	public static FindCursor find(String collectionName, Bson filter, Bson sort, Class clazz){
		return new FindCursor(db().getCollection(collectionName).find(filter).sort(sort),clazz);
	}
	
	/**
	 * Provides a cursor to the objects in a collection.
	 * 
	 * @param collectionName - the target collection
	 * @param clazz - the type of MongoModel
	 * @return - a mongo cursor
	 */ 
	public static AggregateCursor aggregate(String collectionName, List<? extends Bson> pipeline, Class clazz) {
		return new AggregateCursor(db().getCollection(collectionName).aggregate(pipeline), clazz);
	}
	
	/**
	 * Saves a model to its collection.
	 * @param <T> - the type of MongoModel to save
	 * @param collectionName - the collection to save it to
	 * @param model - the model to save
	 * @return - an instance of the model saved
	 */
	public static <T extends MongoModel> T save(String collectionName, T model){
		
		/* 
		 * Perhaps it would be better to immediately save the object to the database and assign its id. 
		 * 
		 */
		Document document = new Document(MongoMapper.convertValue(model, Map.class));
		
		if (model.get_id() == null){
			document.remove("_id");
			db().getCollection(collectionName).insertOne(document);
			model.set_id((ObjectId)(document.get("_id")));
		} else {
			document.remove("_id");
			db().getCollection(collectionName).replaceOne(new BasicDBObject("_id",model.get_id()), document);
		}
		
		return model;
		
	}
	
	/**
	 * Deletes a model from a collection.
	 * 
	 * @param <T> - the type of model
	 * @param collectionName - the collection
	 * @param model - the model
	 */
	public static <T extends MongoModel> void delete (String collectionName, T model){
		Document document = new Document("_id", model.get_id());
		db().getCollection(collectionName).deleteOne(document);
	}
	
	/**
	 * Deletes models from a collection that match a specific query string
	 * 
	 * @param collectionName - the collection 
	 * @param query - the query string
	 * @param params - parameters for the query string
	 * @return - the number of models deleted
	 */
	public static long delete (String collectionName, String query, Object[] params) {
		
		BasicDBObject dbObject = createQueryDbObject(query, params);
		long deleteCount = db().getCollection(collectionName).count(dbObject);
		db().getCollection(collectionName).deleteMany(dbObject);
		
		return deleteCount;
		
	}
	
	/**
	 * Deletes all models from the collection.
	 * 
	 * @param collectionName - the collection
	 * @return - the number of models deleted
	 */
	public static long deleteAll (String collectionName){
		
		long deleteCount = count(collectionName);
		db().getCollection(collectionName).drop();
		
		return deleteCount;
		
	}
	
	/**
	 * Creates a query object for use with other methods
	 * 
	 * @param query - the query string
	 * @param values - values for the query
	 * @return - a BasicDBObject representing the query
	 */
	public static BasicDBObject createQueryDbObject(String query, Object[] values){
		
		String keys = extractKeys(query);
		
		BasicDBObject object = new BasicDBObject(); 	
    	String [] keyList = keys.split(",");
    	
    	if (keyList.length > values.length){
    		throw new IllegalArgumentException("Not enough values for the keys provided");
    	}
    	
		for (int i = 0; i < keyList.length; i++){
			object.put(keyList[i].trim(), values[i]);
		}
    	
    	return object;
    }
	
	/**
	 * Creates an ordering object for use with other methods
	 * 
	 * @param query - the query string
	 * @param values - values for the query
	 * @return - a DBObject representing the ordering
	 */
	public static BasicDBObject createOrderDbObject(String query){
		
		String keys = extractKeys(query);
		
    	BasicDBObject object = new BasicDBObject(); 	
    	String [] keyList = keys.split(",");
    	
		for (int i = 0; i < keyList.length; i++){
			
			int value = 1;
			if (keyList[i].charAt(0) == '-'){
				value = -1;
				keyList[i] = keyList[i].substring(1);
			}
			
			object.put(keyList[i].trim(), value);
		}  
    	
    	return object;
    }
	
	/**
	 * Extracts parameter names from a query string
	 * 
	 * @param queryString - the query string
	 * @return - a comma seperated string of parameter names
	 */
	private static String extractKeys(String queryString){
		queryString = queryString.substring(2);
		List<String> keys = new ArrayList<String>();
        String[] parts = queryString.split("And");
        for (String part : parts){
        	if (part.charAt(0) == '-'){
        		keys.add((part.charAt(0) + "") + (part.charAt(1) + "").toLowerCase() + part.substring(2));
        	}
        	else{
        		keys.add((part.charAt(0) + "").toLowerCase() + part.substring(1));
        	}
        }
        return StringUtils.join(keys.toArray(), ",");
	}

} 
