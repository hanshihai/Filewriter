import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Execute {

    public final static byte[] content = "abcdefghijklmnopqrstuvwxyz0123456789".getBytes();
    public final static long idle = 1000 * 3;
    public final static long max_life_time = 1000 * 60 * 60 * 24;
    public final static int retry = 3;

    private final RandomAccessFile randomAccess;
    private final long totalLength;

    public Execute(File file, long length) throws IOException {
        this.randomAccess = new RandomAccessFile(file, "rw");
        if(length < 0) {
            throw new IllegalArgumentException("total length is negative");
        }
        this.totalLength = length;
    }

    public synchronized long writeContent(byte[] buffer, int length, long startOffset) throws IOException {
        if (startOffset < 0 || startOffset >= totalLength) {
            throw new IndexOutOfBoundsException("start offset out of range: " + startOffset);
        }
        randomAccess.seek(startOffset);
        randomAccess.write(buffer, 0, length);
        return startOffset + length;
    }

    public synchronized void close() throws IOException {
        if(randomAccess != null) {
                randomAccess.close();
        }
    }
    public static void printUsage() {
        System.out.println(" ------ usage ------");
        System.out.println(" java Execute path length idleTime runTime");
        System.out.println(" such as: java Execute /tmp/sample 1024 3000 10000");
        System.out.println(" ------ end ------");
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            Execute.printUsage();
        }

        System.out.println(" ------ start at : " + SimpleDateFormat.getInstance().format(new Date()) + " ------");

        File file = new File(args[0]);
        long fileLength = Long.parseLong(args[1]);
        long idleT = idle;
        long runT = max_life_time;

        if(args.length > 2) {
            idleT = Long.parseLong(args[2]);
        }

        if(args.length > 3) {
            runT = Long.parseLong(args[3]);
        }

        Execute exec = new Execute(file, fileLength);
        long startTime = new Date().getTime();

        long intOffset = 0;
        while(true) {
            if(intOffset + content.length >= fileLength) {
                intOffset = 0;
            }
            int again = 0;
            while(again < retry) {
                try{
                    intOffset = exec.writeContent(content, content.length, intOffset);
                    System.out.print(" >> " + intOffset);
                    Thread.sleep(idleT);
                    break;
                }catch(IOException ioe) {
                    Thread.sleep(idleT);
                    again++;
                }
            }

            long now = new Date().getTime();
            if(startTime + runT <= now) {
                System.out.println("\n");
                break;
            }
        }

        exec.close();

        System.out.println(" ------ end at : " + SimpleDateFormat.getInstance().format(new Date()) + " ------");
    }
}
