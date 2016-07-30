import com.justin.tool.util.Http;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin on 7/30/16.
 */
public class HttpTest {
    public static void main(String[] args) throws IOException {
//        Http.Response r = Http.get("http://www.baidu.com/2", Collections.<String, String>emptyMap());
        Map<String, String> header = new HashMap<String, String>();
        header.put("lufax_user_id", "1");
        header.put("Content-type", "application/json");
        Http.Response r  = Http.post("http://localhost:8888", header, "{A:1,C:2}".getBytes("UTF-8"));

        System.out.println(r);

        System.out.println(Integer.parseInt("9"));
    }
}
