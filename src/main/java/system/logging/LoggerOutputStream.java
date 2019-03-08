package system.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LoggerOutputStream extends OutputStream {
    private PrintStream stdout;
    private PrintStream fileStream;

    public LoggerOutputStream(PrintStream stdout, PrintStream fileStream) {
        this.stdout = stdout;
        this.fileStream = fileStream;
    }

    @Override
    public void write(int b) throws IOException {
        stdout.write(b);
        fileStream.write(b);
    }
}
