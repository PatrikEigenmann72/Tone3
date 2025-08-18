// --------------------------------------------------------------------------------------
// SineWaveGenerator.java - Real-time sine wave sample generator for test tone output. This
// class produces continuous sine tones based on a set frequency and shared sample rate.
// It is stateless apart from phase tracking and is designed for live synthesis inside the 
// Tone3 audio thread without needing pre-buffered lookup tables.
//
// SineWaveGenerator extends AGenerator to remain polymorphically compatible with the audio 
// backend. AudioPlayer doesn’t care what generator is in use—as long as nextSample() returns 
// a valid 16-bit PCM frame. This pattern minimizes interdependence and makes the signal chain 
// extensible for future tone types without modifying playback logic.
//
// Phase accumulation is performed in double-precision to ensure smooth cycling and prevent 
// perceptual drift over long runtimes. When 2π is exceeded, phase wraps around to preserve
// continuity without overflow.
// --------------------------------------------------------------------------------------
// Author:  Patrik Eigemann  
// eMail:   p.eigenmann@gmx.net  
// GitHub:  www.github.com/PatrikEigemann/Tone3  
// --------------------------------------------------------------------------------------
// Change Log:
// 2025-06-29 Sun File created and sine oscillator implemented.             Version 00.01
// --------------------------------------------------------------------------------------

/* Package tone3.audio */
package tone3.audio;

/**
 * SineWaveGenerator.java - Real-time sine wave sample generator for test tone output. This
 * class produces continuous sine tones based on a set frequency and shared sample rate.
 * It is stateless apart from phase tracking and is designed for live synthesis inside the 
 * Tone3 audio thread without needing pre-buffered lookup tables.
 *
 * SineWaveGenerator extends AGenerator to remain polymorphically compatible with the audio 
 * backend. AudioPlayer doesn’t care what generator is in use—as long as nextSample() returns 
 * a valid 16-bit PCM frame. This pattern minimizes interdependence and makes the signal chain 
 * extensible for future tone types without modifying playback logic.
 *
 * Phase accumulation is performed in double-precision to ensure smooth cycling and prevent 
 * perceptual drift over long runtimes. When 2π is exceeded, phase wraps around to preserve
 * continuity without overflow. 
 */
public class SineWaveGenerator extends AGenerator {

    /* 
     * Target frequency for the sine wave (Hz), defaulting to A4.
     * Declared volatile to ensure visibility across threads—
     * the audio thread reads it while the GUI thread may write to it.
     */
    private volatile double frequency = 440.0;

    /* 
     * Accumulated phase angle in radians.
     * Advances every sample based on frequency and sample rate,
     * then wraps around at 2π to maintain continuous waveform.
     */
    private double phase = 0.0;

    /**
     * Sets the playback frequency for this sine generator.
     * Can be called on the fly to change pitch during runtime—
     * the audio thread will pick up the new value immediately 
     * thanks to the `volatile` keyword on the frequency field.
     *
     * @param frequency new target frequency in Hz (e.g. 440.0 for A4)
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /**
     * Generates the next sine wave audio sample.
     * Computes the current waveform value based on phase and frequency,
     * then advances the oscillator's phase for the next call.
     *
     * This is called once per frame by AudioPlayer and returns a 16-bit PCM value.
     * Double-precision math ensures smooth and stable oscillation across frequencies.
     *
     * @return next sine wave sample in the range [-32768, 32767]
     */
    @Override
    public short nextSample() {
        // Evaluate sine at current phase angle and scale to full 16-bit range
        double sample = Math.sin(phase) * Short.MAX_VALUE;

        // Advance the phase based on frequency and sample rate
        // (Δphase = 2π * f / Fs)
        phase += 2 * Math.PI * frequency / sampleRate;

        // Wrap phase to stay within [0, 2π) range and avoid runaway drift
        if (phase >= 2 * Math.PI) {
            phase -= 2 * Math.PI;
        }

        // Return quantized 16-bit sample
        return (short) sample;
    }
}