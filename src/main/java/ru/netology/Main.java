package ru.netology;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static final int textQuantity = 10_00;
    public static ArrayBlockingQueue<String> aText = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> bText = new ArrayBlockingQueue<>(100);
    public static ArrayBlockingQueue<String> cText = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        new Thread(() -> {
            for (int i = 0; i < textQuantity; i++) {
                String text = generateText("abc", 100_000);
                try {
                    aText.put(text);
                    bText.put(text);
                    cText.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread.sleep(1000);

        final ExecutorService threadPoll = Executors.newFixedThreadPool(3);
        List<Future> taskList = new ArrayList<>();

        final Future<String> future1 = threadPoll.submit(logic("a"));
        taskList.add(future1);
        final Future<String> future2 = threadPoll.submit(logic("b"));
        taskList.add(future2);
        final Future<String> future3 = threadPoll.submit(logic("c"));
        taskList.add(future3);

        for (Future<String> future: taskList) {
            System.out.println(future.get());
        }

        threadPoll.shutdown();
    }

    public static Callable<String> logic(String regex) {
        Callable<String> logic = () -> {
            String result = null;
            String str = null;
            int currentMax = 0;
            for (int i = 0; i < textQuantity - 1; i++) {
                switch (regex) {
                    case "a":
                        str = aText.poll(15, TimeUnit.SECONDS);
                        break;
                    case "b":
                        str = bText.poll(15, TimeUnit.SECONDS);
                        break;
                    case "c":
                        str = cText.poll(15, TimeUnit.SECONDS);
                        break;
                }
                if (currentMax < letterRepit(regex, str)) result = str;
            }
            return result;
        };
        return logic;
    }

    public static int letterRepit(String regex, String str) {
        return str.length() - str.replaceAll(regex, "").length();
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
//        Callable<String> logicA = () -> {
//            String result = null;
//            int currentMax = 0;
//            for (int i = 0; i < textQuantity; i++) {
//                String str = aText.take();
//                if (currentMax < letterRepit("a", str)) result = str;
//            }
//            return result;
//        };