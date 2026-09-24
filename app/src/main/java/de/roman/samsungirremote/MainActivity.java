package de.roman.samsungirremote;

import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final int CARRIER_HZ = 38000;

    // Samsung AA59 / classic Samsung TV 32-bit IR codes.
    private static final long POWER   = 0xE0E040BFL;
    private static final long SOURCE  = 0xE0E0807FL;
    private static final long VOL_UP  = 0xE0E0E01FL;
    private static final long VOL_DN  = 0xE0E0D02FL;
    private static final long CH_UP   = 0xE0E048B7L;
    private static final long CH_DN   = 0xE0E008F7L;
    private static final long MUTE    = 0xE0E0F00FL;
    private static final long MENU    = 0xE0E058A7L;
    private static final long GUIDE   = 0xE0E0F20DL;
    private static final long TOOLS   = 0xE0E0D22DL;
    private static final long INFO    = 0xE0E0F807L;
    private static final long UP      = 0xE0E006F9L;
    private static final long DOWN    = 0xE0E08679L;
    private static final long LEFT    = 0xE0E0A659L;
    private static final long RIGHT   = 0xE0E046B9L;
    private static final long OK      = 0xE0E016E9L;
    private static final long RETURN  = 0xE0E01AE5L;
    private static final long EXIT    = 0xE0E0B44BL;
    private static final long RED     = 0xE0E036C9L;
    private static final long GREEN   = 0xE0E028D7L;
    private static final long YELLOW  = 0xE0E0A857L;
    private static final long BLUE    = 0xE0E06897L;
    private static final long STOP    = 0xE0E0629DL;
    private static final long PREV    = 0xE0E0A25DL;
    private static final long PLAY    = 0xE0E0E21DL;
    private static final long PAUSE   = 0xE0E052ADL;
    private static final long NEXT    = 0xE0E012EDL;

    private static final long[] DIGITS = {
            0xE0E08877L, // 0
            0xE0E020DFL, // 1
            0xE0E0A05FL, // 2
            0xE0E0609FL, // 3
            0xE0E010EFL, // 4
            0xE0E0906FL, // 5
            0xE0E050AFL, // 6
            0xE0E030CFL, // 7
            0xE0E0B04FL, // 8
            0xE0E0708FL  // 9
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ConsumerIrManager irManager;
    private TextView statusView;

    private final int bg = Color.rgb(15, 23, 42);
    private final int panel = Color.rgb(30, 41, 59);
    private final int text = Color.rgb(248, 250, 252);
    private final int subText = Color.rgb(148, 163, 184);
    private final int good = Color.rgb(34, 197, 94);
    private final int bad = Color.rgb(239, 68, 68);
    private final int accent = Color.rgb(37, 99, 235);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        irManager = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        final int baseLeft = dp(16);
        final int baseTop = dp(18);
        final int baseRight = dp(16);
        final int baseBottom = dp(28);
        root.setPadding(baseLeft, baseTop, baseRight, baseBottom);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(
                    baseLeft + insets.getSystemWindowInsetLeft(),
                    baseTop + insets.getSystemWindowInsetTop(),
                    baseRight + insets.getSystemWindowInsetRight(),
                    baseBottom + insets.getSystemWindowInsetBottom());
            return insets;
        });

        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Samsung TV IR Remote");
        title.setTextColor(text);
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dp(4));
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Samsung AA59 / ältere TV-Modelle • 38 kHz");
        subtitle.setTextColor(subText);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitle.setPadding(0, 0, 0, dp(12));
        root.addView(subtitle, matchWrap());

        statusView = new TextView(this);
        statusView.setTextSize(14);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.addView(statusView, matchWrap());
        refreshIrStatus();

        addSpacer(root, 14);
        addRow(root,
                spec("⏻  Power", POWER, Color.rgb(153, 27, 27), false),
                spec("Source", SOURCE, accent, false));

        addRow(root,
                spec("Mute", MUTE, panel, false),
                spec("Menu", MENU, panel, false),
                spec("Info", INFO, panel, false));

        addRow(root,
                spec("Guide", GUIDE, panel, false),
                spec("Tools", TOOLS, panel, false),
                spec("Exit", EXIT, panel, false));

        addSection(root, "Lautstärke / Programme");
        addRow(root,
                spec("VOL +", VOL_UP, panel, true),
                spec("CH +", CH_UP, panel, true));
        addRow(root,
                spec("VOL −", VOL_DN, panel, true),
                spec("CH −", CH_DN, panel, true));

        addSection(root, "Navigation");
        addRow(root,
                spec("", -1, bg, false),
                spec("▲", UP, panel, true),
                spec("", -1, bg, false));
        addRow(root,
                spec("◀", LEFT, panel, true),
                spec("OK", OK, accent, false),
                spec("▶", RIGHT, panel, true));
        addRow(root,
                spec("Return", RETURN, panel, false),
                spec("▼", DOWN, panel, true),
                spec("Exit", EXIT, panel, false));

        addSection(root, "Ziffern");
        for (int r = 0; r < 3; r++) {
            int a = r * 3 + 1;
            addRow(root,
                    spec(String.valueOf(a), DIGITS[a], panel, false),
                    spec(String.valueOf(a + 1), DIGITS[a + 1], panel, false),
                    spec(String.valueOf(a + 2), DIGITS[a + 2], panel, false));
        }
        addRow(root,
                spec("", -1, bg, false),
                spec("0", DIGITS[0], panel, false),
                spec("", -1, bg, false));

        addSection(root, "Farbtasten");
        addRow(root,
                spec("Rot", RED, Color.rgb(185, 28, 28), false),
                spec("Grün", GREEN, Color.rgb(21, 128, 61), false),
                spec("Gelb", YELLOW, Color.rgb(202, 138, 4), false),
                spec("Blau", BLUE, Color.rgb(29, 78, 216), false));

        addSection(root, "Medien");
        addRow(root,
                spec("⏮", PREV, panel, false),
                spec("▶", PLAY, panel, false),
                spec("⏸", PAUSE, panel, false),
                spec("⏭", NEXT, panel, false));
        addRow(root,
                spec("■ Stop", STOP, panel, false));

        TextView hint = new TextView(this);
        hint.setText("Tipp: Oberkante des OnePlus 13R auf den IR-Empfänger des Fernsehers richten. VOL/CH und Pfeiltasten können gehalten werden.");
        hint.setTextColor(subText);
        hint.setTextSize(13);
        hint.setPadding(0, dp(18), 0, 0);
        root.addView(hint, matchWrap());

        setContentView(scroll);
    }

    private void refreshIrStatus() {
        boolean hasEmitter = irManager != null && irManager.hasIrEmitter();
        if (!hasEmitter) {
            statusView.setText("⚠ Android meldet keinen IR-Sender");
            statusView.setTextColor(Color.WHITE);
            statusView.setBackground(makeRounded(bad, 16));
            return;
        }

        boolean supports38 = false;
        boolean rangesKnown = false;
        ConsumerIrManager.CarrierFrequencyRange[] ranges = irManager.getCarrierFrequencies();
        if (ranges != null) {
            rangesKnown = true;
            for (ConsumerIrManager.CarrierFrequencyRange range : ranges) {
                if (range.getMinFrequency() <= CARRIER_HZ && range.getMaxFrequency() >= CARRIER_HZ) {
                    supports38 = true;
                    break;
                }
            }
        }

        String suffix = !rangesKnown ? " • Frequenzbereich nicht gemeldet"
                : (supports38 ? " • 38 kHz verfügbar" : " • 38 kHz nicht im gemeldeten Bereich");
        statusView.setText("✓ IR-Sender erkannt" + suffix);
        statusView.setTextColor(Color.WHITE);
        statusView.setBackground(makeRounded(good, 16));
    }

    private void addSection(LinearLayout root, String label) {
        TextView section = new TextView(this);
        section.setText(label);
        section.setTextColor(subText);
        section.setTextSize(13);
        section.setPadding(0, dp(18), 0, dp(6));
        root.addView(section, matchWrap());
    }

    private ButtonSpec spec(String label, long code, int color, boolean repeat) {
        return new ButtonSpec(label, code, color, repeat);
    }

    private void addRow(LinearLayout root, ButtonSpec... specs) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        root.addView(row, matchWrap());

        for (ButtonSpec s : specs) {
            if (s.code < 0) {
                View spacer = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1f);
                lp.setMargins(dp(4), dp(4), dp(4), dp(4));
                row.addView(spacer, lp);
                continue;
            }

            Button b = new Button(this);
            b.setText(s.label);
            b.setTextColor(text);
            b.setTextSize(15);
            b.setAllCaps(false);
            b.setGravity(Gravity.CENTER);
            b.setPadding(dp(6), 0, dp(6), 0);
            b.setBackground(makeSelector(s.color));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            row.addView(b, lp);

            if (s.repeat) {
                bindRepeating(b, s.label, s.code);
            } else {
                b.setOnClickListener(v -> sendCommand(s.label, s.code, v));
            }
        }
    }

    private void bindRepeating(Button button, String label, long code) {
        final Runnable[] repeater = new Runnable[1];
        repeater[0] = new Runnable() {
            @Override
            public void run() {
                sendCommand(label, code, button);
                handler.postDelayed(this, 140);
            }
        };

        button.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand(label, code, v);
                    handler.postDelayed(repeater[0], 430);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(repeater[0]);
                    v.performClick();
                    return true;
                default:
                    return true;
            }
        });
    }

    private void sendCommand(String label, long code, View source) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(this, "Android stellt keinen IR-Sender bereit.", Toast.LENGTH_SHORT).show();
            refreshIrStatus();
            return;
        }

        try {
            int[] pattern = buildSamsungPattern(code);
            irManager.transmit(CARRIER_HZ, pattern);
            source.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            statusView.setText(String.format(Locale.ROOT,
                    "Gesendet: %s  •  0x%08X", label, code));
            statusView.setTextColor(Color.WHITE);
            statusView.setBackground(makeRounded(good, 16));
        } catch (RuntimeException ex) {
            statusView.setText("IR-Fehler: " + ex.getClass().getSimpleName());
            statusView.setTextColor(Color.WHITE);
            statusView.setBackground(makeRounded(bad, 16));
            Toast.makeText(this, "IR-Senden fehlgeschlagen: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Samsung32 raw-wire representation.
     * Codes such as E0E040BF are already bit-reversed per byte relative to
     * Samsung's logical address/command bytes. Emitting this 32-bit value
     * MSB-first therefore produces Samsung32's LSB-first wire order.
     */
    private int[] buildSamsungPattern(long code) {
        int[] pattern = new int[67];
        int p = 0;
        pattern[p++] = 4500;
        pattern[p++] = 4500;

        for (int bit = 31; bit >= 0; bit--) {
            pattern[p++] = 560;
            boolean one = ((code >>> bit) & 1L) != 0;
            pattern[p++] = one ? 1690 : 560;
        }
        pattern[p] = 560;
        return pattern;
    }

    private StateListDrawable makeSelector(int normalColor) {
        int pressed = blend(normalColor, Color.WHITE, 0.12f);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, makeRounded(pressed, 14));
        states.addState(new int[]{}, makeRounded(normalColor, 14));
        return states;
    }

    private GradientDrawable makeRounded(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private int blend(int c1, int c2, float ratio) {
        float inv = 1f - ratio;
        int r = Math.round(Color.red(c1) * inv + Color.red(c2) * ratio);
        int g = Math.round(Color.green(c1) * inv + Color.green(c2) * ratio);
        int b = Math.round(Color.blue(c1) * inv + Color.blue(c2) * ratio);
        return Color.rgb(r, g, b);
    }

    private void addSpacer(LinearLayout root, int heightDp) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static class ButtonSpec {
        final String label;
        final long code;
        final int color;
        final boolean repeat;

        ButtonSpec(String label, long code, int color, boolean repeat) {
            this.label = label;
            this.code = code;
            this.color = color;
            this.repeat = repeat;
        }
    }
}
