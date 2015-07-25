package play.modules.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.ObjectIdCodec;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;

public class AggregateCursor implements MongoCursor {

	public AggregateIterable<Document> cursor;
	public Class clazz;
	
	/**
	 * Constructor
	 * 
	 * @param findIterable
	 * @param clazz
	 */
	public AggregateCursor(AggregateIterable<Document> findIterable, Class clazz){
		this.cursor = findIterable;
		this.clazz = clazz;
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
		
		List<T> resultList = new ArrayList<T>();
				
		for(Document document : cursor) {
				
			try {
				
				document.remove("_id");
				T model = (T) MongoMapper.convertValue(document, clazz);
//				model.set_id(id);
				
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
	 * Return the first model in 
	 * @param <T> - the specific MongoModel type
	 * @return - one instance of a MongoModel
	 */
	@Override
	public <T extends MongoModel> T first(){
			
		Document document = cursor.first();
		
		ObjectId id = (ObjectId) document.remove("_id");
		T model = (T) MongoMapper.convertValue(document, clazz);
		model.set_id(id);
		
		return model;
		
	}
		
}
