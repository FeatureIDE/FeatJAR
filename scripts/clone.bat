:<<BATCH
    @echo off
    for /F "tokens=*" %%A in (scripts/repo.txt) do git clone %%A
    exit /b
BATCH
xargs -L1 git clone --progress < scripts/repo.txt