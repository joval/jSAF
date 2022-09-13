// Copyright (C) 2022 Arctic Wolf Networks.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jsaf.JSAFSystem;
import jsaf.Message;
import jsaf.intf.io.IReader;
import jsaf.util.Strings;
import org.slf4j.cal10n.LocLogger;

/**
 * A PerishableReader is a class that implements IReader but has time-limits for all read operations.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class PerishableReader extends InputStream implements IReader {

  private static final ExecutorService service = Executors.newCachedThreadPool(new PRThreadFactory());
  private static final HashSet<String> interruptableTypeNames = new HashSet<String>();

  static {
    interruptableTypeNames.add("java.net.SocketInputStream");
  }

  /**
   * Create a new instance using the given InputStream and read timeout.
   *
   * If the specified InputStream is already a PerishableReader, then its timeout is altered and it is returned.
   *
   * @param timeout the maximum amount of time that should be allowed for a read operation to return, in milliseconds.
   *                If timeout &lt;= 0, a default of 1hr will apply.
   */
  public static final PerishableReader newInstance(InputStream in, long timeout) {
    if (in == null) {
      throw new NullPointerException();
    }
    if (in instanceof PerishableReader) {
      PerishableReader reader = (PerishableReader) in;
      reader.setTimeout(timeout);
      return reader;
    } else {
      return new PerishableReader(in, timeout);
    }
  }

  /**
   * Add an InputStream type name whose read operations can be interrupted.
   *
   * If an InputStream type is known to have interruptable reads, then a PerishableReader will use TimerTasks and interrupts
   * to implement the timeout functionality. This is fairly low-cost. If an InputStream is not known to have interruptable
   * reads, then a PerishableReader will use Futures to implement the timeout functionality. This means every read operation
   * will have to run in a new Thread, which is fairly high-cost.
   *
   * By default, PerishableReader knows only about java.net.SocketInputStream.
   */
  public static final void addInterruptableTypeName(String typeName) {
    interruptableTypeNames.add(typeName);
  }

  private LocLogger logger;
  private InputStream in;
  private boolean interruptable, closed, expired;
  private long timeout;

  protected Buffer buffer;
  protected boolean isEOF;

  /**
   * Check whether a read has expired.
   *
   * @since 1.0
   */
  public synchronized boolean checkExpired() {
    return expired;
  }

  /**
   * Set the read timeout.
   *
   * @since 1.0
   */
  public synchronized void setTimeout(long timeout) {
    if (timeout <= 0) {
      this.timeout = 3600000L; // 1hr
    } else {
      this.timeout = timeout;
    }
  }

  // Implement ILoggable

  public LocLogger getLogger() {
    return logger;
  }

  public void setLogger(LocLogger logger) {
    this.logger = logger;
  }

  // Implement IReader

  public InputStream getStream() {
    return this;
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      in.close();
      closed = true;
    }
  }

  @Override
  public synchronized int available() throws IOException {
    int buffered = 0;
    if (buffer.hasNext()) {
      buffered = buffer.len - buffer.pos;
    }
    return buffered + in.available();
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public synchronized void mark(int readLimit) {
    buffer.mark(readLimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    if (buffer.isEmpty()) {
      throw new IOException("empty buffer");
    }
    buffer.reset();
  }

  public boolean checkClosed() {
    return buffer.hasNext() ? false : closed;
  }

  public boolean checkEOF() {
    return buffer.hasNext() ? false : isEOF;
  }

  public synchronized String readLine() throws IOException {
    return readLine(Strings.ASCII);
  }

  public synchronized String readLine(Charset charset) throws IOException {
    ByteArrayOutputStream buff = new ByteArrayOutputStream();
    String result = null;
    int ch = 0;
    while (result == null && (ch = read()) != -1) {
      switch (ch) {
        case '\n':
          result = new String(buff.toByteArray(), charset);
          break;

        case '\r':
          mark(1);
          if (read() != '\n') {
            reset();
          }
          result = new String(buff.toByteArray(), charset);
          break;

        default:
          buff.write((byte) ch);
          break;
      }
    }
    if (result == null) {
      isEOF = true;
      if (buff.size() > 0) {
        result = new String(buff.toByteArray(), charset);
      }
    }
    return result;
  }

  public synchronized void readFully(byte[] buff) throws IOException {
    readFully(buff, 0, buff.length);
  }

  public synchronized void readFully(byte[] buff, int offset, int len) throws IOException {
    int end = offset + len;
    for (int i = offset; i < end; i++) {
      int ch = read();
      if (ch == -1) {
        isEOF = true;
        throw new EOFException(Message.getMessage(Message.ERROR_EOS));
      } else {
        buff[i] = (byte) (ch & 0xFF);
      }
    }
  }

  public synchronized byte[] readUntil(byte[] delim) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean found = false;
    do {
      byte[] buff = readUntil(delim[0]);
      if (buff == null) {
        return null;
      }
      out.write(buff);
      mark(delim.length);
      byte[] b2 = new byte[delim.length];
      b2[0] = delim[0];
      try {
        readFully(b2, 1, b2.length - 1);
        if (Arrays.equals(b2, delim)) {
          found = true;
        } else {
          out.write(b2[0]);
          reset();
        }
      } catch (EOFException e) {
        reset();
        int len = 0;
        buff = new byte[512];
        while ((len = read(buff)) > 0) {
          out.write(buff, 0, len);
        }
        break;
      }
    } while (!found);
    return out.toByteArray();
  }

  public synchronized byte[] readUntil(int delim) throws IOException {
    int ch = 0, len = 0;
    byte[] buff = new byte[512];
    while ((ch = read()) != -1 && ch != delim) {
      if (len == buff.length) {
        byte[] old = buff;
        buff = new byte[old.length + 512];
        for (int i = 0; i < old.length; i++) {
          buff[i] = old[i];
        }
        old = null;
      }
      buff[len++] = (byte) (ch & 0xFF);
    }
    if (ch == -1 && len == 0) {
      isEOF = true;
      return null;
    } else {
      byte[] result = new byte[len];
      for (int i = 0; i < len; i++) {
        result[i] = buff[i];
      }
      return result;
    }
  }

  @Override
  public synchronized int read(byte[] buff) throws IOException {
    return read(buff, 0, buff.length);
  }

  @Override
  public synchronized int read(byte[] buff, int offset, int len) throws IOException {
    if (offset < 0 || len < 0 || len > buff.length - offset) {
      throw new IndexOutOfBoundsException();
    }
    int bufferBytesRead = 0;
    while (buffer.hasNext() && bufferBytesRead < len) {
      buff[offset++] = buffer.next();
      bufferBytesRead++;
    }
    int streamBytesRead = streamRead(buff, offset, len - bufferBytesRead);
    if (streamBytesRead == -1) {
      return bufferBytesRead == 0 ? -1 : bufferBytesRead;
    } else {
      int bytesRead = bufferBytesRead + streamBytesRead;
      int end = offset + bytesRead;
      for (int i = offset; buffer.hasCapacity() && i < end; i++) {
        buffer.add((byte) (i & 0xFF));
      }
      return bufferBytesRead == 0 ? streamBytesRead : bytesRead;
    }
  }

  @Override
  public synchronized int read() throws IOException {
    int i = -1;
    if (buffer.hasNext()) {
      i = (int) buffer.next();
    } else if (!isEOF) {
      i = streamRead();
      if (i != -1) {
        if (buffer.hasCapacity()) {
          buffer.add((byte) (i & 0xFF));
        } else {
          buffer.clear(); // buffer overflow
        }
      }
    }
    if (i == -1) {
      isEOF = true;
    }
    return i;
  }

  // Protected

  protected PerishableReader(InputStream in, long timeout) {
    if (in instanceof PerishableReader) {
      throw new IllegalArgumentException(in.getClass().getName());
    } else {
      logger = Message.getLogger();
      this.in = in;
      interruptable = isInterruptable(in);
      isEOF = false;
      closed = false;
      expired = false;
      buffer = new Buffer(0);
    }
    setTimeout(timeout);
  }

  /**
   * Read a character directly from the wrapped InputStream.
   *
   * @throws InterruptedIOException if the no byte is returned before the timeout expires
   */
  protected int streamRead() throws IOException {
    if (interruptable) {
      InterruptTask task = new InterruptTask(Thread.currentThread());
      JSAFSystem.schedule(task, timeout);
      int ch = in.read();
      JSAFSystem.cancelTask(task);
      return ch;
    } else {
      try {
        return service.submit(new ReadTask(in)).get(timeout, TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
          throw (IOException) cause;
        } else {
          throw new IOException(cause);
        }
      } catch (TimeoutException e) {
        expired = true;
        throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
      } catch (InterruptedException e) {
        expired = true;
        throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
      }
    }
  }

  /**
   * Identical to streamRead(buff, 0, buff.length)
   */
  protected int streamRead(byte[] buff) throws IOException {
    return streamRead(buff, 0, buff.length);
  }

  /**
   * Read from the wrapped InputStream directly into the supplied buffer, with the specified offset and maximum length..
   *
   * @throws InterruptedIOException if the no bytes are read before the timeout expires
   */
  protected int streamRead(byte[] buff, int offset, int len) throws IOException {
    if (offset < 0 || len < 0 || len > buff.length - offset) {
      throw new IndexOutOfBoundsException();
    }
    if (interruptable) {
      InterruptTask task = new InterruptTask(Thread.currentThread());
      JSAFSystem.schedule(task, timeout);
      int bytesRead = in.read(buff, offset, len);
      JSAFSystem.cancelTask(task);
      isEOF = bytesRead == -1;
      return bytesRead;
    } else {
      try {
        int bytesRead = service.submit(new ReadTask(in, buff, offset, len)).get(timeout, TimeUnit.MILLISECONDS);
        isEOF = bytesRead == -1;
        return bytesRead;
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
          throw (IOException) cause;
        } else {
          throw new IOException(cause);
        }
      } catch (TimeoutException e) {
        expired = true;
        throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
      } catch (InterruptedException e) {
        expired = true;
        throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
      }
    }
  }

  // Private

  /**
   * Determine whether the specified InputStream is (or wraps) an interruptable type.
   */
  private boolean isInterruptable(InputStream in) {
    try {
      Field f = FilterInputStream.class.getDeclaredField("in");
      f.setAccessible(true);
      while (in instanceof FilterInputStream) {
        in = (InputStream) f.get((FilterInputStream) in);
      }
      String className = in.getClass().getName();
      return interruptableTypeNames.contains(className);
    } catch (Exception e) {
      logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
      return false;
    }
  }

  static class PRThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private int counter = 0;

    PRThreadFactory() {
      group = new ThreadGroup("PerishableReader Thread Group");
    }

    // Implement ThreadFactory

    public synchronized Thread newThread(Runnable r) {
      String name = new StringBuffer("perishable-reader-").append(Integer.toString(counter++)).toString();
      Thread t = new Thread(group, r, name);
      t.setDaemon(true);
      return t;
    }

  }

  static class InterruptTask implements Runnable {

    private Thread t;

    InterruptTask(Thread t) {
      this.t = t;
    }

    // Implement Runnable

    public void run() {
      t.interrupt();
    }

  }

  static class ReadTask implements Callable<Integer> {

    private InputStream in;
    private byte[] buff = null;
    private int offset = 0, len = 0;
    private Thread caller;

    ReadTask(InputStream in) {
      this.in = in;
      caller = Thread.currentThread();
    }

    ReadTask(InputStream in, byte[] buff, int offset, int len) {
      this(in);
      this.buff = buff;
      this.offset = offset;
      this.len = Math.min(len, buff.length - offset);
    }

    // Implement Callable<Integer>

    public Integer call() throws IOException {
      try {
        if (buff == null) {
          return in.read();
        } else {
          return in.read(buff, offset, len);
        }
      } catch (IOException e) {
        IOException e2 = new IOException(e);
        e2.setStackTrace(caller.getStackTrace());
        throw e2;
      }
    }

  }

  protected class Buffer {

    byte[] buff = null;
    int pos = 0;
    int len = 0;
    int resetPos = 0;

    public Buffer(int size) {
      mark(size);
    }

    @Override
    public String toString() {
      if (buff == null) {
        return "Buffer empty";
      }
      return "Buffer size: " + buff.length + " pos: " + pos + " len: " + len +
             " Ahead: \"" + new String(buff, pos, len - pos) + "\"" + " hasNext: " + hasNext();
    }

    public void mark(int size) {
      if (hasNext()) {
        //
        // If the stream is already reading from inside the buffer, then don't lose the buffered data.
        //
        if (pos + size > len) {
          byte[] temp = buff;
          buff = new byte[size + pos];
          len = len - pos;
          System.arraycopy(temp, pos, buff, 0, len);
          pos = 0;
          resetPos = 0;
        }
      } else {
        buff = new byte[size];
        len = 0;
        pos = 0;
        resetPos = 0;
      }
    }

    public void reset() {
      pos = resetPos;
    }

    public boolean hasNext() {
      return buff != null && pos < len;
    }

    public synchronized byte next() throws NoSuchElementException {
      if (hasNext()) {
        return buff[pos++];
      } else {
        throw new NoSuchElementException();
      }
    }

    public synchronized void add(int ch) {
      add((byte) (ch & 0xFF));
    }

    public synchronized void add(byte b) {
      if (hasNext()) {
        //
        // A delayed add: insert the byte before the active part of the buffer
        //
        if (!hasCapacity()) {
          mark(buff.length + 1);
        }
        for (int i = len; i > pos; i--) {
          buff[i] = buff[i - 1];
        }
        len++;
        buff[pos++] = b;
      } else if (hasCapacity()) {
        buff[len++] = b;
        pos = len;
      } else {
        clear();
      }
    }

    public synchronized void add(byte[] bytes, int offset, int length) {
      int end = Math.min(bytes.length, (offset + length));
      for (int i = offset; i < end; i++) {
        add(bytes[i]);
      }
    }

    public synchronized void clear() throws IllegalStateException {
      if (hasNext()) {
        throw new IllegalStateException(Integer.toString(len - pos));
      } else if (buff != null) {
        buff = null;
      }
    }

    // Protected

    protected boolean hasCapacity() {
      return buff != null && len < buff.length;
    }

    protected boolean isEmpty() {
      return buff == null;
    }

  }

}
