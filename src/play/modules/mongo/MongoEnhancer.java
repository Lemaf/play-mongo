package play.modules.mongodb;



import java.util.List;

import org.bson.conversions.Bson;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

/**
 * This class uses the Play framework enhancement process to enhance 
 * classes marked with the mongo module annotations.
 * 
 * @author Andrew Louth
 */
public class MongoEnhancer extends Enhancer {

	public static final String PACKAGE_NAME = "play.modules.mongodb";
	
	public static final String ENTITY_ANNOTATION_NAME = "play.modules.mongodb.MongoEntity";
	public static final String ENTITY_ANNOTATION_VALUE = "value";
	
	@Override
	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
		
		final CtClass ctClass = makeClass(applicationClass);

		// Enhance MongoEntity annotated classes
        if (hasAnnotation(ctClass, ENTITY_ANNOTATION_NAME)) {
            enhanceMongoEntity(ctClass, applicationClass);
        }
        else {
        	return;
        }
	}
	
	/**
	 * Enhance classes marked with the MongoEntity annotation.
	 * 
	 * @param ctClass
	 * @throws Exception
	 */
	private void enhanceMongoEntity(CtClass ctClass, ApplicationClass applicationClass) throws Exception {
    	// Don't need to fully qualify types when compiling methods below
        classPool.importPackage(PACKAGE_NAME);
		
		String entityName = ctClass.getName();
        
        // Set the default collection name
        String collectionName = "\"" + ctClass.getSimpleName().toLowerCase() + "\"";
        
        Logger.debug("using collectionName %s", collectionName);
        
        AnnotationsAttribute attr = getAnnotations(ctClass);
        Annotation annotation = attr.getAnnotation(ENTITY_ANNOTATION_NAME);
        if (annotation.getMemberValue(ENTITY_ANNOTATION_VALUE) != null){
        	collectionName = annotation.getMemberValue(ENTITY_ANNOTATION_VALUE).toString();
        }

        Logger.debug( this.getClass().getName() + "-->enhancing MongoEntity-->" + ctClass.getName() + "-->collection-->" + collectionName);
        
        // getCollectionName
        CtMethod getCollectionName = CtMethod.make("public static java.lang.String getCollectionName() { return " + collectionName + ";}", ctClass);
        ctClass.addMethod(getCollectionName);
        
        // create an _id field
        CtField idField = new CtField(classPool.get("org.bson.types.ObjectId"), "_id", ctClass);
        idField.setModifiers(Modifier.PRIVATE);
        ctClass.addField(idField);
        
        //get_id
        CtMethod get_id = CtMethod.make("public org.bson.types.ObjectId get_id() { return _id;}", ctClass);
        ctClass.addMethod(get_id);
        
        // set_id
        CtMethod set_id = CtMethod.make("public void set_id(org.bson.types.ObjectId _id) { this._id = _id;}", ctClass);
        ctClass.addMethod(set_id);
        
        // count
        CtMethod count = CtMethod.make("public static long count() { return MongoDB.count(getCollectionName());}", ctClass);
        ctClass.addMethod(count);

        // count2
        CtMethod count2 = CtMethod.make("public static long count(java.lang.String query, java.lang.Object[] params) { return MongoDB.count(getCollectionName(), query, params); }", ctClass);
        ctClass.addMethod(count2);
        
        // count3
        CtMethod count3 = CtMethod.make("public static long count(org.bson.conversions.Bson filter) { return MongoDB.count(getCollectionName(), filter); }", ctClass);
        ctClass.addMethod(count3);
        
        // count4
        CtMethod count4 = CtMethod.make("public static long count(org.bson.conversions.Bson filter, com.mongodb.client.model.CountOptions options) { return MongoDB.count(getCollectionName(), filter, options); }", ctClass);
        ctClass.addMethod(count4);

        // find        
        CtMethod find = CtMethod.make("public static FindCursor find(String query, Object[] params){ return MongoDB.find(getCollectionName(),query,params,"+entityName+".class); }", ctClass);
        ctClass.addMethod(find);
        
        // find2        
        CtMethod find2 = CtMethod.make("public static FindCursor find(){ return MongoDB.find(getCollectionName(),"+entityName+".class); }", ctClass);
        ctClass.addMethod(find2);
      
        // find3        
        CtMethod find3 = CtMethod.make("public static FindCursor find(org.bson.conversions.Bson filter, org.bson.conversions.Bson sort){ return MongoDB.find(getCollectionName(),filter,sort,"+entityName+".class); }", ctClass);
        ctClass.addMethod(find3);
        
        // aggregate
        CtMethod aggregate = CtMethod.make("public static AggregateCursor aggregate(java.util.List pipeline) { return MongoDB.aggregate(getCollectionName(), pipeline, "+entityName+".class); }", ctClass);
        ctClass.addMethod(aggregate);
        
        // delete        
        CtMethod delete = CtMethod.make("public void delete() { MongoDB.delete(getCollectionName(), this); }", ctClass);
        ctClass.addMethod(delete);
        
        // delete        
        CtMethod delete2 = CtMethod.make("public static long delete(String query, Object[] params) { return MongoDB.delete(getCollectionName(), query, params); }", ctClass);
        ctClass.addMethod(delete2);
    
        // deleteAll        
        CtMethod deleteAll = CtMethod.make("public static long deleteAll() { return MongoDB.deleteAll(getCollectionName()); }", ctClass);
        ctClass.addMethod(deleteAll);
    
        // save     
        CtMethod save = CtMethod.make("public MongoModel save() { return (MongoModel)MongoDB.save("+ entityName +".getCollectionName(), this); }", ctClass);
        ctClass.addMethod(save);
        
        // index
        CtMethod index = CtMethod.make("public static void index(String indexString) { MongoDB.index("+ entityName +".getCollectionName(), indexString); }", ctClass);
        ctClass.addMethod(index);
        
        // dropIndex
        CtMethod dropIndex = CtMethod.make("public static void dropIndex(String indexString) { MongoDB.dropIndex("+ entityName +".getCollectionName(), indexString); }", ctClass);
        ctClass.addMethod(dropIndex);
        
        // dropIndexes
        CtMethod dropIndexes = CtMethod.make("public static void dropIndexes() { MongoDB.dropIndexes("+ entityName +".getCollectionName()); }", ctClass);
        ctClass.addMethod(dropIndexes);
        
        // getIndexes
        CtMethod getIndexes = CtMethod.make("public static String[] getIndexes() { return MongoDB.getIndexes("+ entityName +".getCollectionName()); }", ctClass);
        ctClass.addMethod(getIndexes);
        
        // Done.
        applicationClass.enhancedByteCode = ctClass.toBytecode(); 
        ctClass.detach();
	}
}
