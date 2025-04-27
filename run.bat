@echo off
set args=%*
gradlew run --quiet --args="%args%" --no-daemon
