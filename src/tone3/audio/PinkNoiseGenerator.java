// --------------------------------------------------------------------------------------
// PinkNoiseGenerator.java - Sample-accurate pink noise generator using filtered white
// noise. This class implements a computationally efficient approximation of true 1/f
// noise, adapted from Paul Kellet’s method. Unlike white noise, which distributes equal
// energy per hertz, pink noise rolls off -3 dB per octave—aligning with human perception.
//
// This class is part of the polymorphic generator system driven by AGenerator.java.
// AudioPlayer doesn’t care what kind of sound it's playing—as long as nextSample() 
// returns valid PCM shorts, it’s happy. That design keeps signal logic isolated and
// extendable, with zero coupling to the playback engine.
//
// PinkNoiseGenerator is entirely self-contained and non-blocking. It’s optimized for
// real-time sample synthesis with no pre-buffering or lookup tables—just one lightweight
// method call per frame inside the audio thread.
// --------------------------------------------------------------------------------------
// Author:  Patrik Eigemann
// eMail:   p.eigenmann@gmx.net
// GitHub:  www.github.com/PatrikEigemann/Tone3
// --------------------------------------------------------------------------------------
// Change Log:
// 2025-06-29 Sun File created and implemented pink noise logic.            Version 00.01
// --------------------------------------------------------------------------------------
/* Package tone3.audio */
package tone3.audio;

/* Standard Java imports */
import java.util.Random;

/**
 * PinkNoiseGenerator.java - Sample-accurate pink noise generator using filtered white
 * noise. This class implements a computationally efficient approximation of true 1/f
 * noise, adapted from Paul Kellet’s method. Unlike white noise, which distributes equal
 * energy per hertz, pink noise rolls off -3 dB per octave—aligning with human perception.
 *
 * This class is part of the polymorphic generator system driven by AGenerator.java.
 * AudioPlayer doesn’t care what kind of sound it's playing—as long as nextSample() 
 * returns valid PCM shorts, it’s happy. That design keeps signal logic isolated and
 * extendable, with zero coupling to the playback engine.
 *
 * PinkNoiseGenerator is entirely self-contained and non-blocking. It’s optimized for
 * real-time sample synthesis with no pre-buffering or lookup tables—just one lightweight
 * method call per frame inside the audio thread.
 */
public class PinkNoiseGenerator extends AGenerator {
    
    /* Pseudo-random number generator used to create raw white noise values */
    private final Random random = new Random();

    /* 
     * Internal filter state variables used in the pink noise generation algorithm.
     * Based on Paul Kellet’s refined approximation of 1/f noise (pink noise), 
     * these variables represent recursive filter taps that each model a different 
     * spectral shape. Each tap (b0 to b5) holds a decaying contribution of 
     * previous white noise samples, filtered at different time constants.
     *
     * b6 adds a final bias correction and helps flatten out the overall spectrum.
     * All of them accumulate to approximate a -3 dB/octave rolloff in real time.
     *
     * These values are updated every sample and persist across frames to 
     * maintain spectral continuity, which is essential for authentic pink noise.
     */
    private double b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0;

    /**
     * Generates the next pink noise audio sample. Applies a recursive filter to white
     * noise input, shaping its spectral density to approximate 1/f distribution (pink
     * noise) in real time. This method is called once per frame by AudioPlayer and
     * returns a 16-bit PCM value.
     *
     * @return next pink noise sample in the range [-32768, 32767]
     */
    @Override
    public short nextSample() {

        // Generate a white noise sample in the range [-1.0, 1.0]
        // This serves as the unfiltered, high-entropy input
        double white = random.nextDouble() * 2.0 - 1.0;


        // Pass the white noise sample through six recursive IIR filters (b0–b5).
        // Each filter tap has a different decay coefficient, effectively tuning
        // it to respond to a specific portion of the frequency spectrum.
        // These coefficients were chosen to approximate pink noise (1/f rolloff).
        // The b6 value holds a leftover from the previous sample to correct spectral tilt.
        b0 = 0.99886 * b0 + white * 0.0555179;   // Very slow decay → low frequencies
        b1 = 0.99332 * b1 + white * 0.0750759;   // Slightly faster
        b2 = 0.96900 * b2 + white * 0.1538520;   // Mid-lows
        b3 = 0.86650 * b3 + white * 0.3104856;   // Mid frequencies
        b4 = 0.55000 * b4 + white * 0.5329522;   // Mid-highs
        b5 = -0.7616 * b5 - white * 0.0168980;   // High frequency tweak (note the subtraction)

        // Mix all taps + a weighted white noise term + correction from last frame
        double pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362;

        // Update b6 using current white sample; this adds a small persistent bias
        b6 = white * 0.115926;

        // Scale the output into a safe [-1.0, 1.0] range.
        // These coefficients are empirically tuned, not normalized by strict math—
        // but they yield a balanced, perceptually correct pink noise sound.
        pink *= 0.11;
        pink = Math.max(-1.0, Math.min(1.0, pink));  // Clamping for safety

        // Convert to 16-bit signed PCM and return
        return (short) (pink * Short.MAX_VALUE);

    }
}