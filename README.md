# blackstar_droid
Android app to control Blackstar amps via USB

Blackstar amps have a USB interface, allowing you to control them from your computer. Unfortunately, the "Insider" software 
doesn't work on the latest Mac OS, and the Windows app requires Silverlight (OMG!) and is, well, kinda crappy. 

This is a work in progress, and my Android threading skills are...weak...so use at your own risk. As of this writing, the main 
controls, effects "pedals", and tuner are working (though the tuner UI is ugly, to say the least).

## Acknowledgements
This work is based heavily on https://github.com/jonathanunderwood/outsider/. They did the heavy lifting of figuring out 
the byte packets that get sent to/from the Blackstar amp. 
