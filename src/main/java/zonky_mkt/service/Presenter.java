package zonky_mkt.service;

import zonky_mkt.model.Loan;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class Presenter implements Runnable {
    private final static Logger log = Logger.getLogger(Presenter.class.getName());
    private static final int MAX_WIDTH = 80;

    private BlockingQueue<Loan> loanQueue;
    private PrintWriter writer;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYY hh:mm:ss");

    public Presenter(BlockingQueue<Loan> loanQueue, OutputStream out) {
        this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        this.loanQueue = loanQueue;
    }

    private void printWrapped(String text) {
        if (text != null) {
            StringTokenizer st = new StringTokenizer(text);
            int spaceLeft = MAX_WIDTH;
            int spaceWidth = 1;
            while (st.hasMoreTokens()) {
                String word = st.nextToken();
                if ((word.length() + spaceWidth) > spaceLeft) {
                    writer.print("\n" + word + " ");
                    spaceLeft = MAX_WIDTH - word.length();
                } else {
                    writer.print(word + " ");
                    spaceLeft -= (word.length() + spaceWidth);
                }
            }
            writer.println();
        }
    }

    private void print(Loan loan) {
        try {
            writer.println(loan.getName() + " (" + loan.getUrl() + ")");
            writer.println("-".repeat(loan.getName().length()));
            writer.println(loan.getDatePublished().format(formatter));
            printWrapped(loan.getStory());
            writer.println("=".repeat(MAX_WIDTH));
            writer.flush();
        } catch (Exception e) {
            log.severe("Error while printing a loan (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Loan loan = loanQueue.take();
                print(loan);
            } catch (InterruptedException e) {
                log.info("Interrupted. Aborting presentation.");
                Thread.currentThread().interrupt();
            }
        }
    }
}
