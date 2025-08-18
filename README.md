# Tone3

## About

Tone3 was built for those moments in live sound when you need a signal generator now—not after Googling console layers or digging through vendor-specific logic. Whether the desk buries its tools or you simply want something reliable on your own terms, Tone3 has your back.

It’s your fallback when gear doesn’t cooperate. Plug in a 3.5mm to dual XLR, designate a tone and noise channel, and you're ready. No menus, no manuals, no second-guessing.

This isn’t about not knowing the console—it’s about working smarter when time matters. Tone3 lets you say: "I’ve got this."

## Folder Structure

The folders build/ with their artifacts *.jar, *.exe, *.dll, *.so, etc will not be tracked, except libraries in the lib/ folder.

```
Tone3/  
├── build/  
│    ├── App.class                      ← Entry point: sets up audio + GUI
│    ├── MainFrame.class                ← GUI logic: slider, mode toggle.
│    ├── Audio/  
│    │    ├── AGenerator.class          ← Abstract parent class for generators.
│    │    ├── AudioPlayer.class         ← Audio Player class.
│    │    ├── SineWaveGenerator.class   ← Generates a SineWave.
│    │    ├── PinkNoiseGenerator.class  ← Generates Pink Noise.
│    │    └── [CustomGenerator].class   ← Your welcome to create your own customized generators.   
├── src/  
│    ├── App.java                       ← Entry point: sets up audio + GUI
|    ├── MainFrame.java            ← GUI logic: slider, mode toggle
│    ├── Audio/  
│    │    ├── AGenerator.java           ← Abstract parent class for generators.
│    │    ├── AudioPlayer.java          ← Audio Player class
│    │    ├── SineWaveGenerator.java    ← Generates a SineWave
│    │    ├── PinkNoiseGenerator.java   ← Generates Pink Noise
│    │    └── [CustomGenerator].java    ← Your welcome to create your own customized generators.
├── LICENSE                             ← GNU General Public License  
├── README.md                           ← Project mission, usage, build instructions
└── MANIFEST.MF                          ← Java manifest for building clickable jar files.  
```

## Author

My name is Patrik Eigenmann. After nine years as a professional software engineer, I switched careers to live sound. I still code on the side—projects like Tone3 keep me curious, challenged, and mentally sharp.
These tools aren’t polished products backed by teams. They’re personal explorations, built to deepen my understanding and share something useful with others. If you’d like to support what I do, you can donate to p.eigenmann@gmx.net via PayPal. I’d truly appreciate it.
Tone3 is and always will be free—licensed under the GNU Public License v3.0. Feel free to modify it, break it, rebuild it, or run it just as it is. Thanks for checking it out.