package com.itzixi.service;

import com.itzixi.bean.SearchResult;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface SearXngService {

    public List<SearchResult> search(String query);

}
