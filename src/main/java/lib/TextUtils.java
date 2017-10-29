package lib;

import com.sun.istack.internal.Nullable;

public class TextUtils {

    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    public static void printUsage(){
        System.out.println( ">>> Please input command as below: ");
        System.out.println( "   STATUS: Get reader status");
        System.out.println( "   START: Start working ");
        System.out.println( "   STOP: Stop working");
    }
}
