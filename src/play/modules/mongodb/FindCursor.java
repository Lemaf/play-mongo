package play.modules.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;

public class FindCursor implements MongoCursor {

	public FindIterable<Document> cursor;
	public Class clazz;
	
	/**
	 * Constructor
	 * 
	 * @param findIterable
	 * @param clazz
	 */
	public FindCursor(FindIterable<Document> findIterable, Class clazz){
		this.cursor = findIterable;
		this.clazz = clazz;
	}
	
	/**
	 * Retrieves a list of MongoModels. 
	 * 
	 * @param <T> - the specific type of MongoModel
	 * @param page - the offset
	 * @param length - the length of a page
	 * @return - the list of MongoModel types
	 */
	public <T extends MongoModel> List<T> fetch(int page, int length){
		
		List<T> resultList = new ArrayList<T>();
		
		if (length != 0){
			cursor.limit(length);
		}
		
		if (page > 1){
			cursor.skip((page-1)*length);
		}
		
		for(Document document : cursor) {
				
			try {
				
				ObjectId id = (ObjectId) document.remove("_id");
				T model = (T) MongoMapper.convertValue(document, clazz);
				model.set_id(id);
				
				resultList.add(model);
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
		}

		return resultList;
	}
	
	/**
	 * Retrieves a list of MongoModels.
	 * 
	 * @param <T> - the specific type of MongoModel
	 * @param limit - the number of models to return
	 * @return - the list of MongoModel types
	 */
	public <T extends MongoModel> List<T> fetch(int limit){
		return fetch(1,limit);
	}
	
	/**
	 * Retrieves a list of MongoModels. This method will
	 * return all of the models reachable from this cursor.
	 * 
	 * @param <T> - the specific MongoModel type
	 * @return - the list of MongoModel types
	 */
	@Override
	public <T extends MongoModel> List<T> fetch(){
		return fetch(0);
	}
	
	/**
	 * Return the first model in 
	 * @param <T> - the specific MongoModel type
	 * @return - one instance of a MongoModel
	 */
	@Override
	public <T extends MongoModel> T first(){
		
		List<T> models = fetch(1,1);
		
		if(models == null && models.size() == 0)
			return null;
		
		return (T)fetch(1,1).get(0);
		
	}
	
	/**
	 * Skips the given the number of records.
	 * 
	 * @param from - the number of records to skip
	 * @return - the cursor
	 */
	public MongoCursor from(int from){
		cursor.skip(from);
		return this;
	}
	
	/**
	 * Orders the objects pointed to by the cursor, using the
	 * orderBy string.
	 * @param orderBy - the string determining the parameters to order by
	 * @return - the cursor
	 */
	public MongoCursor order(String orderBy){
		
		BasicDBObject order = MongoDB.createOrderDbObject(orderBy);
		cursor.sort(order);
		
		return this;
		
	}
	
}
