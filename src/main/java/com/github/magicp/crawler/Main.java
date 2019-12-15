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
    public static String getHtml(String url) throws IOException {
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

    public static Set<String> parseHtmlGetLinks(Document doc) {
        Set<String> links = new HashSet<>();
        for (Element aTag : doc.select("a")) {
            String link = aTag.attr("href");
            if (link.startsWith("https://news.sina.cn/")) {
                links.add(link);
            }
        }
        return links;
    }

    public static boolean isNews(Document doc) {
        return !doc.select(".art_content").isEmpty()
                && !doc.select("time").isEmpty()
                && !doc.select(".art_tit_h1").isEmpty();
    }

    public static String getNewsContent(Document doc) {
        return doc.select(".art_content").text();
    }

    public static String getHeadLine(Document doc) {
        return doc.select("h1").text();
    }

    public static void main(String[] args) throws IOException {
        Set<String> linkPool = new HashSet<>();
        String indexUrl = "https://sina.cn/";
        linkPool.add(indexUrl);
        Set<String> processedLinks = new HashSet<>();
        int count = 0;
        while (!linkPool.isEmpty()) {
            //解析当前页面，获取链接
            String link = linkPool.iterator().next();
            if (processedLinks.contains(link)) {
                linkPool.remove(link);
                continue;
            }
            Document doc = Jsoup.parse(getHtml(link));
            linkPool.addAll(parseHtmlGetLinks(doc));
            linkPool.remove(link);
            processedLinks.add(link);

            //如果是新闻页面

            if (isNews(doc)) {
                System.out.println("第"+(++count)+"条新闻:");
                String headLine = getHeadLine(doc);
                System.out.println(headLine);
                String content = getNewsContent(doc);
                System.out.println(content);
                System.out.println(link);
            }
        }
    }
}
