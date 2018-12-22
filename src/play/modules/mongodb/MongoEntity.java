package play.modules.mongodb;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used by the MongoEnhancer to 
 * signal it to provide implementations for MongoModel
 * methods.
 * 
 * @author Andrew Louth
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoEntity {

	/**
	 * This value represents the collectionName.
	 * @return
	 */
	String value() default "default";

}
