import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import unithon.boot.io.files.FileCreator;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;
import unithon.boot.io.uitils.FileType;

import java.io.File;

public class WorkFrequency {
    public static void main(String[] args) {
        File output = FileCreator.create(FileType.File).setPath("output.json").doCreate();
        JSONObject result = new JSONObject();
        File file = new File("work");
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                JSONArray array = JSONArray.parseArray(NativeReader.createFileReader(f).getResult());
                array.forEach((object) -> {
                    assert object instanceof JSONObject;
                    JSONObject jsonObject = (JSONObject) object;
                    jsonObject.getJSONArray("paragraph").forEach((s) -> {
                        assert s instanceof String;
                        char[] c = ((String) s).toCharArray();
                        for (char c1 : c) {
                            String c1s = String.valueOf(c1);
                            if (result.containsKey(c1s)) {
                                long count = result.getLong(c1s);
                                result.put(c1s, count + 1);
                            } else {
                                result.put(c1s, 1);
                            }
                        }
                    });
                });
            }

            NativeWriter.createFileWriter(output)
                    .add(result.toString(SerializerFeature.PrettyFormat, SerializerFeature.MapSortField))
                    .flush();
        }
    }
}
