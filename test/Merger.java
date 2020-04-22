import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;

import java.io.File;
import java.io.IOException;

public class Merger {
    private static JSONArray result = new JSONArray();

    public static void main(String[] args) {
        File p1 = new File("p1");
        File p2 = new File("p2");
        put(p1.listFiles());
        put(p2.listFiles());
        NativeWriter.createFileWriter("merged.json").add(result.toJSONString()).flush();
    }

    public static void put(File[] fileList) {
        for (File file : fileList) {
            JSONArray src = JSONObject.parseArray(NativeReader
                    .createFileReader(file)
                    .getResult());
            result.addAll(src);
        }
    }
}
