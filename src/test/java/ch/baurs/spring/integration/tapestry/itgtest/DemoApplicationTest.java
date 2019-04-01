package ch.baurs.spring.integration.tapestry.itgtest;

import ch.baurs.spring.integration.tapestry.itgtest.services.TestService;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.services.RequestGlobals;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.ViewResolver;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestBootApplication.class)
public class DemoApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RequestGlobals requestGlobals;

    @Autowired
    TestService testService;

    @Autowired
    @Qualifier("mvcViewResolver")
    ViewResolver viewResolver;

    @Test
    public void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    public void testTapestryServiceToSpringInjection() {
        assertNotNull(requestGlobals);
    }

    @Test
    public void testSpringServiceToTapestryInjection() {
        assertNotNull(testService);
        assertEquals(viewResolver, testService.getSpringMvcViewResolver());
    }

    @Test
    public void testTapestryPageRendering() throws Exception {
        String baseUrl = "http://127.0.0.1:" + port;

        //make GET request and parse html
        String page = sendGet(baseUrl + "/CtxPath");
        assertNotNull(page);
        Document pageDoc = Jsoup.parse(page);
        assertNotNull(pageDoc);

        //assert
        Elements mainDiv = pageDoc.body().select("div:contains(INDEX CONTENT)");
        assertEquals(1, mainDiv.size());
        assertEquals("main", mainDiv.attr("class"));
        assertEquals("FULL INDEX CONTENT", mainDiv.text());


        Elements testStyles = pageDoc.head().select("link[href$=test-styles.css]");
        assertEquals(1, testStyles.size());

        String href = testStyles.attr("href");
        String assetUrl = baseUrl + href;
        String asset = sendGet(assetUrl);
        assertNotNull(asset);
        assertTrue(asset.contains("* { color: red; }"));

    }

    private String sendGet(String url) throws Exception {


        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Test");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        InputStream inputStream = con.getInputStream();
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

    }

}
