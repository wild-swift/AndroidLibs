package name.wildswift.android.libs.json.parser;

import java.util.List;
import java.util.Map;

/**
 * Container factory for creating containers for JSON object and JSON array.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 * @see JSONParser#parse(java.io.Reader, ContainerFactory)
 */
public interface ContainerFactory {
	/**
	 * @return A Map instance to store JSON object, or null if you want to use JSONObject.
	 */
	Map createObjectContainer();

	/**
	 * @return A List instance to store JSON array, or null if you want to use JSONArray.
	 */
	List creatArrayContainer();
}
