@echo off
REM WakeSpeed 构建脚本（需要 JDK 25）
REM 用法: build.bat "C:\path\to\client.jar;C:\path\to\fabric-api.jar;..."

if "%~1"=="" (
  echo 用法: build.bat "依赖jars用分号分隔"
  exit /b 1
)

set CP=%~1
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -nowarn -d out -cp %CP% @sources.txt
if errorlevel 1 exit /b 1

echo 编译完成，开始打包...
if exist WakeSpeed.jar del WakeSpeed.jar
jar --create --file WakeSpeed.jar -C out . -C res .
echo 输出: WakeSpeed.jar