package unithon;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;

public class Clean {
    public static void main(String[] args) {
        JSONArray newsList = JSONArray
                .parseArray(NativeReader.createFileReader("distance_e.json")
                        .getResult());
        JSONArray result = new JSONArray();
        for (int i = 0; i < newsList.size(); i++) {
            JSONObject jsonObject = newsList.getJSONObject(i);
            JSONArray distance = jsonObject.getJSONArray("distance");
            long count = 0;
            for (int j = 0; j < distance.size(); j++) {
                if (distance.getDouble(j) != Double.parseDouble("0.0")) {
                    count++;
                }
            }
            if (count > 32) {
                result.add(jsonObject);
            }
        }
        NativeWriter.createFileWriter("distance_clean.json").add(result.toString()).flush();
    }
}
