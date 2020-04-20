package unithon.boot.executor;

import unithon.boot.Log;

import java.io.Closeable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * schedule task
 */
public final class ScheduleTask implements Closeable {
    /**
     * suffix for zero hour
     */
    private static final String ZERO_HOUR = " 00:00:00";
    /**
     * suffix for zero minutes
     */
    private static final String ZERO_MINUTES = ":00:00";
    /**
     * mills per hour is 60 * 60 * 1000
     */
    private static final int MILLS_PER_HOUR = 3600000;
    /**
     * mills per day is 24 * {@code MILLS_PER_HOUR}
     */
    private static final int MILLS_PER_DAY = MILLS_PER_HOUR * 24;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat HOUR = new SimpleDateFormat("yyyy-MM-dd HH");
    private static final SimpleDateFormat FULLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * executor service
     */
    private final ScheduledExecutorService service;
    private final ThreadLocal<Integer> period = ThreadLocal.withInitial(() -> 0);

    /**
     * Schedule event.
     *
     * @param frequency run frequency
     */
    private ScheduleTask(Frequency frequency) {
        service = Executors.newSingleThreadScheduledExecutor();
        this.period.set(switch (frequency) {
            case Hourly -> 3600;
            case Daily -> 86400;
        });
    }

    public static void createSingleThreadTask(final Runnable command,
                                              final Frequency frequency,
                                              final boolean instantlyRun) {
        ScheduleTask scheduleTask = new ScheduleTask(frequency);
        scheduleTask.addTask(command, instantlyRun);
    }

    public static void createGroupedThreadTask(final ArrayList<Runnable> taskList,
                                               final Frequency frequency,
                                               final boolean instantlyRun) {
        ScheduleTask scheduleTask = new ScheduleTask(frequency);
        scheduleTask.addTask(taskList, instantlyRun);
    }

    /**
     * add a group of tasks.
     *
     * @param taskList     task list. task will be execute in specified ordinary.(not concurrent)
     * @param instantlyRun if is true. Task will run at once.
     */
    private void addTask(final ArrayList<Runnable> taskList, final boolean instantlyRun) {
        long initial_delay = period.get() == 86400 ? t2NextDay() : t2NextHour();
        Runnable runnable = () -> taskList.forEach(Runnable::run);
        if (instantlyRun) {
            runnable.run();
        }
        service.scheduleAtFixedRate(runnable, initial_delay, period.get(), TimeUnit.SECONDS);
    }

    /**
     * add a single task.
     *
     * @param command      task you want to run.
     * @param instantlyRun if is true. Task will run at once.
     */
    private void addTask(final Runnable command, final boolean instantlyRun) {
        long initial_delay = period.get() == 86400 ? t2NextDay() : t2NextHour();
        if (instantlyRun) {
            command.run();
        }
        service.scheduleAtFixedRate(command, initial_delay, period.get(), TimeUnit.SECONDS);
    }

    /**
     * calculate time to next day
     *
     * @return seconds to next day.
     */
    private long t2NextDay() {
        return fixDate(DATE_FORMAT, ZERO_HOUR, MILLS_PER_DAY);
    }

    /**
     * calculate time to next hour
     *
     * @return seconds to next hour.
     */
    private long t2NextHour() {
        return fixDate(HOUR, ZERO_MINUTES, MILLS_PER_HOUR);
    }

    /**
     * fix date time
     */
    private long fixDate(SimpleDateFormat simpleDateFormat, String now, int fixMills) {
        Date date = new Date();
        String s = simpleDateFormat.format(date) + now;
        Date next;
        try {
            next = FULLY_FORMAT.parse(s);
            next = new Date(next.getTime() + fixMills);
        } catch (ParseException e) {
            Log.e(e);
            return 0;
        }
        return (next.getTime() - date.getTime()) / 1000;
    }

    @Override
    public void close() {
        service.shutdown();
    }

    /**
     * running frequency
     */
    public enum Frequency {
        Hourly, Daily
    }
}
