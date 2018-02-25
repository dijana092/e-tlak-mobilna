package unipu.oikt.djaranovic.etlak.dijagnoza;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class DecimalRemover extends PercentFormatter { // pomoćna klasa za uklanjanje decimalnog zareza postotka kod 'tortnog' prikaza

    protected DecimalFormat mFormat;

    public DecimalRemover(DecimalFormat format) {
        this.mFormat = format;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format((int)value);
    }
}