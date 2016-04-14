@echo on
rem This is shim.bat
cd C:\Users\HP\workspace\Mimer\src
echo "We are now in a shim called from the Web Browser"
echo Arg one is: %1
cd C:\Users\HP\workspace\Mimer\src
pause
set classpath=%classpath%C:\Users\HP\workspace\Mimer\src;C:\Program Files\Java\jdk1.8.0_60\lib\xstream-1.2.1.jar;C:\Program Files\Java\jdk1.8.0_60\lib\xpp3_min-1.1.3.4.O.jar;C:\Program Files\Java\jdk1.8.0_60\jre\lib\jsse.jar;
java -Dfirstarg=%1 BCHandler
pause
