package com.teamparkin.mtdapp.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Locale;

public class Util {

    private Util() {
    } // this class cannot be instantiated

    /**
     * Converts a time string YYYY-MM-DDTHH:MM:SS-06:00 (HH = 00-23) to a
     * calendar
     *
     * @param time - string YYYY-MM-DDTHH:MM:SS-06:00
     * @return "HH:MM" + "am" or "pm", or null if invalid input.
     */
    public static Calendar convertMTDTimeString(String time) {
        if (time == null || !time.contains(":"))
            return null;
        Calendar calendar = Calendar.getInstance();
        String split[] = time.split("T");
        String dates[] = split[0].split("-");
        String times[] = split[1].split("-")[0].split(":");

        calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]));
        calendar.set(Calendar.DATE, Integer.parseInt(dates[2]));

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(times[1]));
        calendar.set(Calendar.SECOND, Integer.parseInt(times[2]));

        return calendar;
    }

    /**
     * Returns the number of minutes end is after start.
     *
     * @param start
     * @param end
     * @return
     */
    public static long getMinutesBetween(Calendar start, Calendar end) {
        if (start == null || end == null)
            return -987654321;
        long mins = 0;

        // Get the times in milliseconds since epoch
        long startTime = start.getTime().getTime();
        long finishTime = end.getTime().getTime();

        mins = (finishTime / 60000) - (startTime / 60000);

        return mins;
    }

    /**
     * Returns the 12 hour time of the calendar, eg 12:04 PM
     *
     * @param calendar
     * @return
     */
    public static String getTimeText(Calendar calendar) {
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR);
        String minString = (minute > 9) ? "" + minute : "0" + minute;
        String ampmString = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM"
                : "PM";
        int hour2 = hour % 12;
        String hourString = (hour2 > 0) ? "" + hour2 : "12";
        return hourString + ":" + minString + " " + ampmString;
    }

    /**
     * Gets the day, month and date, eg "Mon, Jan 6"
     *
     * @return
     */
    public static String getDateText(Calendar calendar) {
        String dayString = getDayName(calendar);
        String monthString = getMonthName(calendar);

        return dayString + ", " + monthString + " "
                + calendar.get(Calendar.DATE);
    }

    /**
     * Returns the day of the week name (3 letters only) of the calendar.
     *
     * @param calendar
     * @return
     */
    public static String getDayName(Calendar calendar) {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
                Locale.getDefault());// Locale.US);
    }

    /**
     * Returns the short month name of the calendar.
     *
     * @param calendar
     * @return
     */
    public static String getMonthName(Calendar calendar) {
        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault());// Locale.US);
    }

    /**
     * Makes a parcel, then makes an object out of the parcel, then returns that
     * object as a copy.
     *
     * @return
     */
    public static Parcelable copyParcelable(Parcelable original) {
        if (original == null)
            return null;
        Parcel source = Parcel.obtain();
        Parcel dest = Parcel.obtain();
        final byte[] bytes;

        source.writeParcelable(original, 0);
        bytes = source.marshall();

        dest.unmarshall(bytes, 0, bytes.length);
        dest.setDataPosition(0);

        Parcelable ret = dest.readParcelable(original.getClass()
                .getClassLoader());

        source.recycle();
        dest.recycle();

        return ret;
    }

    /**
     * Converts the number of Dips to number of Pixels using the context display
     * metrics.
     * <p/>
     * float scale = context.getResources().getDisplayMetrics().density;
     * <p/>
     * int pixels =(int) (dips * scale + 0.5f);
     *
     * @param context the context of with DisplayMetrics.
     * @param dips    the number of Density-Independent Pixels to convert to Pixels for this context.
     * @return the number of pixels corresponding to the number of dips on the screen defined by
     * the context.
     */
    public static int getPixelsFromDips(Context context, int dips) {
        return getPixelsFromDips(context, dips, 0)[0];
    }


    /**
     * <p>
     * Converts a list of Density-Independent pixels into just Pixels given a context used to
     * grab the screen density.
     * </p>
     * <p>
     * Usage example:
     * <pre>
     * {@code
     * List<Integer> pixelList = Util.getPixelsFromDips(getActivity(), 10, 100);
     * }
     * </pre>
     * </p>
     *
     * @param context the context of with DisplayMetrics.
     * @param dips    the number of Density-Independent Pixels to convert to Pixels for this context.
     * @return a list the number of pixels corresponding to each dips parameter. If the context
     * is null, just returns a list of zeros.
     */
    public static int[] getPixelsFromDips(Context context, int... dips) {
        int ret[] = new int[dips.length];
        float scale = 0.0f;
        if (context != null && context.getResources() != null && context.getResources()
                .getDisplayMetrics() != null)
            scale = context.getResources().getDisplayMetrics().density;
        int i = 0;
        for (int dip : dips) {
            ret[i] = getPixelsFromDipsAndScale(dip, scale);
            i++;
        }
        return ret;
    }

    /**
     * Helper method to scale the dips into pixels.
     *
     * @param dips
     * @param scale
     * @return
     */
    private static int getPixelsFromDipsAndScale(int dips, float scale) {
        return (int) (dips * scale + 0.5f);
    }

    public static String[] appendStringArrays(String[] arr1, String[] arr2) {
        return concatenate(arr1, arr2);
    }

    private static <T> T[] concatenate(T[] A, T[] B) {
        int aLen = A.length;
        int bLen = B.length;

        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(A.getClass().getComponentType(), aLen
                + bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);

        return C;
    }
}
