package org.mockserver.examples.proxy.service.jettyclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.api.ContentResponse;
import org.mockserver.examples.proxy.model.Book;
import org.mockserver.examples.proxy.service.BookService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import static org.mockserver.examples.proxy.json.ObjectMapperFactory.createObjectMapper;

/**
 * @author jamesdbloom
 */
@Component
public class BookServiceJettyHttpClient implements BookService {

    @Resource
    private Environment environment;
    private Integer port;
    private Integer proxyPort;
    private String host;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;

    @PostConstruct
    private void initialise() {
        port = environment.getProperty("bookService.port", Integer.class);
        proxyPort = environment.getProperty("bookService.proxyPort", Integer.class);
        host = environment.getProperty("bookService.host", "localhost");
        objectMapper = createObjectMapper();
        httpClient = createHttpClient();
    }

    private HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();
        try {
            new HttpProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException("Exception creating HttpClient", e);
        }
        return httpClient;
    }

    @PreDestroy
    private void stopClient() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            throw new RuntimeException("Exception stopping HttpClient", e);
        }
    }

    @Override
    public Book[] getAllBooks() {
        try {
            return objectMapper.readValue(httpClient.GET("http://" + host + ":" + port + "/get_books").getContentAsString(), Book[].class);
        } catch (Exception e) {
            throw new RuntimeException("Exception making request to retrieve all books", e);
        }
    }

    @Override
    public Book getBook(String id) {
        try {
            return objectMapper.readValue(httpClient.GET("http://" + host + ":" + port + "/get_book" + "?id=" + id).getContentAsString(), Book.class);
        } catch (Exception e) {
            throw new RuntimeException("Exception making request to retrieve a book with id [" + id + "]", e);
        }
    }
}
