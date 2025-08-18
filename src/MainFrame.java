// --------------------------------------------------------------------------------------
// MainFrame.java - Provides the root window and primary GUI container for the application.
// It manages layout, event routing, and user interaction across core components and is
// intended to be reused across projects.
//
// By encapsulating the UI logic in this class, the application retains a clear separation
// of concerns between interface and backend logic. This promotes modularity and allows
// the UI to remain decoupled from specific functionality, making it suitable for Swing
// applications or future frameworks.
// --------------------------------------------------------------------------------------
// Author:  Patrik Eigemann
// eMail:   p.eigenmann@gmx.net
// GitHub:  www.github.com/PatrikEigemann
// --------------------------------------------------------------------------------------
// Change Log:
// Sun 2025-06-29 File created & key components implemented.                Version 00.01  
// Sun 2025-06-29 Window size and layout update for clarity.                Version 00.02  
// Sun 2025-06-29 Slider responds to arrow keys (←, →).                     Version 00.03  
// Sun 2025-06-29 Holding Shift accelerates slider movement.                Version 00.04  
// Sun 2025-06-29 Radio buttons made passive (disabled keyboard nav).       Version 00.05  
// Sun 2025-06-29 Sine wave audio output implemented.                       Version 00.06  
// Mon 2025-06-30 Pink noise generator integrated.                          Version 00.07  
// Mon 2025-06-30 Bugfix: Split mode now updates sine frequency live.       Version 00.08  
// Mon 2025-06-30 Mode switching activates generators immediately.          Version 00.09  
// Mon 2025-06-30 Slider regains focus after toggling playback.             Version 00.10  
// Mon 2025-06-30 Added header comments to the file.                        Version 00.11
// --------------------------------------------------------------------------------------
/* Package Tone3.Gui */
//package Tone3.Gui;

/* All imports alphabetical sorted. */
import java.awt.*;
import java.awt.event.*;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import tone3.audio.*;

/**
 * MainFrame.java - Provides the root window and primary GUI container for the application.
 * It manages layout, event routing, and user interaction across core components and is
 * intended to be reused across projects.
 *
 * By encapsulating the UI logic in this class, the application retains a clear separation
 * of concerns between interface and backend logic. This promotes modularity and allows
 * the UI to remain decoupled from specific functionality, making it suitable for Swing
 * applications or future frameworks.
 */
public class MainFrame extends JFrame {
    
/** The frequency slider. Move it left or right to sweep pitch in real time. */
private final JSlider frequencySlider;

/** Displays the frequency in Hz. Gives visual feedback while you tune. */
private final JLabel frequencyDisplay;

/** Mode 1: Plain sine tone — left, right, both. Good old calibration. */
private final JRadioButton sineButton;

/** Mode 2: Pink noise. Equal energy per octave. Ear-safe, analyzer-approved. */
private final JRadioButton pinkButton;

/** Mode 3: Split stereo — Sine on the left, Pink on the right. Field tech’s favorite. */
private final JRadioButton splitButton;

/** Big button. Says Run or Stop. Launches or kills the signal path. */
private final JButton toggleButton;

/** Handles the actual audio thread. Sends samples to speakers. Runs like hell in its own loop. */
private final AudioPlayer audioPlayer = new AudioPlayer();

/** Track whether we are actively generating sound. Multithreading guardrail. */
private boolean isRunning = false;

/** If we’re in Sine or Split mode, this holds the sine generator for real-time updates. */
private SineWaveGenerator activeSineGen = null;

    /**
     * Constructor for the main window. Sets up the user interface: the frequency slider,
     * buttons, and control logic. Wires up the interactions, lays out the components,
     * and initializes defaults. This is where everything visible (and some invisible)
     * gets wired together.
     */
    public MainFrame() {
        /* 
         * Initialize the main application window. Title is fixed and layout is locked
         * for minimal footprint. 
         */
        super("Tone3 – Audio Test Tool");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 180);
        setResizable(false);
        setLayout(new BorderLayout());

        /* 
        * Create the frequency display label at the top of the window.
        * Shows current frequency in Hz, visually updated in real time.
        */
        frequencyDisplay = new JLabel("440 Hz", SwingConstants.CENTER);
        frequencyDisplay.setOpaque(true);
        frequencyDisplay.setBackground(Color.YELLOW);
        frequencyDisplay.setFont(new Font("SansSerif", Font.BOLD, 16));
        frequencyDisplay.setPreferredSize(new Dimension(400, 40));
        add(frequencyDisplay, BorderLayout.NORTH);

        /* 
         * Set up the frequency slider for audio frequency control.
         * Ranges from 20 Hz to 20,000 Hz—covering full hearing range.
         */
        frequencySlider = new JSlider(20, 20000, 440);
        frequencySlider.setPaintTicks(false);
        frequencySlider.setPaintLabels(true);
        frequencySlider.setFocusable(true);

        /* 
         * Add minimal tick marks to the slider: 20 Hz and 20 kHz.
         * Keeps UI clean while giving reference points for ears.
         */
        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(20, new JLabel("20"));
        labelTable.put(20000, new JLabel("20k"));
        frequencySlider.setLabelTable(labelTable);

        /* 
         * Update frequency display and sine generator when the slider is moved.
         * Live frequency control during playback. 
         */
        frequencySlider.addChangeListener(e -> {
            /* 
             * Grab current slider value and update the on-screen frequency label.
             * If playback is active and a sine generator exists, update its frequency live.
             */
            int freq = frequencySlider.getValue();
            frequencyDisplay.setText(freq + " Hz");

            /* 
             * Read the slider, update the label, and—if we’re live—push the freq to the sine generator.
             */
            if (isRunning && activeSineGen != null) {
                activeSineGen.setFrequency(freq);
            }
        });

        /* 
         * Allow arrow keys to move the slider.
         * Holding Shift speeds up navigation for coarse adjustments.
         */
        frequencySlider.addKeyListener(new KeyAdapter() {
            @Override  /* We're overriding the keyPressed method from KeyAdapter */
            public void keyPressed(KeyEvent e) {
                /* I personally find it smoother the shift on 250 and regular on 5 */
                int step = e.isShiftDown() ? 250 : 2;  /* Shift = faster step */
                int val = frequencySlider.getValue();   /* Current slider value */

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    /* Decrease frequency, but don't go below 20 Hz */
                    frequencySlider.setValue(Math.max(20, val - step));
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    /* Increase frequency, but cap at 20,000 Hz */
                    frequencySlider.setValue(Math.min(20000, val + step));
                }
            }
        });
        add(frequencySlider, BorderLayout.CENTER);

        /* 
         * Create the mode toggle buttons: Sine, Pink, and Split.
         * Default is Sine. Each mode sets up a different audio path.
         */
        sineButton = new JRadioButton("Sine");
        pinkButton = new JRadioButton("Pink");
        splitButton = new JRadioButton("Sine L / Pink R");
        sineButton.setSelected(true);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(sineButton);
        modeGroup.add(pinkButton);
        modeGroup.add(splitButton);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(sineButton);
        controlPanel.add(pinkButton);
        controlPanel.add(splitButton);

        sineButton.setFocusable(false);
        pinkButton.setFocusable(false);
        splitButton.setFocusable(false);

        /* 
         * Create the toggle button to start/stop audio playback.
         * Switches text between Run and Stop.
         */
        toggleButton = new JButton("Run");

        /* 
         * Listener for mode button clicks.
         * Applies new generator configuration live during playback.
         */
        toggleButton.addActionListener(e -> {
            /* Flip the playback state */
            isRunning = !isRunning;

            /* Update button label accordingly */
            toggleButton.setText(isRunning ? "Stop" : "Run");

            if (isRunning) {
                /* Determine the selected mode and configure audio path */
                if (sineButton.isSelected()) {
                    /* Mono sine tone */
                    activeSineGen = new SineWaveGenerator();
                    activeSineGen.setFrequency(getFrequency());
                    audioPlayer.setGenerator(activeSineGen);
                } else if (pinkButton.isSelected()) {
                    /* Mono pink noise — frequency slider is irrelevant */
                    activeSineGen = null;
                    audioPlayer.setGenerator(new PinkNoiseGenerator());
                } else if (splitButton.isSelected()) {
                    /* Stereo mode: sine on left, pink on right */
                    activeSineGen = new SineWaveGenerator();
                    activeSineGen.setFrequency(getFrequency());
                    PinkNoiseGenerator pink = new PinkNoiseGenerator();
                    audioPlayer.setGenerators(activeSineGen, pink);
                }

                /* Start audio playback thread */
                audioPlayer.start();

                /* Return keyboard focus to slider for arrow key control */
                SwingUtilities.invokeLater(() -> frequencySlider.requestFocusInWindow());
            } else {
                /* Stop playback and clear sine generator reference */
                audioPlayer.stop();
                activeSineGen = null;
            }
        });
        controlPanel.add(toggleButton);
        add(controlPanel, BorderLayout.SOUTH);

        /* 
         * Listener for mode selection changes.
         * When the user clicks a radio button (Sine, Pink, or Split),
         * this updates the enabled state of the frequency slider
         * and applies the new generator configuration immediately.
         */
        ActionListener modeListener = e -> {
            updateSliderEnabledState();
            applyAudioMode(); // <-- switch tones live!
        };

        sineButton.addActionListener(modeListener);
        pinkButton.addActionListener(modeListener);
        splitButton.addActionListener(modeListener);

        updateSliderEnabledState();
        setVisible(true);
        SwingUtilities.invokeLater(() -> frequencySlider.requestFocusInWindow());
    }

    /**
     * Enables or disables the frequency slider based on the selected mode.
     * The slider is only active when in Sine or Split mode—not in Pink mode,
     * since pink noise ignores frequency. Focus is returned to the slider if re-enabled.
     */
    private void updateSliderEnabledState() {
        boolean enabled = !pinkButton.isSelected();
        frequencySlider.setEnabled(enabled);
        if (enabled) {
            SwingUtilities.invokeLater(() -> frequencySlider.requestFocusInWindow());
        }
    }

    /**
     * Returns the current frequency value from the slider in Hz.
     * Used whenever we need to retrieve the user-selected frequency,
     * typically during initialization or live updates.
     *
     * @return current frequency in Hz
     */
    public int getFrequency() {
        return frequencySlider.getValue();
    }

    /**
     * Returns true if Sine mode is currently active.
     * Used to route signal generation accordingly.
     *
     * @return true if Sine is selected; false otherwise
     */
    public boolean isSineSelected() {
        return sineButton.isSelected();
    }

    /**
     * Returns true if Pink noise mode is active.
     * Pink disables the frequency slider since it's not frequency-dependent.
     *
     * @return true if Pink mode is selected; false otherwise
     */
    public boolean isPinkSelected() {
        return pinkButton.isSelected();
    }

    /**
     * Returns true if Split mode is selected.
     * In Split, Sine plays on the left channel, Pink on the right.
     *
     * @return true if Split mode is active; false otherwise
     */
    public boolean isSplitSelected() {
        return splitButton.isSelected();
    }

    /**
     * Adds an external listener to the frequency slider.
     * Useful for components outside this class that need to react
     * when the user changes the frequency—e.g., analytics or logging tools.
     *
     * @param listener the ChangeListener to notify on frequency updates
     */
    public void addFrequencyChangeListener(ChangeListener listener) {
        frequencySlider.addChangeListener(listener);
    }

    /**
     * Applies the currently selected audio mode to the AudioPlayer.
     * Called whenever mode is changed during playback.
     * 
     * If running, this method sets up the appropriate generator(s)
     * depending on the mode:
     * - Sine → Single sine tone, updated live via slider
     * - Pink → Mono pink noise, frequency-agnostic
     * - Split → Stereo: sine (left) + pink (right)
     *
     * No effect if playback is not active.
     */
    private void applyAudioMode() {
        if (!isRunning) return;

        if (isSineSelected()) {
            activeSineGen = new SineWaveGenerator();
            activeSineGen.setFrequency(getFrequency());
            audioPlayer.setGenerator(activeSineGen);
        } else if (isPinkSelected()) {
            activeSineGen = null;
            audioPlayer.setGenerator(new PinkNoiseGenerator());
        } else if (isSplitSelected()) {
            activeSineGen = new SineWaveGenerator();
            activeSineGen.setFrequency(getFrequency());
            PinkNoiseGenerator pink = new PinkNoiseGenerator();
            audioPlayer.setGenerators(activeSineGen, pink);
        }
    }
}