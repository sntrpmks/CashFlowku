package com.example.cashflowkujava.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SimpleBarChartView extends View {

    private double[] salesData = new double[]{0, 0, 0, 0, 0, 0, 0};
    private double[] expensesData = new double[]{0, 0, 0, 0, 0, 0, 0};
    private String[] labels = new String[]{"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};

    private Paint gridPaint;
    private Paint salesPaint;
    private Paint expensesPaint;
    private Paint textPaint;

    private int paddingLeft = 100;
    private int paddingRight = 40;
    private int paddingTop = 60;
    private int paddingBottom = 80;

    public SimpleBarChartView(Context context) {
        super(context);
        init();
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xFFE2E8F0); // light gray border
        gridPaint.setStrokeWidth(2f);

        salesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        salesPaint.setColor(0xFF10B981); // Emerald Green
        salesPaint.setStyle(Paint.Style.FILL);

        expensesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        expensesPaint.setColor(0xFFEF4444); // Red Rose
        expensesPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF64748B); // Slate Gray
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(double[] sales, double[] expenses, String[] labels) {
        if (sales != null) this.salesData = sales;
        if (expenses != null) this.expensesData = expenses;
        if (labels != null) this.labels = labels;
        invalidate(); // Redraw view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Draw horizontal grid lines and Y axis
        double maxVal = 10000;
        for (double val : salesData) {
            if (val > maxVal) maxVal = val;
        }
        for (double val : expensesData) {
            if (val > maxVal) maxVal = val;
        }

        // Add 15% padding to top of chart
        maxVal = maxVal * 1.15;

        // Draw 4 grid levels
        for (int i = 0; i <= 4; i++) {
            float y = paddingTop + chartHeight - (chartHeight * i / 4f);
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);

            // Draw Y-axis labels
            double gridVal = maxVal * i / 4.0;
            String labelStr;
            if (gridVal >= 1000000) {
                labelStr = String.format(java.util.Locale.US, "%.1fM", gridVal / 1000000.0);
            } else if (gridVal >= 1000) {
                labelStr = String.format(java.util.Locale.US, "%.0fk", gridVal / 1000.0);
            } else {
                labelStr = String.format(java.util.Locale.US, "%.0f", gridVal);
            }
            
            Paint yLabelPaint = new Paint(textPaint);
            yLabelPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(labelStr, paddingLeft - 20, y + 10, yLabelPaint);
        }

        int numGroups = labels.length;
        if (numGroups == 0) return;

        float groupWidth = (float) chartWidth / numGroups;
        float barSpacing = 8f;
        float innerBarWidth = (groupWidth - (barSpacing * 3)) / 2f;

        for (int i = 0; i < numGroups; i++) {
            float groupLeft = paddingLeft + (i * groupWidth);
            float groupCenterX = groupLeft + (groupWidth / 2f);

            // Draw group label
            canvas.drawText(labels[i], groupCenterX, height - paddingBottom + 40, textPaint);

            // Calculate heights
            float salesHeight = maxVal > 0 ? (float) (chartHeight * (salesData[i] / maxVal)) : 0;
            float expensesHeight = maxVal > 0 ? (float) (chartHeight * (expensesData[i] / maxVal)) : 0;

            // Draw Sales Bar
            float salesLeft = groupLeft + barSpacing;
            float salesTop = paddingTop + chartHeight - salesHeight;
            float salesRight = salesLeft + innerBarWidth;
            float salesBottom = paddingTop + chartHeight;
            
            if (salesHeight > 0) {
                RectF salesRect = new RectF(salesLeft, salesTop, salesRight, salesBottom);
                canvas.drawRoundRect(salesRect, 8f, 8f, salesPaint);
            }

            // Draw Expenses Bar
            float expensesLeft = salesRight + barSpacing;
            float expensesTop = paddingTop + chartHeight - expensesHeight;
            float expensesRight = expensesLeft + innerBarWidth;
            float expensesBottom = paddingTop + chartHeight;

            if (expensesHeight > 0) {
                RectF expensesRect = new RectF(expensesLeft, expensesTop, expensesRight, expensesBottom);
                canvas.drawRoundRect(expensesRect, 8f, 8f, expensesPaint);
            }
        }
    }
}
