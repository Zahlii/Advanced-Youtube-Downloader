@echo off
REM set /p Vid=Video?
REM youtube-dl -F %Vid%
REM set VideoFile=%Vid%
REM set /p VideoQuality=ID of Desired Quality?
REM youtube-dl -f %VideoQuality% -o "dl__%VideoFile%.mp4" %Vid%
REM ffmpeg -i "dl__%VideoFile%.mp4" -ab 320k "audio__%VideoFile%.mp3"
ffmpeg -i "test.mp4" -ab 192k "audio__%VideoFile%.mp3"
REM "%ProgramFiles(x86)%\Mp3tag\Mp3tag.exe" %CD%\audio__%VideoFile%.mp3