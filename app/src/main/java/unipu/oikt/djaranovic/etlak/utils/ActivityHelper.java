package unipu.oikt.djaranovic.etlak.utils;

import android.app.Activity;
import android.content.Intent;

public class ActivityHelper { // pomoćna klasa vezano za aktivnosti

    public static void startActivity(Activity activity, Class klasa) {
        Intent intent = new Intent(activity, klasa);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}