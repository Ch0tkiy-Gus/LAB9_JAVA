import java.io.PrintStream;

class RingBuffer<T> {
    private final Object[] buffer;
    private int head = 0;
    private int tail = 0;

    public RingBuffer(int capacity) {
        this.buffer = new Object[capacity + 1];
    }

    public synchronized void put(T item) throws InterruptedException {
        while ((tail + 1) % buffer.length == head) {
            wait();
        }
        buffer[tail] = item;
        tail = (tail + 1) % buffer.length;
        notifyAll();
    }

    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (head == tail) {
            wait();
        }
        T item = (T) buffer[head];
        buffer[head] = null;
        head = (head + 1) % buffer.length;
        notifyAll();
        return item;
    }
}

public class Task2ProducerConsumer {
    public static void main(String[] args) throws InterruptedException {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RingBuffer<String> buffer1 = new RingBuffer<>(10);
        RingBuffer<String> buffer2 = new RingBuffer<>(10);

        for (int i = 1; i <= 5; i++) {
            final int threadNum = i;
            Thread producer = new Thread(() -> {
                int msgCount = 1;
                try {
                    while (true) {
                        String msg = "Потік № " + threadNum + " згенерував повідомлення " + msgCount++;
                        buffer1.put(msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            producer.setDaemon(true);
            producer.start();
        }

        for (int i = 1; i <= 2; i++) {
            final int threadNum = i;
            Thread translator = new Thread(() -> {
                try {
                    while (true) {
                        String originalMsg = buffer1.take();
                        String translatedMsg = "Потік № " + threadNum + " переклав повідомлення [" + originalMsg + "]";
                        buffer2.put(translatedMsg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            translator.setDaemon(true);
            translator.start();
        }

        for (int i = 1; i <= 100; i++) {
            String message = buffer2.take();
            System.out.println(i + ". " + message);
        }

        System.out.println("Усі 100 повідомлень вичитано. Програма завершує роботу.");
    }
}