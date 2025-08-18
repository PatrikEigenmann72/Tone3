// --------------------------------------------------------------------------------------
// AudioPlayer.java - Realtime stereo audio engine for Tone3. Streams 16-bit PCM audio
// directly to the system’s output line using Java’s javax.sound API. AudioPlayer is
// generator-agnostic. It accepts one or two AGenerator instances and streams the output
// in a lock-free loop inside a high-priority thread. This design lets Tone3 alternate
// between mono and stereo test modes without touching playback internals.
//
// The main loop pulls fresh samples from the configured generator(s), encodes them into
// a little-endian byte buffer, and writes them in chunks to the SourceDataLine. Threading
// and timing are simple by intent—no clocks, no scheduling, just sample-pull and
// stream-write. The tone's fidelity and responsiveness are delegated to the generators
// themselves.
// --------------------------------------------------------------------------------------
// Author:  Patrik Eigemann  
// eMail:   p.eigenmann@gmx.net  
// GitHub:  www.github.com/PatrikEigemann/Tone3  
// --------------------------------------------------------------------------------------
// Change Log:
// 2025-06-29 Sun File created and engine loop implemented.                Version 00.01
// --------------------------------------------------------------------------------------

/* Package tone3.audio */
package tone3.audio;

/* Java Sound Sampled imports */
import javax.sound.sampled.*;

/**
 * AudioPlayer.java - Realtime stereo audio engine for Tone3. Streams 16-bit PCM audio
 * directly to the system’s output line using Java’s javax.sound API. AudioPlayer is
 * generator-agnostic. It accepts one or two AGenerator instances and streams the output
 * in a lock-free loop inside a high-priority thread. This design lets Tone3 alternate
 * between mono and stereo test modes without touching playback internals.
 *
 * The main loop pulls fresh samples from the configured generator(s), encodes them into
 * a little-endian byte buffer, and writes them in chunks to the SourceDataLine. Threading
 * and timing are simple by intent—no clocks, no scheduling, just sample-pull and
 * stream-write. The tone's fidelity and responsiveness are delegated to the generators
 * themselves. 
 */
public class AudioPlayer {

    /* 
     * Output audio line for low-latency PCM streaming.
     * Represents a stereo-capable device line opened with 16-bit, 44.1 kHz format.
     * Managed entirely within the start/stop lifecycle of the player.
     */
    private SourceDataLine line;

    /* 
     * Dedicated audio thread responsible for pulling samples and writing to the line.
     * Spawned once on start(), runs at MAX_PRIORITY for uninterrupted streaming.
     */
    private Thread thread;

    /* 
     * Flag indicating whether the audio engine is actively streaming.
     * Used to start and gracefully exit the audio thread’s main loop.
     */
    private boolean running = false;

    /* 
     * Signal generator for the left audio channel.
     * Can be independent or linked to the right generator for mono output.
     */
    private AGenerator leftGenerator;

    /* 
     * Signal generator for the right audio channel.
     * Can be set independently or mirror the left for mono playback.
     */
    private AGenerator rightGenerator;

    /* 
     * Playback mode flag.
     * If true, the same generator feeds both left and right channels (mono).
     * If false, left and right generators run independently (stereo).
     */
    private boolean isMono = false;

    /**
     * Configures the player for mono playback using a single generator.
     * The same generator is routed to both left and right channels,
     * which is ideal for phase-locked test tones or symmetrical signals.
     *
     * Internally sets both generator slots and activates mono mode.
     *
     * @param monoGenerator the generator to use for both channels
     */
    public void setGenerator(AGenerator monoGenerator) {
        this.leftGenerator = monoGenerator;
        this.rightGenerator = monoGenerator;
        this.isMono = true;
    }

    /**
     * Configures the player for stereo playback using two independent generators. Each generator
     * will drive its own channel—left and right—allowing for asymmetrical test tones, phase
     * offset experiments, or binaural cues. This method replaces the current signal sources
     * and disables mono mode.
     *
     * @param left  generator for the left audio channel
     * @param right generator for the right audio channel
     */
    public void setGenerators(AGenerator left, AGenerator right) {
        this.leftGenerator = left;
        this.rightGenerator = right;
        this.isMono = false;
    }
    /**
     * Starts the audio output engine. Sets up a 44.1 kHz stereo SourceDataLine, verifies that
     * generators are assigned, and launches a high-priority thread to stream samples in real
     * time. The audio loop dynamically handles mono or stereo output based on current generator
     * configuration. If the engine is already running or no generators have been set, it does
     * nothing. Ensures clean initialization and proper handoff to the audio thread.
     */
    public void start() {

        // Skip startup if already running or generator configuration is incomplete
        if (running || leftGenerator == null || rightGenerator == null) return;
        // Mark the engine as running to prevent re-entrance
        // and to signal the audio thread to start processing.
        running = true;

        // Define stereo audio format:
        // 44.1 kHz sample rate, 16-bit PCM, 2 channels (stereo),
        // signed samples, little-endian byte order
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);

        // Attempt to acquire a SourceDataLine for the specified audio format
        // This line will handle the actual audio output to the system's speakers
        // or headphones. If the system cannot provide a suitable line,
        // the engine will gracefully exit without crashing.
        try {
            // Request a suitable SourceDataLine for this format from the system
            line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));

            // Attempt to open the audio line with our format
            line.open(format);

            // Begin streaming audio immediately
            line.start();

            // Launch the generator-driven audio thread
            thread = new Thread(() -> {

                // Allocate buffer for 1024 stereo frames:
                // 2 bytes (16 bits) × 2 channels × 1024 = 4096 bytes
                byte[] buffer = new byte[4096];

                // Main loop: runs as long as the engine is marked running
                while (running) {

                    // Fill buffer frame by frame (4 bytes = 1 stereo sample)
                    for (int i = 0; i < buffer.length; i += 4) {
                        short leftSample, rightSample;

                        if (isMono) {
                            // For mono: pull a single sample and write it to both channels
                            short s = leftGenerator.nextSample();
                            leftSample = s;
                            rightSample = s;
                        } else {
                            // For stereo: fetch from left and right generators independently
                            leftSample = leftGenerator.nextSample();
                            rightSample = rightGenerator.nextSample();
                        }

                        // Write each channel’s sample into the buffer in little-endian order
                        // Left channel, LSB first
                        buffer[i]     = (byte) (leftSample & 0xFF);
                        buffer[i + 1] = (byte) ((leftSample >> 8) & 0xFF);

                        // Right channel, LSB first
                        buffer[i + 2] = (byte) (rightSample & 0xFF);
                        buffer[i + 3] = (byte) ((rightSample >> 8) & 0xFF);
                    }

                    // Push the filled buffer to the audio line for immediate playback
                    line.write(buffer, 0, buffer.length);
                }

                // Engine has stopped—flush remaining audio and release resources
                // Ensure all queued audio is played
                line.drain();
                // Stop the hardware stream
                line.stop();
                // Close the line and release OS-level resources
                line.close();
            });

            // Give the thread a clear identity for debugging / thread management
            thread.setName("Tone3-AudioEngine");
            // Assign maximum JVM thread priority to reduce risk of audio stuttering
            thread.setPriority(Thread.MAX_PRIORITY);
            // Start the real-time processing thread
            thread.start();

        } catch (LineUnavailableException e) {
            // If no suitable output line could be acquired, report and exit gracefully
            System.err.println("Audio line unavailable: " + e.getMessage());
            running = false;
        }
    }

    /**
     * Stops the audio engine and releases all resources. This method gracefully halts
     * the audio thread, flushes any remaining audio data, and closes the SourceDataLine.
     * It sets the running flag to false, which signals the audio thread to exit its loop.
     * After calling this method, the player cannot be restarted without calling start() again.
     */
    public void stop() {
        // If the engine is not running, there's nothing to stop.
        if (!running) return;
        // Signal the audio thread to stop processing.
        running = false;
    }
}