package com.atguigu.gmall0715.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testES() throws IOException {

        // 创建一个dsl语句
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"match_all\": {}\n" +
                "  }\n" +
                "}";
        // 查询es 中的数据
        Search search = new Search.Builder(query).build();
        // 准备执行search
        SearchResult searchResult = jestClient.execute(search);
        // 取得结果集
        List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
        // 循环集合中的数据
        for (SearchResult.Hit<HashMap, Void> hit : hits) {
            HashMap map = hit.source;
            System.out.println("token:" + map.get("token"));
        }
    }

}
