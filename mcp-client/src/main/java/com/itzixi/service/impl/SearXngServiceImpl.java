package com.itzixi.service.impl;

import cn.hutool.json.JSONUtil;
import com.itzixi.bean.ChatEntity;
import com.itzixi.bean.SearXNGResponse;
import com.itzixi.bean.SearchResult;
import com.itzixi.service.SearXngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearXngServiceImpl implements SearXngService {

    @Value("${internet.websearch.searxng.url}")
    private String SEARXNG_URL;

    @Value("${internet.websearch.searxng.counts}")
    private Integer COUNTS;

    private final OkHttpClient okHttpClient;

    private final ChatEntity chatEntity;

    @Override
    public List<SearchResult> search(String query) {

        // 构建url
        HttpUrl url = HttpUrl.get(SEARXNG_URL)
                .newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .build();

        log.info("注入的ChatEntity为：" + chatEntity);

        log.info("搜索的url地址为：" + url.url());

        // 构建request
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 发送请求
        try (Response response = okHttpClient.newCall(request).execute()) {

            // 判断请求是否成功还是失败
            if (!response.isSuccessful()) throw new RuntimeException("请求失败: HTTP " + response.code());

            // 获得响应的数据
            if (response.body() != null) {
                String responseBody = response.body().string();

                SearXNGResponse searXNGResponse = JSONUtil.toBean(responseBody, SearXNGResponse.class);

                return dealResults(searXNGResponse.getResults());
            }
            log.error("搜索失败：{}", response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Collections.emptyList();
    }

    private List<SearchResult> dealResults(List<SearchResult> results) {

        return results.subList(0, Math.min(COUNTS, results.size()))
                        .parallelStream()
                        .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                        .limit(COUNTS).toList();
    }
}
