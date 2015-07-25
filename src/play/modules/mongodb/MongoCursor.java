package play.modules.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;


/**
 * Provides Mongo DBCursor functionality.
 * 
 * @author Andrew Louth
 */
public interface MongoCursor {
		
	/**
	 * Retrieves a list of MongoModels. This method will
	 * return all of the models reachable from this cursor.
	 * 
	 * @param <T> - the specific MongoModel type
	 * @return - the list of MongoModel types
	 */
	<T extends MongoModel> List<T> fetch();
	
	/**
	 * Return the first model in 
	 * @param <T> - the specific MongoModel type
	 * @return - one instance of a MongoModel
	 */
	<T extends MongoModel> T first();
		
}
