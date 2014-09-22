@echo off
ffmpeg.exe -i STING.mp3 -af silencedetect=n=-50dB:d=1 -af volumedetect -f null - 
REM ffmpeg.exe -i STING.mp4 -ab 192k -af volume=5.8dB:precision=double -y STING.mp3
pause 