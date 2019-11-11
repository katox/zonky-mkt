package zonky_mkt;

import org.junit.BeforeClass;
import org.junit.Test;
import zonky_mkt.model.Loan;
import zonky_mkt.service.Presenter;
import zonky_mkt.util.Logging;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Presenter.
 */
public class PresenterTest {

    @BeforeClass
    public static void beforeAllTestMethods() {
        Logging.setupTestLogging();
    }

    @Test
    public void blocksOnEmptyInputQueue() {
        try {
            BlockingQueue<Loan> loanQueue = new LinkedBlockingDeque<>();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Presenter presenter = new Presenter(loanQueue, out);

            Thread t = new Thread(presenter);
            t.start();
            Thread.sleep(250);
            assertEquals("", out.toString(StandardCharsets.UTF_8));

            t.interrupt();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Test
    public void basicOutput() {
        try {
            BlockingQueue<Loan> loanQueue = new LinkedBlockingDeque<>();
            loanQueue.put(new Loan(1, new URL("https://test.url"),
                    "name1", "story",
                    OffsetDateTime.parse("1968-08-21T17:11:19.890+02:00")));
            loanQueue.put(new Loan(1, new URL("https://test.url"),
                    "longer name and story", "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.\n\n Duis condimentum augue id magna semper rutrum. Aliquam erat volutpat. Curabitur vitae diam non enim vestibulum interdum. Suspendisse sagittis ultrices augue. Etiam commodo dui eget wisi. Aliquam erat volutpat. ...\n" +
                    "\n" +
                    "Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.",
                    OffsetDateTime.parse("2019-11-01T01:23:45.678+01:00")));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Presenter presenter = new Presenter(loanQueue, out);

            Thread t = new Thread(presenter);
            t.start();
            while (!loanQueue.isEmpty()) {
                Thread.sleep(100);
            }
            String expectedText = "name1 (https://test.url)\n" +
                    "-----\n" +
                    "21.08.1968 17:11:19\n" +
                    "story \n" +
                    "================================================================================\n" +
                    "longer name and story (https://test.url)\n" +
                    "---------------------\n" +
                    "01.11.2019 01:23:45\n" +
                    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis condimentum \n" +
                    "augue id magna semper rutrum. Aliquam erat volutpat. Curabitur vitae diam non \n" +
                    "enim vestibulum interdum. Suspendisse sagittis ultrices augue. Etiam commodo dui \n" +
                    "eget wisi. Aliquam erat volutpat. ... Itaque earum rerum hic tenetur a sapiente \n" +
                    "delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut \n" +
                    "perferendis doloribus asperiores repellat. \n" +
                    "================================================================================\n";
            assertEquals(expectedText, out.toString(StandardCharsets.UTF_8));

            t.interrupt();
        } catch (InterruptedException e) {
            // ignore
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
