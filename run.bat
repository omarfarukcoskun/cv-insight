@echo off
:: CV Insight — launch script
:: Requires: Java 21+ and Maven on PATH
::
:: Usage:
::   run.bat                  — start the app
::   set ANTHROPIC_API_KEY=sk-ant-... && run.bat   — start with API key
::
:: Alternative (no script needed):
::   mvn javafx:run

echo Starting CV Insight...
mvn javafx:run
