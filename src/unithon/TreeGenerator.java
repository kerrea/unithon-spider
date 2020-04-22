package unithon;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TreeGenerator {
    private static final JSONArray positionList = JSONArray
            .parseArray(NativeReader
                    .createFileReader("positions.json")
                    .getResult());

    public static void main(String[] args) {
        JSONArray newsList = JSONArray
                .parseArray(NativeReader.createFileReader("distance_clean.json")
                        .getResult());
        System.out.println(newsList.size());
        Runtime.getRuntime().exit(0);
        for (int i = 0; i < newsList.size(); i++) {
            JSONArray array = new JSONArray();
            for (int j = 0; j < newsList.size(); j++) {
                if (i == j) {
                    array.add(1);
                } else {
                    array.add(cosDistance(newsList.getJSONObject(i), newsList.getJSONObject(j), "entities"));
                }
            }
            newsList.getJSONObject(i).put("distance", array);
        }
        stop(newsList);
    }

    public static double cosDistance(JSONObject a, JSONObject b, String target) {
        ArrayList<String> temp = new ArrayList<>();
        List<String> listA = a.getJSONArray(target).toJavaList(String.class);
        List<String> listB = b.getJSONArray(target).toJavaList(String.class);
        temp.addAll(listA);
        temp.addAll(listB);
        List<String> refer;
        HashMap<String, Integer> vectorA = new HashMap<>(), vectorB = new HashMap<>();
        refer = temp.stream().filter(positionList::contains).collect(Collectors.toList());
        refer.forEach((v) -> {
            vectorA.put(v, 0);
            vectorB.put(v, 0);
        });
        listA.forEach((s) -> {
            if (vectorA.containsKey(s)) {
                var prev = vectorA.get(s) + 1;
                vectorA.put(s, prev);
            }
        });
        listB.forEach((s) -> {
            if (vectorB.containsKey(s)) {
                var prev = vectorB.get(s) + 1;
                vectorB.put(s, prev);
            }
        });
        AtomicInteger sumA = new AtomicInteger();
        AtomicInteger sumB = new AtomicInteger();
        AtomicInteger sumAB = new AtomicInteger();
        refer.forEach((v) -> {
            int va = vectorA.get(v), vb = vectorB.get(v);
            sumA.addAndGet(va * va);
            sumB.addAndGet(vb * vb);
            sumAB.addAndGet(va * vb);
        });
        if (sumAB.get() == 0) {
            return 0;
        }
        double result = sumAB.get() / ((Math.pow(sumA.get(), 0.5) * Math.pow(sumB.get(), 0.5)));
        return Math.rint(result * 10000)/ 10000.0;
    }

    private static void stop(JSONArray result) {
        NativeWriter.createFileWriter("distance_clean_clean.json").add(result.toString()).flush();
        Runtime.getRuntime().exit(0);
    }
}
