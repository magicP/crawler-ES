package com.github.magicp.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class Main {
    public static void main(String[] args) throws IOException {
        Set<String> linkPool = new HashSet<>();
        String indexUrl = "https://sina.cn/";
        linkPool.add(indexUrl);
        Set<String> processedLinks = new HashSet<>();
        while (!linkPool.isEmpty()) {
            String link = linkPool.iterator().next();
            if (processedLinks.contains(link)) {
                linkPool.remove(link);
                continue;
            }
            Document doc = Jsoup.parse(getHtml(link));
            linkPool.addAll(parseHtmlGetLinks(doc));
            linkPool.remove(link);
            processedLinks.add(link);
            if (isNews(doc)) {
                String headLine = getHeadLine(doc);
                String content = getNewsContent(doc);
                storeIntoDataBase(link, headLine, content);
            }
        }
    }

    private static String getHtml(String url) throws IOException {
        CloseableHttpClient httpclient;
        httpclient = HttpClients.createMinimal();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            return html;
        }
    }

    private static Set<String> parseHtmlGetLinks(Document doc) {
        Set<String> links = new HashSet<>();
        for (Element aTag : doc.select("a")) {
            String link = aTag.attr("href");
            if (link.startsWith("https://news.sina.cn/")) {
                links.add(link);
            }
        }
        return links;
    }

    private static boolean isNews(Document doc) {
        return !doc.select(".art_content").isEmpty()
                && !doc.select("time").isEmpty()
                && !doc.select(".art_tit_h1").isEmpty();
    }

    private static String getNewsContent(Document doc) {
        return doc.select(".art_content").text();
    }

    private static String getHeadLine(Document doc) {
        return doc.select("h1").text();
    }

    private static void storeIntoDataBase(String link, String headLine, String content) {

    }
}
