package com.newbie;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Foo{
    private final AtomicBoolean firstFlag = new AtomicBoolean(false);
    private final AtomicBoolean secondFlag = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition conditionFlag = lock.newCondition();

    public void first() {
       Runnable runFirst = () -> {
        lock.lock();
        try {
            conditionFlag.signalAll();
            System.out.print("first");
            firstFlag.getAndSet(true);
            Thread.interrupted();
            }finally {
                     lock.unlock();
                    }
       };
       new Thread(runFirst).start();
    }

    public void second() {
        Runnable runSecond = () -> {
            lock.lock();
            try {
                while (!secondFlag.get()) {
                    conditionFlag.signalAll();
                    if (firstFlag.get()) {
                        System.out.print("second");
                        secondFlag.getAndSet(true);
                        Thread.interrupted();
                    } else
                        try {
                            conditionFlag.await();
                        } catch (InterruptedException ix) {
                            System.out.println(ix + " second");
                        }
                }
            }finally {
                lock.unlock();
            }
        };
        new Thread(runSecond).start();
    }

    public void third() {
       Runnable runThird = () -> {
            lock.lock();
            try{
                 while (!secondFlag.get()) {
                    conditionFlag.signalAll();
                    try {
                        conditionFlag.await();
                    } catch (InterruptedException exception) {
                    System.out.print(exception + "third");
                }
            }
            System.out.print("third");
            Thread.interrupted();
            } finally {
            lock.unlock();
         }
       };
        new Thread(runThird).start();
    }
}
