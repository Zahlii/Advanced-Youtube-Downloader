Advanced-Youtube-Downloader
===========================

Why another Youtube downloader? Aren't there enough already?
Well, there might be. But since none of them fitted my needs, I created this one.

# Where do I get a runnable version?
Head over to the releases page at https://github.com/Zahlii/Advanced-Youtube-Downloader/releases.

# Features

- Relies on FFMPEG.exe and youtube-dl.exe and thus can easily be updated to support new websites and formats.
- Support single video links as well as different playlists.
- Automatically removes silence from the videos.
- Automatically normalizes the audio volume (peak normalization, mp3Gain planned)
- Automatically searches the Gracenote Music DB for song information. The user can then change the information and write it into the file.
- The album-art is automatically downloaded and included.

# Screenshots
<img src="http://i.imgur.com/jr8Epxk.png" />
<img src="http://i.imgur.com/AiiNced.png" />
<img src="http://i.imgur.com/ltTflPU.png" />
<img src="http://i.imgur.com/Sbf3vpu.png" />

# Which Features are planned?

- Supporting MP3Gain / don't do peak normalization

# Used Tools/Libraries

- http://www.jthink.net/jaudiotagger/
- http://commons.apache.org/
- http://rg3.github.io/youtube-dl/
- https://www.ffmpeg.org/

# How can I contribute?

It's simple. Just open tickets in the Issue Tracker, or make a pull request when implementing new features.
I did not include a build file yet which will most likely change later on, as well as adding tests is planned.
