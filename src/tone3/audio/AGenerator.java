// --------------------------------------------------------------------------------------
// AGenerator.java - This abstract audio generator is a blueprint, and serves as the
// shared base class for all signal sources—sine, pink, and anything else that produces
// samples. Rather than 
// hardwiring logic into AudioPlayer, we use this abstract interface to achieve polymorphic 
// routing of sample streams.
//
// This structure keeps AudioPlayer completely agnostic about what kind of signal it's 
// sending—it just pulls shorts from a generator. That way, new tone types (like square 
// waves, speech synthesis, or external streams) can be added by simply subclassing 
// AGenerator and overriding nextSample(). No changes required to playback logic.
//
// Using an abstract base here provides clarity and intent: it's not meant to be instantiated. 
// It defines a contract—a shared agreement for frequency-agnostic sample generators that 
// operate at a given sample rate and produce 16-bit PCM.
// --------------------------------------------------------------------------------------
// Author:  Patrik Eigemann
// eMail:   p.eigenmann@gmx.net
// GitHub:  www.github.com/PatrikEigemann/Tone3
// --------------------------------------------------------------------------------------
// Change Log:
// 2025-06-29 Sun File created as base class for all signal generators.     Version 00.01
// --------------------------------------------------------------------------------------

/* Package tone3.audio */
package tone3.audio;

/**
 * AGenerator.java - This abstract audio generator is a blueprint, and serves as the
 * shared base class for all signal sources—sine, pink, and anything else that produces
 * samples. Rather than 
 * hardwiring logic into AudioPlayer, we use this abstract interface to achieve polymorphic 
 * routing of sample streams.
 *
 * This structure keeps AudioPlayer completely agnostic about what kind of signal it's 
 * sending—it just pulls shorts from a generator. That way, new tone types (like square 
 * waves, speech synthesis, or external streams) can be added by simply subclassing 
 * AGenerator and overriding nextSample(). No changes required to playback logic.
 *
 * Using an abstract base here provides clarity and intent: it's not meant to be instantiated. 
 * It defines a contract—a shared agreement for frequency-agnostic sample generators that 
 * operate at a given sample rate and produce 16-bit PCM.
 */
public abstract class AGenerator {

    /* Audio playback sample rate (Hz). Default is 44100. */
    protected int sampleRate = 44100;

    /**
     * Set the internal sample rate for this generator.
     * May be called externally if the system sample rate changes.
     *
     * @param rate sample rate in Hz
     */
    public void setSampleRate(int rate) {
        this.sampleRate = rate;
    }

    /**
     * Generate the next audio sample.
     * Subclasses must override this to return 16-bit PCM values.
     *
     * @return next audio sample (range: -32768 to 32767)
     */
    public abstract short nextSample();
}