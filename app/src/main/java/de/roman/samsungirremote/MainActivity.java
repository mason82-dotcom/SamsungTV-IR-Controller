package de.roman.samsungirremote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private enum DeviceMode {
        SAMSUNG,
        PHILIPS_QM163E
    }

    private static final int SAMSUNG_CARRIER_HZ = 38000;
    private static final int PHILIPS_CARRIER_HZ = 36000;
    private static final int RC6_UNIT_US = 444;
    private static final String PREFS = "tv_ir_prefs";
    private static final String PREF_DEVICE = "device";

    // Samsung AA59 / classic Samsung TV 32-bit IR codes.
    private static final long S_POWER   = 0xE0E040BFL;
    private static final long S_SOURCE  = 0xE0E0807FL;
    private static final long S_VOL_UP  = 0xE0E0E01FL;
    private static final long S_VOL_DN  = 0xE0E0D02FL;
    private static final long S_CH_UP   = 0xE0E048B7L;
    private static final long S_CH_DN   = 0xE0E008F7L;
    private static final long S_MUTE    = 0xE0E0F00FL;
    private static final long S_MENU    = 0xE0E058A7L;
    private static final long S_GUIDE   = 0xE0E0F20DL;
    private static final long S_TOOLS   = 0xE0E0D22DL;
    private static final long S_INFO    = 0xE0E0F807L;
    private static final long S_UP      = 0xE0E006F9L;
    private static final long S_DOWN    = 0xE0E08679L;
    private static final long S_LEFT    = 0xE0E0A659L;
    private static final long S_RIGHT   = 0xE0E046B9L;
    private static final long S_OK      = 0xE0E016E9L;
    private static final long S_RETURN  = 0xE0E01AE5L;
    private static final long S_EXIT    = 0xE0E0B44BL;
    private static final long S_RED     = 0xE0E036C9L;
    private static final long S_GREEN   = 0xE0E028D7L;
    private static final long S_YELLOW  = 0xE0E0A857L;
    private static final long S_BLUE    = 0xE0E06897L;
    private static final long S_STOP    = 0xE0E0629DL;
    private static final long S_PREV    = 0xE0E0A25DL;
    private static final long S_PLAY    = 0xE0E0E21DL;
    private static final long S_PAUSE   = 0xE0E052ADL;
    private static final long S_NEXT    = 0xE0E012EDL;

    private static final long[] S_DIGITS = {
            0xE0E08877L, 0xE0E020DFL, 0xE0E0A05FL, 0xE0E0609FL, 0xE0E010EFL,
            0xE0E0906FL, 0xE0E050AFL, 0xE0E030CFL, 0xE0E0B04FL, 0xE0E0708FL
    };

    // Philips RC6 Mode 0, system/address 0. Values are decimal command codes.
    private static final int P_POWER      = 12;
    private static final int P_MUTE       = 13;
    private static final int P_INFO       = 15;
    private static final int P_VOL_UP     = 16;
    private static final int P_VOL_DN     = 17;
    private static final int P_CH_UP      = 32;
    private static final int P_CH_DN      = 33;
    private static final int P_FFWD       = 40;
    private static final int P_REWIND     = 43;
    private static final int P_PLAY       = 44;
    private static final int P_PAUSE      = 48;
    private static final int P_STOP       = 49;
    private static final int P_RECORD     = 55;
    private static final int P_SOURCE     = 56;
    private static final int P_BACK       = 10;
    private static final int P_HOME       = 84;
    private static final int P_UP         = 88;
    private static final int P_DOWN       = 89;
    private static final int P_LEFT       = 90;
    private static final int P_RIGHT      = 91;
    private static final int P_OK         = 92;
    private static final int P_RED        = 109;
    private static final int P_GREEN      = 110;
    private static final int P_YELLOW     = 111;
    private static final int P_BLUE       = 112;
    private static final int P_AMBILIGHT  = 143;
    private static final int P_EXIT       = 159;
    private static final int P_MENU       = 191;
    private static final int P_GUIDE      = 204;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ConsumerIrManager irManager;
    private TextView statusView;
    private TextView subtitleView;
    private LinearLayout remoteContainer;
    private Button samsungButton;
    private Button philipsButton;
    private DeviceMode currentDevice = DeviceMode.SAMSUNG;
    private boolean philipsToggle = false;

    private final int bg = Color.rgb(15, 23, 42);
    private final int panel = Color.rgb(30, 41, 59);
    private final int text = Color.rgb(248, 250, 252);
    private final int subText = Color.rgb(148, 163, 184);
    private final int good = Color.rgb(34, 197, 94);
    private final int bad = Color.rgb(239, 68, 68);
    private final int accent = Color.rgb(37, 99, 235);
    private final int selected = Color.rgb(14, 116, 144);

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
        title.setText("TV IR Controller");
        title.setTextColor(text);
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dp(4));
        root.addView(title, matchWrap());

        subtitleView = new TextView(this);
        subtitleView.setTextColor(subText);
        subtitleView.setTextSize(14);
        subtitleView.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitleView.setPadding(0, 0, 0, dp(12));
        root.addView(subtitleView, matchWrap());

        addDeviceSelector(root);

        statusView = new TextView(this);
        statusView.setTextSize(14);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams statusLp = matchWrap();
        statusLp.setMargins(0, dp(8), 0, 0);
        root.addView(statusView, statusLp);

        remoteContainer = new LinearLayout(this);
        remoteContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(remoteContainer, matchWrap());

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(PREF_DEVICE, DeviceMode.SAMSUNG.name());
        DeviceMode initial;
        try {
            initial = DeviceMode.valueOf(saved);
        } catch (IllegalArgumentException ex) {
            initial = DeviceMode.SAMSUNG;
        }
        selectDevice(initial);

        setContentView(scroll);
    }

    private void addDeviceSelector(LinearLayout root) {
        TextView label = new TextView(this);
        label.setText("Fernseher");
        label.setTextColor(subText);
        label.setTextSize(13);
        label.setPadding(0, dp(4), 0, dp(4));
        root.addView(label, matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        root.addView(row, matchWrap());

        samsungButton = new Button(this);
        samsungButton.setText("Samsung");
        samsungButton.setTextColor(text);
        samsungButton.setAllCaps(false);
        samsungButton.setOnClickListener(v -> selectDevice(DeviceMode.SAMSUNG));

        philipsButton = new Button(this);
        philipsButton.setText("Philips QM16.3E");
        philipsButton.setTextColor(text);
        philipsButton.setAllCaps(false);
        philipsButton.setOnClickListener(v -> selectDevice(DeviceMode.PHILIPS_QM163E));

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, dp(52), 1f);
        lp1.setMargins(dp(4), dp(2), dp(4), dp(2));
        row.addView(samsungButton, lp1);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, dp(52), 1.25f);
        lp2.setMargins(dp(4), dp(2), dp(4), dp(2));
        row.addView(philipsButton, lp2);
    }

    private void selectDevice(DeviceMode mode) {
        handler.removeCallbacksAndMessages(null);
        currentDevice = mode;
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(PREF_DEVICE, mode.name())
                .apply();

        samsungButton.setBackground(makeSelector(mode == DeviceMode.SAMSUNG ? selected : panel));
        philipsButton.setBackground(makeSelector(mode == DeviceMode.PHILIPS_QM163E ? selected : panel));

        if (mode == DeviceMode.SAMSUNG) {
            subtitleView.setText("Samsung AA59 / ältere TV-Modelle • Samsung32 • 38 kHz");
        } else {
            subtitleView.setText("Philips QM16.3E • RC6 Mode 0 • 36 kHz");
        }

        refreshIrStatus();
        buildRemoteControls();
    }

    private void buildRemoteControls() {
        remoteContainer.removeAllViews();
        addSpacer(remoteContainer, 12);

        if (currentDevice == DeviceMode.SAMSUNG) {
            buildSamsungControls();
        } else {
            buildPhilipsControls();
        }

        TextView hint = new TextView(this);
        hint.setText(currentDevice == DeviceMode.SAMSUNG
                ? "Samsung: Oberkante des OnePlus 13R auf den IR-Empfänger richten. VOL/CH und Pfeiltasten können gehalten werden."
                : "Philips QM16.3E: RC6 Mode 0 bei 36 kHz. Oberkante des OnePlus 13R auf den IR-Sensor des Fernsehers richten.");
        hint.setTextColor(subText);
        hint.setTextSize(13);
        hint.setPadding(0, dp(18), 0, 0);
        remoteContainer.addView(hint, matchWrap());
    }

    private void buildSamsungControls() {
        addRow(remoteContainer,
                spec("⏻ Power", S_POWER, Color.rgb(153, 27, 27), false),
                spec("Source", S_SOURCE, accent, false));

        addRow(remoteContainer,
                spec("Mute", S_MUTE, panel, false),
                spec("Menu", S_MENU, panel, false),
                spec("Info", S_INFO, panel, false));

        addRow(remoteContainer,
                spec("Guide", S_GUIDE, panel, false),
                spec("Tools", S_TOOLS, panel, false),
                spec("Exit", S_EXIT, panel, false));

        addVolumeChannel(remoteContainer, S_VOL_UP, S_VOL_DN, S_CH_UP, S_CH_DN);
        addNavigation(remoteContainer, S_UP, S_DOWN, S_LEFT, S_RIGHT, S_OK, S_RETURN, S_EXIT);
        addDigits(remoteContainer, S_DIGITS);

        addSection(remoteContainer, "Farbtasten");
        addRow(remoteContainer,
                spec("Rot", S_RED, Color.rgb(185, 28, 28), false),
                spec("Grün", S_GREEN, Color.rgb(21, 128, 61), false),
                spec("Gelb", S_YELLOW, Color.rgb(202, 138, 4), false),
                spec("Blau", S_BLUE, Color.rgb(29, 78, 216), false));

        addSection(remoteContainer, "Medien");
        addRow(remoteContainer,
                spec("⏮", S_PREV, panel, false),
                spec("▶", S_PLAY, panel, false),
                spec("⏸", S_PAUSE, panel, false),
                spec("⏭", S_NEXT, panel, false));
        addRow(remoteContainer, spec("■ Stop", S_STOP, panel, false));
    }

    private void buildPhilipsControls() {
        addRow(remoteContainer,
                spec("⏻ Power", P_POWER, Color.rgb(153, 27, 27), false),
                spec("Source", P_SOURCE, accent, false));

        addRow(remoteContainer,
                spec("Mute", P_MUTE, panel, false),
                spec("Menu", P_MENU, panel, false),
                spec("Info", P_INFO, panel, false));

        addRow(remoteContainer,
                spec("Guide", P_GUIDE, panel, false),
                spec("Home", P_HOME, accent, false),
                spec("Exit", P_EXIT, panel, false));

        addRow(remoteContainer,
                spec("Ambilight", P_AMBILIGHT, panel, false));

        addVolumeChannel(remoteContainer, P_VOL_UP, P_VOL_DN, P_CH_UP, P_CH_DN);
        addNavigation(remoteContainer, P_UP, P_DOWN, P_LEFT, P_RIGHT, P_OK, P_BACK, P_EXIT);

        long[] digits = new long[10];
        for (int i = 0; i <= 9; i++) {
            digits[i] = i;
        }
        addDigits(remoteContainer, digits);

        addSection(remoteContainer, "Farbtasten");
        addRow(remoteContainer,
                spec("Rot", P_RED, Color.rgb(185, 28, 28), false),
                spec("Grün", P_GREEN, Color.rgb(21, 128, 61), false),
                spec("Gelb", P_YELLOW, Color.rgb(202, 138, 4), false),
                spec("Blau", P_BLUE, Color.rgb(29, 78, 216), false));

        addSection(remoteContainer, "Medien");
        addRow(remoteContainer,
                spec("⏪", P_REWIND, panel, true),
                spec("▶", P_PLAY, panel, false),
                spec("⏸", P_PAUSE, panel, false),
                spec("⏩", P_FFWD, panel, true));
        addRow(remoteContainer,
                spec("■ Stop", P_STOP, panel, false),
                spec("● Rec", P_RECORD, panel, false));
    }

    private void addVolumeChannel(LinearLayout root, long volUp, long volDn, long chUp, long chDn) {
        addSection(root, "Lautstärke / Programme");
        addRow(root,
                spec("VOL +", volUp, panel, true),
                spec("CH +", chUp, panel, true));
        addRow(root,
                spec("VOL −", volDn, panel, true),
                spec("CH −", chDn, panel, true));
    }

    private void addNavigation(
            LinearLayout root,
            long up,
            long down,
            long left,
            long right,
            long ok,
            long back,
            long exit) {

        addSection(root, "Navigation");
        addRow(root,
                spec("", -1, bg, false),
                spec("▲", up, panel, true),
                spec("", -1, bg, false));
        addRow(root,
                spec("◀", left, panel, true),
                spec("OK", ok, accent, false),
                spec("▶", right, panel, true));
        addRow(root,
                spec("Zurück", back, panel, false),
                spec("▼", down, panel, true),
                spec("Exit", exit, panel, false));
    }

    private void addDigits(LinearLayout root, long[] digits) {
        addSection(root, "Ziffern");
        for (int r = 0; r < 3; r++) {
            int a = r * 3 + 1;
            addRow(root,
                    spec(String.valueOf(a), digits[a], panel, false),
                    spec(String.valueOf(a + 1), digits[a + 1], panel, false),
                    spec(String.valueOf(a + 2), digits[a + 2], panel, false));
        }
        addRow(root,
                spec("", -1, bg, false),
                spec("0", digits[0], panel, false),
                spec("", -1, bg, false));
    }

    private void refreshIrStatus() {
        if (statusView == null) {
            return;
        }

        boolean hasEmitter = irManager != null && irManager.hasIrEmitter();
        if (!hasEmitter) {
            statusView.setText("⚠ Android meldet keinen IR-Sender");
            statusView.setTextColor(Color.WHITE);
            statusView.setBackground(makeRounded(bad, 16));
            return;
        }

        int carrier = currentCarrier();
        boolean supportsCarrier = false;
        boolean rangesKnown = false;
        ConsumerIrManager.CarrierFrequencyRange[] ranges = irManager.getCarrierFrequencies();
        if (ranges != null) {
            rangesKnown = true;
            for (ConsumerIrManager.CarrierFrequencyRange range : ranges) {
                if (range.getMinFrequency() <= carrier && range.getMaxFrequency() >= carrier) {
                    supportsCarrier = true;
                    break;
                }
            }
        }

        int khz = carrier / 1000;
        String suffix = !rangesKnown
                ? " • Frequenzbereich nicht gemeldet"
                : (supportsCarrier
                ? " • " + khz + " kHz verfügbar"
                : " • " + khz + " kHz nicht im gemeldeten Bereich");

        statusView.setText("✓ IR-Sender erkannt" + suffix);
        statusView.setTextColor(Color.WHITE);
        statusView.setBackground(makeRounded(good, 16));
    }

    private int currentCarrier() {
        return currentDevice == DeviceMode.PHILIPS_QM163E
                ? PHILIPS_CARRIER_HZ
                : SAMSUNG_CARRIER_HZ;
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
                b.setOnClickListener(v -> sendCommand(s.label, s.code, v, true));
            }
        }
    }

    private void bindRepeating(Button button, String label, long code) {
        final Runnable[] repeater = new Runnable[1];
        repeater[0] = new Runnable() {
            @Override
            public void run() {
                sendCommand(label, code, button, false);
                handler.postDelayed(this, currentDevice == DeviceMode.PHILIPS_QM163E ? 110 : 140);
            }
        };

        button.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand(label, code, v, true);
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

    private void sendCommand(String label, long code, View source, boolean newPress) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(this, "Android stellt keinen IR-Sender bereit.", Toast.LENGTH_SHORT).show();
            refreshIrStatus();
            return;
        }

        try {
            int carrier;
            int[] pattern;

            if (currentDevice == DeviceMode.PHILIPS_QM163E) {
                carrier = PHILIPS_CARRIER_HZ;
                if (newPress) {
                    philipsToggle = !philipsToggle;
                }
                pattern = buildPhilipsRc6Pattern(0, (int) code, philipsToggle);
            } else {
                carrier = SAMSUNG_CARRIER_HZ;
                pattern = buildSamsungPattern(code);
            }

            irManager.transmit(carrier, pattern);
            source.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            String detail = currentDevice == DeviceMode.PHILIPS_QM163E
                    ? String.format(Locale.ROOT, "RC6 0.%03d", code)
                    : String.format(Locale.ROOT, "0x%08X", code);

            statusView.setText("Gesendet: " + label + " • " + detail);
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

    /**
     * Philips RC6 Mode 0:
     * header 6T mark + 2T space, start bit (1), 3 mode bits (000),
     * double-width toggle bit, 8-bit address and 8-bit command, MSB first.
     * Logical 1 = mark->space, logical 0 = space->mark.
     */
    private int[] buildPhilipsRc6Pattern(int address, int command, boolean toggle) {
        List<Integer> durations = new ArrayList<>();

        appendIrSegment(durations, true, 6 * RC6_UNIT_US);
        appendIrSegment(durations, false, 2 * RC6_UNIT_US);

        // Leading/start bit = 1.
        appendIrSegment(durations, true, RC6_UNIT_US);
        appendIrSegment(durations, false, RC6_UNIT_US);

        int raw = ((toggle ? 1 : 0) << 16)
                | ((address & 0xFF) << 8)
                | (command & 0xFF);

        // 20 bits after the leading bit: mode[2:0], toggle, address[7:0], command[7:0].
        for (int bit = 19; bit >= 0; bit--) {
            boolean one = ((raw >>> bit) & 1) != 0;
            boolean isToggleBit = bit == 16;
            int half = isToggleBit ? 2 * RC6_UNIT_US : RC6_UNIT_US;

            if (one) {
                appendIrSegment(durations, true, half);
                appendIrSegment(durations, false, half);
            } else {
                appendIrSegment(durations, false, half);
                appendIrSegment(durations, true, half);
            }
        }

        int[] result = new int[durations.size()];
        for (int i = 0; i < durations.size(); i++) {
            result[i] = durations.get(i);
        }
        return result;
    }

    /**
     * ConsumerIrManager expects alternating MARK/SPACE durations starting with MARK.
     * Manchester encoding can produce adjacent equal levels at bit boundaries, so
     * equal neighbouring levels are merged into one duration.
     */
    private void appendIrSegment(List<Integer> durations, boolean mark, int durationUs) {
        boolean nextWouldBeMark = durations.size() % 2 == 0;
        if (durations.isEmpty() || nextWouldBeMark == mark) {
            durations.add(durationUs);
        } else {
            int last = durations.size() - 1;
            durations.set(last, durations.get(last) + durationUs);
        }
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
