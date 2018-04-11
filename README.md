# au4h
The aim of this application is giving to musicians (in particular guitar players) the possibility to control the guitar
and its sound using various gestures. Obliviously the guitar is plugged into a soundcard, while the body's movements are tracked
using the Microsoft Kinect v2. When a gesture is recognized, an OSC Message is sent to a PureData patch to control various effects (like reverb or delay).
However au4h this application uses the OpenNI library (v1.5), so the tracking part could be done also by other devices supported
by the library. 
